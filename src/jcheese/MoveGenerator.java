package jcheese;

import java.util.*;
import jcheese.util.BitBoard;
import jcheese.util.FEN;
import jcheese.util.MoveData;
import jcheese.util.MoveData.IScanner;

import static jcheese.util.MoveData.NORTH;
import static jcheese.util.MoveData.EAST;
import static jcheese.util.MoveData.SOUTH;
import static jcheese.util.MoveData.WEST;
import static jcheese.util.MoveData.NORTHEAST;
import static jcheese.util.MoveData.SOUTHEAST;
import static jcheese.util.MoveData.SOUTHWEST;
import static jcheese.util.MoveData.NORTHWEST;

public class MoveGenerator {
	// Board cache
	private int activeSide;
	private long[] bitboards;
	private boolean[] castles;
	private int epSquare;
	// Important derivations from board state
	private int inactiveSide;
	private long epBit;
	private int activeKingSquare;
	private long activeKingBit;
	private int inactiveKingSquare;
	private long inactiveKingBit;
	private long generalOccupancy;
	private long inactiveOccupancy;
	private long unoccupied;
	// Generator state
	private long attacksToActive;
	private long checkers;
	private long checkPaths;
	private long[] pins = new long[Square.COUNT];
	private ArrayList<Integer> moves;
	
	// Initialization phase
	private void init(Board board, ArrayList<Integer> moves) {
	  activeSide = board.getPlySide();
	  bitboards = board.getBoards();
	  castles = board.getCastles();
	  epSquare = board.getEpSquare();
	  
	  inactiveSide = Piece.invertSide(activeSide);
	  epBit = epSquare != Square.NIL ? BitBoard.bit(epSquare) : BitBoard.allClear();
	  activeKingBit = bitboards[Board.KINGS | activeSide];
	  assert BitBoard.popCount(activeKingBit) == 1; // Must contain exactly one king
	  activeKingSquare = BitBoard.bitScanForward(activeKingBit);
	  inactiveKingBit = bitboards[Board.KINGS | inactiveSide];
	  assert BitBoard.popCount(inactiveKingBit) == 1; // Must contain exactly one king
	  inactiveKingSquare = BitBoard.bitScanForward(inactiveKingBit);
	  inactiveOccupancy = bitboards[Board.ALL | inactiveSide];
	  generalOccupancy = inactiveOccupancy | bitboards[Board.ALL | activeSide];
	  unoccupied = ~generalOccupancy;
	  
	  attacksToActive = BitBoard.allClear();
	  checkers = BitBoard.allClear();
	  checkPaths = BitBoard.allClear();
	  Arrays.fill(pins, BitBoard.allSet());
	  this.moves = moves;
	  moves.clear();
	}
	
	// Attacks generation phase
	/* [Design Note]
	 * The attacks generation phase calculates all the possible captures of the opposing side
	 * This is useful in determining whether a square is safe for the king to move to or capture
	 * This phase also calculates the checkers and the checker paths of the king (if present)
	 * ... and the pinned pieces of the current side
	 * This phase mutates the [pins], [checkers], [checkPaths] and [attacksToActive] fields
	 */
	
	private void addAttacks(int theSquare, long attacks) {
	  attacksToActive |= attacks;
	  if (BitBoard.notEmpty(attacks & activeKingBit)) {
	    checkers |= BitBoard.bit(theSquare);
	  }
	}
	
	private void attacksByPawn() {
	  for (long pawns = bitboards[Board.PAWNS | inactiveSide];
	       BitBoard.notEmpty(pawns); pawns = BitBoard.popLSB(pawns)) {
	    int pawnSquare = BitBoard.bitScanForward(pawns);
	    addAttacks(pawnSquare, MoveData.pawnCaptures[pawnSquare][inactiveSide]);
	  }
	}
	
	private void attacksByKnight() {
	  for (long knights = bitboards[Board.KNIGHTS | inactiveSide];
	       BitBoard.notEmpty(knights); knights = BitBoard.popLSB(knights)) {
	    int knightSquare = BitBoard.bitScanForward(knights);
	    addAttacks(knightSquare, MoveData.knightMoves[knightSquare]);
	  }
	}
	
	private void attacksByKing() {
	  long kingAttacks = MoveData.kingMoves[inactiveKingSquare];
	  assert BitBoard.isEmpty(kingAttacks & activeKingBit); // Inactive king cannot give check to active king
	  addAttacks(inactiveKingSquare, kingAttacks);
	}
	
	private void attacksBySlider(int boardIndex, int dirStart, int dirEnd) {
	  for (long thePieces = bitboards[boardIndex | inactiveSide];
	       BitBoard.notEmpty(thePieces); thePieces = BitBoard.popLSB(thePieces)) {
	    int theSquare = BitBoard.bitScanForward(thePieces);
	    long[] rays = MoveData.sliders[theSquare];
	    long extent = BitBoard.allClear();
	    
	    for (int dir = dirStart; dir < dirEnd; ++dir) {
	      long ray = rays[dir];
	      IScanner scanner = MoveData.scanners[dir];
	      
	      // Check for blockers
	      long blockers = ray & generalOccupancy;
	      if (BitBoard.notEmpty(blockers)) {
	        int hitSquare = scanner.run(blockers);
	        long hitBit = BitBoard.bit(hitSquare);
	        long shadow = MoveData.sliders[hitSquare][dir];
	        // Check if an active piece is hit
	        if (BitBoard.notEmpty(hitBit & bitboards[Board.ALL | activeSide])) {
	          if (hitSquare == activeKingSquare) {
	            checkPaths |= ray & ~shadow;
	          } else {
	            // A potential pinned piece
	            if (BitBoard.notEmpty(MoveData.extractors[dir].run(shadow & generalOccupancy) & activeKingBit)) {
	              // The active piece at [hitSquare] was protecting the active king from a check. Pin the piece
	              assert BitBoard.isEmpty(~pins[hitSquare]); // Cannot pin the piece twice
	              pins[hitSquare] = ray | BitBoard.bit(theSquare);
	            }
	            ray &= ~shadow; // Apply the shadow. The caster is an active non-king piece
	          }
	        } else {
	          ray &= ~shadow; // Apply the shadow. The caster is an inactive piece
	        }
	      } // If [ray] hit a piece
	      extent |= ray;
	    } // For each direction
	    
	    addAttacks(theSquare, extent);
	  } // For each piece in [thePieces]
	}
	
	private void getAllAttacks() {
	  attacksByPawn();
	  attacksBySlider(Board.ROOKS, MoveData.CARDINAL, MoveData.DIAGONAL);
	  attacksByKnight();
	  attacksBySlider(Board.BISHOPS, MoveData.DIAGONAL, MoveData.DIRECTION_COUNT);
	  attacksBySlider(Board.QUEENS, MoveData.CARDINAL, MoveData.DIRECTION_COUNT);
	  attacksByKing();
	}
	
	// Legal move generation phase
	/* [Design Note]
	 * This phase generates legal moves by accumulating the [moves] field
	 * The generated masks by the attacks generation phase is used here to determine what moves are legal
	 * This phase also encodes the moves into 32-bit integers
 	* movesBy<Piece> methods process the generation of legal moves
 	* General parameters [pushMask] and [captureMask] contains:
 	*  - the bits that tell whether a push/capture is allowed due to the circumstances of the king
 	*    i.e., the king is saved from a check by pushing/capturing that square/piece
 	*  - the bits that tell whether a push/capture is inherently possible
 	*    i.e., the piece can push to the square because it is unoccupied
 	*    or a piece can capture that other piece because the two pieces are not of the same side
 	*  - it does not contain the bits that tell whether a piece is pinned along a ray
 	*    as the pin information only applies to a specific piece and is calculated for each square
 	* Generating king moves are different altogether. The subroutine only needs the attacked squares
 	*  ... in calculating moves. The king cannot possibly push to or capture squares that are attacked by the opposition
	 */
	
	private void emit(int move) { moves.add(move); }
	private void emit(int... moves) {
	  for (final int move : moves) emit(move);
	}
	
	private void emitPush(int theSquare, long pushes) {
	  for (; BitBoard.notEmpty(pushes); pushes = BitBoard.popLSB(pushes)) {
	    emit(Move.push(theSquare, BitBoard.bitScanForward(pushes)));
	  }
	}
	
	private void emitCapture(int theSquare, long captures) {
	  for (; BitBoard.notEmpty(captures); captures = BitBoard.popLSB(captures)) {
	    emit(Move.capture(theSquare, BitBoard.bitScanForward(captures)));
	  }
	}
	
	private void emitPawnPush(int theSquare, long pushBit, int promoteRank) {
	  if (BitBoard.notEmpty(pushBit)) {
	    int pushSquare = BitBoard.bitScanForward(pushBit);
	    if (Square.getY(pushSquare) == promoteRank) {
	      emit(
	        Move.promotePush(theSquare, pushSquare, Piece.ROOK),
	        Move.promotePush(theSquare, pushSquare, Piece.KNIGHT),
	        Move.promotePush(theSquare, pushSquare, Piece.BISHOP),
	        Move.promotePush(theSquare, pushSquare, Piece.QUEEN)
	      );
	    } else {
	      emit(Move.push(theSquare, pushSquare));
	    }
	  }
	}
	
	private void emitPawnCapture(int theSquare, long captureBits, int promoteRank) {
	  for (; BitBoard.notEmpty(captureBits); captureBits = BitBoard.popLSB(captureBits)) {
	    int captureSquare = BitBoard.bitScanForward(captureBits);
	    if (Square.getY(captureSquare) == promoteRank) {
	      emit(
	        Move.promoteCapture(theSquare, captureSquare, Piece.ROOK),
	        Move.promoteCapture(theSquare, captureSquare, Piece.KNIGHT),
	        Move.promoteCapture(theSquare, captureSquare, Piece.BISHOP),
	        Move.promoteCapture(theSquare, captureSquare, Piece.QUEEN)
	      );
	    } else {
	      emit(Move.capture(theSquare, captureSquare));
	    }
	  }
	}
	
	private void movesByPawn(long pushMask, long captureMask) {
	  for (long pawns = bitboards[Board.PAWNS | activeSide];
	       BitBoard.notEmpty(pawns); pawns = BitBoard.popLSB(pawns)) {
	    int pawnSquare = BitBoard.bitScanForward(pawns);
	    long pawnBit = BitBoard.bit(pawnSquare);
	    long pawnCaptures = MoveData.pawnCaptures[pawnSquare][activeSide];
	    // Apply the pins
	    long legalPushes = pushMask & pins[pawnSquare];
	    long legalCaptures = captureMask & pins[pawnSquare];
	    
	    // Generate pushes, double pushes and captures
	    if (activeSide == Piece.LIGHT) {
	      long pushBit = pawnBit << 8;
	      emitPawnPush(pawnSquare, pushBit & legalPushes, 7);
	      // Check if nothing blocks the first push and destination is a valid double push and the double push is legal
	      pushBit = ((pushBit & unoccupied) << 8) & legalPushes & MoveData.doublePushes[Piece.LIGHT];
	      if (BitBoard.notEmpty(pushBit)) {
	        emit(Move.doublePush(pawnSquare, pawnSquare + 16, pawnSquare + 8));
	      }
	      emitPawnCapture(pawnSquare, pawnCaptures & legalCaptures, 7);
	    } else {
	      long pushBit = pawnBit >> 8;
	      emitPawnPush(pawnSquare, pushBit & legalPushes, 0);
	      // Check if nothing blocks the first push and destination is a valid double push and the double push is legal
	      pushBit = ((pushBit & unoccupied) >> 8) & legalPushes & MoveData.doublePushes[Piece.DARK];
	      if (BitBoard.notEmpty(pushBit)) {
	        emit(Move.doublePush(pawnSquare, pawnSquare - 16, pawnSquare - 8));
	      }
	      emitPawnCapture(pawnSquare, pawnCaptures & legalCaptures, 0);
	    }
	    // Generate en passant
	    // Skip en passant generation if [epBit] is not within [pawnCaptures]
	    if (BitBoard.isEmpty(pawnCaptures & epBit)) continue;
	    // Calculate the push and capture of the en passant move
	    long epPushBit = epBit & legalPushes;
	    long epCaptureBit;
	    if (activeSide == Piece.LIGHT) {
	      epCaptureBit = (epBit >> 8) & legalCaptures;
	    } else {
	      epCaptureBit = (epBit << 8) & legalCaptures;
	    }
	    // Skip en passant generation if both [epPushBit] and [epCaptureBit] is empty
	    if (BitBoard.isEmpty(epPushBit) && BitBoard.isEmpty(epCaptureBit)) continue;
	    // Probe for en passant discovered checks
	    // Stage the board as if en passant move is made
	    long workingBoard = generalOccupancy & ~(pawnBit | epCaptureBit) | epPushBit;
	    // Get all the possible checkers
	    long cardinalCheckers = bitboards[Board.ROOKS | inactiveSide] | bitboards[Board.QUEENS | inactiveSide];
	    long diagonalCheckers = bitboards[Board.BISHOPS | inactiveSide] | bitboards[Board.QUEENS | inactiveSide];
	    long[] rays = MoveData.sliders[activeKingSquare];
	    // Probe for [cardinalCheckers] from the East or West and for [diagonalCheckers] everywhere
	    // The pin masks handle restricting discovered checks and some keep the king protected from a vertical slider anyways
	    if (BitBoard.notEmpty(MoveData.extractors[EAST].run(rays[EAST] & workingBoard) & cardinalCheckers)
	        || BitBoard.notEmpty(MoveData.extractors[WEST].run(rays[WEST] & workingBoard) & cardinalCheckers)
	        || BitBoard.notEmpty(MoveData.extractors[NORTHEAST].run(rays[NORTHEAST] & workingBoard) & diagonalCheckers)
	        || BitBoard.notEmpty(MoveData.extractors[SOUTHEAST].run(rays[SOUTHEAST] & workingBoard) & diagonalCheckers)
	        || BitBoard.notEmpty(MoveData.extractors[SOUTHWEST].run(rays[SOUTHWEST] & workingBoard) & diagonalCheckers)
	        || BitBoard.notEmpty(MoveData.extractors[NORTHWEST].run(rays[NORTHWEST] & workingBoard) & diagonalCheckers)
	    ) continue; // King can be checked. Skip en passant generation
	    
	    emit(Move.enPassant(pawnSquare, epSquare, epSquare + (activeSide == Piece.LIGHT ? -8 : +8)));
	  }
	}
	
	private void movesByKnight(long pushMask, long captureMask) {
	  for (long knights = bitboards[Board.KNIGHTS | activeSide];
	       BitBoard.notEmpty(knights); knights = BitBoard.popLSB(knights)) {
	    int knightSquare = BitBoard.bitScanForward(knights);
	    long knightMoves = MoveData.knightMoves[knightSquare] & pins[knightSquare];
	    emitPush(knightSquare, knightMoves & pushMask);
	    emitCapture(knightSquare, knightMoves & captureMask);
	  }
	}
	
	private void movesByKing() {
	  long kingMoves = MoveData.kingMoves[activeKingSquare] & ~attacksToActive;
	  
	  emitPush(activeKingSquare, kingMoves & unoccupied);
	  emitCapture(activeKingSquare, kingMoves & inactiveOccupancy);
	  
	  // King cannot castle when in check
	  if (BitBoard.notEmpty(checkers)) return;
	  
	  for (int castle = Board.KING_CASTLE | activeSide; castle < Board.CASTLE_COUNT; castle += 2) {
	    if (castles[castle] && (
	      BitBoard.isEmpty(MoveData.castleClearSquares[castle] & generalOccupancy)
	      && BitBoard.isEmpty(MoveData.castleSafeSquares[castle] & attacksToActive)
	    )) {
	      emit(Move.castle(castle));
	    }
	  }
	}
	
	private void movesBySlider(int boardIndex, int dirStart, int dirEnd, long pushMask, long captureMask) {
	  for (long theSliders = bitboards[boardIndex | activeSide];
	       BitBoard.notEmpty(theSliders); theSliders = BitBoard.popLSB(theSliders)) {
	    int theSquare = BitBoard.bitScanForward(theSliders);
	    // Calculate the legal moves
	    long legalPushes = pins[theSquare] & pushMask;
	    long legalCaptures = pins[theSquare] & captureMask;
	    long[] rays = MoveData.sliders[theSquare];
	    long extent = BitBoard.allClear();
	    
	    for (int dir = dirStart; dir < dirEnd; ++dir) {
	      IScanner scanner = MoveData.scanners[dir];
	      long ray = rays[dir];
	      long blockers = ray & generalOccupancy;
	      
	      if (BitBoard.notEmpty(blockers)) {
	        ray &= ~MoveData.sliders[scanner.run(blockers)][dir];
	      }
	      
	      extent |= ray;
	    }
	    
	    emitPush(theSquare, extent & legalPushes);
	    emitCapture(theSquare, extent & legalCaptures);
	  }
	}
	
	private void getAllMoves() {
	  // Calculate all the attacks to the current side to move
	  getAllAttacks();
	  // Calculate the preliminary allowed pushes/captures
	  long pushMask = unoccupied;
	  long captureMask = inactiveOccupancy;
	  
	  int checkerCount = BitBoard.popCount(checkers);
	  if (checkerCount > 1) {
	    // Double check. The only piece that can move is the king
	    pushMask = BitBoard.allClear();
	    captureMask = BitBoard.allClear();
	  } else if (checkerCount == 1) {
	    pushMask &= checkPaths; // Can only push to checker path
	    captureMask &= checkers; // Can only capture the checker
	  }
	  // With the calculated [pushMask] and [captureMask] generate the moves
	  movesByPawn(pushMask, captureMask);
	  movesBySlider(Board.ROOKS, MoveData.CARDINAL, MoveData.DIAGONAL, pushMask, captureMask);
	  movesByKnight(pushMask, captureMask);
	  movesBySlider(Board.BISHOPS, MoveData.DIAGONAL, MoveData.DIRECTION_COUNT, pushMask, captureMask);
	  movesBySlider(Board.QUEENS, MoveData.CARDINAL, MoveData.DIRECTION_COUNT, pushMask, captureMask);
	  movesByKing();
	}
	
	public boolean isChecked() {
	  return BitBoard.popCount(checkers) > 0;
	}
	
	public void getMoves(Board board, ArrayList<Integer> moves) {
	  init(board, moves);
	  getAllMoves();
	}
	
	public ArrayList<Integer> getMoves(Board board) {
	  init(board, new ArrayList<>());
	  getAllMoves();
	  return moves;
	}
	
	// A more memory expensive method but can tell the source squares
	public void getSortedMoves(Board board, ArrayList<Integer>[] moves) {
	  for (int move : getMoves(board)) {
	    var subContainer = moves[Move.getSrc(move)];
	    assert subContainer != null;
	    subContainer.add(move);
	  }
	}	
	public void getSortedMoves(Board board) {
	  getSortedMoves(board, newMoveContainer());
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer>[] newMoveContainer() {
	  ArrayList<Integer>[] container = new ArrayList[Square.COUNT];
	  for (int i = 0; i < container.length; ++i) {
	    container[i] = new ArrayList<Integer>();
	  }
	  return container;
	}
	
	// Test methods
	private void testAttacksReloadBoard(Board board, String fen) {
	  FEN.loadFEN(board, fen);
	  init(board, new ArrayList<>());
	  getAllAttacks();
	}
	
	public void testAttacks() {
	  Board board = new Board();
	  testAttacksReloadBoard(board, Board.START_POS);
	  
	  try (Scanner sc = new Scanner(System.in)) {
	    System.out.println("Test Attack Set for Move Generation");
	    for (;;) {
	      System.out.println("--------------------");
	      String input = sc.next();
	      
	      if (input.equalsIgnoreCase("exit")) {
	        break;
	      } else if (input.equalsIgnoreCase("info")) {
	        System.out.println("Attacks to active king");
	        BitBoard.print(attacksToActive);
	        System.out.println("Checkers");
	        BitBoard.print(checkers);
	        System.out.println("Check paths");
	        BitBoard.print(checkPaths);
	      } else if (input.equalsIgnoreCase("fen")) {
	        String fen = sc.nextLine();
	        System.out.println("Loading" + fen);
	        try {
	          testAttacksReloadBoard(board, fen.substring(1));
	        } catch (IllegalArgumentException exc) {
	          System.out.println("Oops! Another exception occured");
	          exc.printStackTrace();
	          System.out.println();
	        } catch (StringIndexOutOfBoundsException exc) {
	          System.out.println("Usage: fen <fen string>");
	        }
	      } else if (input.equalsIgnoreCase("gen")) {
	        testShowMoves(board);
	      } else if (input.equalsIgnoreCase("occu")) {
	        System.out.println("Inactive occupancy");
	        BitBoard.print(inactiveOccupancy);
	        System.out.println("General occupancy");
	        BitBoard.print(generalOccupancy);
	        System.out.println("Unoccupied");
	        BitBoard.print(unoccupied);
	      } else if (input.equalsIgnoreCase("eps")) {
	        System.out.printf("EP Square @ %s\n", Square.names[epSquare]);
	        BitBoard.print(epBit);
	      } else if (input.length() == 2) {
	        int file = Character.toLowerCase(input.charAt(0)) - 'a';
	        int rank = input.charAt(1) - '1';
	        
	        if (file < 0 || file >= 8 || rank < 0 || rank >= 8) {
	          System.out.println("Invalid square");
	        } else {
	          BitBoard.print(pins[Square.fromCoords(file, rank)]);
	        }
	      } else {
	        System.out.println("Invalid response");
	      }
	    }
	  }
	}
	
  public void testShowMoves(Board board) {
	  board.print();
	  for (final int move : getMoves(board)) {
	    System.out.println(Move.getLAN(move));
	  }
	}
	
	private int depthTest(Board board, int depth) {
	  if (depth <= 0) return 1;
	  
	  Board cloneBoard = new Board();
	  ArrayList<Integer> moves = getMoves(board);
	  int nodes = 0;
	  
	  for (final int move : moves) {
	    cloneBoard.copyFrom(board);
	    cloneBoard.applyMove(move);
	    nodes += depthTest(cloneBoard, depth - 1);
	  }
	  
	  return nodes;
	}
	
	public void testForPerftree(int depth, String fen, String[] ssans) {
	  Board board = new Board();
	  FEN.loadFEN(board, fen);
	  
	  if (ssans.length == 1) {
	    for (String ssan : ssans[0].split("\\s+")) {
	      int move = Move.fromSSAN(board, ssan);
	      board.applyMove(move);
	    }
	  }
	  
	  Board cloneBoard = new Board();
	  ArrayList<Integer> moves = getMoves(board);
	  int totalNodes = 0;
	  
	  for (final int move : moves) {
	    System.out.print(Move.getSSAN(move) + ' ');
	    cloneBoard.copyFrom(board);
	    cloneBoard.applyMove(move);
	    int nodes = depthTest(cloneBoard, depth - 1);
	    System.out.println(nodes);
	    totalNodes += nodes;
	  }
	  
	  System.out.println();
	  System.out.print(totalNodes);
	}
}