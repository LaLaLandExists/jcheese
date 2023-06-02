package jcheese.katubuyan;

import jcheese.Piece;
import jcheese.swing_ui.BoardPane;

import javax.swing.*;
import java.awt.*;

public class GameScreen extends JPanel {
  public BoardPane bp;

  public GameScreen() {
    super();
    setLayout(new BorderLayout());
  }
  
  public void refresh(int playerSide) {
    if (bp != null) {
      remove(bp);
      bp.dispose();
    }
    bp = new BoardPane();
    
    if (playerSide != Piece.LIGHT) bp.flip();
    add(bp, BorderLayout.CENTER);
    revalidate();
  }
}
