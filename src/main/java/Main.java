import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jcheese.Board;
import jcheese.FEN;
import jcheese.Move;
import jcheese.MoveGenerator;
import jcheese.Piece;
import jcheese.ai.RandomController;
import jcheese.client.CLI;
import jcheese.katubuyan.Katubuyan;
import jcheese.katubuyan.StartScreen;
import jcheese.server.Server;
import jcheese.swing_ui.BoardPane;

public class Main {
  public static void swingUITest() throws InterruptedException {
    BoardPane bp = new BoardPane();
    JFrame frame = new JFrame("Random AI Test");

    SwingUtilities.invokeLater(() -> {
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(bp, BorderLayout.CENTER);
      frame.setSize(640, 640);
      frame.setLocation(80, 80);
      frame.setVisible(true);
    });

    Board mainBoard = new Board();
    MoveGenerator gen = new MoveGenerator();
    FEN.loadFEN(mainBoard, Board.START_POS);
    ArrayList<Integer> moves = new ArrayList<>();
    
    final int thePlayer = Piece.LIGHT;
    if (thePlayer != Piece.LIGHT) bp.setFlipped(true);
    
    for (;;) {
      gen.getMoves(mainBoard, moves);

      if (moves.size() == 0) {
        if (gen.isChecked()) {
          System.out.println("Checkmate!");
        } else {
          System.out.println("Stalemate!");
        }
        break;
      }

      bp.setBoard(mainBoard);

      int move;
      if (mainBoard.getPlySide() == thePlayer) {
        bp.setActions(moves, gen.getLastCheckers());
        bp.setSelectables(mainBoard.bitboards[thePlayer]);
        bp.awaitMove();
        move = Move.findMove(moves, bp.getResponseSrc(), bp.getResponseDst(), bp.getResponseKind());
        bp.setSelectables(0L);
        bp.clearActions();
      } else {
        move = moves.get((int) (Math.random() * moves.size()));
      }

      mainBoard.applyMove(move);
      bp.setLastMove(move);
      //Thread.sleep(250);

      bp.awaitAnimation();
    }

    frame.dispose();
  }
  
  private static void pvp() throws InterruptedException {
      BoardPane bp = new BoardPane();
      JFrame frame = new JFrame("Over-the-Board Test");
      
      SwingUtilities.invokeLater(() -> {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(bp, BorderLayout.CENTER);
        frame.setSize(800, 800);
        frame.setLocation(80, 80);
        frame.setVisible(true);
      });
      
      Board board = new Board();
      FEN.loadFEN(board, Board.START_POS);
      MoveGenerator gen = new MoveGenerator();
      ArrayList<Integer> moves = new ArrayList<>();
      
      int move;
      for(;;) {
          gen.getMoves(board, moves);
          
          bp.setBoard(board);
          bp.setActions(moves, gen.getLastCheckers());
          bp.setSelectables(board.bitboards[board.getPlySide()]);
          bp.awaitMove();
          move = Move.findMove(moves, bp.getResponseSrc(), bp.getResponseDst(), bp.getResponseKind());
          board.applyMove(move);
          bp.setLastMove(move);
          bp.awaitAnimation();
      }
      
  }

  public static void main(String[] args) throws InterruptedException {
    SwingUtilities.invokeLater(() -> {
      Katubuyan kt = new Katubuyan();
      kt.setVisible(true);
    });
  }
}
