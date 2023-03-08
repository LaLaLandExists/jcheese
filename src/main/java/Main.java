import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import jcheese.*;
import jcheese.ai.RandomController;
import jcheese.client.CLI;
import jcheese.server.Server;
import jcheese.swing_ui.Assets;
import jcheese.swing_ui.BoardPanel;

import javax.swing.*;

public class Main {
  public static void randomGameTest() {
    Server server = new Server();
    
    RandomController random = new RandomController();
    server.setDarkControl(random);
    server.setLightControl(random);
    server.addView(new CLI());
    
    server.launch();
    
    System.out.printf("Random seed = %d\n", random.seed);
  }
  
  public static void perftreeTest(String[] args) {
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

  public static void swingUITest() {
    BoardPanel bp = new BoardPanel();
    JFrame frame = new JFrame("The Test of Wills");

    SwingUtilities.invokeLater(() -> {
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(bp, BorderLayout.CENTER);
      frame.setSize(800, 800);
      frame.setVisible(true);
    });

    Board mainBoard = new Board();
    MoveGenerator gen = new MoveGenerator();
    FEN.loadFEN(mainBoard, Board.START_POS);
    ArrayList<Integer> moves = new ArrayList<>();
    bp.flipped = true;

    for (;;) {
      CountDownLatch latch = new CountDownLatch(1);

      gen.getMoves(mainBoard, moves);

      if (moves.size() == 0 && gen.isChecked()) break;

      bp.updateBoard(mainBoard);
      bp.repaint();

      int move;
      if (mainBoard.getPlySide() == Piece.DARK) {
        bp.setActions(moves, gen.getLastCheckers());

        bp.queryForAction(new BoardPanel.IQueryIndicator() {
          @Override
          public void start() {
            try {
              latch.await();
            } catch (InterruptedException exc) {
              throw new RuntimeException("UI Thread was interrupted");
            }
          }

          @Override
          public void end() {
            latch.countDown();
          }
        });
        move = Move.findMove(moves, bp.getActionSrc(), bp.getActionDst(), bp.getActionPromoteKind());
      } else {
        move = moves.get((int) (Math.random() * moves.size()));
      }

      mainBoard.applyMove(move);
    }

    frame.dispose();
  }
  
  public static void main(String[] args) {
    swingUITest();
  }
}
