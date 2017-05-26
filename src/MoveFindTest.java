// For debug
public class MoveFindTest{
	public static void main(String [] args){
		Game game = new GoGame();
		Board board = new Board(9);

		game.getPossibleMoves(board, true);
		
	}
}
