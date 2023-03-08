package jcheese.swing_ui;

import jcheese.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Scanner;

@SuppressWarnings("serial")
public class BoardPanel extends JPanel {  
  // Graphical elements
  public static final Color DEFAULT_LIGHT_COLOR = new Color(0xF0DAB5);
  public static final Color DEFAULT_DARK_COLOR = new Color(0xB68763);
  public final Color[] colors = { DEFAULT_LIGHT_COLOR, DEFAULT_DARK_COLOR };
  private static final Stroke selectedStroke = new BasicStroke(
    5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {21, 9, 3, 9}, 0.0f
  );
  private static final Stroke annotationStroke = new BasicStroke(
      6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f
  );
  private static final Color selectedColor = new Color(192, 192, 192, 178);
  private static final Color annotationColor = new Color(0, 200, 0, 240);
  private static final Color checkColor = new Color(225, 0, 0, 122);
  private final BufferedImage[] pieceImages = new BufferedImage[Piece.SIDE_COUNT * Piece.KIND_COUNT + 2];
  private final Image[] resoluteImages = new Image[pieceImages.length];
  // Dimensions of each square
  private int squareWidth;
  private int squareHeight;
  // State needed for rendering the board
  public boolean flipped = false;
  private int excludedSquare = Square.NIL;
  private int selectedSquare = Square.NIL;
  private final long[] pushSquares = new long[Square.COUNT];
  private final long[] captureSquares = new long[Square.COUNT];
  private long promoteSquares = BitBoard.allClear();
  private long checkSquares = BitBoard.allClear();
  private long selectableSquares = BitBoard.allClear();
  private final Board board = new Board();
  // Contains the hooks to call when a query starts/ends
  private IQueryIndicator indicator;
  // User interaction state
  private final ClickListener onClick = new ClickListener();
  private final DragListener onDrag = new DragListener();
  private int userSrc, userDst, userKind = Piece.QUEEN;
  private Point dragOriginPt;
  private Point pressedPt;
  private Image dragImage;

  public BoardPanel() {
    retrievePieceImages();
    this.setMinimumSize(new Dimension(240, 240));
    this.setBackground(Color.DARK_GRAY);
    this.setForeground(Color.LIGHT_GRAY);
    this.setLayout(null);

    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        super.componentResized(e);
        Dimension panelSize = getSize();
        squareWidth = (int) (panelSize.getWidth() / Square.FILE_COUNT);
        squareHeight = (int) (panelSize.getHeight() / Square.RANK_COUNT);
        for (int i = Piece.PAWN; i < resoluteImages.length; ++i) {
          resoluteImages[i] = pieceImages[i].getScaledInstance(squareWidth, squareHeight, Image.SCALE_SMOOTH);
        }
        repaint();
      }
    });
  }

  // User input methods
  
  private void goBlankState() {
    pressedPt = null;
    dragOriginPt = null;
    selectedSquare = Square.NIL;
    dragImage = null;
    excludedSquare = Square.NIL;
    repaint();
  }
  
  private boolean isValidDstSquare(int srcSquare, int dstSquare) {
    long dstBit = BitBoard.bit(dstSquare);
    return BitBoard.notEmpty(pushSquares[srcSquare] & dstBit)
        || BitBoard.notEmpty(captureSquares[srcSquare] & dstBit);
  }
  
  private void goSelectState(int square) {
    if (BitBoard.isEmpty(BitBoard.bit(square) & selectableSquares)) {
      goBlankState();
      return;
    }
    
    selectedSquare = square;
    int thePiece = board.getPiece(square);
    dragImage = resoluteImages[thePiece];
    repaint();
  }
  
  // Release [queryLock] here when the user clicks or releases the mouse
  private class ClickListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent event) {
      Point prevPt = pressedPt;
      pressedPt = event.getPoint();
      int clickedSquare = getSquareAtPoint(pressedPt);
      // Check if the user clicked the mouse from blank state
      if (prevPt == null) {
        // Check if valid square
        if (clickedSquare != Square.NIL) {
          // Select the square
          goSelectState(clickedSquare);
        } else {
          // If not valid. Go back to blank state
          goBlankState();
        }
      } else if (clickedSquare != Square.NIL) {
        // The user has clicked another valid square
        // Check if any of the annotations allow the input
        if (isValidDstSquare(selectedSquare, clickedSquare)) {
          userSrc = selectedSquare;
          userDst = clickedSquare;
          indicator.end();
        } else {
          // Not a valid target. Select the square instead
          goSelectState(clickedSquare);
        }
      } else {
        // The user clicked invalid square. Return to blank state
        goBlankState();
      }
    }
    
    @Override
    public void mouseReleased(MouseEvent event) {
      // Check if previously in drag state
      if (dragOriginPt != null) {
        int releaseSquare = getSquareAtPoint(event.getPoint());
        // Check if destination of drag is valid
        if (isValidDstSquare(selectedSquare, releaseSquare)) {
          userSrc = selectedSquare;
          userDst = releaseSquare;
          indicator.end();
        }
        goBlankState();
      }
    }
  }
  
  private class DragListener extends MouseMotionAdapter {
    @Override
    public void mouseDragged(MouseEvent event) {
      // Only respond to this event if is currently dragging a square
      if (selectedSquare != Square.NIL) {
        dragOriginPt = event.getPoint();
        // Hide the selected square as it is being dragged
        excludedSquare = selectedSquare;
        repaint();
      }
    }
  }
  
  private int getSquareAtPoint(Point pt) {
    int xOffset = (int) pt.getX() / squareWidth;
    int yOffset = (int) pt.getY() / squareHeight;
    if (xOffset > 7 || yOffset > 7) return Square.NIL;
    if (flipped) {
      xOffset = 7 - xOffset;
    } else {
      yOffset = 7 - yOffset;
    }
    return Square.fromCoords(xOffset, yOffset);
  }
  
  public void enableInteraction() {
    this.addMouseListener(onClick);
    this.addMouseMotionListener(onDrag);
    goBlankState();
  }
  
  public void disableInteraction() {
    this.removeMouseListener(onClick);
    this.removeMouseMotionListener(onDrag);
    goBlankState();
  }

  public void resetAnnotations() {
    Arrays.fill(pushSquares, BitBoard.allClear());
    Arrays.fill(captureSquares, BitBoard.allClear());
    promoteSquares = BitBoard.allClear();
    checkSquares = BitBoard.allClear();
  }
  
  private void retrievePieceImages() {
    pieceImages[Piece.LIGHT | Piece.PAWN] = Assets.getImageAsset("light_pawn.png");
    pieceImages[Piece.LIGHT | Piece.ROOK] = Assets.getImageAsset("light_rook.png");
    pieceImages[Piece.LIGHT | Piece.KNIGHT] = Assets.getImageAsset("light_knight.png");
    pieceImages[Piece.LIGHT | Piece.BISHOP] = Assets.getImageAsset("light_bishop.png");
    pieceImages[Piece.LIGHT | Piece.QUEEN] = Assets.getImageAsset("light_queen.png");
    pieceImages[Piece.LIGHT | Piece.KING] = Assets.getImageAsset("light_king.png");
    pieceImages[Piece.DARK | Piece.PAWN] = Assets.getImageAsset("dark_pawn.png");
    pieceImages[Piece.DARK | Piece.ROOK] = Assets.getImageAsset("dark_rook.png");
    pieceImages[Piece.DARK | Piece.KNIGHT] = Assets.getImageAsset("dark_knight.png");
    pieceImages[Piece.DARK | Piece.BISHOP] = Assets.getImageAsset("dark_bishop.png");
    pieceImages[Piece.DARK | Piece.QUEEN] = Assets.getImageAsset("dark_queen.png");
    pieceImages[Piece.DARK | Piece.KING] = Assets.getImageAsset("dark_king.png");
  }

  private void paintGrid(Graphics2D g2d) {
    int currentColor = Piece.LIGHT;
    int cursorX, cursorY = 1;

    for (int j = 0; j < Square.RANK_COUNT; ++j) {
      int sideRank = currentColor;
      cursorX = 1;
      for (int i = 0; i < Square.FILE_COUNT; ++i) {
        // Paint the grid here
        g2d.setColor(colors[currentColor]);
        g2d.fillRect(cursorX, cursorY, squareWidth, squareHeight);
        currentColor = Piece.invertSide(currentColor);
        cursorX += squareWidth;
      }
      cursorY += squareHeight;
      currentColor = Piece.invertSide(sideRank);
    }
  }

  private Point getTopLeftPoint(int square) {
    if (flipped) {
      // Top-down, x = [0, 7] maps to x = [7, 0]
      return new Point(
        squareWidth * (7 - Square.getX(square)),
        squareHeight * Square.getY(square)
      );
    } else {
      // By default, y = [0, 7] maps to y = [7, 0]
      return new Point(
        squareWidth * Square.getX(square),
        squareHeight * (7 - Square.getY(square))
      );
    }
  }
  
  private Point getTopLeftPointAbsolute(int square) {
    return new Point(squareWidth * Square.getX(square), squareHeight * (7 - Square.getY(square)));
  }

  private static int getPixelFontSize(int pixels) {
    return (int) (138.0 * pixels / Toolkit.getDefaultToolkit().getScreenResolution());
  }

  private void paintRankFileIndicator(Graphics2D g2d) {
    // The magic constants in calculation are products of trial and error. Edit with caution
    int currentColor = Piece.LIGHT;

    int labelPixHeight = squareHeight / 5;
    g2d.setFont(new Font("Monospace", Font.BOLD, getPixelFontSize(labelPixHeight)));

    for (int y = 7; y >= 0; --y) {
      Point pt = getTopLeftPointAbsolute(Square.fromCoords(7, y));
      g2d.setColor(colors[currentColor]);
      g2d.drawString(
          Integer.toString(flipped ? (8 - y) : (y + 1)),
          (int) (pt.getX() + (squareWidth * 0.02) + (squareWidth * 0.8)),
          (int) (pt.getY() + labelPixHeight * 1.25)
      );
      currentColor = Piece.invertSide(currentColor);
    }
    for (int x = 0; x < 8; ++x) {
      Point pt = getTopLeftPointAbsolute(Square.fromCoords(x, 0));
      g2d.setColor(colors[currentColor]);
      g2d.drawString(
          Character.toString(flipped ? ('H' - x) : ('A' + x)),
          (int) (pt.getX() + squareWidth * 0.05),
          (int) (pt.getY() + squareHeight * 0.985)
      );
      currentColor = Piece.invertSide(currentColor);
    }
  }

  private void paintAnnotations(Graphics2D g2d) {
    long checkBits = checkSquares;
    
    if (selectedSquare != Square.NIL) {
      Point pt = getTopLeftPoint(selectedSquare);
      g2d.setStroke(selectedStroke);
      g2d.setColor(selectedColor);
      g2d.drawRect((int) pt.getX(), (int) pt.getY(), squareWidth, squareHeight);

      g2d.setColor(annotationColor);
      int circleRadius = (int) (Math.min(squareWidth, squareHeight) * 0.35);
      for (long bits = pushSquares[selectedSquare]; BitBoard.notEmpty(bits); bits = BitBoard.popLSB(bits)) {
        int square = BitBoard.bitScanForward(bits);
        pt = getTopLeftPoint(square);
        pt.translate((int) (squareWidth * 0.33), (int) (squareHeight * 0.33));
        g2d.fillOval((int) pt.getX(), (int) pt.getY(), circleRadius, circleRadius);
      }

      g2d.setStroke(annotationStroke);
      for (long bits = captureSquares[selectedSquare]; BitBoard.notEmpty(bits); bits = BitBoard.popLSB(bits)) {
        int square = BitBoard.bitScanForward(bits);
        pt = getTopLeftPoint(square);
        g2d.drawRect((int) pt.getX(), (int) pt.getY(), squareWidth, squareHeight);
      }
      
      // Do not render check annotation over any annotation
      checkBits &= ~(BitBoard.bit(selectedSquare) | captureSquares[selectedSquare]);
    }

    g2d.setStroke(annotationStroke);
    g2d.setColor(checkColor);
    for (; BitBoard.notEmpty(checkBits); checkBits = BitBoard.popLSB(checkBits)) {
      Point pt = getTopLeftPoint(BitBoard.bitScanForward(checkBits));
      g2d.drawRect((int) pt.getX(), (int) pt.getY(), squareWidth, squareHeight);
    }
  }

  private void paintPieces(Graphics2D g2d) {
    if (board == null) return;
    
    for (int square = Square.A1; square < Square.COUNT; ++square) {
      if (excludedSquare == square) continue;

      Point dstPt = getTopLeftPoint(square);
      int piece = board.getPiece(square);
      if (Piece.kind(piece) == Piece.NONE) continue;

      g2d.drawImage(resoluteImages[piece], (int) dstPt.getX(), (int) dstPt.getY(), squareWidth, squareHeight, null);
    }
  }
  
  @Override
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g2d = (Graphics2D) graphics;

    // flipped = true;
    // excludedSquare = Square.NIL;
    paintGrid(g2d);
    paintRankFileIndicator(g2d);
    paintPieces(g2d);
    paintAnnotations(g2d);
    
    // Paint the drag overlay
    if (dragOriginPt != null) {
      g2d.drawImage(dragImage, (int) dragOriginPt.getX(), (int) dragOriginPt.getY(), squareWidth, squareHeight, null);
    }
  }

  // A functional interface that runs everytime a query for action is finished
  public interface IQueryIndicator {
    void start();
    void end();
  }

  // This method should block until given a valid input, lest this method is ineffective
  public void queryForAction(IQueryIndicator indicator) {
    this.indicator = indicator;
    enableInteraction();
    indicator.start(); // This should be blocking
    // Continues here if the EDT invokes indicator.end()
    disableInteraction();
    resetAnnotations();
  }

  public int getActionSrc() { return userSrc; }
  public int getActionDst() { return userDst; }
  public int getActionPromoteKind() { return userKind; }

  public void updateBoard(Board board) {
    this.board.copyFrom(board);
    // Calculate the masks
    selectableSquares = board.getBoards()[Board.ALL | board.getPlySide()];
  }

  public void setActions(ArrayList<Integer> moves, long checkers) {
    for (final int move : moves) {
      int srcSquare = Move.getSrc(move);
      if (Move.isCapture(move) || Move.isEnPassant(move)) {
        captureSquares[srcSquare] |= BitBoard.bit(Move.getDst(move));
      } else {
        pushSquares[srcSquare] |= BitBoard.bit(Move.getDst(move));
      }

      if (Move.isPromote(move)) {
        promoteSquares |= BitBoard.bit(Move.getDst(move));
      }
    }

    if (BitBoard.notEmpty(checkers)) {
      this.checkSquares = checkers | board.getBoards()[Board.KINGS | board.getPlySide()];
    }
  }
  
  // Test methods
  
  public void test() {
    try (Scanner sc = new Scanner(System.in)) {
      for (;;) {
        String cmd = sc.next();
        
        switch (cmd.toLowerCase()) {
          case "fen": {
            if (!sc.hasNextLine()) {
              System.out.println("Command format: 'fen' <fen_pos>");
              continue;
            }
            String fen = sc.nextLine().strip();
            
            if (fen.equals("default")) {
              fen = Board.START_POS;
            }
            
            try {
              FEN.loadFEN(board, fen);
            } catch (IllegalArgumentException exc) {
              System.out.println("Invalid board");
              continue;
            }
            System.out.println("Loaded board");
            this.repaint();
          } break;
          
          case "hide": {
            if (!sc.hasNext()) {
              System.out.println("Command format: 'hide' <square>");
              continue;
            }
            String arg = sc.next();
            if (arg.equals("none")) {
              excludedSquare = Square.NIL;
            } else {
              int x, y;
              try {
                x = Character.toLowerCase(arg.charAt(0)) - 'a';
                y = arg.charAt(1) - '1';
              } catch (StringIndexOutOfBoundsException exc) {
                System.out.println("Invalid square");
                continue;
              }
              if (!Square.withinBounds(x, y)) {
                System.out.println("Invalid square");
                continue;
              }
              excludedSquare = Square.fromCoords(x, y);
            }
            this.repaint();
          } break;
          
          case "flip": {
            flipped = !flipped;
            this.repaint();
          } break;
          
          case "color": {
            if (!sc.hasNext()) {
              System.out.println("Command format: 'color' <side> (<r> <g> <b> | <'default'>");
              continue;
            }
            
            int color;
            switch (sc.next().strip()) {
              case "light": color = Piece.LIGHT; break;
              case "dark": color = Piece.DARK; break;
              case "default": 
                colors[Piece.LIGHT] = DEFAULT_LIGHT_COLOR;
                colors[Piece.DARK] = DEFAULT_DARK_COLOR;
                this.repaint();
                continue;
              default:
                System.out.println("Invalid side argument");
                continue;
            }
            
            int r, g, b;
            try {
              r = sc.nextInt();
              g = sc.nextInt();
              b = sc.nextInt();
            } catch (IllegalFormatException exc) {
              System.out.println("Invalid rgb argument");
              continue;
            }
            
            colors[color] = new Color(r, g, b);
            this.repaint();
          } break;
          
          case "move": {
            if (!sc.hasNext()) {
              System.out.println("Command format: 'move' <ssan>");
              continue;
            }
            
            String ssan = sc.next().strip();
            int move;
            try {
              move = Move.fromSSAN(board, ssan);
            } catch (IllegalArgumentException exc) {
              System.out.println("Invalid SSAN move");
              continue;
            }
            try {
              board.applyMove(move);
            } catch (Throwable exc) {
              System.out.println("Cannot apply move");
            }
            this.repaint();
          } break;

          case "select": {
            if (!sc.hasNext()) {
              System.out.println("Command format: 'select' <square>");
              continue;
            }
            String arg = sc.next();
            if (arg.equals("none")) {
              selectedSquare = Square.NIL;
            } else {
              int x, y;
              try {
                x = Character.toLowerCase(arg.charAt(0)) - 'a';
                y = arg.charAt(1) - '1';
              } catch (StringIndexOutOfBoundsException exc) {
                System.out.println("Invalid square");
                continue;
              }
              if (!Square.withinBounds(x, y)) {
                System.out.println("Invalid square");
                continue;
              }
              selectedSquare = Square.fromCoords(x, y);
            }
            this.repaint();
          } break;
          
          case "interact": {
            if (sc.hasNext()) {
              switch (sc.next().strip().toLowerCase()) {
                case "on": enableInteraction(); break;
                case "off": disableInteraction(); break;
                default:
                  System.out.println("Unknown setting");
                  continue;
              }
            } else {
              System.out.println("Command format: 'interact' <on|off>");
            }
          } break;
          
          case "allow": {
            if (sc.hasNext()) {
              switch (sc.next().strip().toLowerCase()) {
                case "light":
                  selectableSquares = board.getBoards()[Board.ALL | Piece.LIGHT];
                  break;
                case "dark":
                  selectableSquares = board.getBoards()[Board.ALL | Piece.DARK];
                  break;
              }
            }
          } break;
          
          case "exit": return;
          default:
            System.out.println("Invalid command");
        }
      }
    }
  }
}
