package jcheese.katubuyan;

import jcheese.Piece;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StartScreen extends JPanel {
  private static final Color LIGHT_GRID_COLOR = new Color(0xF0DAB5);
  private static final Color DARK_GRID_COLOR = new Color(0xB68763);
  private static final Color[] COLORS = new Color[] { LIGHT_GRID_COLOR, DARK_GRID_COLOR };
  private static final Color OVERLAY_COLOR = new Color(0x006B00);

  private static final BufferedImage RAW_JROOK_IMG;
  private static final BufferedImage RAW_CHEESE_IMG;
  private static final BufferedImage RAW_LIGHT_KING_IMG;
  private static final BufferedImage RAW_DARK_KING_IMG;
  private static final BufferedImage OVERLAY_LIGHT_KING_IMG;
  private static final BufferedImage OVERLAY_DARK_KING_IMG;

  private static final Font GOODDOG_FONT;

  private static BufferedImage addOverlay(BufferedImage img) {
    int width = img.getWidth();
    int height = img.getHeight();
    BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = res.createGraphics();
    g2d.drawImage(img, 0, 0, null);
    g2d.setColor(OVERLAY_COLOR);
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
    g2d.fillRect(0, 0, width, height);
    return res;
  }

  static {
    try {
      RAW_JROOK_IMG = ImageIO.read(new FileInputStream("./assets/katubuyan/jrook.png"));
      RAW_CHEESE_IMG = ImageIO.read(new FileInputStream("./assets/katubuyan/cheese_text.png"));
      RAW_LIGHT_KING_IMG = ImageIO.read(new FileInputStream("./assets/images/light_king.png"));
      RAW_DARK_KING_IMG = ImageIO.read(new FileInputStream("./assets/images/dark_king.png"));

      OVERLAY_LIGHT_KING_IMG = addOverlay(RAW_LIGHT_KING_IMG);
      OVERLAY_DARK_KING_IMG = addOverlay(RAW_DARK_KING_IMG);

      GOODDOG_FONT = Font.createFont(Font.TRUETYPE_FONT, new File("./assets/katubuyan/good_dog.ttf"));
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(GOODDOG_FONT);
    } catch (IOException | FontFormatException e) {
      JOptionPane.showMessageDialog(null, "Fatal Error: Missing or invalid assets");
      throw new AssertionError();
    }
  }

  private final Katubuyan.GameOptions options;

  private class SidePicker {
    public final BufferedImage sprite;
    public final BufferedImage selectedSprite;
    public final int side;
    public Rectangle bounds = new Rectangle();

    public SidePicker(BufferedImage sprite, BufferedImage selectedSprite, int side) {
      this.sprite = sprite;
      this.selectedSprite = selectedSprite;
      this.side = side;
    }

    public boolean isHovered(Point pt) {
      return bounds.contains(pt);
    }
  }

  private SidePicker currentlyHovered = null;
  private final SidePicker lightPicker = new SidePicker(RAW_LIGHT_KING_IMG, OVERLAY_LIGHT_KING_IMG, Piece.LIGHT);
  private final SidePicker darkPicker = new SidePicker(RAW_DARK_KING_IMG, OVERLAY_DARK_KING_IMG, Piece.DARK);

  public StartScreen(Katubuyan.GameOptions options, Katubuyan.OnScreenDone onDone) {
    super();
    this.options = options;

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (currentlyHovered != null) {
          options.playerColor = currentlyHovered.side;
          onDone.run();
        }
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        Point hit = e.getPoint();
        if (lightPicker.isHovered(hit)) {
          currentlyHovered = lightPicker;
        } else if (darkPicker.isHovered(hit)) {
          currentlyHovered = darkPicker;
        } else {
          currentlyHovered = null;
        }
        repaint();
      }
    });
  }

  private static final Color SUBTEXT_COLOR = new Color(0xCC1D00);

  private void adjustPicker(SidePicker picker, int x) {
    Rectangle bounds = picker.bounds;
    double side = getWidth() * 0.25f;
    bounds.setRect(x, getHeight() * 0.6f, side, side);
  }

  private void paintPicker(Graphics2D g2d, SidePicker picker) {
    Rectangle bounds = picker.bounds;
    if (currentlyHovered == picker) {
      g2d.drawImage(picker.selectedSprite, bounds.x, bounds.y, bounds.width, bounds.height, null);
    } else {
      g2d.drawImage(picker.sprite, bounds.x, bounds.y, bounds.width, bounds.height, null);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    int color = Piece.DARK;
    int slotWidth = getWidth() / 8;
    int slotHeight = getHeight() / 8;
    int cursorX, cursorY = 1;

    for (int y = 7; y >= 0; --y) {
      cursorX = 1;
      for (int x = 0; x < 8; ++x) {
        g2d.setColor(COLORS[color]);
        g2d.fillRect(cursorX, cursorY, slotWidth, slotHeight);
        color ^= 1;
        cursorX += slotWidth;
      }
      color ^= 1;
      cursorY += slotHeight;
    }

    int jrookSide = (int) (getWidth() * 0.36f);
    g2d.drawImage(RAW_JROOK_IMG, (int) (getWidth() * 0.02f), (int) (getHeight() * 0.1f), jrookSide, jrookSide, null);
    g2d.drawImage(RAW_CHEESE_IMG, (int) (jrookSide - jrookSide * 0.15f + getWidth() * 0.02f), (int) (getHeight() * 0.12f), (int) (getWidth() * 0.6f), jrookSide, null);

    g2d.setFont(GOODDOG_FONT.deriveFont(jrookSide / 6.0f));
    g2d.setColor(SUBTEXT_COLOR);
    g2d.drawString("It's too easy!", (int) (getWidth() * 0.58f), (int) (getHeight() * 0.46f));
    g2d.setColor(OVERLAY_COLOR);
    g2d.drawString("Select a side", getWidth() * 0.375f, getHeight() * 0.95f);

    adjustPicker(lightPicker, (int) (getWidth() * 0.17f));
    adjustPicker(darkPicker, (int) (getWidth() * 0.57f));

    // Draw each of the pickers
    paintPicker(g2d, lightPicker);
    paintPicker(g2d, darkPicker);
  }
}
