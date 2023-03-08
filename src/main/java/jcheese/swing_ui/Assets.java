package jcheese.swing_ui;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

public class Assets {
  public static final Path IMAGES_PATH = Paths.get("./assets/images");
  private static final HashMap<String, BufferedImage> imageResources = new HashMap<>();
  
  static {
    // Load the assets to memory
    File assetDir = IMAGES_PATH.toFile();
    assert assetDir.exists() && assetDir.isDirectory(); // [IMAGES_PATH] must point to a directory
    try {
      for (final File file : assetDir.listFiles()) {
        if (file.isFile()) {
          imageResources.put(file.getName(), ImageIO.read(new FileInputStream(file)));
        }
      }
    } catch (IOException exc) {
      throw new IllegalStateException("Cannot read file");
    }
  }
  
  public static BufferedImage getImageAsset(String id) {
    if (!imageResources.containsKey(id)) {
      throw new IllegalArgumentException("Cannot find resource");
    }
    return imageResources.get(id);
  }
}