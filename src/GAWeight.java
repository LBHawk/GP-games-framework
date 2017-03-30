import java.util.Random;

public class GAWeight{
	private double netStones;
	private double goodLibs;
	private double badLibs;
	private double goodAtari;
	private double badAtari;
	private Random r;
	private double score;

	public GAWeight(){
		r = new Random();
		netStones = r.nextDouble() * 2 - 1;
		goodLibs = r.nextDouble() * 2 - 1;
		badLibs = r.nextDouble() * 2 - 1;
		goodAtari = r.nextDouble() * 2 - 1;
		badAtari = r.nextDouble() * 2 - 1;
	}

	public void resetScore(){
		score = 0.0;
	}

	public double getNetStones(){
		return netStones;
	}

	public double getGoodLibs(){
		return goodLibs;
	}

	public double getBadLibs(){
		return badLibs;
	}

	public double getGoodAtari(){
		return goodAtari;
	}

	public double getBadAtari(){
		return badAtari;
	}

	public double getScore(){
		return score;
	}

	public void incScore(double val){
		score += val;
	}

	public double[] getAllWeights(){
		double[] weights = {netStones, goodLibs, badLibs, goodAtari, badAtari};

		return weights;
	}

	public void setAllWeights(double [] weights){
		netStones = weights[0];
		goodLibs = weights[1];
		badLibs = weights[2];
		goodAtari = weights[3];
		badAtari = weights[4];
	}

	public int[] findValues(Board b, boolean firstPlayer){
		int temp = 0;
		// 0: netStone, 1: goodLibs, 2: badLibs, 3: goodAtari, 4: badAtari
		int[] finalVals = new int[5];
		char[][] board = b.getBoard();

		for(int i = 0; i < 5; i++){
			finalVals[i] = 0;
		}

		char goodChar = ' ';
		char badChar = ' ';
		if(firstPlayer){
			goodChar = 'O';
			badChar = 'X';
		}else{
			goodChar = 'X';
			badChar = 'O';
		}

		for(int i = 0; i < b.getSize(); i++){
			for(int j = 0; j < b.getSize(); j++){

				// NetStones
				if (board[i][j] == goodChar){
					// NetStones
					finalVals[0]++;

					// Goodlibs
					if(i < b.getSize()-1 && board[i+1][j] == '-'){
						finalVals[1]++;
					}
					if(i > 0 && board[i-1][j] == '-'){
						finalVals[1]++;
					}
					if(j < b.getSize()-1 && board[i][j+1] == '-'){
						finalVals[1]++;
					}
					if(j > 0 && board[i][j-1] == '-'){
						finalVals[1]++;
					}

					// GoodAtari
					if(checkAtari(board, i, j, goodChar)){
						finalVals[3]++;
					}

				}else if(board[i][j] == badChar){
					// NetStones
					finalVals[0]--;

					// Badlibs
					if(i < b.getSize()-1 && board[i+1][j] == '-'){
						finalVals[2]++;
					}
					if(i > 0 && board[i-1][j] == '-'){
						finalVals[2]++;
					}
					if(j < b.getSize()-1 && board[i][j+1] == '-'){
						finalVals[2]++;
					}
					if(j > 0 && board[i][j-1] == '-'){
						finalVals[2]++;
					}

					// BadAtari
					if(checkAtari(board, i, j, badChar)){
						finalVals[4]++;
					}
				}
			}
		}

		return finalVals;
	}

	private boolean checkAtari(char[][] board, int i, int j, char current){
		int libs = 0;

		if(i < board.length - 1 && (board[i+1][j] == '-' || board[i+1][j] == current)){
			libs++;
		}
		if(i > 0 && (board[i-1][j] == '-' || board[i-1][j] == current)){
			libs++;
		}
		if(j < board.length - 1 && (board[i][j+1] == '-' || board[i][j+1] == current)){
			libs++;
		}
		if(j > 0 && (board[i][j-1] == '-' || board[i][j-1] == current)){
			libs++;
		}

		if(libs == 1){
			return true;
		}else{
			return false;
		}

	}
}
