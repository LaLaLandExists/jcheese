package jcheese.client;

import java.util.ArrayList;

public interface IController {
  int getMove(ArrayList<Integer> legalMoves);
  void illegalMove(int move);
  void acceptedMove(int move);
}