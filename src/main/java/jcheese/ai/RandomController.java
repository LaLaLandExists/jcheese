package jcheese.ai;

import java.util.*;
import jcheese.*;
import jcheese.client.*;

public class RandomController implements IController {
  private final Random randomDevice = new Random();
  public final long seed;
  
  public RandomController() {
    seed = new Random().nextLong();
    randomDevice.setSeed(seed);
  }
  public RandomController(long seed) {
    randomDevice.setSeed(seed);
    this.seed = seed;
  }
  
  @Override
  public int getMove(ArrayList<Integer> legalMoves) {
    assert !legalMoves.isEmpty();
    return legalMoves.get(randomDevice.nextInt(legalMoves.size()));
  }

  @Override
  public void illegalMove(int move) {
    throw new AssertionError("Illegal move " + Move.getLAN(move));
  }

  @Override
  public void acceptedMove(int move) {}
}