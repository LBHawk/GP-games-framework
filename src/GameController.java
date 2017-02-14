//-----------
// This class handles the back and forth play of the game.
// This is where the game, the game board, and any agents
// playing in the instance of the game are initialized.
//-----------
import java.io.*;
import java.util.*;
import com.beust.jcommander.JCommander;

public class GameController{

	static int boardSize;
	static boolean firstPlayerHuman; 	// Is first player human
	static boolean secondPlayerHuman; 	// Is second player human
	static boolean gameOver; 			// Is the game over?
	static boolean turn; 				// Which player's turn is it
	static int timeAllowed; 			// How long do agent's have to make a move

	public static void main(String [] args){
		// Parse commands with JCommander
		JCommanderInput input = new JCommanderInput();
		JCommander jc = new JCommander(input, args);

		if(input.getHelp()){
			jc.usage();
			return;
		}

		if(!input.validateParams()){
			jc.usage();
			return;
		}

		//------ Variable Assignment/Init ------------
		Board gameBoard; 			// Master board

		GameAgent firstPlayer = null;
		GameAgent secondPlayer = null;
		Game game = null;

		String gameType = input.getGame();
		switch(gameType){
			case "go": 		game = new GoGame();
							System.out.println("made gogame");
							break;
			case "hex": 	//game = new HexGame();
							break;
			case "sprouts": //game = new SproutsGame();
							break;
		}

		String agent1 = input.getAgent1();
		String agent2 = input.getAgent2();
		firstPlayerHuman = false;
		secondPlayerHuman = false;

		switch(agent1){
			case "human":	firstPlayerHuman = true;
						  	break;
			case "random": 	firstPlayer = new RandomAgent(gameType, false);
							break;
			case "mcts": 	//firstPlayer = new MCTSAgent(game, true);
							break;
		}

		switch(agent2){
			case "human":	secondPlayerHuman = true;
						  	break;
			case "random": 	secondPlayer = new RandomAgent(gameType, true);
							break;
			case "mcts": 	//secondPlayer = new MCTSAgent(game, true);
							break;
		}

		boardSize = input.getBoardSize();
		gameBoard = new Board(boardSize);
		gameOver = false;
		turn = true; 	// Tracks which player's turn it is
		timeAllowed = input.getTimeAllowed();

		// For human input
		Scanner s = new Scanner(System.in);
		String play;
		
		//----- Done initializing-----

		/*
		//---- Temporary initializations until more functionality added----------
		boardSize = 9;
		gameBoard = new Board(boardSize); 		// For now, just initialize 9x9 board
		firstPlayerHuman = true;
		secondPlayerHuman = true;
		gameOver = false;
		turn = true; 					// First player's turn to start (first player is X)
		*/

		while(!gameOver){
			gameBoard.printBoard();
			//Board boardCopy = new Board(gameBoard.getSize());

			if(turn){ 	// If first player's turn
				System.out.println("+++++Player One+++++");
				if(firstPlayerHuman){ 	// If player is human
					play = s.next(); 	// Get input from console
					playPiece(play, gameBoard);
				}else{
					System.out.println("!!!!!!!!!");
					Board theBoard = new Board(gameBoard.getSize());
					theBoard = firstPlayer.makeMove(gameBoard, timeAllowed);
					gameBoard.setBoard(theBoard.getBoard());
					turn = !turn;
					System.out.println("turn done");
				}

			}else{
				System.out.println("+++++Player Two+++++");
				if(secondPlayerHuman){
					play = s.next();
					playPiece(play, gameBoard);
				}else{
					Board theBoard = new Board(gameBoard.getSize());
					theBoard = secondPlayer.makeMove(gameBoard, timeAllowed);
					gameBoard.setBoard(theBoard.getBoard());
					turn = !turn;
				}
			}
			
			Board resolvedBoard = new Board(boardSize);
			resolvedBoard.setBoard(game.resolveBoard(gameBoard, boardSize).getBoard());

			gameBoard.setBoard(resolvedBoard.getBoard());
		}
	}

	public static boolean checkInput(String in){
		if(in.length() != 2){
			return false;
		}

		try{
			if((Integer.parseInt(in) / 10) < 0 || (Integer.parseInt(in) % 10) < 0){
				return false;
			}

			if((Integer.parseInt(in) / 10) > (boardSize - 1) || (Integer.parseInt(in) % 10) > (boardSize - 1)){
				return false;
			}
		}catch(NumberFormatException e){
			return false;
		}

		//		System.out.println("Good input");
		return true;
	}

	public static void playPiece(String play, Board gameBoard){
		if(checkInput(play)){ 	// Check the input given
			// Attempt to put the piece on the board
			if(gameBoard.putPiece(Integer.parseInt(play) / 10, Integer.parseInt(play) % 10, turn)){
				turn = !turn; // If successful, next player's turn
			}else{
				System.out.println("Space already occupied!");
			}
		}
	}
}
