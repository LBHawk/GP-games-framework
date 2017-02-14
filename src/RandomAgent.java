import java.util.Random;
import java.util.ArrayList;

public class RandomAgent extends GameAgent{
	private Game game;
	private boolean firstPlayer;
	private Random r;

	public RandomAgent(String game, boolean firstPlayer){
		super(game, firstPlayer);
		switch(game){
			case "go": 		this.game = new GoGame();
							System.out.println("made go game in agent");
							break;
			case "hex": 	//this.game = new HexGame();
							break;
			case "sprouts":	//this.game = new SproutsGame();
							break;
		}
		r = new Random();
		this.firstPlayer = firstPlayer;
		System.out.println("RA const");
	}

	@Override
	protected Board makeMove(Board board, int timeAllowed){
		//System.out.println("makeMove");
		ArrayList<Board> moves = new ArrayList<Board>();
		System.out.println(board.getBoard()[0][0] + "" + firstPlayer);
		if(game == null){
			System.out.println("null game");
		}
		moves = game.getPossibleMoves(board, firstPlayer);

		System.out.println(moves.size());
		return moves.get(r.nextInt(moves.size()));
	}

	@Override
	protected boolean checkMoveIntegrity(Board first, Board second){
		return super.checkMoveIntegrity(first, second);
	}
	
}
