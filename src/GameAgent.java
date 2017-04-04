import java.util.Random;

public abstract class GameAgent{
	private Game game;
	private boolean firstPlayer;
	private Random r;
	public int iterations;

	public GameAgent(String game, boolean firstPlayer){
		switch(game){
			case "go": 		this.game = new GoGame();
							System.out.println("made go game in agent");
							break;
			case "hex": 	this.game = new HexGame();
							break;
			case "sprouts":	//this.game = new SproutsGame();
							break;
		}
		//this.game = game;
		this.firstPlayer = firstPlayer;
		this.r = new Random();
	}

	protected abstract void select(Node currentNode, int moveNumber);	
	protected abstract void setNetwork(int boardSize);
	protected abstract Board makeMove(Board board, int timeAllowed, int moveNumber);
	protected abstract double estimateNodesScore(Board board, int iterations, int moveNumber);
	
	// Checks the integrity of the move made (Only 0 or 1 pieces placed)
	protected boolean checkMoveIntegrity(Board first, Board second){
		int counter = 0;
		for(int i = 0; i < first.getSize(); i++){
			for(int j = 0; j < first.getSize(); j++){
				if(first.getBoard()[i][j] != second.getBoard()[i][j]){
					counter++;
					//Check counter here so we don't waste time after counter exceeds max
					if(counter > 1){ return false; }
				}
			}
		}

		return true;
	}

	protected abstract int getIterations();
}
