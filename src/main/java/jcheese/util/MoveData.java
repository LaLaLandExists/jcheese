package jcheese.util;

import jcheese.*;

@SuppressWarnings("unused")
public class MoveData {
  public static final int CARDINAL = 0;
  public static final int NORTH = 0;
  public static final int EAST = 1;
  public static final int SOUTH = 2;
  public static final int WEST = 3;

  public static final int DIAGONAL = 4;
  public static final int NORTHEAST = 4;
  public static final int SOUTHEAST = 5;
  public static final int SOUTHWEST = 6;
  public static final int NORTHWEST = 7;
  public static final int DIRECTION_COUNT = 8;

  public static final long[][] pawnCaptures = new long[Square.COUNT][Piece.SIDE_COUNT];
  public static final long[][] sliders  = new long[Square.COUNT][DIRECTION_COUNT];
  public static final long[] knightMoves = new long[Square.COUNT];
  public static final long[] kingMoves = new long[Square.COUNT];
  public static final long[] castleClearSquares = new long[] {
      0x60L, 0x6000000000000000L, 0xEL, 0xE00000000000000L
  };
  public static final long[] castleSafeSquares = new long[] {
      0x60L, 0x6000000000000000L, 0xCL, 0xC00000000000000L
  };
  public static final long[] ranks = new long[Square.RANK_COUNT];
  public static final long[] files = new long[Square.FILE_COUNT];
  public static final long[] doublePushes = new long[Piece.SIDE_COUNT];
  public static final int[] promoteRanks = new int[] { 7, 0 };
  public static final long[] enPassantRanks = new long[Piece.SIDE_COUNT];
  
  public static final int[] kingSrcSquares = new int[] {
    Square.E1, Square.E8, Square.E1, Square.E8
  };
  public static final int[] kingDstSquares = new int[] {
    Square.G1, Square.G8, Square.C1, Square.C8
  };
  public static final int[] rookSrcSquares = new int[] {
    Square.H1, Square.H8, Square.A1, Square.A8
  };
  public static final int[] rookDstSquares = new int[] {
    Square.F1, Square.F8, Square.D1, Square.D8
  };
  
  public interface IScanner {
		int run(long bb);
	}

	public static final IScanner[] scanners = {
		BitBoard::bitScanForward, // NORTH
		BitBoard::bitScanForward, // EAST
		BitBoard::bitScanReverse, // SOUTH
		BitBoard::bitScanReverse, // WEST
		BitBoard::bitScanForward, // NORTHEAST
		BitBoard::bitScanReverse, // SOUTHEAST
		BitBoard::bitScanReverse, // SOUTHWEST
		BitBoard::bitScanForward, // NORTHWEST
	};

	public interface IExtractor {
		long run(long bb);
	}

	public static final IExtractor[] extractors = {
		BitBoard::extractLowest, // NORTH
		BitBoard::extractLowest, // EAST
		BitBoard::extractHighest, // SOUTH
		BitBoard::extractHighest, // WEST
		BitBoard::extractLowest, // NORTHEAST
		BitBoard::extractHighest, // SOUTHEAST
		BitBoard::extractHighest, // SOUTHWEST
		BitBoard::extractLowest, // NORTHWEST
	};

  // WARNING: don't scroll down. it's bottomless.
  static {
    // Pawn captures.
    pawnCaptures[Square.A2][Piece.LIGHT] = 0x20000L;
    pawnCaptures[Square.B2][Piece.LIGHT] = 0x50000L;
    pawnCaptures[Square.C2][Piece.LIGHT] = 0xA0000L;
    pawnCaptures[Square.D2][Piece.LIGHT] = 0x140000L;
    pawnCaptures[Square.E2][Piece.LIGHT] = 0x280000L;
    pawnCaptures[Square.F2][Piece.LIGHT] = 0x500000L;
    pawnCaptures[Square.G2][Piece.LIGHT] = 0xA00000L;
    pawnCaptures[Square.H2][Piece.LIGHT] = 0x400000L;
    pawnCaptures[Square.A3][Piece.LIGHT] = 0x2000000L;
    pawnCaptures[Square.B3][Piece.LIGHT] = 0x5000000L;
    pawnCaptures[Square.C3][Piece.LIGHT] = 0xA000000L;
    pawnCaptures[Square.D3][Piece.LIGHT] = 0x14000000L;
    pawnCaptures[Square.E3][Piece.LIGHT] = 0x28000000L;
    pawnCaptures[Square.F3][Piece.LIGHT] = 0x50000000L;
    pawnCaptures[Square.G3][Piece.LIGHT] = 0xA0000000L;
    pawnCaptures[Square.H3][Piece.LIGHT] = 0x40000000L;
    pawnCaptures[Square.A4][Piece.LIGHT] = 0x200000000L;
    pawnCaptures[Square.B4][Piece.LIGHT] = 0x500000000L;
    pawnCaptures[Square.C4][Piece.LIGHT] = 0xA00000000L;
    pawnCaptures[Square.D4][Piece.LIGHT] = 0x1400000000L;
    pawnCaptures[Square.E4][Piece.LIGHT] = 0x2800000000L;
    pawnCaptures[Square.F4][Piece.LIGHT] = 0x5000000000L;
    pawnCaptures[Square.G4][Piece.LIGHT] = 0xA000000000L;
    pawnCaptures[Square.H4][Piece.LIGHT] = 0x4000000000L;
    pawnCaptures[Square.A5][Piece.LIGHT] = 0x20000000000L;
    pawnCaptures[Square.B5][Piece.LIGHT] = 0x50000000000L;
    pawnCaptures[Square.C5][Piece.LIGHT] = 0xA0000000000L;
    pawnCaptures[Square.D5][Piece.LIGHT] = 0x140000000000L;
    pawnCaptures[Square.E5][Piece.LIGHT] = 0x280000000000L;
    pawnCaptures[Square.F5][Piece.LIGHT] = 0x500000000000L;
    pawnCaptures[Square.G5][Piece.LIGHT] = 0xA00000000000L;
    pawnCaptures[Square.H5][Piece.LIGHT] = 0x400000000000L;
    pawnCaptures[Square.A6][Piece.LIGHT] = 0x2000000000000L;
    pawnCaptures[Square.B6][Piece.LIGHT] = 0x5000000000000L;
    pawnCaptures[Square.C6][Piece.LIGHT] = 0xA000000000000L;
    pawnCaptures[Square.D6][Piece.LIGHT] = 0x14000000000000L;
    pawnCaptures[Square.E6][Piece.LIGHT] = 0x28000000000000L;
    pawnCaptures[Square.F6][Piece.LIGHT] = 0x50000000000000L;
    pawnCaptures[Square.G6][Piece.LIGHT] = 0xA0000000000000L;
    pawnCaptures[Square.H6][Piece.LIGHT] = 0x40000000000000L;
    pawnCaptures[Square.A7][Piece.LIGHT] = 0x200000000000000L;
    pawnCaptures[Square.B7][Piece.LIGHT] = 0x500000000000000L;
    pawnCaptures[Square.C7][Piece.LIGHT] = 0xA00000000000000L;
    pawnCaptures[Square.D7][Piece.LIGHT] = 0x1400000000000000L;
    pawnCaptures[Square.E7][Piece.LIGHT] = 0x2800000000000000L;
    pawnCaptures[Square.F7][Piece.LIGHT] = 0x5000000000000000L;
    pawnCaptures[Square.G7][Piece.LIGHT] = 0xA000000000000000L;
    pawnCaptures[Square.H7][Piece.LIGHT] = 0x4000000000000000L;
    pawnCaptures[Square.A7][Piece.DARK] = 0x20000000000L;
    pawnCaptures[Square.B7][Piece.DARK] = 0x50000000000L;
    pawnCaptures[Square.C7][Piece.DARK] = 0xA0000000000L;
    pawnCaptures[Square.D7][Piece.DARK] = 0x140000000000L;
    pawnCaptures[Square.E7][Piece.DARK] = 0x280000000000L;
    pawnCaptures[Square.F7][Piece.DARK] = 0x500000000000L;
    pawnCaptures[Square.G7][Piece.DARK] = 0xA00000000000L;
    pawnCaptures[Square.H7][Piece.DARK] = 0x400000000000L;
    pawnCaptures[Square.A6][Piece.DARK] = 0x200000000L;
    pawnCaptures[Square.B6][Piece.DARK] = 0x500000000L;
    pawnCaptures[Square.C6][Piece.DARK] = 0xA00000000L;
    pawnCaptures[Square.D6][Piece.DARK] = 0x1400000000L;
    pawnCaptures[Square.E6][Piece.DARK] = 0x2800000000L;
    pawnCaptures[Square.F6][Piece.DARK] = 0x5000000000L;
    pawnCaptures[Square.G6][Piece.DARK] = 0xA000000000L;
    pawnCaptures[Square.H6][Piece.DARK] = 0x4000000000L;
    pawnCaptures[Square.A5][Piece.DARK] = 0x2000000L;
    pawnCaptures[Square.B5][Piece.DARK] = 0x5000000L;
    pawnCaptures[Square.C5][Piece.DARK] = 0xA000000L;
    pawnCaptures[Square.D5][Piece.DARK] = 0x14000000L;
    pawnCaptures[Square.E5][Piece.DARK] = 0x28000000L;
    pawnCaptures[Square.F5][Piece.DARK] = 0x50000000L;
    pawnCaptures[Square.G5][Piece.DARK] = 0xA0000000L;
    pawnCaptures[Square.H5][Piece.DARK] = 0x40000000L;
    pawnCaptures[Square.A4][Piece.DARK] = 0x20000L;
    pawnCaptures[Square.B4][Piece.DARK] = 0x50000L;
    pawnCaptures[Square.C4][Piece.DARK] = 0xA0000L;
    pawnCaptures[Square.D4][Piece.DARK] = 0x140000L;
    pawnCaptures[Square.E4][Piece.DARK] = 0x280000L;
    pawnCaptures[Square.F4][Piece.DARK] = 0x500000L;
    pawnCaptures[Square.G4][Piece.DARK] = 0xA00000L;
    pawnCaptures[Square.H4][Piece.DARK] = 0x400000L;
    pawnCaptures[Square.A3][Piece.DARK] = 0x200L;
    pawnCaptures[Square.B3][Piece.DARK] = 0x500L;
    pawnCaptures[Square.C3][Piece.DARK] = 0xA00L;
    pawnCaptures[Square.D3][Piece.DARK] = 0x1400L;
    pawnCaptures[Square.E3][Piece.DARK] = 0x2800L;
    pawnCaptures[Square.F3][Piece.DARK] = 0x5000L;
    pawnCaptures[Square.G3][Piece.DARK] = 0xA000L;
    pawnCaptures[Square.H3][Piece.DARK] = 0x4000L;
    pawnCaptures[Square.A2][Piece.DARK] = 0x2L;
    pawnCaptures[Square.B2][Piece.DARK] = 0x5L;
    pawnCaptures[Square.C2][Piece.DARK] = 0xAL;
    pawnCaptures[Square.D2][Piece.DARK] = 0x14L;
    pawnCaptures[Square.E2][Piece.DARK] = 0x28L;
    pawnCaptures[Square.F2][Piece.DARK] = 0x50L;
    pawnCaptures[Square.G2][Piece.DARK] = 0xA0L;
    pawnCaptures[Square.H2][Piece.DARK] = 0x40L;
    // Sliders.
    sliders[Square.A1][NORTH] = 0x101010101010100L;
    sliders[Square.B1][NORTH] = 0x202020202020200L;
    sliders[Square.C1][NORTH] = 0x404040404040400L;
    sliders[Square.D1][NORTH] = 0x808080808080800L;
    sliders[Square.E1][NORTH] = 0x1010101010101000L;
    sliders[Square.F1][NORTH] = 0x2020202020202000L;
    sliders[Square.G1][NORTH] = 0x4040404040404000L;
    sliders[Square.H1][NORTH] = 0x8080808080808000L;
    sliders[Square.A2][NORTH] = 0x101010101010000L;
    sliders[Square.B2][NORTH] = 0x202020202020000L;
    sliders[Square.C2][NORTH] = 0x404040404040000L;
    sliders[Square.D2][NORTH] = 0x808080808080000L;
    sliders[Square.E2][NORTH] = 0x1010101010100000L;
    sliders[Square.F2][NORTH] = 0x2020202020200000L;
    sliders[Square.G2][NORTH] = 0x4040404040400000L;
    sliders[Square.H2][NORTH] = 0x8080808080800000L;
    sliders[Square.A3][NORTH] = 0x101010101000000L;
    sliders[Square.B3][NORTH] = 0x202020202000000L;
    sliders[Square.C3][NORTH] = 0x404040404000000L;
    sliders[Square.D3][NORTH] = 0x808080808000000L;
    sliders[Square.E3][NORTH] = 0x1010101010000000L;
    sliders[Square.F3][NORTH] = 0x2020202020000000L;
    sliders[Square.G3][NORTH] = 0x4040404040000000L;
    sliders[Square.H3][NORTH] = 0x8080808080000000L;
    sliders[Square.A4][NORTH] = 0x101010100000000L;
    sliders[Square.B4][NORTH] = 0x202020200000000L;
    sliders[Square.C4][NORTH] = 0x404040400000000L;
    sliders[Square.D4][NORTH] = 0x808080800000000L;
    sliders[Square.E4][NORTH] = 0x1010101000000000L;
    sliders[Square.F4][NORTH] = 0x2020202000000000L;
    sliders[Square.G4][NORTH] = 0x4040404000000000L;
    sliders[Square.H4][NORTH] = 0x8080808000000000L;
    sliders[Square.A5][NORTH] = 0x101010000000000L;
    sliders[Square.B5][NORTH] = 0x202020000000000L;
    sliders[Square.C5][NORTH] = 0x404040000000000L;
    sliders[Square.D5][NORTH] = 0x808080000000000L;
    sliders[Square.E5][NORTH] = 0x1010100000000000L;
    sliders[Square.F5][NORTH] = 0x2020200000000000L;
    sliders[Square.G5][NORTH] = 0x4040400000000000L;
    sliders[Square.H5][NORTH] = 0x8080800000000000L;
    sliders[Square.A6][NORTH] = 0x101000000000000L;
    sliders[Square.B6][NORTH] = 0x202000000000000L;
    sliders[Square.C6][NORTH] = 0x404000000000000L;
    sliders[Square.D6][NORTH] = 0x808000000000000L;
    sliders[Square.E6][NORTH] = 0x1010000000000000L;
    sliders[Square.F6][NORTH] = 0x2020000000000000L;
    sliders[Square.G6][NORTH] = 0x4040000000000000L;
    sliders[Square.H6][NORTH] = 0x8080000000000000L;
    sliders[Square.A7][NORTH] = 0x100000000000000L;
    sliders[Square.B7][NORTH] = 0x200000000000000L;
    sliders[Square.C7][NORTH] = 0x400000000000000L;
    sliders[Square.D7][NORTH] = 0x800000000000000L;
    sliders[Square.E7][NORTH] = 0x1000000000000000L;
    sliders[Square.F7][NORTH] = 0x2000000000000000L;
    sliders[Square.G7][NORTH] = 0x4000000000000000L;
    sliders[Square.H7][NORTH] = 0x8000000000000000L;
    sliders[Square.A1][EAST] = 0xFEL;
    sliders[Square.B1][EAST] = 0xFCL;
    sliders[Square.C1][EAST] = 0xF8L;
    sliders[Square.D1][EAST] = 0xF0L;
    sliders[Square.E1][EAST] = 0xE0L;
    sliders[Square.F1][EAST] = 0xC0L;
    sliders[Square.G1][EAST] = 0x80L;
    sliders[Square.A2][EAST] = 0xFE00L;
    sliders[Square.B2][EAST] = 0xFC00L;
    sliders[Square.C2][EAST] = 0xF800L;
    sliders[Square.D2][EAST] = 0xF000L;
    sliders[Square.E2][EAST] = 0xE000L;
    sliders[Square.F2][EAST] = 0xC000L;
    sliders[Square.G2][EAST] = 0x8000L;
    sliders[Square.A3][EAST] = 0xFE0000L;
    sliders[Square.B3][EAST] = 0xFC0000L;
    sliders[Square.C3][EAST] = 0xF80000L;
    sliders[Square.D3][EAST] = 0xF00000L;
    sliders[Square.E3][EAST] = 0xE00000L;
    sliders[Square.F3][EAST] = 0xC00000L;
    sliders[Square.G3][EAST] = 0x800000L;
    sliders[Square.A4][EAST] = 0xFE000000L;
    sliders[Square.B4][EAST] = 0xFC000000L;
    sliders[Square.C4][EAST] = 0xF8000000L;
    sliders[Square.D4][EAST] = 0xF0000000L;
    sliders[Square.E4][EAST] = 0xE0000000L;
    sliders[Square.F4][EAST] = 0xC0000000L;
    sliders[Square.G4][EAST] = 0x80000000L;
    sliders[Square.A5][EAST] = 0xFE00000000L;
    sliders[Square.B5][EAST] = 0xFC00000000L;
    sliders[Square.C5][EAST] = 0xF800000000L;
    sliders[Square.D5][EAST] = 0xF000000000L;
    sliders[Square.E5][EAST] = 0xE000000000L;
    sliders[Square.F5][EAST] = 0xC000000000L;
    sliders[Square.G5][EAST] = 0x8000000000L;
    sliders[Square.A6][EAST] = 0xFE0000000000L;
    sliders[Square.B6][EAST] = 0xFC0000000000L;
    sliders[Square.C6][EAST] = 0xF80000000000L;
    sliders[Square.D6][EAST] = 0xF00000000000L;
    sliders[Square.E6][EAST] = 0xE00000000000L;
    sliders[Square.F6][EAST] = 0xC00000000000L;
    sliders[Square.G6][EAST] = 0x800000000000L;
    sliders[Square.A7][EAST] = 0xFE000000000000L;
    sliders[Square.B7][EAST] = 0xFC000000000000L;
    sliders[Square.C7][EAST] = 0xF8000000000000L;
    sliders[Square.D7][EAST] = 0xF0000000000000L;
    sliders[Square.E7][EAST] = 0xE0000000000000L;
    sliders[Square.F7][EAST] = 0xC0000000000000L;
    sliders[Square.G7][EAST] = 0x80000000000000L;
    sliders[Square.A8][EAST] = 0xFE00000000000000L;
    sliders[Square.B8][EAST] = 0xFC00000000000000L;
    sliders[Square.C8][EAST] = 0xF800000000000000L;
    sliders[Square.D8][EAST] = 0xF000000000000000L;
    sliders[Square.E8][EAST] = 0xE000000000000000L;
    sliders[Square.F8][EAST] = 0xC000000000000000L;
    sliders[Square.G8][EAST] = 0x8000000000000000L;
    sliders[Square.A2][SOUTH] = 0x1L;
    sliders[Square.B2][SOUTH] = 0x2L;
    sliders[Square.C2][SOUTH] = 0x4L;
    sliders[Square.D2][SOUTH] = 0x8L;
    sliders[Square.E2][SOUTH] = 0x10L;
    sliders[Square.F2][SOUTH] = 0x20L;
    sliders[Square.G2][SOUTH] = 0x40L;
    sliders[Square.H2][SOUTH] = 0x80L;
    sliders[Square.A3][SOUTH] = 0x101L;
    sliders[Square.B3][SOUTH] = 0x202L;
    sliders[Square.C3][SOUTH] = 0x404L;
    sliders[Square.D3][SOUTH] = 0x808L;
    sliders[Square.E3][SOUTH] = 0x1010L;
    sliders[Square.F3][SOUTH] = 0x2020L;
    sliders[Square.G3][SOUTH] = 0x4040L;
    sliders[Square.H3][SOUTH] = 0x8080L;
    sliders[Square.A4][SOUTH] = 0x10101L;
    sliders[Square.B4][SOUTH] = 0x20202L;
    sliders[Square.C4][SOUTH] = 0x40404L;
    sliders[Square.D4][SOUTH] = 0x80808L;
    sliders[Square.E4][SOUTH] = 0x101010L;
    sliders[Square.F4][SOUTH] = 0x202020L;
    sliders[Square.G4][SOUTH] = 0x404040L;
    sliders[Square.H4][SOUTH] = 0x808080L;
    sliders[Square.A5][SOUTH] = 0x1010101L;
    sliders[Square.B5][SOUTH] = 0x2020202L;
    sliders[Square.C5][SOUTH] = 0x4040404L;
    sliders[Square.D5][SOUTH] = 0x8080808L;
    sliders[Square.E5][SOUTH] = 0x10101010L;
    sliders[Square.F5][SOUTH] = 0x20202020L;
    sliders[Square.G5][SOUTH] = 0x40404040L;
    sliders[Square.H5][SOUTH] = 0x80808080L;
    sliders[Square.A6][SOUTH] = 0x101010101L;
    sliders[Square.B6][SOUTH] = 0x202020202L;
    sliders[Square.C6][SOUTH] = 0x404040404L;
    sliders[Square.D6][SOUTH] = 0x808080808L;
    sliders[Square.E6][SOUTH] = 0x1010101010L;
    sliders[Square.F6][SOUTH] = 0x2020202020L;
    sliders[Square.G6][SOUTH] = 0x4040404040L;
    sliders[Square.H6][SOUTH] = 0x8080808080L;
    sliders[Square.A7][SOUTH] = 0x10101010101L;
    sliders[Square.B7][SOUTH] = 0x20202020202L;
    sliders[Square.C7][SOUTH] = 0x40404040404L;
    sliders[Square.D7][SOUTH] = 0x80808080808L;
    sliders[Square.E7][SOUTH] = 0x101010101010L;
    sliders[Square.F7][SOUTH] = 0x202020202020L;
    sliders[Square.G7][SOUTH] = 0x404040404040L;
    sliders[Square.H7][SOUTH] = 0x808080808080L;
    sliders[Square.A8][SOUTH] = 0x1010101010101L;
    sliders[Square.B8][SOUTH] = 0x2020202020202L;
    sliders[Square.C8][SOUTH] = 0x4040404040404L;
    sliders[Square.D8][SOUTH] = 0x8080808080808L;
    sliders[Square.E8][SOUTH] = 0x10101010101010L;
    sliders[Square.F8][SOUTH] = 0x20202020202020L;
    sliders[Square.G8][SOUTH] = 0x40404040404040L;
    sliders[Square.H8][SOUTH] = 0x80808080808080L;
    sliders[Square.B1][WEST] = 0x1L;
    sliders[Square.C1][WEST] = 0x3L;
    sliders[Square.D1][WEST] = 0x7L;
    sliders[Square.E1][WEST] = 0xFL;
    sliders[Square.F1][WEST] = 0x1FL;
    sliders[Square.G1][WEST] = 0x3FL;
    sliders[Square.H1][WEST] = 0x7FL;
    sliders[Square.B2][WEST] = 0x100L;
    sliders[Square.C2][WEST] = 0x300L;
    sliders[Square.D2][WEST] = 0x700L;
    sliders[Square.E2][WEST] = 0xF00L;
    sliders[Square.F2][WEST] = 0x1F00L;
    sliders[Square.G2][WEST] = 0x3F00L;
    sliders[Square.H2][WEST] = 0x7F00L;
    sliders[Square.B3][WEST] = 0x10000L;
    sliders[Square.C3][WEST] = 0x30000L;
    sliders[Square.D3][WEST] = 0x70000L;
    sliders[Square.E3][WEST] = 0xF0000L;
    sliders[Square.F3][WEST] = 0x1F0000L;
    sliders[Square.G3][WEST] = 0x3F0000L;
    sliders[Square.H3][WEST] = 0x7F0000L;
    sliders[Square.B4][WEST] = 0x1000000L;
    sliders[Square.C4][WEST] = 0x3000000L;
    sliders[Square.D4][WEST] = 0x7000000L;
    sliders[Square.E4][WEST] = 0xF000000L;
    sliders[Square.F4][WEST] = 0x1F000000L;
    sliders[Square.G4][WEST] = 0x3F000000L;
    sliders[Square.H4][WEST] = 0x7F000000L;
    sliders[Square.B5][WEST] = 0x100000000L;
    sliders[Square.C5][WEST] = 0x300000000L;
    sliders[Square.D5][WEST] = 0x700000000L;
    sliders[Square.E5][WEST] = 0xF00000000L;
    sliders[Square.F5][WEST] = 0x1F00000000L;
    sliders[Square.G5][WEST] = 0x3F00000000L;
    sliders[Square.H5][WEST] = 0x7F00000000L;
    sliders[Square.B6][WEST] = 0x10000000000L;
    sliders[Square.C6][WEST] = 0x30000000000L;
    sliders[Square.D6][WEST] = 0x70000000000L;
    sliders[Square.E6][WEST] = 0xF0000000000L;
    sliders[Square.F6][WEST] = 0x1F0000000000L;
    sliders[Square.G6][WEST] = 0x3F0000000000L;
    sliders[Square.H6][WEST] = 0x7F0000000000L;
    sliders[Square.B7][WEST] = 0x1000000000000L;
    sliders[Square.C7][WEST] = 0x3000000000000L;
    sliders[Square.D7][WEST] = 0x7000000000000L;
    sliders[Square.E7][WEST] = 0xF000000000000L;
    sliders[Square.F7][WEST] = 0x1F000000000000L;
    sliders[Square.G7][WEST] = 0x3F000000000000L;
    sliders[Square.H7][WEST] = 0x7F000000000000L;
    sliders[Square.B8][WEST] = 0x100000000000000L;
    sliders[Square.C8][WEST] = 0x300000000000000L;
    sliders[Square.D8][WEST] = 0x700000000000000L;
    sliders[Square.E8][WEST] = 0xF00000000000000L;
    sliders[Square.F8][WEST] = 0x1F00000000000000L;
    sliders[Square.G8][WEST] = 0x3F00000000000000L;
    sliders[Square.H8][WEST] = 0x7F00000000000000L;
    sliders[Square.A1][NORTHEAST] = 0x8040201008040200L;
    sliders[Square.B1][NORTHEAST] = 0x80402010080400L;
    sliders[Square.C1][NORTHEAST] = 0x804020100800L;
    sliders[Square.D1][NORTHEAST] = 0x8040201000L;
    sliders[Square.E1][NORTHEAST] = 0x80402000L;
    sliders[Square.F1][NORTHEAST] = 0x804000L;
    sliders[Square.G1][NORTHEAST] = 0x8000L;
    sliders[Square.A2][NORTHEAST] = 0x4020100804020000L;
    sliders[Square.B2][NORTHEAST] = 0x8040201008040000L;
    sliders[Square.C2][NORTHEAST] = 0x80402010080000L;
    sliders[Square.D2][NORTHEAST] = 0x804020100000L;
    sliders[Square.E2][NORTHEAST] = 0x8040200000L;
    sliders[Square.F2][NORTHEAST] = 0x80400000L;
    sliders[Square.G2][NORTHEAST] = 0x800000L;
    sliders[Square.A3][NORTHEAST] = 0x2010080402000000L;
    sliders[Square.B3][NORTHEAST] = 0x4020100804000000L;
    sliders[Square.C3][NORTHEAST] = 0x8040201008000000L;
    sliders[Square.D3][NORTHEAST] = 0x80402010000000L;
    sliders[Square.E3][NORTHEAST] = 0x804020000000L;
    sliders[Square.F3][NORTHEAST] = 0x8040000000L;
    sliders[Square.G3][NORTHEAST] = 0x80000000L;
    sliders[Square.A4][NORTHEAST] = 0x1008040200000000L;
    sliders[Square.B4][NORTHEAST] = 0x2010080400000000L;
    sliders[Square.C4][NORTHEAST] = 0x4020100800000000L;
    sliders[Square.D4][NORTHEAST] = 0x8040201000000000L;
    sliders[Square.E4][NORTHEAST] = 0x80402000000000L;
    sliders[Square.F4][NORTHEAST] = 0x804000000000L;
    sliders[Square.G4][NORTHEAST] = 0x8000000000L;
    sliders[Square.A5][NORTHEAST] = 0x804020000000000L;
    sliders[Square.B5][NORTHEAST] = 0x1008040000000000L;
    sliders[Square.C5][NORTHEAST] = 0x2010080000000000L;
    sliders[Square.D5][NORTHEAST] = 0x4020100000000000L;
    sliders[Square.E5][NORTHEAST] = 0x8040200000000000L;
    sliders[Square.F5][NORTHEAST] = 0x80400000000000L;
    sliders[Square.G5][NORTHEAST] = 0x800000000000L;
    sliders[Square.A6][NORTHEAST] = 0x402000000000000L;
    sliders[Square.B6][NORTHEAST] = 0x804000000000000L;
    sliders[Square.C6][NORTHEAST] = 0x1008000000000000L;
    sliders[Square.D6][NORTHEAST] = 0x2010000000000000L;
    sliders[Square.E6][NORTHEAST] = 0x4020000000000000L;
    sliders[Square.F6][NORTHEAST] = 0x8040000000000000L;
    sliders[Square.G6][NORTHEAST] = 0x80000000000000L;
    sliders[Square.A7][NORTHEAST] = 0x200000000000000L;
    sliders[Square.B7][NORTHEAST] = 0x400000000000000L;
    sliders[Square.C7][NORTHEAST] = 0x800000000000000L;
    sliders[Square.D7][NORTHEAST] = 0x1000000000000000L;
    sliders[Square.E7][NORTHEAST] = 0x2000000000000000L;
    sliders[Square.F7][NORTHEAST] = 0x4000000000000000L;
    sliders[Square.G7][NORTHEAST] = 0x8000000000000000L;
    sliders[Square.A2][SOUTHEAST] = 0x2L;
    sliders[Square.B2][SOUTHEAST] = 0x4L;
    sliders[Square.C2][SOUTHEAST] = 0x8L;
    sliders[Square.D2][SOUTHEAST] = 0x10L;
    sliders[Square.E2][SOUTHEAST] = 0x20L;
    sliders[Square.F2][SOUTHEAST] = 0x40L;
    sliders[Square.G2][SOUTHEAST] = 0x80L;
    sliders[Square.A3][SOUTHEAST] = 0x204L;
    sliders[Square.B3][SOUTHEAST] = 0x408L;
    sliders[Square.C3][SOUTHEAST] = 0x810L;
    sliders[Square.D3][SOUTHEAST] = 0x1020L;
    sliders[Square.E3][SOUTHEAST] = 0x2040L;
    sliders[Square.F3][SOUTHEAST] = 0x4080L;
    sliders[Square.G3][SOUTHEAST] = 0x8000L;
    sliders[Square.A4][SOUTHEAST] = 0x20408L;
    sliders[Square.B4][SOUTHEAST] = 0x40810L;
    sliders[Square.C4][SOUTHEAST] = 0x81020L;
    sliders[Square.D4][SOUTHEAST] = 0x102040L;
    sliders[Square.E4][SOUTHEAST] = 0x204080L;
    sliders[Square.F4][SOUTHEAST] = 0x408000L;
    sliders[Square.G4][SOUTHEAST] = 0x800000L;
    sliders[Square.A5][SOUTHEAST] = 0x2040810L;
    sliders[Square.B5][SOUTHEAST] = 0x4081020L;
    sliders[Square.C5][SOUTHEAST] = 0x8102040L;
    sliders[Square.D5][SOUTHEAST] = 0x10204080L;
    sliders[Square.E5][SOUTHEAST] = 0x20408000L;
    sliders[Square.F5][SOUTHEAST] = 0x40800000L;
    sliders[Square.G5][SOUTHEAST] = 0x80000000L;
    sliders[Square.A6][SOUTHEAST] = 0x204081020L;
    sliders[Square.B6][SOUTHEAST] = 0x408102040L;
    sliders[Square.C6][SOUTHEAST] = 0x810204080L;
    sliders[Square.D6][SOUTHEAST] = 0x1020408000L;
    sliders[Square.E6][SOUTHEAST] = 0x2040800000L;
    sliders[Square.F6][SOUTHEAST] = 0x4080000000L;
    sliders[Square.G6][SOUTHEAST] = 0x8000000000L;
    sliders[Square.A7][SOUTHEAST] = 0x20408102040L;
    sliders[Square.B7][SOUTHEAST] = 0x40810204080L;
    sliders[Square.C7][SOUTHEAST] = 0x81020408000L;
    sliders[Square.D7][SOUTHEAST] = 0x102040800000L;
    sliders[Square.E7][SOUTHEAST] = 0x204080000000L;
    sliders[Square.F7][SOUTHEAST] = 0x408000000000L;
    sliders[Square.G7][SOUTHEAST] = 0x800000000000L;
    sliders[Square.A8][SOUTHEAST] = 0x2040810204080L;
    sliders[Square.B8][SOUTHEAST] = 0x4081020408000L;
    sliders[Square.C8][SOUTHEAST] = 0x8102040800000L;
    sliders[Square.D8][SOUTHEAST] = 0x10204080000000L;
    sliders[Square.E8][SOUTHEAST] = 0x20408000000000L;
    sliders[Square.F8][SOUTHEAST] = 0x40800000000000L;
    sliders[Square.G8][SOUTHEAST] = 0x80000000000000L;
    sliders[Square.B2][SOUTHWEST] = 0x1L;
    sliders[Square.C2][SOUTHWEST] = 0x2L;
    sliders[Square.D2][SOUTHWEST] = 0x4L;
    sliders[Square.E2][SOUTHWEST] = 0x8L;
    sliders[Square.F2][SOUTHWEST] = 0x10L;
    sliders[Square.G2][SOUTHWEST] = 0x20L;
    sliders[Square.H2][SOUTHWEST] = 0x40L;
    sliders[Square.B3][SOUTHWEST] = 0x100L;
    sliders[Square.C3][SOUTHWEST] = 0x201L;
    sliders[Square.D3][SOUTHWEST] = 0x402L;
    sliders[Square.E3][SOUTHWEST] = 0x804L;
    sliders[Square.F3][SOUTHWEST] = 0x1008L;
    sliders[Square.G3][SOUTHWEST] = 0x2010L;
    sliders[Square.H3][SOUTHWEST] = 0x4020L;
    sliders[Square.B4][SOUTHWEST] = 0x10000L;
    sliders[Square.C4][SOUTHWEST] = 0x20100L;
    sliders[Square.D4][SOUTHWEST] = 0x40201L;
    sliders[Square.E4][SOUTHWEST] = 0x80402L;
    sliders[Square.F4][SOUTHWEST] = 0x100804L;
    sliders[Square.G4][SOUTHWEST] = 0x201008L;
    sliders[Square.H4][SOUTHWEST] = 0x402010L;
    sliders[Square.B5][SOUTHWEST] = 0x1000000L;
    sliders[Square.C5][SOUTHWEST] = 0x2010000L;
    sliders[Square.D5][SOUTHWEST] = 0x4020100L;
    sliders[Square.E5][SOUTHWEST] = 0x8040201L;
    sliders[Square.F5][SOUTHWEST] = 0x10080402L;
    sliders[Square.G5][SOUTHWEST] = 0x20100804L;
    sliders[Square.H5][SOUTHWEST] = 0x40201008L;
    sliders[Square.B6][SOUTHWEST] = 0x100000000L;
    sliders[Square.C6][SOUTHWEST] = 0x201000000L;
    sliders[Square.D6][SOUTHWEST] = 0x402010000L;
    sliders[Square.E6][SOUTHWEST] = 0x804020100L;
    sliders[Square.F6][SOUTHWEST] = 0x1008040201L;
    sliders[Square.G6][SOUTHWEST] = 0x2010080402L;
    sliders[Square.H6][SOUTHWEST] = 0x4020100804L;
    sliders[Square.B7][SOUTHWEST] = 0x10000000000L;
    sliders[Square.C7][SOUTHWEST] = 0x20100000000L;
    sliders[Square.D7][SOUTHWEST] = 0x40201000000L;
    sliders[Square.E7][SOUTHWEST] = 0x80402010000L;
    sliders[Square.F7][SOUTHWEST] = 0x100804020100L;
    sliders[Square.G7][SOUTHWEST] = 0x201008040201L;
    sliders[Square.H7][SOUTHWEST] = 0x402010080402L;
    sliders[Square.B8][SOUTHWEST] = 0x1000000000000L;
    sliders[Square.C8][SOUTHWEST] = 0x2010000000000L;
    sliders[Square.D8][SOUTHWEST] = 0x4020100000000L;
    sliders[Square.E8][SOUTHWEST] = 0x8040201000000L;
    sliders[Square.F8][SOUTHWEST] = 0x10080402010000L;
    sliders[Square.G8][SOUTHWEST] = 0x20100804020100L;
    sliders[Square.H8][SOUTHWEST] = 0x40201008040201L;
    sliders[Square.B1][NORTHWEST] = 0x100L;
    sliders[Square.C1][NORTHWEST] = 0x10200L;
    sliders[Square.D1][NORTHWEST] = 0x1020400L;
    sliders[Square.E1][NORTHWEST] = 0x102040800L;
    sliders[Square.F1][NORTHWEST] = 0x10204081000L;
    sliders[Square.G1][NORTHWEST] = 0x1020408102000L;
    sliders[Square.H1][NORTHWEST] = 0x102040810204000L;
    sliders[Square.B2][NORTHWEST] = 0x10000L;
    sliders[Square.C2][NORTHWEST] = 0x1020000L;
    sliders[Square.D2][NORTHWEST] = 0x102040000L;
    sliders[Square.E2][NORTHWEST] = 0x10204080000L;
    sliders[Square.F2][NORTHWEST] = 0x1020408100000L;
    sliders[Square.G2][NORTHWEST] = 0x102040810200000L;
    sliders[Square.H2][NORTHWEST] = 0x204081020400000L;
    sliders[Square.B3][NORTHWEST] = 0x1000000L;
    sliders[Square.C3][NORTHWEST] = 0x102000000L;
    sliders[Square.D3][NORTHWEST] = 0x10204000000L;
    sliders[Square.E3][NORTHWEST] = 0x1020408000000L;
    sliders[Square.F3][NORTHWEST] = 0x102040810000000L;
    sliders[Square.G3][NORTHWEST] = 0x204081020000000L;
    sliders[Square.H3][NORTHWEST] = 0x408102040000000L;
    sliders[Square.B4][NORTHWEST] = 0x100000000L;
    sliders[Square.C4][NORTHWEST] = 0x10200000000L;
    sliders[Square.D4][NORTHWEST] = 0x1020400000000L;
    sliders[Square.E4][NORTHWEST] = 0x102040800000000L;
    sliders[Square.F4][NORTHWEST] = 0x204081000000000L;
    sliders[Square.G4][NORTHWEST] = 0x408102000000000L;
    sliders[Square.H4][NORTHWEST] = 0x810204000000000L;
    sliders[Square.B5][NORTHWEST] = 0x10000000000L;
    sliders[Square.C5][NORTHWEST] = 0x1020000000000L;
    sliders[Square.D5][NORTHWEST] = 0x102040000000000L;
    sliders[Square.E5][NORTHWEST] = 0x204080000000000L;
    sliders[Square.F5][NORTHWEST] = 0x408100000000000L;
    sliders[Square.G5][NORTHWEST] = 0x810200000000000L;
    sliders[Square.H5][NORTHWEST] = 0x1020400000000000L;
    sliders[Square.B6][NORTHWEST] = 0x1000000000000L;
    sliders[Square.C6][NORTHWEST] = 0x102000000000000L;
    sliders[Square.D6][NORTHWEST] = 0x204000000000000L;
    sliders[Square.E6][NORTHWEST] = 0x408000000000000L;
    sliders[Square.F6][NORTHWEST] = 0x810000000000000L;
    sliders[Square.G6][NORTHWEST] = 0x1020000000000000L;
    sliders[Square.H6][NORTHWEST] = 0x2040000000000000L;
    sliders[Square.B7][NORTHWEST] = 0x100000000000000L;
    sliders[Square.C7][NORTHWEST] = 0x200000000000000L;
    sliders[Square.D7][NORTHWEST] = 0x400000000000000L;
    sliders[Square.E7][NORTHWEST] = 0x800000000000000L;
    sliders[Square.F7][NORTHWEST] = 0x1000000000000000L;
    sliders[Square.G7][NORTHWEST] = 0x2000000000000000L;
    sliders[Square.H7][NORTHWEST] = 0x4000000000000000L;
    // Knight moves.
    knightMoves[Square.A1] = 0x20400L;
    knightMoves[Square.B1] = 0x50800L;
    knightMoves[Square.C1] = 0xA1100L;
    knightMoves[Square.D1] = 0x142200L;
    knightMoves[Square.E1] = 0x284400L;
    knightMoves[Square.F1] = 0x508800L;
    knightMoves[Square.G1] = 0xA01000L;
    knightMoves[Square.H1] = 0x402000L;
    knightMoves[Square.A2] = 0x2040004L;
    knightMoves[Square.B2] = 0x5080008L;
    knightMoves[Square.C2] = 0xA110011L;
    knightMoves[Square.D2] = 0x14220022L;
    knightMoves[Square.E2] = 0x28440044L;
    knightMoves[Square.F2] = 0x50880088L;
    knightMoves[Square.G2] = 0xA0100010L;
    knightMoves[Square.H2] = 0x40200020L;
    knightMoves[Square.A3] = 0x204000402L;
    knightMoves[Square.B3] = 0x508000805L;
    knightMoves[Square.C3] = 0xA1100110AL;
    knightMoves[Square.D3] = 0x1422002214L;
    knightMoves[Square.E3] = 0x2844004428L;
    knightMoves[Square.F3] = 0x5088008850L;
    knightMoves[Square.G3] = 0xA0100010A0L;
    knightMoves[Square.H3] = 0x4020002040L;
    knightMoves[Square.A4] = 0x20400040200L;
    knightMoves[Square.B4] = 0x50800080500L;
    knightMoves[Square.C4] = 0xA1100110A00L;
    knightMoves[Square.D4] = 0x142200221400L;
    knightMoves[Square.E4] = 0x284400442800L;
    knightMoves[Square.F4] = 0x508800885000L;
    knightMoves[Square.G4] = 0xA0100010A000L;
    knightMoves[Square.H4] = 0x402000204000L;
    knightMoves[Square.A5] = 0x2040004020000L;
    knightMoves[Square.B5] = 0x5080008050000L;
    knightMoves[Square.C5] = 0xA1100110A0000L;
    knightMoves[Square.D5] = 0x14220022140000L;
    knightMoves[Square.E5] = 0x28440044280000L;
    knightMoves[Square.F5] = 0x50880088500000L;
    knightMoves[Square.G5] = 0xA0100010A00000L;
    knightMoves[Square.H5] = 0x40200020400000L;
    knightMoves[Square.A6] = 0x204000402000000L;
    knightMoves[Square.B6] = 0x508000805000000L;
    knightMoves[Square.C6] = 0xA1100110A000000L;
    knightMoves[Square.D6] = 0x1422002214000000L;
    knightMoves[Square.E6] = 0x2844004428000000L;
    knightMoves[Square.F6] = 0x5088008850000000L;
    knightMoves[Square.G6] = 0xA0100010A0000000L;
    knightMoves[Square.H6] = 0x4020002040000000L;
    knightMoves[Square.A7] = 0x400040200000000L;
    knightMoves[Square.B7] = 0x800080500000000L;
    knightMoves[Square.C7] = 0x1100110A00000000L;
    knightMoves[Square.D7] = 0x2200221400000000L;
    knightMoves[Square.E7] = 0x4400442800000000L;
    knightMoves[Square.F7] = 0x8800885000000000L;
    knightMoves[Square.G7] = 0x100010A000000000L;
    knightMoves[Square.H7] = 0x2000204000000000L;
    knightMoves[Square.A8] = 0x4020000000000L;
    knightMoves[Square.B8] = 0x8050000000000L;
    knightMoves[Square.C8] = 0x110A0000000000L;
    knightMoves[Square.D8] = 0x22140000000000L;
    knightMoves[Square.E8] = 0x44280000000000L;
    knightMoves[Square.F8] = 0x88500000000000L;
    knightMoves[Square.G8] = 0x10A00000000000L;
    knightMoves[Square.H8] = 0x20400000000000L;
    // King moves.
    kingMoves[Square.A1] = 0x302L;
    kingMoves[Square.B1] = 0x705L;
    kingMoves[Square.C1] = 0xE0AL;
    kingMoves[Square.D1] = 0x1C14L;
    kingMoves[Square.E1] = 0x3828L;
    kingMoves[Square.F1] = 0x7050L;
    kingMoves[Square.G1] = 0xE0A0L;
    kingMoves[Square.H1] = 0xC040L;
    kingMoves[Square.A2] = 0x30203L;
    kingMoves[Square.B2] = 0x70507L;
    kingMoves[Square.C2] = 0xE0A0EL;
    kingMoves[Square.D2] = 0x1C141CL;
    kingMoves[Square.E2] = 0x382838L;
    kingMoves[Square.F2] = 0x705070L;
    kingMoves[Square.G2] = 0xE0A0E0L;
    kingMoves[Square.H2] = 0xC040C0L;
    kingMoves[Square.A3] = 0x3020300L;
    kingMoves[Square.B3] = 0x7050700L;
    kingMoves[Square.C3] = 0xE0A0E00L;
    kingMoves[Square.D3] = 0x1C141C00L;
    kingMoves[Square.E3] = 0x38283800L;
    kingMoves[Square.F3] = 0x70507000L;
    kingMoves[Square.G3] = 0xE0A0E000L;
    kingMoves[Square.H3] = 0xC040C000L;
    kingMoves[Square.A4] = 0x302030000L;
    kingMoves[Square.B4] = 0x705070000L;
    kingMoves[Square.C4] = 0xE0A0E0000L;
    kingMoves[Square.D4] = 0x1C141C0000L;
    kingMoves[Square.E4] = 0x3828380000L;
    kingMoves[Square.F4] = 0x7050700000L;
    kingMoves[Square.G4] = 0xE0A0E00000L;
    kingMoves[Square.H4] = 0xC040C00000L;
    kingMoves[Square.A5] = 0x30203000000L;
    kingMoves[Square.B5] = 0x70507000000L;
    kingMoves[Square.C5] = 0xE0A0E000000L;
    kingMoves[Square.D5] = 0x1C141C000000L;
    kingMoves[Square.E5] = 0x382838000000L;
    kingMoves[Square.F5] = 0x705070000000L;
    kingMoves[Square.G5] = 0xE0A0E0000000L;
    kingMoves[Square.H5] = 0xC040C0000000L;
    kingMoves[Square.A6] = 0x3020300000000L;
    kingMoves[Square.B6] = 0x7050700000000L;
    kingMoves[Square.C6] = 0xE0A0E00000000L;
    kingMoves[Square.D6] = 0x1C141C00000000L;
    kingMoves[Square.E6] = 0x38283800000000L;
    kingMoves[Square.F6] = 0x70507000000000L;
    kingMoves[Square.G6] = 0xE0A0E000000000L;
    kingMoves[Square.H6] = 0xC040C000000000L;
    kingMoves[Square.A7] = 0x302030000000000L;
    kingMoves[Square.B7] = 0x705070000000000L;
    kingMoves[Square.C7] = 0xE0A0E0000000000L;
    kingMoves[Square.D7] = 0x1C141C0000000000L;
    kingMoves[Square.E7] = 0x3828380000000000L;
    kingMoves[Square.F7] = 0x7050700000000000L;
    kingMoves[Square.G7] = 0xE0A0E00000000000L;
    kingMoves[Square.H7] = 0xC040C00000000000L;
    kingMoves[Square.A8] = 0x203000000000000L;
    kingMoves[Square.B8] = 0x507000000000000L;
    kingMoves[Square.C8] = 0xA0E000000000000L;
    kingMoves[Square.D8] = 0x141C000000000000L;
    kingMoves[Square.E8] = 0x2838000000000000L;
    kingMoves[Square.F8] = 0x5070000000000000L;
    kingMoves[Square.G8] = 0xA0E0000000000000L;
    kingMoves[Square.H8] = 0x40C0000000000000L;
    // Files.
    files[0] = 0x101010101010101L;
    files[1] = 0x202020202020202L;
    files[2] = 0x404040404040404L;
    files[3] = 0x808080808080808L;
    files[4] = 0x1010101010101010L;
    files[5] = 0x2020202020202020L;
    files[6] = 0x4040404040404040L;
    files[7] = 0x8080808080808080L;
    // Ranks.
    ranks[0] = 0xFFL;
    ranks[1] = 0xFF00L;
    ranks[2] = 0xFF0000L;
    ranks[3] = 0xFF000000L;
    ranks[4] = 0xFF00000000L;
    ranks[5] = 0xFF0000000000L;
    ranks[6] = 0xFF000000000000L;
    ranks[7] = 0xFF00000000000000L;
    // Double pushes.
    doublePushes[Piece.LIGHT] = ranks[3];
    doublePushes[Piece.DARK] = ranks[4];
    // En passant ranks.
    enPassantRanks[Piece.LIGHT] = ranks[4];
    enPassantRanks[Piece.DARK] = ranks[3];
  }
}