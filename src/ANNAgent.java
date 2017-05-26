import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationStep;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

// ANNAgent uses an artificial neural network to prune the game tree at each turn before the MCTS process
public class ANNAgent extends GameAgent{
	private Game game; 										// For use in MCTS
	private boolean firstPlayer;							// Tracks which player
	private Random r; 		
	private double explorationConstant = Math.sqrt(2.0); 	// C value for MCTS, standard is sqrt(2)
	private String gameType; 								
	private BasicNetwork network;							// Reference to ANN being used
	private int iterations; 								// Tracks iterations taken each turn

	public ANNAgent(String game, boolean firstPlayer){
		super(game, firstPlayer);
		gameType = game;
		switch(game){
			case "go": 		this.game = new GoGame();
							System.out.println("made go game in agent");
							break;
			case "hex": 	this.game = new HexGame();
							break;
			case "sprouts":	//this.game = new SproutsGame();
							break;
		}
		r = new Random();
		this.firstPlayer = firstPlayer;
		System.out.println("MCTS const");
	}

	// For data output
	@Override
	protected int getIterations(){
		return this.iterations;
	}

	// Handles the ANN assignment at the creation of the agent
	@Override
	protected void setNetwork(int boardSize){
		String path = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/agents/";
		String netName = "";
		if(this.gameType.equals("go")){
			switch(boardSize){
				case 4:  netName = "4Go0.ser";
					     break;
				case 5:  netName = "5Go0.ser";
						 break;
				case 7:  netName = "7Go0.ser";
						 break;
				case 9:  netName = "9Go0.ser";
						 break;
				case 11: netName = "11Go0.ser";
						 break;
				case 13: netName = "13Go0.ser";
 						 break;
				case 15: netName = "15Go93.ser";
						 break;
			}
		}else if (this.gameType.equals("hex")){
			switch(boardSize){
				case 9:   netName = "9hex0.ser";
					      break;
				case 11:  netName = "11hex0.ser";
						  break;
				case 14:  netName = "14hex0.ser";
						  break;
			}
		}

		try {
			File file = new File(path + "" + netName);
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.network = (BasicNetwork)in.readObject();
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

	// Begins the move selection process.  This is what is called from the controller, but this uses the select method to begin the actual mcts
	@Override
	protected Board makeMove(Board startingBoard, int timeAllowed, int moveNumber){
		//System.out.println("In makeMove");
		// Begin making tree, essentially
		Node rootNode = new Node(startingBoard, firstPlayer);
		Board bestMoveFound = null;

		// set time/iteration tracker
		long startTime = System.currentTimeMillis();
		this.iterations = 0;

		// Ensures the root node has a list of children before beginning MCTS
		if(rootNode.unvisitedChildren == null){
			//System.out.println("null unvis");
			rootNode.expandNode(rootNode.b, game.getPossibleMoves(rootNode.b, rootNode.firstPlayer));
		}

		// Before beginning MCTS, prune the tree
		pruneNodes(rootNode, moveNumber, startingBoard.getSize());

		// MCTS
		while(System.currentTimeMillis() - startTime < timeAllowed){
			select(rootNode, moveNumber);
			iterations++;
			//System.out.println(iterations);
		}
		// MCTS done

		System.out.println("Made move after " + iterations + "iterations thru MCTS.");

		// Find child node with highest score
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

		// If multiple nodes have same score, pick one randomly
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
		double score = game.randomPlayout(foundNode.b, !foundNode.firstPlayer, moveNumber);
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

		
		// Normalize score depending on game
		if(this.gameType.equals("go")){
			int size = foundNode.b.getSize();
			score += (size * size);
			score = score / (size * size * 2);
		}else if(this.gameType.equals("hex")){
			score += 1;
			score = score / 2;
		}
		

		//System.out.println("backprop start");


		// Backpropagate results of playout.
		foundNode.backPropagateScore(score);
	}

	private Node treePolicy(Node node) {
		//System.out.println("In treePolicy");
		// Ensure the node has children to explore
		if (node.unvisitedChildren == null) {
			node.expandNode(node.b, game.getPossibleMoves(node.b, node.firstPlayer));
		}

		// The node has no children (we have reached a terminal pos)
		if(!node.unvisitedChildren.isEmpty()){
			return node;
		}

		// Find best child to expand on
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

	// Removes a number of nodes from the search tree prior to MCTS
	private void pruneNodes(Node root, int moveNumber, int boardSize){
		// Copy list of unexplored nodes
		ArrayList<Node> unexplored = new ArrayList<Node>();
		for(int i = 0; i < root.unvisitedChildren.size(); i++){
			unexplored.add(root.unvisitedChildren.get(i));
		}

		// For storing estimated values of each child
		HashMap<Integer, Double> estValues = new HashMap<Integer, Double>(root.unvisitedChildren.size());
		// To hold network-friendly board representations
		double[][] nodeRep = new double[unexplored.size()][boardSize * boardSize];

		// Convert each board to a network-friendly representation, store in nodeRep
		int count = 0;
		for(int a = 0; a < unexplored.size(); a++){
			count = 0;
			for(int i = 0; i < root.b.getSize(); i++){
				for(int j = 0; j < root.b.getSize(); j++){
					if(unexplored.get(a).b.getBoard()[i][j] == 'X'){
						nodeRep[a][count] = 1.0;
					}else if(unexplored.get(a).b.getBoard()[i][j] == 'O'){
						nodeRep[a][count] = -1.0;
					}else{
						nodeRep[a][count] = 0.0;
					}
					count++;
				}
			}
		}

		// Estimate the value of each child using the network
		for(int i = 0; i < unexplored.size(); i++){
			MLData temp = network.compute(new BasicMLData(nodeRep[i]));
			estValues.put(i, temp.getData(0));
			//System.out.println(temp.getData(0));
			
		}
		//System.out.println("values above");
		//try{ System.in.read(); }catch(IOException e){ }

		// Determine the number of nodes to prune from the children
		// y = 50 - 48.6*(1 - e^(-0.0244*x))
		// %50 at 0 -> %10 at 70 -> %4 at 125 -> %2 at 200 -> %1 at 250
		double percentPrune = (50.0 - (48.6 * (1 - Math.exp(-0.0244 * moveNumber)))) / 100.0;
		int numToPrune = (int)(unexplored.size() * percentPrune);
		ArrayList<Integer> pruningIndices = new ArrayList<Integer>();

		// Find the indices of the least valuable children (necessary to remove in proper order)
		for(int i = 0; i < numToPrune; i++){
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			int tracker = 0;
			for(int j = 0; j < root.unvisitedChildren.size(); j++){
				double nodeVal = estValues.get(Integer.valueOf(j));
				if(!firstPlayer){
					if(nodeVal < min && !pruningIndices.contains(j)){
						tracker = j;
						min = estValues.get(j);
					}
				}else{
					if(nodeVal > max && !pruningIndices.contains(j)){
						tracker = j;
						max = estValues.get(j);
					}
				}
			}
			//estValues.remove(Integer.valueOf(tracker)); 
			pruningIndices.add(tracker);
		}

		// Removes the undesirable children from unvisited list
		root.prune(pruningIndices);


	}


	@Override
	protected boolean checkMoveIntegrity(Board first, Board second){
		return super.checkMoveIntegrity(first, second);
	}
}

