//------------------------
// This class is a template for the different games we will be using.
// Within the different games, we define different "helper" methods
// for the MCTS agents to use.  These methods return information
// about the game (e.g. "is the game complete" or "what are the possible moves")
// from a given board position.  The actual running of the game
// (i.e. taking player input, keeping a master board, etc.) is not handled here.

import java.util.ArrayList;

public abstract class Game{

	public Game(){

	}
	// Returns a list of possible board states following a move from the given board
	ArrayList<Board> getPossibleMoves(Board board, boolean firstPlayer){
		System.out.println("Super possmoves (bad!)");
		return null;
	}
	
	//Is the game complete for the given board
	abstract boolean gameFinished(Board board, int moveNumber);

	// Returns score of a random playout
	abstract int randomPlayout(Board board, boolean firstPlayer, int moveNumber);

	// Calculate score of the game (may not be applicable for all games)
	abstract int calculateScore(Board board);

	Board resolveBoard(Board board, int boardSize){
		return board;
	}
}
