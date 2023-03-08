package jcheese.client;

import java.util.*;
import jcheese.*;

public class CLI extends AbstractViewer implements IController {
  private final Scanner sc = new Scanner(System.in);
  
  @Override
  public int getMove(ArrayList<Integer> legalMoves) {
    int max = legalMoves.size();
    
    System.out.println("Select one move by entering the [index]");
    for (int i = 0; i < max; ++i) {
      System.out.printf("[%d] -> %s\n", i, Move.getLAN(legalMoves.get(i)));
    }

    int selected;
    for (;;) {
      try {
        System.out.print("--------------\n>>> ");
        selected = sc.nextInt();
        if (selected < 0 || selected >= max) {
          System.out.printf("Index %d out of bounds", selected);
          continue; // Continue asking for input
        }
        return legalMoves.get(selected);
      } catch (InputMismatchException exc) {
        System.out.println("Index must be a number");
        // Continue asking for input
      }
    }
  }
  
  @Override public void illegalMove(int move) {
    System.out.println("Invalid move?");
  }
  @Override public void acceptedMove(int move) {}

  @Override
  public void update() {
    getBoard().print();
  }

  @Override
  public void announceWin(int cause, int side) {
    System.out.println((side == Piece.LIGHT ? "WHITE" : "BLACK") + " wins!");
  }

  @Override
  public void announceDraw(int cause) {
    System.out.println("Draw!");
  }
}