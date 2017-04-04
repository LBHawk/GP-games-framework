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
	static final boolean KILLHUMANS = false; // !!!!DO NOT CHANGE!!!!

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
			case "hex": 	game = new HexGame();
							break;
			case "sprouts": //game = new SproutsGame();
							break;
		}

		String agent1 = input.getAgent1();
		String agent2 = input.getAgent2();
		firstPlayerHuman = false;
		secondPlayerHuman = false;

		boardSize = input.getBoardSize();
		gameBoard = new Board(boardSize);

		switch(agent1){
			case "human":	firstPlayerHuman = true;
						  	break;
			case "random": 	firstPlayer = new RandomAgent(gameType, false);
							break;
			case "mcts": 	firstPlayer = new MCTSAgent(gameType, false);
							break;
			case "ann": 	firstPlayer = new ANNAgent(gameType, false);
							firstPlayer.setNetwork(boardSize);
							break;
			case "ga": 		firstPlayer = new GAAgent(gameType, false);
							break;
			case "neat": 	firstPlayer = new NEATAgent(gameType, false);
							firstPlayer.setNetwork(boardSize);
							break;
		}

		switch(agent2){
			case "human":	secondPlayerHuman = true;
						  	break;
			case "random": 	secondPlayer = new RandomAgent(gameType, true);
							break;
			case "mcts": 	secondPlayer = new MCTSAgent(gameType, true);
							break;
			case "ann": 	secondPlayer = new ANNAgent(gameType, true);
							secondPlayer.setNetwork(boardSize);
							break;
			case "ga": 		secondPlayer = new GAAgent(gameType, true);
							break;
			case "neat": 	secondPlayer = new NEATAgent(gameType, true);
							secondPlayer.setNetwork(boardSize);
							break;
		}

		gameOver = false;
		turn = true; 	// Tracks which player's turn it is
		timeAllowed = input.getTimeAllowed();

		// For human input
		Scanner s = new Scanner(System.in);
		String play;

		// For saving the game data
		boolean saveData = input.getRecord();
		File output = null;
		FileWriter fw = null;
		File output2 = null;
		File output3 = null;
		FileWriter itFirstfw = null;
		FileWriter itSecondfw = null;

		List<FileWriter> fws = new ArrayList<FileWriter>();
		
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

		// Initialize the output file if needed
		if(saveData){
			try{
				String dir = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/data/";
				String filePath = dir +"SCORES"+ timeAllowed + gameType + boardSize + agent1 + agent2 + ".csv";
				output = new File(filePath);
				fw = new FileWriter(output, true);
				//bw = new BufferedWriter(fw);
				//
				String filePath2 = dir + "ITS" + timeAllowed + gameType + boardSize + agent1 + ".csv";
				output2 = new File(filePath2);
				itFirstfw = new FileWriter(output2, true);

				String filePath3 = dir + "ITS" + timeAllowed + gameType + boardSize + agent2 + ".csv";
				output3 = new File(filePath3);
				itSecondfw = new FileWriter(output3, true);

				fws.add(fw);
				fws.add(itFirstfw);
				fws.add(itSecondfw);

				//fws.get(1).write("TEST");
				//	fws.get(2).write("TEST");
			}catch(IOException e){
				e.printStackTrace();
				System.exit(-1);
			}
		}

		int moveNumber = 0;
		while(!gameOver){
			gameBoard.printBoard();
			//Board boardCopy = new Board(gameBoard.getSize());

			if(turn){ 	// If first player's turn
				System.out.println("+++++Player One+++++");
				if(firstPlayerHuman){ 	// If player is human
					play = s.next(); 	// Get input from console
					playPiece(play, gameBoard);
				}else{
					//System.out.println("!!!!!!!!!");
					Board theBoard = new Board(gameBoard.getSize());
					theBoard = firstPlayer.makeMove(gameBoard, timeAllowed, moveNumber);
					gameBoard.setBoard(theBoard.getBoard());
					turn = !turn;
					System.out.println("turn done");

					String toWrite = moveNumber + " " + secondPlayer.getIterations() + "\n";

					if(saveData){
					try{
						System.out.println(toWrite);
						fws.get(1).write(toWrite);
						fws.get(1).flush();
						System.out.println("WROTE");
					}catch(IOException e){
						e.printStackTrace();
						System.exit(-1);
					}
					}
				}

			}else{
				System.out.println("+++++Player Two+++++");
				if(secondPlayerHuman){
					play = s.next();
					playPiece(play, gameBoard);
				}else{
					Board theBoard = new Board(gameBoard.getSize());
					theBoard = secondPlayer.makeMove(gameBoard, timeAllowed, moveNumber);
					gameBoard.setBoard(theBoard.getBoard());
					turn = !turn;
					System.out.println("turn done");

					String toWrite = moveNumber + " " + secondPlayer.getIterations() + "\n";
					
					if(saveData){
					try{
						System.out.println(toWrite);
						fws.get(2).write(toWrite);
						fws.get(2).flush();
						System.out.println("WROTE");
					}catch(IOException e){
						e.printStackTrace();
						System.exit(-1);
					}
					}
				}
			}
			
			Board resolvedBoard = new Board(boardSize);
			resolvedBoard.setBoard(game.resolveBoard(gameBoard, boardSize).getBoard());

			gameBoard.setBoard(resolvedBoard.getBoard());

			moveNumber++;
			if(game.gameFinished(gameBoard, moveNumber)){ gameOver = true; }
			int gameScore = game.calculateScore(gameBoard);
			System.out.println("Score: " + gameScore);

			if(saveData){
			try{
				fws.get(0).write(gameScore + " ");
				fws.get(0).flush();
			}catch(IOException e){
				e.printStackTrace();
			}
			}
		}


		if(saveData){
		try{
			fws.get(0).write("\n");
			fws.get(0).flush();
			fws.get(0).close();
			//fws.get(1).write("\n");
			fws.get(1).flush();
			fws.get(1).close();
			//fws.get(2).write("\n");
			fws.get(2).flush();
			fws.get(2).close();
		}catch(IOException e){
			e.printStackTrace();
		}
		}

		System.out.println("Game took " + moveNumber + " moves.");
		gameBoard.printBoard();		
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
