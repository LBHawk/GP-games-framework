import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MCTSAgent extends GameAgent{
	private Game game;
	private boolean firstPlayer;
	private Random r;
	private double explorationConstant = Math.sqrt(2);
	private String gameType;
	private int iterations;

	public MCTSAgent(String game, boolean firstPlayer){
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

	@Override
	protected void setNetwork(int boardSize){

	}
}
