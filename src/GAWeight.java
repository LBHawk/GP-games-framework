import java.util.Random;

public class GAWeight{
	private double netStones; 	//netBridges 	
	private double goodLibs; 	//goodConnected
	private double badLibs; 	//badConnected
	private double goodAtari; 	//goodDeepest
	private double badAtari; 	//badDeepest
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

	public int[] findValues(Board b, boolean firstPlayer, String gameType){
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

		if(gameType.equals("go")){
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
		}else if(gameType.equals("hex")){
			for(int i = 0; i < b.getSize(); i++){
				for(int j = 0; j < b.getSize(); j++){
					if(board[i][j] == goodChar){
						finalVals[0] += findBridges(board, i, j);
					}else if(board[i][j] == badChar){
						finalVals[0] -= findBridges(board, i, j);
					}
				}
			}

			if(goodChar == 'X'){
				finalVals[1] = findConnected(board, 'X');
				finalVals[2] = findConnected(board, 'O');
				finalVals[3] = findDeepest(board, true);
				finalVals[4] = findDeepest(board, false);
			}else{
				finalVals[1] = findConnected(board, 'O');
				finalVals[2] = findConnected(board, 'X');
				finalVals[3] = findDeepest(board, false);
				finalVals[4] = findDeepest(board, true);
			}
		}

		return finalVals;
	}

	private int findConnected(char[][] b, char piece){
		boolean[][] connected = new boolean[b.length][b.length];

		int maxConn = 0;
		int currConn = 0;

		// Iterate through entire board looking for occupied spaces
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b.length; j++){
				// We find an occupied space that isn't connected to an explored region
				if(b[i][j] == piece && !connected[i][j]){
					currConn = 1;
					connected[i][j] = true;

					// First, flood left and right in the region
					int x = i;
					int ymin = j;
					boolean canLeft, canRight;
					//canUp = (x > 0);
					//canDown = (x < b.length - 1);

					canLeft = (ymin > 0);
					while(canLeft && b[x][ymin-1] == piece && !connected[x][ymin-1]){
						connected[x][ymin-1] = true;
						currConn++;
						ymin--;
						canLeft = (ymin > 0);
					}

					int ymax=j;
					canRight = (ymax < b.length - 1);
					while(canRight && b[x][ymax+1] == piece && !connected[x][ymax+1]){
						connected[x][ymax+1] = true;
						currConn++;
						ymax++;
						canRight = (ymax < b.length - 1);
					}

					// Check for extensions of the region by row
					boolean rowConn = true;
					while(rowConn && x < b.length - 1){
						x++;
						rowConn = false;

						// Check for connections to prev row where possible
						if(ymax < b.length - 1){ ymax++; }
						int newmin = 999;
						int newmax = -1;
						for(int a = ymin; a < ymax; a++){
							// The piece in this row is connected to previous row
							if(b[x][a] == piece && 
									(connected[x-1][a] || (a < b.length - 1 && connected[x-1][a+1]))){

								rowConn = true;
								if(a < newmin){ newmin = a; }
								if(a > newmax){ newmax = a; }

								if(!connected[x][a]){
									connected[x][a] = true;
									currConn++;

									// Check the pieces in previous row for connection.
									// Necessary in case of a structure which loops up
									// (i.e. the pieces are only connected from below)
									if(b[x-1][a] == piece && !connected[x-1][a]){
										connected[x-1][a] = true;
										currConn++;
									}
									if(a < b.length - 1 && b[x-1][a+1] == piece && !connected[x-1][a+1]){
										connected[x-1][a+1] = true;
										currConn++;
									}
								}
							}
						}

						ymin = newmin;
						ymax = newmax;							
					}

					if(currConn > maxConn){
						maxConn = currConn;
					}
				}					
			}
		}

		return maxConn;
	}

	private int findDeepest(char[][] b, boolean white){
		boolean[][] connected = new boolean[b.length][b.length];

		if(white){
			int currentLayer = 0;
			// Set first column to connected = true
			for(int i = 0; i < b.length; i++){
				if(b[i][0] == 'X'){
					connected[i][0] = true;
					currentLayer++;
				}
			}

			int col = 1;
			while(currentLayer > 0 && col < b.length){
				currentLayer = 0;

				for(int i = 0; i < b.length; i++){
					if(!connected[i][col] && (connected[i][col-1] || (i < b.length - 1 && connected[i+1][col]))){
						connected[i][col] = true;
						currentLayer++;
						int j = i;
						while(j > 0 && b[j-1][col] == 'X'){
							j--;
							connected[j][col] = true;
							currentLayer++;
						}

						j = i;
						while(j < b.length - 1 && b[j+1][col] == 'X'){
							j++;
							connected[j][col] = true;
							currentLayer++;
						}
					}
				}

				col++;
			}

			return b.length - col;
		}else{

			int currentLayer = 0;
			// Set first row to connected = true
			for(int i = 0; i < b.length; i++){
				if(b[0][i] == 'O'){
					connected[0][i] = true;
					currentLayer++;
				}
			}

			int row = 1;
			while(currentLayer > 0 && row < b.length){
				currentLayer = 0;

				for(int i = 0; i < b.length; i++){
					if(!connected[row][i] && (connected[row-1][i] || (i < b.length - 1 && connected[row-1][i+1]))){
						connected[row][i] = true;
						currentLayer++;
						int j = i;
						while(j > 0 && b[row][j-1] == 'O'){
							j--;
							connected[row][j] = true;
							currentLayer++;
						}

						j = i;
						while(j < b.length - 1 && b[row][j+1] == 'O'){
							j++;
							connected[row][j] = true;
							currentLayer++;
						}
					}
				}

				row++;
			}

			return b.length - row;

		}
	}

	private int findBridges(char [][] board, int i, int j){
		char start = board[i][j];
		int bridges = 0;

		// Checks if we can check board index without going OOB
		boolean canUp = (i > 0);
		boolean canDown = (i < board.length - 1);
		boolean canLeft = (j > 0);
		boolean canRight = (j < board.length - 1);

		if(start == 'X'){
			if(canUp && canRight){
				// Check dir above
				if(i > 1 && board[i-2][j+1] == 'X' && board[i-1][j] == '-' && board[i-1][j+1] == '-'){
					bridges++;
				}

				// Check up and right
				if(j < board.length - 2 && board[i-1][j+2] == 'X' && board[i-1][j+1] == '-' && board[i][j+1] == '-'){
					bridges++;
				}
			}

			if(canDown && canRight){
				// Check down and right
				if(j < board.length - 2 && board[i+1][j+1] == 'X' && board[i+1][j] == '-' && board[i][j+1] == '-'){
					bridges++;
				}
			}

		}else if(start =='O'){
			if(canDown && canRight){
				// Check down and right
				if(j < board.length - 2 && board[i+1][j+1] == 'O' && board[i+1][j] == '-' && board[i][j+1] == '-'){
					bridges++;
				}

				// Check dir down
				if(canLeft){
					if(i < board.length - 2 && board[i+2][j-1] == 'O' && board[i+1][j-1] == '-' && board[i+1][j] == '-'){
						bridges++;
					}
				}
			}

			if(canDown && canLeft){
				// Check down and left
				if(j > 1 && board[i+1][j-2] == 'O' && board[i][j-1] == '-' && board[i+1][j-1] == '-'){
					bridges++;
				}
			}
		}

		return bridges;

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
