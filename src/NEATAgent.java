import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.util.simple.EncogUtility;
import org.encog.engine.network.activation.ActivationSigmoid;

public class NEATAgent extends GameAgent{
	private Game game;
	private boolean firstPlayer;
	private Random r;
	private double explorationConstant = Math.sqrt(2);
	private String gameType;
	private NEATNetwork network;
	private int iterations;

	public NEATAgent(String game, boolean firstPlayer){
		super(game, firstPlayer);
		gameType = game;
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
		/*if(this.firstPlayer){
		  explorationConstant = Math.sqrt(2.0);
		  }else{
		  explorationConstant = 2 * Math.sqrt(2.0);
		  }*/
		System.out.println("MCTS const");
	}

	@Override
	protected int getIterations(){
		return this.iterations;
	}

	@Override
	protected void setNetwork(int boardSize){
		String path = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/agents/";
		String netName = "";
		if(boardSize == 7){
			netName = "7NEATGo.ser";
		}else if(boardSize == 5){
			netName = "5NEATGo.ser";
		}else if(boardSize == 9){
			netName = "9NEATGo.ser";
		}else if(boardSize == 4){
			netName = "4NEATGo.ser";
		}else if(boardSize == 11){
			netName = "11NEATGo.ser";
		}else if(boardSize == 13){
			netName = "";
		}else if(boardSize == 15){
			netName = "15Go93.ser";
		}

		try {
			File file = new File(path + "" + netName);
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.network = (NEATNetwork)in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i) {
			i.printStackTrace();
			System.exit(-1);
			return;
		}catch(ClassNotFoundException c) {
			System.out.println("BasicNetwork class not found");
			c.printStackTrace();
			System.exit(-1);
			return;
		}
	}

	@Override
	protected Board makeMove(Board startingBoard, int timeAllowed, int moveNumber){
		//System.out.println("In makeMove");
		Node rootNode = new Node(startingBoard, firstPlayer);
		Board bestMoveFound = null;

		long startTime = System.currentTimeMillis();
		this.iterations = 0;

		while(System.currentTimeMillis() - startTime < timeAllowed){
			select(rootNode, moveNumber);
			iterations++;
			//System.out.println(iterations);
		}

		System.out.println("Made move after " + iterations + "iterations thru MCTS.");

		double bestScore = Double.NEGATIVE_INFINITY;
		ArrayList<Node> bestNodes = new ArrayList<Node>();
		for(Node n : rootNode.children){
			double tempBest = n.score / n.games;

			if (tempBest > bestScore) {
				bestNodes.clear();
				bestNodes.add(n);
				bestScore = tempBest;

			} else if (tempBest == bestScore) {
				// If we found an equal node
				bestNodes.add(n);
			}

		}

		Node move = bestNodes.get(r.nextInt(bestNodes.size()));
		System.out.println("+++++++ " + (move.score / move.games) + " / " + move.games);
		return move.b;
	}

	// FOR TRAINING PURPOSES
	@Override
	protected double estimateNodesScore(Board board, int iterations, int moveNumber){
		Node root = new Node(board, firstPlayer);

		for(int i = 0; i < iterations; i++){
			select(root, moveNumber);
			//System.out.println("~" + i);
		}

		return (root.upperConfidenceBound(explorationConstant) + 20) / 40;
	}

	private double biasedPlayout(Node parent, boolean firstPlayer, int moveNumber){
		boolean turn = firstPlayer; // Tracks which piece to play

		ArrayList<Board> moves = new ArrayList<Board>();
		int boardSize = parent.b.getSize();
		Board current = new Board(boardSize);
		current.setBoard(parent.b.getBoard());
		//boardSize *= boardSize;
		double[] scores;
		MLDataSet testingSet;

		//double[][] input = new double[1][1];
		char[] boardRep = new char[boardSize * boardSize];

		int asdf = 0;
		while(!game.gameFinished(current, moveNumber)){
			moves = game.getPossibleMoves(current, turn);
			if(moves.size() == 1){
				current = moves.get(0);
			}else{
				scores = new double[boardSize * boardSize];

				int count = 0;
				for(int i = 0; i < boardSize; i++){
					for(int j = 0; j < boardSize; j++){
						boardRep[count] = current.getBoard()[i][j];
						count++;
					}
				}

				double[] friendlyBoard = new double[boardSize * boardSize];

				for(int i = 0; i < boardRep.length; i++){
					if(boardRep[i] == 'X'){
						friendlyBoard[i] = 1.0;
					}else if(boardRep[i] == 'O'){
						friendlyBoard[i] = -1.0;
					}else{
						friendlyBoard[i] = 0.0;
					}
				}

				//input[0] = boardRep;
				//trainingSet = new BasicMLDataSet(input, empty);

				MLData temp = network.compute(new BasicMLData(friendlyBoard));
				scores = temp.getData();

				count = 0;
				for(int i = 0; i < boardSize; i++){
					for(int j = 0; j < boardSize; j++){
						//System.out.print(scores[count] + ", ");
						if(current.getBoard()[i][j] != '-'){
							scores[count] = -1.0;
						}
					}
				}

				double max = Double.NEGATIVE_INFINITY;
				int tracker = 0;

				//System.out.println("Score: " + scores.length);
				for(int i = 0; i < scores.length; i++){
					//System.out.println("Score: " + scores[i]);
					if(scores[i] > max && i < boardSize * boardSize - 1){
						max = scores[i];
						tracker = i;
					}
				}

				asdf++;
				tracker++;

				// If putPiece fails, pick a random move
				if(!current.putPiece(tracker/boardSize, tracker%boardSize, turn)){
					
					//System.out.println(asdf);
					current = moves.get(r.nextInt(moves.size()));
				}
			}

			current = game.resolveBoard(current, current.getSize());
			turn = !turn;
			moveNumber++;	

			//System.out.println(moves.size());
			//System.out.println("!!!!!!!!!!!!!");
			//board.printBoard();
		}

		return game.calculateScore(current);

		//System.out.println("randomPlay done");
	}

	@Override
	protected void select(Node currentNode, int moveNumber) {
		//System.out.println("In Select");
		// Begin tree policy. Traverse down the tree and expand. Return
		// the new node or the deepest node it could reach. Return too
		// a board matching the returned node.
		Node foundNode = treePolicy(currentNode);

		// Expand foundNode to random unvisited node
		if(foundNode.unvisitedChildren == null){
			//System.out.println("null unvis");
			foundNode.expandNode(foundNode.b, game.getPossibleMoves(foundNode.b, foundNode.firstPlayer));
		}
		//System.out.println("Added child out of " + foundNode.unvisitedChildren.size() + " possibilities");
		foundNode.children.add(foundNode.unvisitedChildren.remove(r.nextInt(foundNode.unvisitedChildren.size())));
		//System.out.println("children size: " + foundNode.children.size());

		// Run a random playout from the found node until the end of the game.
		//System.out.println("starting playout");
		double score = this.biasedPlayout(foundNode, !foundNode.firstPlayer, moveNumber);
		// If the agent is player 2, negative scores are good for p2
		if(this.firstPlayer){
			score = score * -1;
		}

		// Set score to 1 for win, 0 for loss, 0.5 for draw for use in UCT
		/*if(score > 0){
		  score = 1;
		  }else if (score < 0){
		  score = 0;
		  }else{
		  score = 0.5;
		  }*/


		int size = foundNode.b.getSize();
		score += (size * size);
		score = score / (size * size * 2);


		//System.out.println("backprop start");


		// Backpropagate results of playout.
		foundNode.backPropagateScore(score);
	}

	private Node treePolicy(Node node) {
		//System.out.println("In treePolicy");
		if (node.unvisitedChildren == null) {
			node.expandNode(node.b, game.getPossibleMoves(node.b, node.firstPlayer));
		}

		if(!node.unvisitedChildren.isEmpty()){
			return node;
		}

		ArrayList<Node> bestNodes = new ArrayList<Node>();
		bestNodes = findBest(node, explorationConstant);

		if (bestNodes.size() == 0) {
			// The node must be a terminal node (first iteration of this node)
			//System.out.println("bestnodes 0");
			return node;

		}

		// Select random node from bestNodes
		Node finalNode = bestNodes.get(r.nextInt(bestNodes.size()));
		return finalNode;

	}

	public ArrayList<Node> findBest(Node n,	double explorationConstant) {
		double bestValue = Double.NEGATIVE_INFINITY;
		ArrayList<Node> bestNodes = new ArrayList<Node>();
		Queue<Node> nodes = new Queue<Node>();
		nodes.enqueue(n);

		// Iterate through queue, adding children to queue as we go.
		// Update list of best options as we go as well.
		while(!nodes.isEmpty()){
			Node s = nodes.dequeue();
			for(Node node : s.children){
				if (node.unvisitedChildren == null) {
					node.expandNode(node.b, game.getPossibleMoves(node.b, !node.parent.firstPlayer));
				}
				nodes.enqueue(node);
				//System.out.println("enqueued new item, queue size:" + nodes.size());
			}
			double tempBest = s.upperConfidenceBound(explorationConstant);
			//System.out.println(tempBest);

			if ((tempBest > bestValue) && !s.unvisitedChildren.isEmpty()) {
				// If we found a better node
				bestNodes.clear();
				bestNodes.add(s);
				bestValue = tempBest;
				//System.out.println("found new best");
			} else if ((tempBest == bestValue) && !s.unvisitedChildren.isEmpty()) {
				// If we found an equal node
				//System.out.println("found new best");
				bestNodes.add(s);
			}
			//System.out.println("!!!!!!!!!!!!!!");

		}
		//System.out.println("bestNodes size: " + bestNodes.size());
		//System.out.println(bestValue);
		return bestNodes;
	}


	@Override
	protected boolean checkMoveIntegrity(Board first, Board second){
		return super.checkMoveIntegrity(first, second);
	}
}

