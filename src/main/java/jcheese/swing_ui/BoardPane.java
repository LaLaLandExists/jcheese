package jcheese.swing_ui;

import jcheese.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoardPane extends JLayeredPane {
  // To suppress the serial warning
  public static final long serialVersionUID = 0xBEEFB01;
  
  // Static constants
  private static final Color DEFAULT_LIGHT_GRID_COLOR = new Color(0xF0DAB5);
  private static final Color DEFAULT_DARK_GRID_COLOR = new Color(0xB68763);
  private static final int GRID_LIGHT = 0;
  private static final int GRID_DARK = 1;
  private static final int SPRITE_COUNT = Piece.SIDE_COUNT * Piece.KIND_COUNT + 2;
  private static final int ANIMATION_TICK_MS = 20;
  
  // Render elements
  private final Color[] gridColors = { DEFAULT_LIGHT_GRID_COLOR, DEFAULT_DARK_GRID_COLOR };
  private Color promoteWindowColor = invertColor(DEFAULT_LIGHT_GRID_COLOR);
  private Stroke selectStroke = new BasicStroke(
    5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {21, 9, 3, 9}, 0.0f
  );
  private Color selectColor = new Color(190, 190, 190, 190);
  private Stroke captureStroke = new BasicStroke(
    6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f
  );
  private Color moveColor = new Color(0, 200, 0, 190);
  private Stroke checkStroke = new BasicStroke(2.5f);
  private Color checkColor = new Color(200, 0, 0, 190);
  private Color historyColor = new Color(0, 0, 200, 115);
  private BufferedImage[] rawPieceImages = new BufferedImage[SPRITE_COUNT];
  
  // Dynamic render state
  private boolean isFlipped = false;
  private boolean viewOnly;
  private int squareWidth;
  private int squareHeight;
  private int paneWidth;
  private int paneHeight;
  private int omittedSquare = Square.NIL;
  private int overrideSquare = Square.NIL;
  private int overridePiece = Piece.NONE;
  private Image[] scaledImages = new Image[SPRITE_COUNT];
  
  // Movements
  private final DragMovement drag = new DragMovement();
  private final PromoteAfterDragMovement padm = new PromoteAfterDragMovement();
  private final Set<Movement> movements = ConcurrentHashMap.newKeySet();
  private boolean ignoreLastMoveMovement = false;
  
  // Action hints
  private final long[] pushHints = new long[Square.COUNT];
  private final long[] captureHints = new long[Square.COUNT];
  private long promoteHints;
  private long checkHints;
  private long lastSrcHint;
  private long lastDstHint;
  
  // Interaction state
  private int selectedSquare = Square.NIL;
  private Point dragPt;
  private Image dragImage;
  private long selectableSquares = BitBoard.allClear();
  private int responseSrc, responseDst, responseKind = Piece.QUEEN;
  private final Latch queryLatch = new Latch(1);
  
  // Board state
  private final Board board = new Board();
  
  // Movement state
  private boolean suppressMovement = false;
  private int currentX, currentY, deltaX, deltaY;
  private int currentFrame, endFrame;
  private int sourceSquare;
  
  private final PromoteChooserLayer promoteLayer = new PromoteChooserLayer();
  private final ILayer[] layers = {
    promoteLayer,
    new InteractionLayer(),
    new SpriteLayer(),
    new HintLayer(),
    new BoardLayer(),
  };
  
  private final Timer animationClock = new Timer(ANIMATION_TICK_MS, (event) -> {
    movements.forEach((movement) -> {
      if (movement.nextTick()) {
        movements.remove(movement);
      }
    });
  });
  
  private static Color invertColor(Color color) {
    return new Color(0xFFFFFF - color.getRGB());
  }
  
  public BoardPane() {
    // Load the resources
    rawPieceImages[Piece.LIGHT | Piece.PAWN] = Assets.getImageAsset("light_pawn.png");
    rawPieceImages[Piece.LIGHT | Piece.ROOK] = Assets.getImageAsset("light_rook.png");
    rawPieceImages[Piece.LIGHT | Piece.KNIGHT] = Assets.getImageAsset("light_knight.png");
    rawPieceImages[Piece.LIGHT | Piece.BISHOP] = Assets.getImageAsset("light_bishop.png");
    rawPieceImages[Piece.LIGHT | Piece.QUEEN] = Assets.getImageAsset("light_queen.png");
    rawPieceImages[Piece.LIGHT | Piece.KING] = Assets.getImageAsset("light_king.png");
    rawPieceImages[Piece.DARK | Piece.PAWN] = Assets.getImageAsset("dark_pawn.png");
    rawPieceImages[Piece.DARK | Piece.ROOK] = Assets.getImageAsset("dark_rook.png");
    rawPieceImages[Piece.DARK | Piece.KNIGHT] = Assets.getImageAsset("dark_knight.png");
    rawPieceImages[Piece.DARK | Piece.BISHOP] = Assets.getImageAsset("dark_bishop.png");
    rawPieceImages[Piece.DARK | Piece.QUEEN] = Assets.getImageAsset("dark_queen.png");
    rawPieceImages[Piece.DARK | Piece.KING] = Assets.getImageAsset("dark_king.png");
    // Initialize the component
    setBackground(Color.DARK_GRAY);
    setForeground(Color.LIGHT_GRAY);
    
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent event) {
        paneWidth = getWidth();
        paneHeight = getHeight();
        squareWidth = paneWidth / Square.FILE_COUNT;
        squareHeight = paneHeight / Square.RANK_COUNT;
        // Rescale all the sprites
        for (int i = Piece.PAWN; i < scaledImages.length; ++i) {
          scaledImages[i] = rawPieceImages[i].getScaledInstance(squareWidth, squareHeight, Image.SCALE_SMOOTH);
        }
        for (final ILayer layer : layers) layer.updateBounds();
        blankState();
      }
    });
    
    for (int i = 0; i < layers.length; ++i) {
      add(layers[i].getComponent(), i);
    }
    movements.add(drag);
    movements.add(padm);
    animationClock.start();
  }
  
  private int getXFromSquare(int square, float offsetX) {
    if (isFlipped) return (int) (squareWidth * (7 - Square.getX(square)) + squareWidth * offsetX);
    return (int) (squareWidth * Square.getX(square) + squareWidth * offsetX);
  }
  private int getYFromSquare(int square, float offsetY) {
    if (isFlipped) return (int) (squareHeight * Square.getY(square) + squareHeight * offsetY);
    return (int) (squareHeight * (7 - Square.getY(square)) + squareHeight * offsetY);
  }
  
  private Point getPointFromSquare(int square, float offsetX, float offsetY) {
    return new Point(getXFromSquare(square, offsetX), getYFromSquare(square, offsetY));
  }
  private Point getPointFromSquare(int square) {
    return getPointFromSquare(square, 0.0f, 0.0f);
  }
  
  private Point getAbsolutePoint(int x, int y, float offsetX, float offsetY) {
    return new Point(
      (int) (squareWidth * x + squareWidth * offsetX),
      (int) (squareHeight * (7 - y) + squareHeight * offsetY)
    );
  }
  
  private int getFileFromPoint(int x) {
    int file = x / squareWidth;
    if (file > 7) return -1;
    return isFlipped ? 7 - file : file;
  }
  private int getRankFromPoint(int y) {
    int rank = y / squareHeight;
    if (rank > 7) return -1;
    return isFlipped ? rank : 7 - rank;
  }
  
  private int getSquareFromPoint(int x, int y) {
    int file = getFileFromPoint(x);
    int rank = getRankFromPoint(y);
    
    if (file == -1 || rank == -1) return Square.NIL;
    return Square.fromCoords(file, rank);
  }
  private int getSquareFromPoint(Point pt) {
    return getSquareFromPoint(pt.x, pt.y);
  }
  
  private static int getPixelFontSize(int pixels) {
    return (int) (138.0 * pixels / Toolkit.getDefaultToolkit().getScreenResolution());
  }

  public void dispose() {
    animationClock.stop();
  }

  private interface ILayer {
    void updateBounds();
    Component getComponent();
  }
  
  private class BoardLayer extends JPanel implements ILayer {
    public static final long serialVersionUID = 0;
    
    public BoardLayer() {
      setBackground(Color.DARK_GRAY);
    }
    
    @Override
    public void updateBounds() { this.setBounds(0, 0, paneWidth, paneHeight); }
    @Override
    public Component getComponent() { return this; }
    
    @Override
    public void paintComponent(Graphics graph) {
      Graphics2D g2d = (Graphics2D) graph;
      // Draw the grid
      int color = GRID_LIGHT;
      int cursorX, cursorY = 1;
      
      for (int y = 7; y >= 0; --y) {
        int thisRow = color;
        cursorX = 1;
        
        for (int x = 0; x < 8; ++x) {
          g2d.setColor(gridColors[color]);
          g2d.fillRect(cursorX, cursorY, squareWidth, squareHeight);
          color ^= 1; // Invert
          cursorX += squareWidth;
        }
        
        color = thisRow ^ 1; // Invert
        cursorY += squareHeight;
      }
      
      // Draw the rank/file label
      int labelHeightPixels = squareHeight / 5;
      g2d.setFont(new Font("Monospace", Font.PLAIN, getPixelFontSize(labelHeightPixels)));
      
      for (int y = 7; y >= 0; --y) {
        Point pt = getAbsolutePoint(7, y, 0.8f, ((float) labelHeightPixels / squareHeight) * 1.25f);
        g2d.setColor(gridColors[color]);
        g2d.drawString(Integer.toString(isFlipped ? (8 - y) : (y + 1)), pt.x, pt.y);
        color ^= 1; // Invert
      }
      for (int x = 0; x < 8; ++x) {
        Point pt = getAbsolutePoint(x, 0, 0.05f, 0.985f);
        g2d.setColor(gridColors[color]);
        g2d.drawString(Character.toString((char) (isFlipped ? ('H' - x) : ('A' + x))), pt.x, pt.y);
        color ^= 1; // Invert
      }
    }
  } // class BoardLayer
  
  private void resetHints() {
    Arrays.fill(pushHints, BitBoard.allClear());
    Arrays.fill(captureHints, BitBoard.allClear());
    promoteHints = BitBoard.allClear();
    checkHints = BitBoard.allClear();
  }
  
  private class HintLayer extends JPanel implements ILayer {
    public static final long serialVersionUID = 0;
    
    public HintLayer() {
      setOpaque(false);
    }

    @Override
    public void updateBounds() { this.setBounds(0, 0, paneWidth, paneHeight); }
    @Override
    public Component getComponent() { return this; }
    
    @Override
    public void paintComponent(Graphics graph) {
      Graphics2D g2d = (Graphics2D) graph;
      
      long redHints = checkHints;
      long lastSrc = lastSrcHint;
      long lastDst = lastDstHint;
      
      if (selectedSquare != Square.NIL) {
        long pushHint = pushHints[selectedSquare];
        long captureHint = captureHints[selectedSquare];
        // Render selected hint
        g2d.setStroke(selectStroke);
        g2d.setColor(selectColor);
        Point pt = getPointFromSquare(selectedSquare);
        g2d.drawRect(pt.x, pt.y, squareWidth, squareHeight);
        // Render push hints
        g2d.setColor(moveColor);
        int radius = (int) (Math.min(squareWidth, squareHeight) * 0.35f);
        for (long pushes = pushHint; BitBoard.notEmpty(pushes); pushes = BitBoard.popLSB(pushes)) {
          int pushSquare = BitBoard.bitScanForward(pushes);
          pt = getPointFromSquare(pushSquare, 0.33f, 0.33f);
          g2d.fillOval(pt.x, pt.y, radius, radius);
        }
        // Render capture hints
        g2d.setStroke(captureStroke);
        for (long captures = captureHint; BitBoard.notEmpty(captures); captures = BitBoard.popLSB(captures)) {
          int captureSquare = BitBoard.bitScanForward(captures);
          pt = getPointFromSquare(captureSquare);
          g2d.drawRect(pt.x, pt.y, squareWidth, squareHeight);
        }
        
        // Do not render red/blue square over rendered hints
        redHints &= ~(BitBoard.bit(selectedSquare) | captureHints[selectedSquare]);
        long allHint = pushHint | captureHint;
        
        lastSrc &= ~allHint;
        lastDst &= ~allHint;
      }
      
      lastSrc &= ~redHints;
      lastDst &= ~redHints;
      
      // Render check hints
      g2d.setColor(checkColor);
      g2d.setStroke(checkStroke);
      for (; BitBoard.notEmpty(redHints); redHints = BitBoard.popLSB(redHints)) {
        int theSquare = BitBoard.bitScanForward(redHints);
        Point pt = getPointFromSquare(theSquare);
        g2d.drawRect(pt.x, pt.y, squareWidth, squareHeight);
      }
      
      // Render last move done
      g2d.setColor(historyColor);
      if (BitBoard.notEmpty(lastSrc)) {
        g2d.setStroke(selectStroke);
        Point pt = getPointFromSquare(BitBoard.bitScanForward(lastSrc));
        g2d.drawRect(pt.x, pt.y, squareWidth, squareHeight);
      }
      if (BitBoard.notEmpty(lastDst)) {
        g2d.setStroke(checkStroke);
        Point pt = getPointFromSquare(BitBoard.bitScanForward(lastDst));
        g2d.drawRect(pt.x, pt.y, squareWidth, squareHeight);
      }
      
      // Render drag destination hint (if dragging)
      if (dragPt != null) {
        int dragDst = getSquareFromPoint(dragPt);
        if (dragDst != Square.NIL && dragDst != selectedSquare) {
          Point pt = getPointFromSquare(dragDst, 0.25f, 0.25f);
          g2d.setStroke(selectStroke);
          g2d.setColor(selectColor);
          g2d.drawRect(pt.x, pt.y, squareWidth / 2, squareHeight / 2);
        }
      }
    }
  } // class HintLayer
  
  private abstract class Movement {
    public int srcSquare;
    
    public Movement(int srcSquare) {
      this.srcSquare = srcSquare;
    }
    
    public boolean shouldRender(int square) {
      // The default movement should omit the source square to avoid duplicates
      return square != srcSquare;
    }
    
    public abstract void paint(Graphics2D g2d);
    public abstract boolean nextTick();
    
    public void flip() {}
  }
  
  private class DragMovement extends Movement {
    private int dragPiece = Piece.NONE;
    
    public DragMovement() { super(Square.NIL); }
    
	  @Override
	  public void paint(Graphics2D g2d) {
	  	if (dragPt != null) {
	  	  g2d.drawImage(scaledImages[dragPiece], dragPt.x - squareWidth / 2,
	  	    dragPt.y - squareHeight / 2, squareWidth, squareHeight, null);
	  	}
	  }
	  
	  @Override
  	public boolean nextTick() {
		  return false; // Drag Movement never expires
	  }
	  
	  public void setSrc(int square) {
	    srcSquare = square;
	    dragPiece = board.getPiece(square);
	  } 
	  
	  public void unset() {
	    srcSquare = Square.NIL;
	    dragPiece = Piece.NONE;
	  }
  } // class DragMovement
  
  private class PromoteAfterDragMovement extends Movement {
    public static final int MAX_FRAMES = 7;
    
    private int dstSquare;
    private int currentFrame;
    private int cursorX, cursorY, deltaX, deltaY;
    private int thePiece = Piece.NONE;
    
    public PromoteAfterDragMovement() {
      super(Square.NIL);
    }
    
    public void setSquares(int srcSquare, int dstSquare) {
      this.srcSquare = srcSquare;
      this.dstSquare = dstSquare;
      
      currentFrame = -1;
      
      cursorX = getXFromSquare(dstSquare, 0);
      cursorY = getYFromSquare(dstSquare, 0);
      
      int diffX = getXFromSquare(srcSquare, 0) - cursorX;
      int diffY = getYFromSquare(srcSquare, 0) - cursorY;
      
      deltaX = diffX / MAX_FRAMES;
      deltaY = diffY / MAX_FRAMES;
      
      thePiece = board.getPiece(srcSquare);
    }
    
    public void release() {
      currentFrame = 0;
    }
    
    public void discard() {
      dstSquare = Square.NIL;
      srcSquare = Square.NIL;
    }
    
    @Override
    public boolean shouldRender(int square) {
      return srcSquare != square && dstSquare != square;
    }
    
	  @Override
  	public void paint(Graphics2D g2d) {
  		if (dstSquare != Square.NIL) {
  		  g2d.drawImage(scaledImages[thePiece], cursorX, cursorY, squareWidth, squareHeight, null);
  		}
  	}

  	@Override
  	public boolean nextTick() {
  	  if (currentFrame != -1) {
  	    if (currentFrame++ < MAX_FRAMES) {
		      cursorX += deltaX;
		      cursorY += deltaY;
		    } else {
		      discard();
		    }
		    repaint();
		  }
		  return false; // This movement never expires
  	}
  	
  	@Override
  	public void flip() {
  	  cursorX = paneWidth - cursorX;
  	  cursorY = paneHeight - cursorY;
  	  deltaX = -deltaX;
  	  deltaY = -deltaY;
  	}
  } // class PromoteAfterDragMovement
  
  private class MoveMovement extends Movement {
    public static final int MAX_FRAMES = 7;
    
    private final int dstSquare;
    private int cursorX, cursorY, deltaX, deltaY;
    private int thePiece = Piece.NONE;
    private int currentFrame = 0;
    
    public MoveMovement(int srcSquare, int dstSquare) {
      super(srcSquare);
      
      this.dstSquare = dstSquare;
      cursorX = getXFromSquare(srcSquare, 0);
      cursorY = getYFromSquare(srcSquare, 0);
      
      int diffX = getXFromSquare(dstSquare, 0) - cursorX;
      int diffY = getYFromSquare(dstSquare, 0) - cursorY;;
      
      deltaX = diffX / MAX_FRAMES;
      deltaY = diffY / MAX_FRAMES;
      
      thePiece = board.getPiece(srcSquare);
      
      movements.add(this);
    }
    
    @Override
    public boolean shouldRender(int square) {
      return dstSquare != square;
    }

  	@Override
	  public void paint(Graphics2D g2d) {
		  g2d.drawImage(scaledImages[thePiece], cursorX, cursorY, squareWidth, squareHeight, null);
	  }

	  @Override
	  public boolean nextTick() {
		  if (currentFrame++ < MAX_FRAMES) {
		    cursorX += deltaX;
		    cursorY += deltaY;
		    repaint();
		    return false;
		  } else {
		    movements.remove(this);
		    repaint();
		    return true;
		  }
	  }
	  
	  @Override
	  public void flip() {
	    cursorX = paneWidth - cursorX;
	    cursorY = paneHeight - cursorY;
	    deltaX = -deltaX;
	    deltaY = -deltaY;
	  }
  }
  
  private class SpriteLayer extends JPanel implements ILayer {
    public static final long serialVersionUID = 0;
    
    public SpriteLayer() {
      setOpaque(false);
      setDoubleBuffered(true);
    }
    
    @Override public void updateBounds() { this.setBounds(0, 0, paneWidth, paneHeight); }
    @Override public Component getComponent() { return this; }
    
    @Override
    public void paintComponent(Graphics graph) {
      Graphics2D g2d = (Graphics2D) graph;
      
      for (int ptY = 0; ptY < paneHeight; ptY += squareHeight) {
        for (int ptX = 0; ptX < paneWidth; ptX += squareWidth) {
          int square = getSquareFromPoint(ptX, ptY);
          if (square == Square.NIL) continue;
          int piece = board.getPiece(square);
          
          for (final Movement movement : movements) {
            if (!movement.shouldRender(square)) piece = Piece.NONE;
          }
          
          if (square == overrideSquare) {
            piece = overridePiece;
          }
          
          g2d.drawImage(scaledImages[piece], ptX, ptY, squareWidth, squareHeight, null);
        }
      }
      
      movements.forEach((movement) -> movement.paint(g2d));
    }
  } // class SpriteLayer
  
  private void semiBlankState() {
    selectedSquare = Square.NIL;
    dragPt = null;
    dragImage = null;
    drag.unset();
    repaint();
  }
  
  private void blankState() {
    promoteLayer.omit();
    overrideSquare = Square.NIL;
    overridePiece = Piece.NONE;
    omittedSquare = Square.NIL;
    semiBlankState();
  }
  
  private boolean isSelectable(int square) {
    return square != Square.NIL && BitBoard.notEmpty(BitBoard.bit(square) & selectableSquares);
  }
  
  private void selectedState(int selected) {
    if (isSelectable(selected)) {
      selectedSquare = selected;
      dragImage = scaledImages[board.getPiece(selected)];
      repaint();
      return;
    }
    blankState();
  }
  
  private boolean isHintCompliant(int srcSquare, int dstSquare) {
    long dstBit = BitBoard.bit(dstSquare);
    return BitBoard.notEmpty(pushHints[srcSquare] & dstBit) ||
      BitBoard.notEmpty(captureHints[srcSquare] & dstBit);
  }
  
  private void releaseBlocker() {
    queryLatch.countDown();
  }
  
  private boolean isPromoteAction(int srcSquare, int dstSquare) {
    return Piece.kind(board.getPiece(srcSquare)) == Piece.PAWN
      && BitBoard.notEmpty(BitBoard.bit(dstSquare) & promoteHints);
  }
  
  private void chooserState(int srcSquare, int dstSquare) {
    if (dragPt != null) {
      padm.setSquares(srcSquare, dstSquare);
    }
    semiBlankState();
    promoteLayer.show(dstSquare);
  }
  
  // An invisible panel that reacts to mouse events
  private class InteractionLayer extends JPanel implements ILayer {
    public static final long serialVersionUID = 0;
    
    public InteractionLayer() {
      setOpaque(false);
      
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent event) {
          if (viewOnly) return;
          
          if (SwingUtilities.isRightMouseButton(event)) {
            blankState();
            return;
          }
          
          if (inChooserState()) {
            padm.release();
            blankState();
          }
          
          int clickedSquare = getSquareFromPoint(event.getPoint());
          // If user selected something in blank state
          if (selectedSquare != Square.NIL && isHintCompliant(selectedSquare, clickedSquare)) {
            responseSrc = selectedSquare;
            responseDst = clickedSquare;
            if (isPromoteAction(selectedSquare, clickedSquare)) {
              chooserState(selectedSquare, clickedSquare);
              return;
            }
            // Notify request thread
            releaseBlocker();
            blankState();
          } else {
            selectedState(clickedSquare);
          }
        }
        
        @Override
        public void mouseReleased(MouseEvent event) {
          if (viewOnly) return;
          
          if (dragPt != null) {
            int dstSquare = getSquareFromPoint(event.getPoint());
            if (isHintCompliant(selectedSquare, dstSquare)) {
              responseSrc = selectedSquare;
              responseDst = dstSquare;
              if (isPromoteAction(selectedSquare, dstSquare)) {
                chooserState(selectedSquare, dstSquare);
                return;
              }
              // Notify request thread
              releaseBlocker();
              blankState();
            }
            blankState();
          }
        }
      });
      
      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent event) {
          if (viewOnly) return;
          
          if (selectedSquare != Square.NIL) {
            dragPt = event.getPoint();
            drag.setSrc(selectedSquare);
            ignoreLastMoveMovement = true;
            repaint();
          }
        }
      });
    }
    
    @Override public void updateBounds() { this.setBounds(0, 0, paneWidth, paneHeight); }
    @Override public Component getComponent() { return this; }
  }
  
  private boolean inChooserState() {
    return promoteLayer.isVisible();
  }
  
  private class PromoteChooserLayer extends JPanel implements ILayer {
    public static final long serialVersionUID = 0;
    private final JButton buttons[] = new JButton[4];
    
    private JButton createButton(int pieceKind) {
      JButton button = new JButton();
      button.setBackground(gridColors[GRID_LIGHT]);
      button.setForeground(gridColors[GRID_DARK]);
      button.addActionListener((event) -> {
        responseKind = pieceKind;
        padm.discard();
        releaseBlocker();
        blankState();
      });
      return button;
    }
    
    public PromoteChooserLayer() {
      setBackground(promoteWindowColor);
      setForeground(promoteWindowColor.brighter());
      setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      setLayout(null);
      
      Border buttonBorder = BorderFactory.createRaisedBevelBorder();
      for (int i = 0; i < buttons.length; ++i) {
        buttons[i] = createButton(Piece.ROOK + i * Piece.SIDE_COUNT);
        buttons[i].setBorder(buttonBorder);
        add(buttons[i]);
      }
      
      setVisible(false);
    }
    
    private void updateButtons() {
      for (int i = 0; i < buttons.length; ++i) {
        buttons[i].setIcon(new ImageIcon(scaledImages[(Piece.ROOK + i * Piece.SIDE_COUNT) | board.getPlySide()]));
      }
      
      int padding = squareWidth / 10;
      int x = padding;
      int y = (int) (squareHeight * 0.125f);
      
      for (final JButton button : buttons) {
        button.setBounds(x, y, squareWidth, squareHeight);
        x += padding + squareWidth;
      }
    }

    @Override public void updateBounds() { this.setSize((int) (squareWidth * 4.5f), (int) (squareHeight * 1.25f)); }
    @Override public Component getComponent() { return this; }
    
    public void show(int square) {
      updateButtons();
      int x = getXFromSquare(square, 0.4f);
      int y = getYFromSquare(square, 0.4f);
      
      // Calculate if the panel bleeds off the edge
      int bleedX = paneWidth - (int) (x + getWidth() * 1.2f);
      if (bleedX < 0) x += bleedX;
      int bleedY = paneHeight - (int) (y + getHeight() * 1.2f);
      if (bleedY < 0) y += bleedY;
      
      setLocation(x, y);
      setVisible(true);
      repaint();
    }
    
    public void omit() {
      setVisible(false);
      repaint();
    }
  }
  
  @Override
  public void paintComponent(Graphics graph) {
    super.paintComponent(graph);
  }
  
  public void setSelectables(long selectables) {
    selectableSquares = selectables;
    blankState();
  }
  public void setBoard(Board board) {
    this.board.copyFrom(board);
    blankState();
  }
  public void setActions(ArrayList<Integer> moves, long checkers) {
    resetHints();
    for (final int move : moves) {
      int srcSquare = Move.getSrc(move);
      int dstSquare = Move.getDst(move);
      
      if (Move.isCapture(move) || Move.isEnPassant(move)) {
        captureHints[srcSquare] |= BitBoard.bit(dstSquare);
      } else {
        pushHints[srcSquare] |= BitBoard.bit(dstSquare);
      }
      
      if (Move.isPromote(move)) {
        promoteHints |= BitBoard.bit(dstSquare);
      }
    }
    
    if (BitBoard.notEmpty(checkers)) {
      checkHints = board.bitboards[Board.KINGS | board.getPlySide()] | checkers;
    }
    blankState();
  }
  public void setFlipped(boolean isFlipped) {
    if (this.isFlipped != isFlipped) {
      for (final Movement movement : movements) {
        movement.flip();
      }
      this.isFlipped = isFlipped;
      blankState();
    }
  }
  public void setReactive(boolean isReactive) {
    viewOnly = !isReactive;
    blankState();
  }
  public void setLastMove(int move) {
    int src = Move.getSrc(move);
    int dst = Move.getDst(move);
    
    lastSrcHint = BitBoard.bit(src);
    lastDstHint = BitBoard.bit(dst);
    
    if (!ignoreLastMoveMovement) new MoveMovement(src, dst);
    if (Move.isCastle(move)) {
      int castle = Move.getCastle(move);
      new MoveMovement(MoveData.rookSrcSquares[castle], MoveData.rookDstSquares[castle]);
    }
    
    ignoreLastMoveMovement = false;
    repaint();
  }
  
  public void flip() { setFlipped(!isFlipped); }
  
  public void clearLastMove() {
    lastSrcHint = BitBoard.allClear();
    lastDstHint = BitBoard.allClear();
    repaint();
  }
  public void clearActions() {
    resetHints();
  }
  
  public int getResponseSrc() { return responseSrc; }
  public int getResponseDst() { return responseDst; }
  public int getResponseKind() { return responseKind; }
  
  public void awaitMove() throws InterruptedException {
    queryLatch.reset();
    queryLatch.await();
  }
}
