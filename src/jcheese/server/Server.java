package jcheese.server;

import java.util.*;
import jcheese.*;
import jcheese.client.*;
import jcheese.util.FEN;

public class Server {
	public final int LIGHT_WIN = 0;
	public final int DARK_WIN = 1;
	public final int DRAW = 2;
	
	public final int END_CHECKMATE = 0;
	public final int END_TIMEOUT = 1;
	
	public final int END_50_RULE = 0;
	public final int END_STALEMATE = 1;
	
	private final LinkedList<AbstractViewer> views = new LinkedList<>();
	private final IController[] controls = new IController[Piece.SIDE_COUNT];
	public final Board board = new Board();
	private final FEN fen = new FEN();
	private final MoveGenerator moveGen = new MoveGenerator();
	// [TODO] implement timers
	
  public Server setLightControl(IController control) {
    controls[Piece.LIGHT] = control;
    return this;
  }
  
  public Server setDarkControl(IController control) {
    controls[Piece.DARK] = control;
    return this;
  }
	
  public Server addView(AbstractViewer view) {
    views.add(view);
    view.setServer(this);
    return this;
  }
	
	public int launch() {
	  for (final IController control : controls) {
	    if (control == null) throw new IllegalStateException("Must assign controller for a side");
	  }
	  
	  fen.setFEN(Board.START_POS).load(board);
	  
	  ArrayList<Integer> legalMoves = new ArrayList<>();
	  
	  for (;;) {
              views.forEach((view) -> {
                  view.update();
              });
	    
	    if (board.getHalfMoveClock() >= 100) {
                views.forEach((view) -> {
                    view.announceDraw(END_50_RULE);
                  });
	      return DRAW;
	    }
	    
	    int plySide = board.getPlySide();
	    IController control = controls[plySide];
	    
	    moveGen.getMoves(board, legalMoves);
	    // No more legal moves. The current side is immobilized
	    if (legalMoves.isEmpty()) {
	      if (moveGen.isChecked()) {
	        int winningSide = Piece.invertSide(board.getPlySide());
                  // Checkmate
                  views.forEach((view) -> {
                      view.announceWin(END_CHECKMATE, winningSide);
                  });
	        return winningSide;
	      } else {
                  // Stalemate
                  views.forEach((view) -> {
                      view.announceDraw(END_STALEMATE);
                  });
	        return DRAW;
	      }
	    }
	    
	    int chosenMove;
	    for (;;) {
	      chosenMove = control.getMove(legalMoves);
	      if (legalMoves.contains(chosenMove)) {
	        control.acceptedMove(chosenMove);
	        break;
	      } else {
	        control.illegalMove(chosenMove);
	      }
	    }
	    
	    board.applyMove(chosenMove);
	  } // Game loop
	}
}