package jcheese.util;

import java.util.Random;
import jcheese.*;

public class Zobrist {
  public static final long sideKey;
  public static final long[][][] pieceKeys = new long[Square.COUNT][Piece.SIDE_COUNT][Piece.KIND_COUNT];
  public static final long[] castleKeys = new long[Board.CASTLE_COUNT];
  public static final long[][] epKeys = new long[Piece.SIDE_COUNT][Square.FILE_COUNT];

  static {
    Random rand = new Random();
    sideKey = rand.nextLong();

    for (int i = 0; i < pieceKeys.length; ++i) {
      for (int j = 0; j < pieceKeys[i].length; ++j) {
        for (int k = 0; k < pieceKeys[i][j].length; ++k) {
          pieceKeys[i][j][k] = rand.nextLong();
        }
      }
    }

    for (int i = 0; i < castleKeys.length; ++i) {
      castleKeys[i] = rand.nextLong();
    }

    for (int i = 0; i < epKeys.length; ++i) {
      for (int j = 0; j < epKeys[i].length; ++j) {
        epKeys[i][j] = rand.nextLong();
      }
    }
  }

  private Zobrist() {}

  public static long getEpKey(int square) {
    int rank = Square.getY(square);
    assert rank == 5 || rank == 2;
    /* { rank & 1 } is effectively { rank % 2 }
     * Since epKeys[] is indexed with a side index,
     * the modulo 2 of the rank effectively yields the en passant side.
     */
    return epKeys[rank & 1][Square.getX(square)];
  }
}
