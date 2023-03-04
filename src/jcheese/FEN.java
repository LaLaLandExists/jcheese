package jcheese;

import java.util.HashMap;

public class FEN {
  private final Board temp = new Board();
  private String fen;
  private int cursor;

  private void reset() {
    temp.reset();
    cursor = 0;
  }

  public FEN setFEN(String fen) {
    this.fen = fen;
    return this;
  }
  public String getFEN() { return fen; }

  private boolean atEnd() {
    return cursor >= fen.length();
  }
  private char advance() { return atEnd() ? '\0' : fen.charAt(cursor++); }
  private char peek() { return atEnd() ? '\0' : fen.charAt(cursor); }
  private boolean check(char ch) { return peek() == ch; }
  private static void error(String msg) { throw new IllegalArgumentException(msg); }

  private interface IExpectPredicate {
    boolean run(char ch);
  }
  private char expect(IExpectPredicate pred, String err) {
    char got = advance();
    if (!pred.run(got)) error(err);
    return got;
  }
  private char expect(char ch, String err) {
    return expect((got) -> ch == got, err);
  }

  private int nextSide() {
    switch (Character.toLowerCase(advance())) {
      case 'b': return Piece.DARK;
      case 'w': return Piece.LIGHT;
      default: error("Expect side indicator.");
    }
    return 0;
  }
  private int nextCastle() {
    switch (advance()) {
      case 'K': return Board.LIGHT_KING_CASTLE;
      case 'Q': return Board.LIGHT_QUEEN_CASTLE;
      case 'k': return Board.DARK_KING_CASTLE;
      case 'q': return Board.DARK_QUEEN_CASTLE;
      case '-': return -1;
      default: error("Expect castle.");
    }
    return 0;
  }
  private int nextSquare() {
    if (peek() == '-') {
      advance();
      return Square.NIL;
    }

    char file = expect((got) -> {
      char lowerGot = Character.toLowerCase(got);
      return lowerGot >= 'a' && lowerGot <= 'h';
    }, "Expect file letter [A, H] or [a, h]");
    char rank = expect((got) -> got >= '1' && got <= '8', "Expect rank number [1,8].");

    return Square.fromCoords(Character.toLowerCase(file) - 'a', rank - '1');
  }
  private int nextNum() {
    int start = cursor;
    expect(Character::isDigit, "Expect digit.");
    while (Character.isDigit(peek())) advance();
    return Integer.parseInt(fen.substring(start, cursor));
  }

  private static int fenToPiece(char fen) {
    int side;

    if (Character.isUpperCase(fen)) {
      side = Piece.LIGHT;
      fen = Character.toLowerCase(fen);
    } else {
      side = Piece.DARK;
    }

    switch (fen) {
      case 'p': return side | Piece.PAWN;
      case 'r': return side | Piece.ROOK;
      case 'n': return side | Piece.KNIGHT;
      case 'b': return side | Piece.BISHOP;
      case 'q': return side | Piece.QUEEN;
      case 'k': return side | Piece.KING;
      default: error("Expect valid piece.");
    }
    return 0;
  }

  public FEN load(Board board) {
    reset();
    for (int rank = Square.RANK_COUNT - 1; rank >= 0; --rank) {
      for (int file = 0; file < Square.FILE_COUNT; ++file) {
        char ch = advance();
        if (Character.isDigit(ch)) {
          int increment = ch - '0';
          if (file + increment > 8) error("Rank overflow.");
          file += increment - 1; // to anticipate ++file.
        } else if (ch == '/') {
          // Oops. it ended early.
          error("Not enough pieces in rank.");
        } else {
          temp.putPiece(Square.fromCoords(file, rank), fenToPiece(ch));
        }
      }
      if (rank != 0) {
        expect('/', "Expect '/' to end rank.");
      }
    }

    // Make ply side indication optional. Defaults to LIGHT.
    if (!atEnd()) {
      expect(' ', "Expect space after piece position.");
      if (temp.getPlySide() != nextSide()) temp.swapPlySide();
    }

    // Make castle indication optional. Defaults to all false.
    if (!atEnd()) {
      expect(' ', "Expect space after ply side");
      do {
        int castle = nextCastle();
        if (castle == -1) break;
        temp.enableCastle(castle);
      } while (!atEnd() && peek() != ' ');
    }

    // Make en passant indication optional. Defaults to NIL.
    if (!atEnd()) {
      expect(' ', "Expect space after castle.");
      int sq = nextSquare();
      if (sq != Square.NIL) temp.setEpSquare(nextSquare());
    }

    // Make half move clock indication optional. Defaults to 0.
    if (!atEnd()) {
      expect(' ', "Expect space after en passant.");
      temp.setHalfMoveClock(nextNum());
    }

    // Make full move clock indication optional. Defaults to 1.
    if (!atEnd()) {
      expect(' ', "Expect space after half move clock.");
      temp.setFullMoveNumber(nextNum());
    }

    if (!temp.isValid()) error("Invalid FEN.");
    board.copyFrom(temp);
    return this;
  }

  public FEN save(Board board) {
    StringBuilder sb = new StringBuilder();
    int emptyRun = 0;
    for (int rank = Square.RANK_COUNT - 1; rank >= 0; --rank) {
      for (int file = 0; file < Square.FILE_COUNT; ++file) {
        int piece = board.getPiece(Square.fromCoords(file, rank)),
            kind = Piece.kind(piece);
        if (kind == Piece.NONE) {
          emptyRun++;
        } else {
          if (emptyRun != 0) {
            sb.append(emptyRun);
            emptyRun = 0;
          }
          sb.append(Piece.getChar(piece));
        }
      }
      if (emptyRun != 0) {
        sb.append(emptyRun);
        emptyRun = 0;
      }
      if (rank != 0) sb.append('/');
    }

    sb.append(' ')
      .append(board.getPlySide() == Piece.LIGHT ? "w" : "b")
      .append(' ');
    if (board.hasAnyCastle()) {
      if (board.getCastle(Board.LIGHT_KING_CASTLE)) sb.append('K');
      if (board.getCastle(Board.LIGHT_QUEEN_CASTLE)) sb.append('Q');
      if (board.getCastle(Board.DARK_KING_CASTLE)) sb.append('k');
      if (board.getCastle(Board.DARK_QUEEN_CASTLE)) sb.append('q');
    } else {
      sb.append('-');
    }
    sb.append(' ');
    int epSquare = board.getEpSquare();
    if (epSquare == Square.NIL) sb.append('-');
    else sb.append(Square.names[epSquare].toLowerCase());
    sb.append(' ')
      .append(board.getHalfMoveClock())
      .append(' ')
      .append(board.getFullMoveNumber());
    fen = sb.toString();
    return this;
  }

  public static void loadFEN(Board board, String fen) {
    new FEN().setFEN(fen).load(board);
  }
  public static String getFEN(Board board) {
    return new FEN().save(board).getFEN();
  }
}
