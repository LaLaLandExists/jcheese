package jcheese.client;

import jcheese.*;
import jcheese.server.*;

public abstract class AbstractViewer {
  private Server server;
  
  public abstract void update();
  public abstract void announceWin(int cause, int side);
  public abstract void announceDraw(int cause);
  
  public void setServer(Server server) {
    this.server = server;
  }
  
  protected Board getBoard() {
    return server.board;
  }
}