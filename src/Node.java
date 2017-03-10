import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Node implements Comparable<Node>{
	public double score;
	public double games;
	public ArrayList<Node> unvisitedChildren;
	public ArrayList<Node> children;
	public Node parent;
	public Board b;
	public boolean firstPlayer;

	/**
	 * This creates the root node
	 * 
	 * @param b
	 */
	public Node(Board b, boolean firstPlayer) {
		children = new ArrayList<Node>();
		this.firstPlayer = firstPlayer;
		score = 0.0;
		this.b = b;
		this.parent = null;
	}

	/**
	 * This creates non-root nodes
	 * 
	 * @param b
	 * @param m
	 * @param prnt
	 */
	public Node(Board b, Node prnt) {
		children = new ArrayList<Node>();
		parent = prnt;
		this.firstPlayer = !prnt.firstPlayer;
		score = 0.0;
		games = 1.0;
		this.b = b;
	}

	/**
	 * Return the upper confidence bound of this state
	 * 
	 * @param c
	 *            typically sqrt(2). Increase to emphasize exploration. Decrease
	 *            to incr. exploitation
	 * @param t
	 * @return
	 */
	public double upperConfidenceBound(double c) {
		if(this.parent == null){
			return (score / games + c * Math.sqrt(Math.log(1) / games));
		}
		return (score / games + c * Math.sqrt(Math.log(parent.games + 1) / games));
	}

	/**
	 * Update the tree with the new score.
	 * 
	 * @param scr
	 */
	public void backPropagateScore(double scr) {
		this.games++;
		this.score += scr;

		if (parent != null)
			parent.backPropagateScore(scr);
	}

	/**
	 * Expand this node by populating its list of unvisited child nodes.
	 * 
	 * @param currentBoard
	 */
	public void expandNode(Board currentBoard, ArrayList<Board> legalMoves) {
		unvisitedChildren = new ArrayList<Node>();
		for (int i = 0; i < legalMoves.size(); i++) {
			Node tempState = new Node(legalMoves.get(i), this);
			unvisitedChildren.add(tempState);
		}
	}

	@Override
	public int compareTo(Node o) {
		return this.compareTo(o);
	}
}
