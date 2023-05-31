import jcheese.MoveGenerator;
import jcheese.ai.RandomController;
import jcheese.client.CLI;
import jcheese.server.Server;

import java.util.Arrays;

public class Test {
  private Test() {}

  public static void perftree(String[] args) {
    int processed = 0;
    int depth = Integer.parseInt(args[processed++]);
    String fen = args[processed++];
    fen = fen.substring(1, fen.length() - 1);

    String[] ssans;
    if (processed < args.length) {
      ssans = Arrays.copyOfRange(args, processed, args.length);
    } else {
      ssans = new String[] {};
    }

    new MoveGenerator().testForPerftree(depth, fen, ssans);
  }

  public static void randomGame() {
    Server server = new Server();

    RandomController random = new RandomController();
    server.setDarkControl(random);
    server.setLightControl(random);
    server.addView(new CLI());

    server.launch();

    System.out.printf("Random seed = %d\n", random.seed);
  }
}