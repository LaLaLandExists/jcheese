package jcheese;

@SuppressWarnings("unused")
public class Square {
  public static final int COUNT = 64;
  public static final int FILE_COUNT = 8;
  public static final int RANK_COUNT = 8;

  public static final int A1 = 0;
  public static final int B1 = 1;
  public static final int C1 = 2;
  public static final int D1 = 3;
  public static final int E1 = 4;
  public static final int F1 = 5;
  public static final int G1 = 6;
  public static final int H1 = 7;
  public static final int A2 = 8;
  public static final int B2 = 9;
  public static final int C2 = 10;
  public static final int D2 = 11;
  public static final int E2 = 12;
  public static final int F2 = 13;
  public static final int G2 = 14;
  public static final int H2 = 15;
  public static final int A3 = 16;
  public static final int B3 = 17;
  public static final int C3 = 18;
  public static final int D3 = 19;
  public static final int E3 = 20;
  public static final int F3 = 21;
  public static final int G3 = 22;
  public static final int H3 = 23;
  public static final int A4 = 24;
  public static final int B4 = 25;
  public static final int C4 = 26;
  public static final int D4 = 27;
  public static final int E4 = 28;
  public static final int F4 = 29;
  public static final int G4 = 30;
  public static final int H4 = 31;
  public static final int A5 = 32;
  public static final int B5 = 33;
  public static final int C5 = 34;
  public static final int D5 = 35;
  public static final int E5 = 36;
  public static final int F5 = 37;
  public static final int G5 = 38;
  public static final int H5 = 39;
  public static final int A6 = 40;
  public static final int B6 = 41;
  public static final int C6 = 42;
  public static final int D6 = 43;
  public static final int E6 = 44;
  public static final int F6 = 45;
  public static final int G6 = 46;
  public static final int H6 = 47;
  public static final int A7 = 48;
  public static final int B7 = 49;
  public static final int C7 = 50;
  public static final int D7 = 51;
  public static final int E7 = 52;
  public static final int F7 = 53;
  public static final int G7 = 54;
  public static final int H7 = 55;
  public static final int A8 = 56;
  public static final int B8 = 57;
  public static final int C8 = 58;
  public static final int D8 = 59;
  public static final int E8 = 60;
  public static final int F8 = 61;
  public static final int G8 = 62;
  public static final int H8 = 63;
  public static final int NIL = 64;

  public static final String[] names = new String[] {
    "A1", "B1", "C1", "D1",
    "E1", "F1", "G1", "H1",
    "A2", "B2", "C2", "D2",
    "E2", "F2", "G2", "H2",
    "A3", "B3", "C3", "D3",
    "E3", "F3", "G3", "H3",
    "A4", "B4", "C4", "D4",
    "E4", "F4", "G4", "H4",
    "A5", "B5", "C5", "D5",
    "E5", "F5", "G5", "H5",
    "A6", "B6", "C6", "D6",
    "E6", "F6", "G6", "H6",
    "A7", "B7", "C7", "D7",
    "E7", "F7", "G7", "H7",
    "A8", "B8", "C8", "D8",
    "E8", "F8", "G8", "H8",
    "NIL"
  };

  private Square() {} // not instantiable

  public static int getX(int sq) {
    return sq & 7;
  }
  public static int getY(int sq) {
    return sq >> 3;
  }
  public static int fromCoords(int x, int y) {
    return y * FILE_COUNT + x;
  }

  public static boolean withinBounds(int x, int y) {
    return x >= 0 && x < FILE_COUNT &&
           y >= 0 && y < RANK_COUNT;
  }
  public static int offset(int sq, int dx, int dy) {
    assert offsetWithinBounds(sq, dx, dy);
    return fromCoords(getX(sq) + dx, getY(sq) + dy);
  }
  public static boolean offsetWithinBounds(int sq, int dx, int dy) {
    return withinBounds(getX(sq) + dx, getY(sq) + dy);
  }
}
