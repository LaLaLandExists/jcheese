package jcheese.katubuyan;

import jcheese.*;
import jcheese.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Katubuyan extends JFrame {
  private static class SquaringPanel extends JPanel {
    private JPanel keepSquare;

    public SquaringPanel(JPanel panel, Color bgColor) {
      super();
      keepSquare = panel;
      setLayout(new BorderLayout());
      setBackground(bgColor);
      add(panel, BorderLayout.CENTER);
      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          super.componentResized(e);
          int side = Math.min(getWidth(), getHeight());
          keepSquare.setSize(side, side);
          keepSquare.setLocation((getWidth() - side) / 2, (getHeight() - side) / 2);
        }
      });
    }

    public void setPanel(JPanel panel) {
      keepSquare.setVisible(false);
      remove(keepSquare);
      keepSquare = panel;
      add(panel, BorderLayout.CENTER);
      panel.setVisible(true);
    }
  }

  private final SquaringPanel display;
  private final StartScreen start;
  private final GameScreen gameScreen;
  private final GameOptions options;

  public static class GameOptions {
    public int playerColor = Piece.LIGHT;

    public void setPlayerColor(int color) { playerColor = color; }
  }

  public interface OnScreenDone {
    void run();
  }

  private void game(GameScreen gs, int playerSide) throws InterruptedException {
    Board board = new Board();
    MoveGenerator gen = new MoveGenerator();
    ArrayList<Integer> moves = new ArrayList<>();

    FEN.loadFEN(board, Board.START_POS);

    for (;;) {
      gen.getMoves(board, moves);

      if (moves.isEmpty()) {
        gs.bp.setBoard(board);
        if (gen.isChecked()) {
          // Report checkmate
          if (playerSide != board.getPlySide()) {
            JOptionPane.showMessageDialog(null, "You checkmated! You lost.");
          } else {
            JOptionPane.showMessageDialog(null, "You managed to get checkmated. You won!");
          }
        } else {
          // Report stalemate
          JOptionPane.showMessageDialog(null, "It's a draw.");
        }
        display.setPanel(start);
        revalidate();
        return;
      }

      gs.bp.setBoard(board);

      int move;
      if (board.getPlySide() == playerSide) {
        gs.bp.setActions(moves, gen.getLastCheckers());
        gs.bp.setSelectables(board.bitboards[playerSide]);
        gs.bp.awaitMove();
        move = Move.findMove(moves, gs.bp.getResponseSrc(), gs.bp.getResponseDst(), gs.bp.getResponseKind());
        gs.bp.setSelectables(0L);
        gs.bp.clearActions();
      } else {
        move = moves.get((int) (Math.random() * moves.size()));
      }

      board.applyMove(move);
      gs.bp.setLastMove(move);
      gs.bp.awaitAnimation();
    }
  }

  public Katubuyan() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(720, 720);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());
    setResizable(false);
    setTitle("JCheese");
    try {
      setIconImage(ImageIO.read(new FileInputStream("./assets/katubuyan/app_icon.png")));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Missing app_icon.png");
      throw new AssertionError();
    }

    options = new GameOptions();
    start = new StartScreen(options, this::onGameStart);
    gameScreen = new GameScreen();
    display = new SquaringPanel(start, Color.DARK_GRAY);
    add(display, BorderLayout.CENTER);
  }

  private void onGameStart() {
    new Thread(() -> {
      try {
        gameScreen.refresh(options.playerColor);
        display.setPanel(gameScreen);
        display.revalidate();
        game(gameScreen, options.playerColor);
      } catch (InterruptedException e) {
        JOptionPane.showMessageDialog(null, "Game thread interrupted");
        System.exit(-5);
      }
      
//      System.out.println("Thread killed");
    }).start();
  }
}
