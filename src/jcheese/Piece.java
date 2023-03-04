package jcheese;

public class Piece {
  public static final int LIGHT = 0;
  public static final int DARK = 1;
  public static final int SIDE_COUNT = 2;

  // Let s = The side of the piece i.e. whether it's light or dark
  public static final int NONE = 0; // 0000
  public static final int PAWN = 2; // 001s
  public static final int ROOK = 4; // 010s
  public static final int KNIGHT = 6; // 011s
  public static final int BISHOP = 8; // 100s
  public static final int QUEEN = 10; // 101s
  public static final int KING = 12; // 110s
  public static final int KIND_COUNT = 6;
  /*
   * A piece is represented as an Integer where:
   *   b[0] -> The side representation. Dark or Light.
   *   b[3:1] -> The kind representation. Pawn, Rook, etc..
   *   b[32:4] -> unused.
   * - Example:
   *     A light pawn that is, [side = 0 (LIGHT), kind = 2 (PAWN)]
   *       would have the bits 0..0010, effectively [side bit_or kind]
   */

  public static int kindIndex(int kind) { return (kind >> 1) - 1; }

  private Piece() {} // Not instantiable

  public static int create(int side, int kind) {
    return side | kind;
  }
  public static int side(int piece) {
    // Side info is stored at b[0], simply mask it out to get the side.
    return piece & 1;
  }
  public static int kind(int piece) {
    // Kind info is stored at b[3:1], the mask would be: 1110 (dec: 14)
    return piece & 14;
  }

  public static int invertSide(int side) {
    // if [side] is 0 (LIGHT), 0 ^ 1 = 1 (DARK)
    // if [side] is 1 (DARK), 1 ^ 1 = 0 (LIGHT)
    return side ^ 1;
  }

  public static char getChar(int piece) {
    char ch;
    switch (kind(piece)) {
      case NONE: ch = ' '; break;
      case PAWN: ch = 'p'; break;
      case ROOK: ch = 'r'; break;
      case KNIGHT: ch = 'n'; break;
      case BISHOP: ch = 'b'; break;
      case QUEEN: ch = 'q'; break;
      case KING: ch = 'k'; break;
      default: throw new AssertionError("Unreachable.");
    }
    return side(piece) == Piece.LIGHT ? Character.toUpperCase(ch) : ch;
  }
}
