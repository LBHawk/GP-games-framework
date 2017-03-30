// Actually resolves the board.  We do this by iterating through columns left to right
	// looking for empty spots and replacing pieces as we go.
	private Board resolveBoard(Board board, int boardSize, int x, int y, char floodChar){
		// Copy board to ease swapping/returning process
		char[][] resolvedBoard = new char[boardSize][boardSize];
		for(int i = 0; i < boardSize; i++){
			for(int j = 0; j < boardSize; j++){
				resolvedBoard[i][j] = board.getBoard()[i][j];
			}
		}

		// Set the surrounding piece
		char endChar; 	// Other player's piecetype
		if(floodChar == 'X'){
			endChar = 'O';
		}else{
			endChar = 'X';
		}

		//System.out.println("ENDCHAR: " + endChar);

		// Queue holds the pieces we have visited and need to expand on
		Queue<Pair> queue = new Queue<Pair>();

		// Set original coords, set east/west to 0/0 for now (we set on each loop next)
		Pair orig = new Pair(x,y);
		Pair w = new Pair(0, 0);
		Pair e = new Pair(0, 0);

		queue.enqueue(orig);

		//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
		//board.printBoard();

		while(!queue.isEmpty()){
			//System.out.println("1");
			Pair p = queue.dequeue();
			//System.out.println(p.getX() + ", " + p.getY());
			// Set east/west to starting pair
			w.setPair(p);
			e.setPair(p);

			// Move the x coord of the first pair to the left until we hit a boundary,
			// the end char, or an empty space.  If empty, return.
			while(w.getX() >= 0 && board.getBoard()[w.getX()][w.getY()] != endChar){
				//System.out.println("12");
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[w.getX()][w.getY()] == '-'){
					//System.out.println("Found176 - at " + left + "," + y);
					return board;
				}
				w.subX();
			}
			w.addX();
			
			// Move the x coord of the second pair to the right until we hit a boundary,
			// the end char, or an empty space.  If empty, return.
			while(e.getX() < boardSize && board.getBoard()[e.getX()][e.getY()] != endChar){
				//System.out.println("13");
				// If we find an empty spot, doesn't need to be resolved
				if(board.getBoard()[e.getX()][e.getY()] == '-'){
					//System.out.println("LINE 205");
					return board;
				}
				e.addX();
			}
			e.subX();

			//queue.enqueue(new Pair(w.getX(), w.getY() + 1));

			// Iterate through all nodes between w,e on x-axis.
			while(w.getX() <= e.getX()){
				//System.out.println("2");
				//System.out.println("looping: (" + w.getX() + ", " + w.getY() + ")");
				resolvedBoard[w.getX()][w.getY()] = 'A';
				
				// Ensure we don't get an indexOOB
				if(w.getY() + 1 < boardSize){
					if(resolvedBoard[w.getX()][w.getY()+1] == floodChar){
						queue.enqueue(new Pair(w.getX(), w.getY() + 1));
					}

					if(resolvedBoard[w.getX()][w.getY()+1] == '-'){
						return board;
					}
				}

				// Ensure we don't get an indexOOB
				if(w.getY() - 1 >= 0){
					if(resolvedBoard[w.getX()][w.getY()-1] == floodChar){
						queue.enqueue(new Pair(w.getX(), w.getY() - 1));
					}

					if(resolvedBoard[w.getX()][w.getY()-1] == '-'){
						return board;
					}
				}
				w.addX();
			}
		}

		Board newBoard = new Board(boardSize);
		for(int i = 0; i < boardSize; i++){
			for(int j = 0; j < boardSize; j++){
				if(resolvedBoard[i][j] == 'A'){
					resolvedBoard[i][j] = '-';
				}
			}
		}

		newBoard.setBoard(resolvedBoard);

		//System.out.println("------RESOLVED--------");
		//newBoard.printBoard();
		//System.out.println("------RESOLVED--------");

		return newBoard;
	}
