import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.IOException;

public class Node implements Comparable<Node>{
	public double score;
	public double games;
	public ArrayList<Node> unvisitedChildren;
	public ArrayList<Node> children;
	public ArrayList<Node> prunedChildren;
	public Node parent;
	public Board b;
	public boolean firstPlayer;

	// Creates root nodes
	public Node(Board b, boolean firstPlayer) {
		children = new ArrayList<Node>();
		prunedChildren = new ArrayList<Node>();
		this.firstPlayer = firstPlayer;
		score = 0.0;
		this.b = b;
		this.parent = null;
	}

	// This creates non-root nodes
	public Node(Board b, Node prnt) {
		children = new ArrayList<Node>();
		parent = prnt;
		this.firstPlayer = !prnt.firstPlayer;
		score = 0.0;
		games = 1.0;
		this.b = b;
	}


	// Return the upper confidence bound of this state
	public double upperConfidenceBound(double c) {
		if(this.parent == null){
			return (score / games + c * Math.sqrt(Math.log(1) / games));
		}
		return (score / games + c * Math.sqrt(Math.log(parent.games + 1) / games));
	}

	// Update the tree with the new score.
	public void backPropagateScore(double scr) {
		this.games ++;
		this.score += scr;

		if (parent != null){
			parent.backPropagateScore(scr);
		}
	}

	// Expand this node by populating its list of unvisited child nodes.
	public void expandNode(Board currentBoard, ArrayList<Board> legalMoves) {
		unvisitedChildren = new ArrayList<Node>();
		for (int i = 0; i < legalMoves.size(); i++) {
			Node tempState = new Node(legalMoves.get(i), this);
			unvisitedChildren.add(tempState);
		}
	}

	// Remove some children from the search tree (used by annagent)
	public void prune(ArrayList<Integer> indices){
		// Sort the indices in decreasing order
		int temp;
        for (int i = 1; i < indices.size(); i++) {
            for(int j = i ; j > 0 ; j--){
                if(indices.get(j) > indices.get(j-1)){
                    temp = indices.get(j);
                    indices.set(j, indices.get(j-1));
                    indices.set(j-1, temp);
                }
            }
        }

		/*
		System.out.println("Sorted indices:");
        for(int i = 0; i < indices.size(); i++){
        	System.out.print(indices.get(i) + ", ");
		}

		try{ System.in.read(); }catch(IOException e){}
		*/

		System.out.println("pruning " + indices.size() + " moves out of "
				+ unvisitedChildren.size() + " possibilities");

		// Remove the children at each index we are given
		for(int i = indices.size() - 1; i >= 0; i--){
			prunedChildren.add(unvisitedChildren.remove(i));
		}

	}

	@Override
	public int compareTo(Node o) {
		return this.compareTo(o);
	}
}
