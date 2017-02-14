public class Board{
	private char[][] board; 		// 2d array for storing board state
	private int size;

	// Constructor for board, initializes the 2d array as size x size
	public Board(int s){
		this.size = s;
		this.board = new char[s][s];
		for(int i = 0; i < s; i++){
			for(int j = 0; j < s; j++){
				this.board[i][j] = '-';
			}
		}
	}

	public void printBoard(){
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				System.out.print(this.board[i][j]);
			}
			System.out.println();
		}
	}

	// Put a piece at (x,y).  Returns true if successful.
	// 'player' denotes which player (true = X, false = O)
	public boolean putPiece(int x, int y, boolean player){
//		System.out.println("Playing piece");

		// If board position is not empty, return false
		if(this.board[x][y] != '-'){
			//System.out.println("not empty");
			return false;
		}

		if(player){
			//System.out.println("playing x");
			this.board[x][y] = 'X';
		}else{
			//System.out.println("playing o");
			this.board[x][y] = 'O';
		}

		return true;
	}

	public int getSize(){
		return size;
	}

	public char[][] getBoard(){
		return board;
	}

	public void setBoard(char[][] b){
		for(int i = 0; i < this.size; i++){
			for(int j = 0; j < this.size; j++){
				this.board[i][j] = b[i][j];
			}
		}
	}
}
