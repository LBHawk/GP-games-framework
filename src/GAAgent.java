import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class GAAgent extends GameAgent{
	private Game game;
	private boolean firstPlayer;
	private Random r;
	private double explorationConstant = Math.sqrt(2); // C value for MCTS
	private String gameType;
	private HashMap<Integer, GAWeight> weights; 	// Population for GA
	private int numGaWeights = 10; 					// Population size for GA
	private int iterationTrack;
	private double hueristic = 0.3; 				// The amount of "weight" the heuristic analysis is given
	private int iterations;

	public GAAgent(String game, boolean firstPlayer){
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
		//if(this.firstPlayer){
		//	explorationConstant = Math.sqrt(2.0);
		//}else{
		//	explorationConstant = 2 * Math.sqrt(2.0);
		//}

		// Initialize population for GA
		weights = new HashMap<Integer, GAWeight>();
		GAWeight temp;
		for(int i = 0; i < numGaWeights; i++){
			temp = new GAWeight();
			weights.put(i, temp);
		}

		iterationTrack = 0;
	}

	@Override
	protected int getIterations(){
		return this.iterations;
	}

	// Evolve the agents.  Because of the variability of random playouts,
	// we only remove/replace the bottom two performing agents.  Both are
	// replaced with a crossover of the top two performing agents, but one
	// has a low mutation chance while the other has a much higher one.
	private void evolveWeights(){
		GAWeight[] agents = new GAWeight[numGaWeights];

		double mutChance = 0.2; 	// Mutation probability during evo

		// Scores will be between 0 and 1, we can initialize to -1
		int firstWorst = -1;
		int secondWorst = -1;
		int firstBest = -1;
		int secondBest = -1;

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		// Find first worst/best agents
		for(int i = 0; i < numGaWeights; i++){
			agents[i] = new GAWeight();
			agents[i] = weights.get(i);
			if(agents[i].getScore() < min){
				firstWorst = i;
				min = agents[i].getScore();
			}

			if(agents[i].getScore() > max){
				firstBest = i;
				max = agents[i].getScore();
			}
		}

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;

		// Find second worst/best agents
		for(int i = 0; i < numGaWeights; i++){
			agents[i] = weights.get(i);
			if(agents[i].getScore() < min && i != firstWorst){
				secondWorst = i;
				min = agents[i].getScore();
			}

			if(agents[i].getScore() > max && i != firstBest){
				secondBest = i;
				max = agents[i].getScore();
			}
		}

		// Create new agents to take place of worst performing agents.
		// New agents are a crossover of 2 best agents
		GAWeight newAgent;
		double[] firstWeights = agents[firstBest].getAllWeights();
		double[] secondWeights = agents[secondBest].getAllWeights();
		double[] tempWeights = new double[5];
		for(int i = 0; i < 2; i++){
			newAgent = new GAWeight();

			for(int j = 0; j < 5; j++){

				double temp = (firstWeights[j] + secondWeights[j]) / 2;
				// Randomly mutate
				if(r.nextDouble() < mutChance + (i * mutChance * 2)){ // bump up from 0.2 to 0.6
					temp += r.nextDouble();
					temp = temp / 2;
				}

				tempWeights[j] = temp;
			}
			newAgent.setAllWeights(tempWeights);

			// Replace in hashmap
			if(i == 0){
				weights.put(firstWorst, newAgent);
			}else{
				weights.put(secondWorst, newAgent);
			}
		}

		// Reset all the scores before next round of evolution
		GAWeight temp;
		for(int i = 0; i < numGaWeights; i++){
			temp = new GAWeight();
			temp = weights.get(i);
			temp.resetScore();
			weights.put(i, temp);
		}
	}

	// Begins the MCTS process
	@Override
	protected Board makeMove(Board startingBoard, int timeAllowed, int moveNumber){
		//System.out.println("In makeMove");
		Node rootNode = new Node(startingBoard, firstPlayer);
		Board bestMoveFound = null;

		long startTime = System.currentTimeMillis();
		this.iterations = 0;

		// MCTS
		while(System.currentTimeMillis() - startTime < timeAllowed){
			select(rootNode, moveNumber);
			iterations++;
			iterationTrack = iterations % numGaWeights;
			if(iterations % (100 * numGaWeights) == 0){
				evolveWeights();
			}
			//System.out.println(iterations);
		}
		// End MCTS

		System.out.println("Made move after " + iterations + "iterations thru MCTS.");

		GAWeight[] agents = new GAWeight[numGaWeights];

		// Find best agent
		int firstBest = -1;
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < numGaWeights; i++){
			agents[i] = new GAWeight();
			agents[i] = weights.get(i);

			if(agents[i].getScore() > max){
				firstBest = i;
				max = agents[i].getScore();
			}
		}

		GAWeight agent = weights.get(firstBest);

		// Select a move, but bias the result with the best agent
		double bestScore = Double.NEGATIVE_INFINITY;
		ArrayList<Node> bestNodes = new ArrayList<Node>();
		for(Node n : rootNode.children){
			// Standard method of choosing move
			double tempBest = n.score / n.games;

			int[] huerVals = new int[5];
			huerVals = agent.findValues(n.b, this.firstPlayer, this.gameType);
			//System.out.println("UCT: " + tempBest);
			double hueristic = 0.0;

			// Calculate total hueristic score of each agent
			hueristic += (agent.getNetStones() * huerVals[0]);
			hueristic += (agent.getGoodLibs() * huerVals[1]);
			hueristic += (agent.getBadLibs() * huerVals[2]);
			hueristic += (agent.getGoodAtari() * huerVals[3]);
			hueristic += (agent.getBadAtari() * huerVals[4]);

			// Normalize hueristic for more accurate bias, Weigh hueristic by 1/3, add to original score
			hueristic = (hueristic + (n.b.getSize() * n.b.getSize())) / (2*n.b.getSize()*n.b.getSize());
			hueristic = hueristic * 0.33;
			tempBest += hueristic;

			if (tempBest > bestScore) {
				bestNodes.clear();
				bestNodes.add(n);
				bestScore = tempBest;

			} else if (tempBest == bestScore) {
				// If we found an equal node
				bestNodes.add(n);
			}

		}
		
		/*
		System.out.print("Huer vals: ");
		for(int i = 0; i < 5; i++){
			System.out.print(agent.getAllWeights()[i] + ", ");
		}
		System.out.println();
		*/

		// Get best move
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
		GAWeight agent = weights.get(Integer.valueOf(this.iterationTrack));

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
			int[] huerVals = new int[5];
			huerVals = agent.findValues(s.b, this.firstPlayer, this.gameType);
			//System.out.println("UCT: " + tempBest);
			double hueristic = 0.0;

			hueristic += (agent.getNetStones() * huerVals[0]);
			hueristic += (agent.getGoodLibs() * huerVals[1]);
			hueristic += (agent.getBadLibs() * huerVals[2]);
			hueristic += (agent.getGoodAtari() * huerVals[3]);
			hueristic += (agent.getBadAtari() * huerVals[4]);

			//System.out.print("PRENORM huer: " + hueristic);
			hueristic = (hueristic + (n.b.getSize() * n.b.getSize())) / (2*n.b.getSize()*n.b.getSize());
			//System.out.println(" POSTNORM huer: " + hueristic);
		
			hueristic = hueristic * 0.33;
			
			// Add the UCT score of the node to the agent's score totals
			agent.incScore(tempBest);

			tempBest += hueristic;
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

