package jcheese;

import java.io.PrintStream;
import java.util.Arrays;

import jcheese.util.*;

public class Board {
  public static final String START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
  // Indices to get the bitboard representation of each piece kind and side.
  public static final int ALL = 0;
  public static final int PAWNS = 2;
  public static final int ROOKS = 4;
  public static final int KNIGHTS = 6;
  public static final int BISHOPS = 8;
  public static final int QUEENS = 10;
  public static final int KINGS = 12;

  public static final int LIGHT_ALL = ALL + Piece.LIGHT;
  public static final int DARK_ALL = ALL + Piece.DARK;
  public static final int LIGHT_PAWNS = PAWNS + Piece.LIGHT;
  public static final int DARK_PAWNS = PAWNS + Piece.DARK;
  public static final int LIGHT_ROOKS = ROOKS + Piece.LIGHT;
  public static final int DARK_ROOKS = ROOKS + Piece.DARK;
  public static final int LIGHT_KNIGHTS = KNIGHTS + Piece.LIGHT;
  public static final int DARK_KNIGHTS = KNIGHTS + Piece.DARK;
  public static final int LIGHT_BISHOPS = BISHOPS + Piece.LIGHT;
  public static final int DARK_BISHOPS = BISHOPS + Piece.DARK;
  public static final int LIGHT_QUEENS = QUEENS + Piece.LIGHT;
  public static final int DARK_QUEENS = QUEENS + Piece.DARK;
  public static final int LIGHT_KINGS = KINGS + Piece.LIGHT;
  public static final int DARK_KINGS = KINGS + Piece.DARK;
  public static final int BOARD_COUNT = 14;

  public static final int KING_CASTLE = 0;
  public static final int QUEEN_CASTLE = 2;

  public static final int LIGHT_KING_CASTLE = KING_CASTLE | Piece.LIGHT;
  public static final int DARK_KING_CASTLE = KING_CASTLE | Piece.DARK;
  public static final int LIGHT_QUEEN_CASTLE = QUEEN_CASTLE | Piece.LIGHT;
  public static final int DARK_QUEEN_CASTLE = QUEEN_CASTLE | Piece.DARK;
  public static final int CASTLE_COUNT = 4;

  private long zkey = 0L;
  public final long[] bitboards = new long[BOARD_COUNT];
  public final int[] pieceBoard = new int[Square.COUNT];
  private int plySide = Piece.LIGHT;
  private final boolean[] castles = new boolean[CASTLE_COUNT];
  private int epSquare = Square.NIL;
  private int halfMoveClock;
  private int fullMoveNumber;
  private final MoveHistory history = new MoveHistory();

  public Board() { reset(); }

  public void reset() {
    zkey = 0L;
    Arrays.fill(bitboards, BitBoard.allClear());
    Arrays.fill(pieceBoard, Piece.NONE);
    plySide = Piece.LIGHT;
    Arrays.fill(castles, false);
    epSquare = Square.NIL;
    halfMoveClock = 0;
    fullMoveNumber = 1;
  }

  public long getZobrist() { return zkey; }

  public int getPiece(int square) {
    return pieceBoard[square];
  }
  public boolean isEmpty(int square) {
    return getPiece(square) == Piece.NONE;
  }
  public void putPiece(int square, int piece) {
    int side, kind, boardIndex, allIndex;

    assert isEmpty(square); // Target square to put must be empty.
    pieceBoard[square] = piece;
    side = Piece.side(piece);
    kind = Piece.kind(piece);
    boardIndex = side | kind;
    allIndex = ALL | side;
    bitboards[boardIndex] = BitBoard.set(bitboards[boardIndex], square);
    bitboards[allIndex] = BitBoard.set(bitboards[allIndex], square);
    zkey ^= Zobrist.pieceKeys[square][side][Piece.kindIndex(kind)];
  }
  public int removePiece(int square) {
    int piece, side, kind, boardIndex, allIndex;

    assert !isEmpty(square); // Target square must have a piece.
    piece = pieceBoard[square];
    pieceBoard[square] = Piece.NONE;
    side = Piece.side(piece);
    kind = Piece.kind(piece);
    boardIndex = side | kind;
    allIndex = ALL | side;
    bitboards[boardIndex] = BitBoard.clear(bitboards[boardIndex], square);
    bitboards[allIndex] = BitBoard.clear(bitboards[allIndex], square);
    zkey ^= Zobrist.pieceKeys[square][side][Piece.kindIndex(kind)];
    return piece;
  }
  public void movePiece(int srcSquare, int dstSquare) { putPiece(dstSquare, removePiece(srcSquare)); }

  public int getPlySide() { return plySide; }
  public void swapPlySide() {
    plySide = Piece.invertSide(plySide);
    zkey ^= Zobrist.sideKey;
  }
  
  public boolean[] getCastles() { return castles; }
  public boolean getCastle(int castle) { return castles[castle]; }
  public boolean hasAnyCastle() {
    for (final boolean castle : castles) {
      if (castle) return true;
    }
    return false;
  }
  public void enableCastle(int castle) {
    if (!getCastle(castle)) {
      castles[castle] = true;
      zkey ^= Zobrist.castleKeys[castle];
    }
  }
  private void disableCastle(int castle) {
    if (getCastle(castle)) {
      castles[castle] = false;
      zkey ^= Zobrist.castleKeys[castle];
    }
  }

  public int getEpSquare() { return epSquare; }
  public void setEpSquare(int square) {
    assert square != Square.NIL;
    if (epSquare != Square.NIL) clearEpSquare();
    epSquare = square;
    zkey ^= Zobrist.getEpKey(square);
  }
  public void clearEpSquare() {
    if (epSquare != Square.NIL) {
      zkey ^= Zobrist.getEpKey(epSquare);
      epSquare = Square.NIL;
    }
  }

  public int getHalfMoveClock() { return halfMoveClock; }
  public void setHalfMoveClock(int hmove) { halfMoveClock = hmove; }
  public int getFullMoveNumber() { return fullMoveNumber; }
  public void setFullMoveNumber(int fmove) { fullMoveNumber = fmove; }

  public Board copyFrom(Board src) {
    zkey = src.zkey;
    System.arraycopy(src.bitboards, 0, bitboards, 0, bitboards.length);
    System.arraycopy(src.pieceBoard, 0, pieceBoard, 0, pieceBoard.length);
    plySide = src.plySide;
    System.arraycopy(src.castles, 0, castles, 0, castles.length);
    epSquare = src.epSquare;
    halfMoveClock = src.halfMoveClock;
    fullMoveNumber = src.fullMoveNumber;
    return this;
  }
  public Board copy() {
    return new Board().copyFrom(this);
  }

  public void applyMove(int move) {
    clearEpSquare();

    if (Move.isCastle(move)) {
      int castle = Move.getCastle(move),
          kingSrc = MoveData.kingSrcSquares[castle],
          kingDst = MoveData.kingDstSquares[castle],
          rookSrc = MoveData.rookSrcSquares[castle],
          rookDst = MoveData.rookDstSquares[castle];
      movePiece(kingSrc, kingDst);
      movePiece(rookSrc, rookDst);
      // Disable the castles now.
      disableCastle(KING_CASTLE | plySide);
      disableCastle(QUEEN_CASTLE | plySide);
    } else {
      int src = Move.getSrc(move),
          dst = Move.getDst(move),
          kingSide = plySide | KING_CASTLE,
          queenSide = plySide | QUEEN_CASTLE;

      switch (Piece.kind(pieceBoard[src])) {
        case Piece.PAWN: halfMoveClock = -1; break;
        case Piece.ROOK: {
          if (MoveData.rookSrcSquares[kingSide] == src) disableCastle(kingSide);
          if (MoveData.rookSrcSquares[queenSide] == src) disableCastle(queenSide);
        } break;
        case Piece.KING: {
          disableCastle(kingSide);
          disableCastle(queenSide);
        } break;
      }

      if (Move.isCapture(move)) {
        if (Piece.kind(pieceBoard[dst]) == Piece.ROOK) {
          // Disable the castles of opponent if their rooks are captured.
          int opponentKingSide = Piece.invertSide(kingSide),
              opponentQueenSide = Piece.invertSide(queenSide);
          if (MoveData.rookSrcSquares[opponentKingSide] == dst) disableCastle(opponentKingSide);
          else if (MoveData.rookSrcSquares[opponentQueenSide] == dst) disableCastle(opponentQueenSide);
        }
        removePiece(dst);
        halfMoveClock = -1;
      } else if (Move.isEnPassant(move)) {
        removePiece(Move.getOtherSquare(move));
      }

      movePiece(Move.getSrc(move), dst);

      if (Move.isDoublePush(move)) {
        setEpSquare(Move.getOtherSquare(move));
      } else if (Move.isPromote(move)) {
        putPiece(dst, Piece.side(removePiece(dst)) | Move.getPromoteKind(move));
      }
    }
    swapPlySide();
    // Swapped into LIGHT. A full turn has passed.
    if (plySide == Piece.LIGHT) ++fullMoveNumber;
    ++halfMoveClock;
  }

  public int revert() {
    if (history.size() == 0) return Move.error();

    long entry = history.popLast();
    int move = MoveHistory.getMove(entry);

    if (MoveHistory.hasLightKingCastle(Board.LIGHT_KING_CASTLE)) {
      enableCastle(Board.LIGHT_KING_CASTLE);
    } else {
      disableCastle(Board.LIGHT_KING_CASTLE);
    }

    if (MoveHistory.hasLightKingCastle(Board.DARK_KING_CASTLE)) {
      enableCastle(Board.DARK_KING_CASTLE);
    } else {
      disableCastle(Board.DARK_KING_CASTLE);
    }

    if (MoveHistory.hasLightKingCastle(Board.LIGHT_QUEEN_CASTLE)) {
      enableCastle(Board.LIGHT_QUEEN_CASTLE);
    } else {
      disableCastle(Board.LIGHT_QUEEN_CASTLE);
    }

    if (MoveHistory.hasLightKingCastle(Board.DARK_QUEEN_CASTLE)) {
      enableCastle(Board.DARK_QUEEN_CASTLE);
    } else {
      disableCastle(Board.DARK_QUEEN_CASTLE);
    }

    int epSquare = MoveHistory.getEnPassant(entry);
    if (epSquare == Square.NIL) {
      clearEpSquare();
    } else {
      setEpSquare(epSquare);
    }

    setHalfMoveClock(MoveHistory.getHmoveClock(entry));

    return move;
  }

  public long[] getBoards() { return bitboards; }
  
  public boolean isValid() {
    // Check material validity for each side
    for (int side = Piece.LIGHT; side < Piece.SIDE_COUNT; ++side) {
      if (BitBoard.popCount(bitboards[Board.KINGS | side]) != 1) {
        return false; // Must have one king
      }
      
      int materialCount = 0,
          pawnCount = 0;
      
      pawnCount = BitBoard.popCount(bitboards[Board.PAWNS | side]);
      
      if (pawnCount > 8) return false; // Too many pawns
      materialCount = pawnCount
        + BitBoard.popCount(bitboards[Board.ROOKS | side])
        + BitBoard.popCount(bitboards[Board.KNIGHTS | side])
        + BitBoard.popCount(bitboards[Board.BISHOPS | side])
        + BitBoard.popCount(bitboards[Board.QUEENS | side]);
      
      if (materialCount > 15) return false; // Too many pieces
      
      for (int castle = KING_CASTLE | side; castle < CASTLE_COUNT; castle += 2) {
        if (castles[castle]) {
          if (Piece.kind(getPiece(MoveData.kingSrcSquares[castle])) != Piece.KING
              || Piece.kind(getPiece(MoveData.rookSrcSquares[castle])) != Piece.ROOK) {
            return false; // King and rook must be in castling square
          }
        }
      }
    }

    return halfMoveClock == 0 || epSquare == Square.NIL; // Recent pawn push. Half move clock must be zero
    // FIXME: Assert that opponent king should not be checked
  }

  private static final String LINE_DELIMITER = "  +---+---+---+---+---+---+---+---+\n";
  public void print(PrintStream ps) {
    ps.print(LINE_DELIMITER);

    for (int i = Square.RANK_COUNT - 1; i >= 0; --i) {
      ps.printf("%d |", i + 1);
      for (int j = 0; j < Square.FILE_COUNT; ++j) {
        ps.printf(" %c |", Piece.getChar(pieceBoard[Square.fromCoords(j, i)]));
      }
      ps.println();
      ps.print(LINE_DELIMITER);
    }
    ps.print("   ");
    for (int i = 0; i < Square.FILE_COUNT; ++i) {
      ps.printf(" %c  ", (char) ('A' + i));
    }
    ps.println();
  }
  public void print() { print(System.out); }

  public void dumpState(PrintStream ps) {
    ps.printf("ZKey: %x\n", zkey);
    ps.println("Piece board:");
    print(ps);
    ps.println("All bitboards..");
    for (int i = 0; i < BOARD_COUNT; ++i) {
      ps.printf("[%d]:\n", i);
      BitBoard.print(ps, bitboards[i]);
    }
    ps.printf("Side to play: %s\n", plySide == Piece.LIGHT ? "LIGHT" : "DARK");
    ps.printf("Castles: { LK: %b, DK: %b, LQ: %b, DQ: %b }\n",
        castles[LIGHT_KING_CASTLE], castles[DARK_KING_CASTLE],
        castles[LIGHT_QUEEN_CASTLE], castles[DARK_QUEEN_CASTLE]);
    ps.printf("En passant: %s\n", Square.names[epSquare]);
    ps.printf("Half move clock: %d\n", halfMoveClock);
    ps.printf("Ful move number: %d", fullMoveNumber);
  }
  public void dumpState() { dumpState(System.out); }
}
