import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;

public class GoGame extends Game{

	public GoGame(){
		//System.out.println("made go game");
		//Empty
		super();
		System.out.println("gogame constructor");
	}

	// Returns a list of possible moves (in the form of boards).  Takes
	// in the current board as input, as well as a boolean to indicate
	// which player's turn it is.
	@Override
	public ArrayList<Board> getPossibleMoves(Board board, boolean firstPlayer){
		//System.out.println("finding moves");
		int boardSize = board.getSize();

		Board origBoard = new Board(boardSize);
		origBoard.setBoard(board.getBoard());

		ArrayList<Board> moves = new ArrayList<Board>();

		// Add "pass" move (same board as input)
		//moves.add(board);

		// Iterate through current board, replace an empty spot with the
		// appropriate piece and add to moves
		//Board newBoard;
		/*char[][] origCharArray = new char[board.getSize()][board.getSize()];
		//origCharArray = board.getBoard();

		for(int i = 0; i < board.getSize(); i++){
		for(int j = 0; j < board.getSize(); j++){
		origCharArray[i][j] = board.getBoard()[i][j];
		}
		}
		*/

		for(int i = 0; i < board.getSize(); i++){
			for(int j = 0; j < board.getSize(); j++){
				if(board.getBoard()[i][j] == '-'){
					Board newBoard = new Board(boardSize);
					newBoard.setBoard(origBoard.getBoard()); // Start with same board

					//System.out.println("found move");

					// Found empty spot, add appropriate piece to spot add to moves,
					// reset board to keep looking for moves
					newBoard.putPiece(i,j,!firstPlayer);

					/*
					   System.out.println("======origBoard=====");
					   origBoard.printBoard();
					   System.out.println("======origBoard=====");
					   System.out.println("======newBoard=====");
					   newBoard.printBoard();
					   System.out.println("======newBoard=====");
					   */

					moves.add(newBoard);
					//System.out.println("asdfasdf");
					//moves.get(0).printBoard();
					//newBoard.setBoard(board.getBoard());
					//newBoard.printBoard();
					//try{ System.in.read(); } catch(IOException e){}
				}
			}
		}

		/*
		   System.out.println("----------");
		   for(int i = 0; i < board.getSize(); i++){
		   for(int j = 0; j < board.getSize(); j++){
		   System.out.print(origCharArray[i][j]);
		   }
		   System.out.println();
		   }
		   System.out.println("----------");
		   */

		//System.out.println("asdfasdf");
		//moves.get(3).printBoard();
		//System.out.println(moves.size());
		return moves;

	}

	@Override
	public boolean gameFinished(Board board){
		// TODO
		// Need score implemented for more sophisticated way of determining
		// if game is finished other than "no more possible moves"
		// (This is an unrealistic end condition)
		return (getPossibleMoves(board, true).size() == 0);
	}

	// Returns the score the playout results in
	// We will handle whether the playout is a win or not in the agent
	@Override
	public double randomPlayout(Board board, boolean firstPlayer){
		boolean turn = firstPlayer; // Tracks which piece to play
		Random r = new Random(); // For random selection

		ArrayList<Board> moves = new ArrayList<Board>();
		while(!gameFinished(board)){
			moves = getPossibleMoves(board, turn);
			board = moves.get(r.nextInt(moves.size()));

			turn = !turn;
		}

		return calculateScore(board);
	}

	@Override
	public double calculateScore(Board board){
		// TODO
		// implement this
		return 0.0;
	}

	// "Resolves" the board.  That is, uses a flood-fill-esque algo to 
	// check if pieces need to be removed from the board (Only applicable for Go).
	// Pieces are removed if they are surrounded by the other player(and boundary
	// of board) with no gaps
	@Override
	public Board resolveBoard(Board board, int boardSize){
		//TODO
		//return board;

		Board newBoard = new Board(boardSize);
		newBoard.setBoard(board.getBoard());
		//resolvedBoard.setBoard(board.getBoard());
		char floodChar; // Tracks which char we are checking for flood
		for(int i = 0; i < boardSize; i++){
			for(int j = 0; j < boardSize; j++){
				// If we find a non-empty space, attempt to resolve it.
				if(newBoard.getBoard()[i][j] != '-'){
					System.out.println("Resolving (" + i +", " + j + ")");
					newBoard.setBoard(this.resolveBoard(newBoard, boardSize, i, j, board.getBoard()[i][j]).getBoard());
				}
			}
		}

		return newBoard;
	}

	// Actually resolves the board.  We do this by iterating through columns left to right
	// looking for empty spots and replacing pieces as we go.
	private Board resolveBoard(Board board, int boardSize, int x, int y, char floodChar){
		// Copy board to ease swapping/returning process
		char[][] resolvedBoard = new char[boardSize][boardSize];
		for(int i = 0; i < boardSize; i++){
			for(int j = 0; j < boardSize; j++){
				resolvedBoard[i][j] = board.getBoard()[i][j];
			}
		}

		// Set the surrounding piece
		char endChar; 	// Other player's piecetype
		if(floodChar == 'X'){
			endChar = 'O';
		}else{
			endChar = 'X';
		}

		System.out.println("ENDCHAR: " + endChar);

		// Queue holds the pieces we have visited and need to expand on
		Queue<Pair> queue = new Queue<Pair>();

		// Set original coords, set east/west to 0/0 for now (we set on each loop next)
		Pair orig = new Pair(x,y);
		Pair w = new Pair(0, 0);
		Pair e = new Pair(0, 0);

		queue.enqueue(orig);

		for(Pair p : queue){
			// Set east/west to starting pair
			w.setPair(p);
			e.setPair(p);

			// Move the x coord of the first pair to the left until we hit a boundary,
			// the end char, or an empty space.  If empty, return.
			while(w.getX() >= 0 && board.getBoard()[w.getX()][w.getY()] != endChar){
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[w.getX()][w.getY()] == '-'){
					//System.out.println("Found176 - at " + left + "," + y);
					return board;
				}
				w.subX();
			}
			w.addX();
			
			// Move the x coord of the second pair to the right until we hit a boundary,
			// the end char, or an empty space.  If empty, return.
			while(e.getX() < boardSize && board.getBoard()[e.getX()][e.getY()] != endChar){
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[e.getX()][e.getY()] == '-'){
					System.out.println("LINE 205");
					return board;
				}
				e.addX();
			}
			e.subX();

			// Iterate through all nodes between w,e on x-axis.
			while(w.getX() <= e.getX()){
				System.out.println("looping: (" + w.getX() + ", " + w.getY() + ")");
				resolvedBoard[w.getX()][w.getY()] = '-';
				
				// Ensure we don't get an indexOOB
				if(w.getY() + 1 < boardSize){
					if(board.getBoard()[w.getX()][w.getY()+1] == floodChar){
						queue.enqueue(new Pair(w.getX(), w.getY() + 1));
					}

					if(board.getBoard()[w.getX()][w.getY()+1] == '-'){
						return board;
					}
				}

				// Ensure we don't get an indexOOB
				if(w.getY() - 1 >= 0){
					if(board.getBoard()[w.getX()][w.getY()-1] == floodChar){
						queue.enqueue(new Pair(w.getX(), w.getY() - 1));
					}

					if(board.getBoard()[w.getX()][w.getY()-1] == '-'){
						return board;
					}
				}
				w.addX();
			}
		}

		Board newBoard = new Board(boardSize);
		newBoard.setBoard(resolvedBoard);

		System.out.println("------RESOLVED--------");
		newBoard.printBoard();
		System.out.println("------RESOLVED--------");

		return newBoard;

	}
}
