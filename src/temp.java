		

		char[][] resolvedBoard = new char[boardSize][boardSize];
		for(int i = 0; i < boardSize; i++){
			for(int j = 0; j < boardSize; j++){
				resolvedBoard[i][j] = board.getBoard()[i][j];
			}
		}
		//resolvedBoard.setBoard(board.getBoard());
		char endChar; 	// Other player's piecetype
		if(floodChar == 'X'){
			endChar = 'O';
		}else{
			endChar = 'X';
		}
		int left = x;
		int right = x;
		int up = y;
		int down = y;

		// Move left until we hit an edge or the other player's piece
		while(left >= 0 && board.getBoard()[left][y] != endChar){
			// If we find an empty spot, doesn't need to be resolved
			if(board.getBoard()[left][y] == '-'){
				//System.out.println("Found176 - at " + left + "," + y);
				return board;
			}
			left--;
		}
		left++;

		// Move right until we hit an edge or the other player's piece
		while(right < boardSize && board.getBoard()[right][y] != endChar){
			// If we find an empty spot, doesn't need to be resolved
			if(board.getBoard()[right][y] == '-'){
				//System.out.println("Found187 - at " + right + "," + y);
				return board;
			}
			right++;
		}
		right--;
		

		for(;left <= right; left++){
			up = y;
			down = y;
			//System.out.println("l:"+left + "u:"+up);
			while(up >= 0 && board.getBoard()[left][up] != endChar){
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[left][up] == '-'){
					//System.out.println("Found201 - at " + left + "," + up);
					return board;
				}
				up--;
			
			}
			up++;

			while(down < boardSize && board.getBoard()[left][down] != endChar){
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[left][down] == '-'){
					//System.out.println("Found213 - at " + left + "," + down);
					return board;
				}
				down++;
			}
			down--;

			for(;up <= down; up++){
				//System.out.println("swapping: " + left + ", " + up);
				resolvedBoard[left][up] = '-';
			}
		}

		//System.out.println("Made it");

		Board newBoard = new Board(boardSize);
		newBoard.setBoard(resolvedBoard);

		//System.out.println("RESOLVED");
		//newBoard.printBoard();

		return newBoard;
