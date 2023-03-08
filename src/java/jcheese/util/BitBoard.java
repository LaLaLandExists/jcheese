package jcheese.util;

import java.io.PrintStream;
import jcheese.*;

public class BitBoard {
  private BitBoard() {} // Not instantiable

  public static long create(long init) {
    return init;
  }
  public static long create() {
    return 0L;
  }

  public static boolean get(long bb, int i) {
    return (bb & (1L << i)) != 0L;
  }
  public static long set(long bb, int i) {
    return bb | (1L << i);
  }
  public static long clear(long bb, int i) {
    return bb & ~(1L << i);
  }

  public static long allSet() {
    return ~0L;
  }
  public static long allClear() {
    return 0L;
  }

  public static boolean isEmpty(long bb) {
    return bb == 0L;
  }
  public static boolean notEmpty(long bb) {
    return bb != 0L;
  }

  public static int bitScanForward(long bb) {
    return Long.numberOfTrailingZeros(bb);
  }
  public static int bitScanReverse(long bb) {
    return Square.H8 - Long.numberOfLeadingZeros(bb);
  }
  public static int popCount(long bb) {
    return Long.bitCount(bb);
  }
  public static long extractHighest(long bb) { return Long.highestOneBit(bb); }
  public static long extractLowest(long bb) { return Long.lowestOneBit(bb); }

  public static long bit(int i) { return 1L << i; }
  public static long popLSB(long bb) { return bb & (bb - 1); }

  private static final String LINE_DELIMITER = "  +---+---+---+---+---+---+---+---+\n";
  public static void print(PrintStream ps, long bb) {
    ps.print(LINE_DELIMITER);

    for (int y = Square.RANK_COUNT - 1; y >= 0; --y) {
      ps.printf("%d |", y + 1);
      for (int x = 0; x < Square.FILE_COUNT; ++x) {
        ps.printf(" %c |", get(bb, Square.fromCoords(x, y)) ? 'X' : '.');
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
  public static void print(long bb) {
    print(System.out, bb);
  }
}
