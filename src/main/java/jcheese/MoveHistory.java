package jcheese;

import java.util.Stack;

public class MoveHistory {
  private static final long MOVE_MASK = Integer.MAX_VALUE;
  private static final long LIGHT_KING_CASTLE_MASK = 1L << 32;
  private static final long DARK_KING_CASTLE_MASK = 1L << 33;
  private static final long LIGHT_QUEEN_CASTLE_MASK = 1L << 34;
  private static final long DARK_QUEEN_CASTLE_MASK = 1L << 35;
  private static final long CASTLE_MASK =
      LIGHT_KING_CASTLE_MASK | DARK_KING_CASTLE_MASK  | LIGHT_QUEEN_CASTLE_MASK | DARK_QUEEN_CASTLE_MASK;
  private static final long HMOVE_CLOCK_MASK = 0x7FL << 36;
  private static final long EP_MASK = 0x7FL << 43;

  private final Stack<Long> entries = new Stack<>();

  private static long create(int move, boolean lkc, boolean dkc, boolean lqc, boolean dqc, int ep, int hmoveClock) {
    long entry = move;
    entry |= (lkc ? 1L : 0L) << 32;
    entry |= (dkc ? 1L : 0L) << 33;
    entry |= (lqc ? 1L : 0L) << 34;
    entry |= (dqc ? 1L : 0L) << 35;
    entry |= (long) hmoveClock << 36;
    entry |= (long) ep << 43;
    return entry;
  }

  public static int getMove(long entry) {
    return (int) (entry & MOVE_MASK);
  }
  public static boolean hasLightKingCastle(long entry) {
    return (entry & LIGHT_KING_CASTLE_MASK) != 0L;
  }
  public static boolean hasDarkKingCastle(long entry) {
    return (entry & DARK_KING_CASTLE_MASK) != 0L;
  }
  public static boolean hasLightQueenCastle(long entry) {
    return (entry & LIGHT_QUEEN_CASTLE_MASK) != 0L;
  }
  public static boolean hasDarkQueenCastle(long entry) {
    return (entry & DARK_QUEEN_CASTLE_MASK) != 0L;
  }
  public static int getEnPassant(long entry) {
    return (int) ((entry & EP_MASK) >> 43);
  }
  public static int getHmoveClock(long entry) {
    return (int) ((entry & HMOVE_CLOCK_MASK) >> 36);
  }

  public long popLast() {
    assert size() > 0;
    return entries.pop();
  }

  public long get(int i) { return entries.get(i); }

  public void add(Board preMoveState, int move) {
    long entry = create(move, preMoveState.getCastle(Board.LIGHT_KING_CASTLE),
                        preMoveState.getCastle(Board.DARK_KING_CASTLE), preMoveState.getCastle(Board.LIGHT_QUEEN_CASTLE),
                        preMoveState.getCastle(Board.DARK_QUEEN_CASTLE), preMoveState.getEpSquare(), preMoveState.getHalfMoveClock());
    entries.push(entry);
  }

  public int size() { return entries.size(); }
}
