import java.util.Random;
import java.util.ArrayList;
import java.io.IOException;
import java.io.*;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.util.simple.EncogUtility;
import org.encog.engine.network.activation.ActivationSigmoid;

public class NEATTrainer{
	public static Board[] boardSet;
	public static double[][] fullSet;
	//public static double[][] trainingSet;
	//public static double[][] testingSet;
	public static double[][] ideal;
	public static int BOARDSIZE;
	public static int SIZE = 2500;
	public static final double TRAININGRATIO = 0.75;
	public static final int INCREMENT = 5;
	public static Game game;
	public static GameAgent agent;

	public static void main(String[] args){
		BOARDSIZE = Integer.parseInt(args[0]);
		if(args[1] != null){
			SIZE = Integer.parseInt(args[1]);
		}

		boardSet = new Board[SIZE];
		for(int i = 0; i < SIZE; i++){
			boardSet[i] = new Board(BOARDSIZE);
		}
		fullSet = new double[SIZE][BOARDSIZE*BOARDSIZE];
		//trainingSet = new double[(int)(SIZE * TRAININGRATIO)][BOARDSIZE*BOARDSIZE];
		//testingSet = new double[(int)(SIZE * (1 - TRAININGRATIO))][BOARDSIZE*BOARDSIZE];
		ideal = new double[SIZE][BOARDSIZE*BOARDSIZE];
		Board tempBoard = new Board(BOARDSIZE);

		game = new GoGame();
		agent = new MCTSAgent("go", false);

		int numSizes = 100 / INCREMENT;
		int numSteps = SIZE / numSizes;
		int moveTracker = 1;
		int indexTracker = 0;
		for(int i = 0; i < numSizes; i++){
			for(int j = 0; j < numSteps; j++){
				tempBoard = game.randomBoardAfterXMoves(BOARDSIZE, moveTracker);
				boardSet[indexTracker].setBoard(tempBoard.getBoard());
				indexTracker++;
				System.out.println(i + " : " + j);
			}
			moveTracker += 4;

		}

		System.out.println("Done creating boards");

		setData();

		train();

	}

	public static void train(){
		MLDataSet trainingSet = new BasicMLDataSet(fullSet, ideal);
		//NEATPopulation pop = new NEATPopulation(BOARDSIZE * BOARDSIZE, (BOARDSIZE*BOARDSIZE) + 1, 10);
		NEATPopulation pop = new NEATPopulation(BOARDSIZE * BOARDSIZE, BOARDSIZE*BOARDSIZE, 1000);
		//pop.setNEATActivationFunction(new ActivationSigmoid());
		System.out.println(pop.getPopulationSize());
		try{ System.in.read(); }catch(IOException e){}
		//pop.setInitialConnectionDensity(1.0);// not required, but speeds training
		pop.reset();

		CalculateScore score = new TrainingSetScore(trainingSet);
		// train the neural network
		
		final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop,score);
		
		do {
			train.iteration();
			System.out.println("Epoch #" + train.getIteration() + " Error:" + train.getError()+ ", Species:" + pop.getSpecies().size());
		} while(train.getError() > 0.1 && train.getIteration() < 2000);

		NEATNetwork network = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());

		try{ System.in.read(); }catch(IOException e){}

		// test the neural network
		System.out.println("Neural Network Results:");
		EncogUtility.evaluate(network, trainingSet);
		
		Encog.getInstance().shutdown();

		serialize(network);
	}

	public static void serialize(NEATNetwork network){
		System.out.println("Serializing");
		String path = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/";
		try{
			File file = new File(path + "agents/" + BOARDSIZE + "NEATGo.ser");
			file.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(file);
         	ObjectOutputStream out = new ObjectOutputStream(fileOut);
         	out.writeObject(network);
         	out.close();
         	fileOut.close();
         	System.out.println("wrote network to: " + "/agents/" + BOARDSIZE + "NEATGo.ser");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void setData(){
		Board current;
		ArrayList<Board> children;
		double[] tempRep;

		for(int i = 0; i < SIZE; i++){
			tempRep = new double[BOARDSIZE * BOARDSIZE];
			// Initialize current parent/children
			current = boardSet[i];
			children = game.getPossibleMoves(current, false);

			// Get board in network friendly form
			tempRep = boardRep(current);

			// Add board to training set
			fullSet[i] = tempRep;

			setIdeal(i, current, tempRep, children);
			System.out.println(i);

		}
	}

	public static void setIdeal(int index, Board parent, double[] parentRep, ArrayList<Board> children){
		// One spot for each space on the board, plus one for a pass
		double[] tempIdeal = new double[parentRep.length];
		for(int i = 0; i < tempIdeal.length;i++){
			tempIdeal[i] = -1;
		}

		Board temp = new Board(parent.getSize());
		// First, go through and set all occupied spots in parent to 0.0
		// This is because these are invalid moves.
		// We add one to the index, because we have to account for the 'pass' move
		for(int i = 0; i < parentRep.length; i++){
			temp.setBoard(parent.getBoard());
			if(parentRep[i] != 0.0){
				tempIdeal[i] = 0.0;
			}else{
				// We remove suicides at the same time
				temp.putPiece(i/BOARDSIZE, i%BOARDSIZE, false);
				char currentSpot = temp.getBoard()[i/BOARDSIZE][i%BOARDSIZE];
				temp = game.resolveBoard(temp, BOARDSIZE);
				if(temp.getBoard()[i/BOARDSIZE][i%BOARDSIZE] != currentSpot){
					tempIdeal[i] = 0.0;
				}
			}
		}

		// Now perform MCTS on the parent for 5000 iterations
		Node pNode = new Node(parent, false);
		for(int i = 0; i < 5000; i++){
			agent.select(pNode, 50);
		}

		// Assign each child of root to empty spots in tempIdeal, in order
		int tracker = -2;
		for(Node c : pNode.children){
			if(tracker == -2){
				tracker++;
			}else{
				double ratio = c.games / 5000;
				do{
					tracker++;
				}while(tracker < tempIdeal.length - 2 && tempIdeal[tracker] == 0.0);

				if(tracker < tempIdeal.length){
					tempIdeal[tracker] = ratio;
				}
				System.out.println(tracker + " : " + ratio);
			}
		}

		ideal[index] = tempIdeal;

		//for(int i = 0 ; i < ideal[].length; i++){
		//	System.out.println(i);
		//}
		//System.out.println("waiting");
		//try{ System.in.read(); }catch(IOexception e){}

	}

	public static double[] boardRep(Board b){
		int count = 0;
		double[] temp = new double[BOARDSIZE * BOARDSIZE];

		for(int i = 0; i < BOARDSIZE; i++){
			for(int j = 0; j < BOARDSIZE; j++){
				if(b.getBoard()[i][j] == 'X'){
					temp[count] = 1.0;
				}else if(b.getBoard()[i][j] == 'O'){
					temp[count] = -1.0;
				}else{
					temp[count] = 0.0;
				}
				count++;
			}
		}

		return temp;
	}
}
