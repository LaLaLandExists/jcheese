package jcheese;

import java.util.ArrayList;
import jcheese.util.*;

public class Move {
  /* This is the move data format. A single move is encoded as a 32-bit Integer.
   * b[0:5] -> source bits [enough to represent a Square]
   * b[6:11] -> destination bits [enough to represent a Square]
   * b[12:17] -> other bits [enough to represent a Square, a piece kind and a castle]
   * b[18] -> capture bit [if move captures the destination]
   * b[19] -> double push bit [if the move is a double pawn push, skipped Square is indicated in b[12:17]]
   * b[20] -> en passant bit [the en passant capture square is indicated by b[12:17]]
   * b[21] -> castle bit [the castle is indicated by b[12:13], rook destination is deduced]
   * b[22] -> promote bit [the promotion kind is indicated by b[12:15],
   *          where [b[12:15] | side] is computed to get promoted piece]
   * b[23:31] -> unused bits
   */
  public static final int DST_SHIFT = 6;
  public static final int OTHER_SHIFT = 12;
  public static final int SRC_MASK = 0x3F;
  public static final int DST_MASK = 0xFC0;
  public static final int OTHER_SQ_MASK = 0x3F000;
  public static final int OTHER_PK_MASK = 0xF000;
  public static final int OTHER_CASTLE_MASK = 0x3000;
  public static final int CAPTURE_BIT = 1 << 18;
  public static final int DPUSH_BIT = 1 << 19;
  public static final int EP_BIT = 1 << 20;
  public static final int CASTLE_BIT = 1 << 21;
  public static final int PROMOTE_BIT = 1 << 22;
  // Used to flag moves that cannot be deduced from a context
  public static final int ERROR_BIT = 1 << 23;

  private Move() {} // Not instantiable

  public static boolean isCapture(int move) { return (move & CAPTURE_BIT) != 0; }
  public static boolean isDoublePush(int move) { return (move & DPUSH_BIT) != 0; }
  public static boolean isEnPassant(int move) { return (move & EP_BIT) != 0; }
  public static boolean isCastle(int move) { return (move & CASTLE_BIT) != 0; }
  public static boolean isPromote(int move) { return (move & PROMOTE_BIT) != 0; }
  public static boolean isError(int move) { return (move & ERROR_BIT) != 0; }

  public static int getSrc(int move) { return move & SRC_MASK; }
  public static int getDst(int move) { return (move & DST_MASK) >> DST_SHIFT; }
  public static int getOtherSquare(int move) { return (move & OTHER_SQ_MASK) >> OTHER_SHIFT; }
  public static int getPromoteKind(int move) { return (move & OTHER_PK_MASK) >> OTHER_SHIFT; }
  public static int getCastle(int move) { return (move & OTHER_CASTLE_MASK) >> OTHER_SHIFT; }

  public static int push(int srcSq, int dstSq) { return srcSq | (dstSq << DST_SHIFT); }
  public static int capture(int srcSq, int dstSq) { return push(srcSq, dstSq) | CAPTURE_BIT; }
  public static int doublePush(int srcSq, int dstSq, int skipSq) {
    return push(srcSq, dstSq) | (skipSq << OTHER_SHIFT) | DPUSH_BIT;
  }
  public static int promotePush(int srcSq, int dstSq, int kind) {
    return push(srcSq, dstSq) | (kind << OTHER_SHIFT) | PROMOTE_BIT;
  }
  public static int promoteCapture(int srcSq, int dstSq, int kind) {
    return promotePush(srcSq, dstSq, kind) | CAPTURE_BIT;
  }
  public static int enPassant(int srcSq, int dstSq, int captureSq) {
    return push(srcSq, dstSq) | (captureSq << OTHER_SHIFT) | EP_BIT;
  }
  public static int castle(int castle) {
    return push(MoveData.kingSrcSquares[castle], MoveData.kingDstSquares[castle]) | CASTLE_BIT | (castle << OTHER_SHIFT);
  }
  public static int error() {
    return ERROR_BIT;
  }

  public static int findMove(ArrayList<Integer> moves, int srcSquare, int dstSquare, int promoteKind) {
    for (final int move : moves) {
      if (Move.getSrc(move) == srcSquare && Move.getDst(move) == dstSquare) {
        if (Move.isPromote(move)) {
          if (Move.getPromoteKind(move) == promoteKind) return move;
        } else return move;
      }
    }
    // This is error state. A move cannot be chosen from [moves], you do not want this
    return ERROR_BIT;
  }

  public static String getLAN(int move) {
    if (isCastle(move)) {
      switch (getCastle(move)) {
        case Board.LIGHT_KING_CASTLE:
        case Board.DARK_KING_CASTLE:
          return "O-O";
        case Board.LIGHT_QUEEN_CASTLE:
        case Board.DARK_QUEEN_CASTLE:
          return "O-O-O";
      }
    }
    
    String lan = String.format("%s%s%s", Square.names[getSrc(move)].toLowerCase(),
      isCapture(move) || isEnPassant(move) ? "x" : "", Square.names[getDst(move)].toLowerCase());
    
    if (isPromote(move)) {
      switch (getPromoteKind(move)) {
        case Piece.ROOK: return lan + "=R";
        case Piece.KNIGHT: return lan + "=N";
        case Piece.BISHOP: return lan + "=B";
        case Piece.QUEEN: return lan + "=Q";
        default: throw new AssertionError("Unreachable");
      }
    }
    return lan;
  }
  
  // SSAN is (S)uper (S)imple (A)lgebraic (N)otation
  public static String getSSAN(int move) {
    String ssan = String.format("%s%s",
      Square.names[getSrc(move)].toLowerCase(),
      Square.names[getDst(move)].toLowerCase())
    ;
    
    if (isPromote(move)) {
      char promote = 0;
      switch (getPromoteKind(move)) {
        case Piece.ROOK: promote = 'R'; break;
        case Piece.KNIGHT: promote = 'N'; break;
        case Piece.BISHOP: promote = 'B'; break;
        case Piece.QUEEN: promote = 'Q'; break;
        default: throw new AssertionError("Unreachable");
      }
      ssan += promote;
    }
    
    return ssan;
  }
  
  public static int fromSSAN(Board context, String ssan) {
    int side = context.getPlySide();
    
    try {
      int srcX = ssan.charAt(0) - 'a';
      int srcY = ssan.charAt(1) - '1';
      int dstX = ssan.charAt(2) - 'a';
      int dstY = ssan.charAt(3) - '1';
      
      if (!Square.withinBounds(srcX, srcY) || !Square.withinBounds(dstX, dstY)) {
        throw new IllegalArgumentException("Invalid square in SSAN");
      }
      
      int src = Square.fromCoords(srcX, srcY);
      int dst = Square.fromCoords(dstX, dstY);
      
      boolean isCapture = Piece.kind(context.getPiece(dst)) != Piece.NONE
        && Piece.side(context.getPiece(dst)) != side;
      
      if (ssan.length() == 5) {
        int piece;
        switch (ssan.charAt(4)) {
          case 'R': piece = Piece.ROOK; break;
          case 'N': piece = Piece.KNIGHT; break;
          case 'B': piece = Piece.BISHOP; break;
          case 'Q': piece = Piece.QUEEN; break;
          default: throw new IllegalArgumentException("Invalid promotion in SSAN");
        }
        if (isCapture) {
          return promoteCapture(src, dst, piece);
        }
        return promotePush(src, dst, piece);
      } else if (ssan.length() == 4) {
        if (Piece.kind(context.getPiece(src)) == Piece.PAWN && dst == context.getEpSquare()) {
          return enPassant(src, dst, side == Piece.LIGHT ? dst - 8 : dst + 8);
        }
        int delta = src - dst;
        if (Piece.kind(context.getPiece(src)) == Piece.KING && Math.abs(delta) == 2) {
          if (delta > 0) {
            return castle(Board.QUEEN_CASTLE | side);
          }
          return castle(Board.KING_CASTLE | side);
        }
        if (isCapture) {
          return capture(src, dst);
        }
        return push(src, dst);
      }
      throw new IllegalArgumentException(String.format("Invalid SAN (length=%d)", ssan.length()));
    } catch (StringIndexOutOfBoundsException exc) {
      throw new IllegalArgumentException("SSAN is too short");
    }
  }
}
