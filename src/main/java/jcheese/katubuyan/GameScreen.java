package jcheese.katubuyan;

import jcheese.Board;
import jcheese.FEN;
import jcheese.MoveGenerator;
import jcheese.Piece;
import jcheese.swing_ui.BoardPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameScreen extends JPanel {
  public final BoardPane bp = new BoardPane();

  public GameScreen(int playerSide) {
    super();
    setLayout(new BorderLayout());
    add(bp, BorderLayout.CENTER);

    if (playerSide != Piece.LIGHT) bp.setFlipped(true);
  }
}
