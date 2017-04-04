import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;

public class HexGame extends Game{
	boolean[][] visited;

	public HexGame(){
		//Empty
		super();
		System.out.println("hexgame constructor");

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

		for(int i = 0; i < board.getSize(); i++){
			for(int j = 0; j < board.getSize(); j++){
				if(board.getBoard()[i][j] == '-'){
					Board newBoard = new Board(boardSize);
					newBoard.setBoard(origBoard.getBoard()); // Start with same board

					//System.out.println("found move");

					// Found empty spot, add appropriate piece to spot add to moves,
					newBoard.putPiece(i,j,!firstPlayer);
					moves.add(newBoard);
				}
			}
		}

		return moves;

	}

	//Is the game complete for the given board
	@Override
	public boolean gameFinished(Board board, int moveNumber){
		// Game is finished when the score is no longer 0
		return !(calculateScore(board) == 0);
	}

	// Returns score of a random playout
	@Override
	public int randomPlayout(Board board, boolean firstPlayer, int moveNumber){
		boolean turn = firstPlayer; // Tracks which piece to play
		Random r = new Random(); // For random selection

		ArrayList<Board> moves = new ArrayList<Board>();
		while(!gameFinished(board, moveNumber)){
			moves = getPossibleMoves(board, turn);
			//System.out.println(moves.size());
			board = moves.get(r.nextInt(moves.size()));
			board = resolveBoard(board, board.getSize());

			turn = !turn;
			moveNumber++;
			//System.out.println("!!!!!!!!!!!!!");
			//board.printBoard();
		}

		//System.out.println("randomPlay done");

		return calculateScore(board);
	}

	// Calculate score of the game (may not be applicable for all games)
	@Override
	public int calculateScore(Board board){
		if(findPath(board.getBoard(), true)){
			return 1;
		}if(findPath(board.getBoard(),  false)){
			return -1;
		}

		return 0;
	}

	// Attempts to find a path between starting and ending
	// row/column.  Boolean player indicates which player to check.
	// (true = white(X) | false = black(O))
	private boolean findPath(char [][] b, boolean player){
		this.visited = new boolean[b.length][b.length];
		// keeps track of if we have visited a spot on the board.
		// This is so we don't fruitlessly retrace explored paths
		// when starting from a different position.
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b.length; j++){
				this.visited[i][j] = false;
			}
		}

		if(player){
			// Recursive DFS from each piece in starting column
			for(int i = 0; i < b.length; i++){
				if(b[i][0] == 'X' && !visited[i][0]){
					if(recPath(b, i, 0)){
						return true;
					}
				}
			}
		}else{
			// Recursive DFS from each piece in starting row
			for(int i = 0; i < b.length; i++){
				if(b[0][i] == 'O' && !visited[0][i]){
					if(recPath(b, 0, i)){
						return true;
					}
				}
			}

		}

		return false;
		
	}

	// Recursively checks for path to the ending row/column
	private boolean recPath(char[][] b, int row, int column){
		char piece = b[row][column];
		int max = b.length - 1;
		// Mark current node as visited
		this.visited[row][column] = true;

		// Recursively call each neighbor (6 total) in depth-first order if
		// neighbor is not visited and contains proper piece
		if(piece == 'X'){
			if(column == max){
				return true;
			}

			// Above and to the right
			if(row > 0 && !visited[row-1][column+1] && (b[row-1][column+1] == piece)){
				if(recPath(b,row-1, column+1)){
					return true;
				}
			}

			// Directly to right
			if(b[row][column+1] == piece && !visited[row][column+1]){
				if(recPath(b,row, column+1)){
						return true;
				}
			}

			// Directly below
			if(row < max && !visited[row+1][column] && (b[row+1][column] == piece)){
				if(recPath(b,row+1, column)){
					return true;
				}
			}

			// Directly above
			if(row > 0 && !visited[row-1][column] && b[row-1][column] == piece){
				if(recPath(b,row-1, column)){
						return true;
				}
			}

			// Below and to the left
			if(row < max && column > 0 && !visited[row+1][column-1] && (b[row+1][column-1] == piece)){
				if(recPath(b,row+1, column-1)){
					return true;
				}
			}

			// Directly to left
			if(column > 0 && !visited[row][column-1] && b[row][column-1] == piece){
				if(recPath(b,row, column-1)){
						return true;
				}
			}

			return false;

		}else if(piece == 'O'){
			if(row == max){
				return true;
			}

			// Directly below
			if(!visited[row+1][column] && (b[row+1][column] == piece)){
				if(recPath(b,row+1, column)){
					return true;
				}
			}

			// Below and to the left
			if(column > 0 && !visited[row+1][column-1] && (b[row+1][column-1] == piece)){
				if(recPath(b,row+1, column-1)){
					return true;
				}
			}

			// Directly to right
			if(column < max && b[row][column+1] == piece && !visited[row][column+1]){
				if(recPath(b,row, column+1)){
						return true;
				}
			}

			// Directly to left
			if(column > 0 && !visited[row][column-1] && b[row][column-1] == piece){
				if(recPath(b,row, column-1)){
						return true;
				}
			}

			// Above and to the right
			if(row > 0 && column < max && !visited[row-1][column+1] && (b[row-1][column+1] == piece)){
				if(recPath(b,row-1, column+1)){
					return true;
				}
			}

			// Directly above
			if(row > 0 && !visited[row-1][column] && b[row-1][column] == piece){
				if(recPath(b,row-1, column)){
						return true;
				}
			}

			return false;


			

		}else{
			System.out.println("REC PATH F'D UP");
			System.exit(-1);
			return false;
		}
	}

	@Override
	public Board randomBoardAfterXMoves(int boardSize, int numMoves){
		Board board = new Board(boardSize);
		boolean turn = false;
		Random r = new Random();
		int moveNum = 0;

		ArrayList<Board> moves = new ArrayList<Board>();
		while(moveNum < numMoves && !gameFinished(board, moveNum)){
			moves = getPossibleMoves(board, turn);
			board = moves.get(r.nextInt(moves.size()));

			turn = !turn;
			moveNum++;
		}

		return board;

	}

	// For Hex, we never need to remove pieces from the board.
	// Simply return the board we started with
	@Override
	public Board resolveBoard(Board board, int boardSize){
		return board;
	}

}
