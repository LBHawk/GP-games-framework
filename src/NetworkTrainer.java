import java.util.Random;
import java.io.IOException;
import java.io.*;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationStep;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class NetworkTrainer{
	public static Board[] boardSet; 		// Array of training boards
	public static double[][] fullSet; 		// Network friendly array of boards
	//public static double[][] trainingSet;
	//public static double[][] testingSet;
	public static double[][] ideal; 		// Ideal outputs for bourds
	public static int BOARDSIZE; 			// Size of boards to train for
	public static int SIZE = 2500; 			// Size of training data
	//public static final double TRAININGRATIO = 0.75;
	public static final int INCREMENT = 5; 	// Differences in numMoves of random boards
	public static String gameType; 			// Type of game to train for

	public static void main(String[] args){
		if(args.length != 3){
			System.out.println("------usage-------");
			System.out.println("arg1: boardsize");
			System.out.println("arg2: trainingset size");
			System.out.println("arg3: gameType");
			System.exit(-1);
		}

		// Parse commandline
		BOARDSIZE = Integer.parseInt(args[0]);
		SIZE = Integer.parseInt(args[1]);
		gameType = args[2];

		//--------------
		// Initialize all vars

		boardSet = new Board[SIZE];
		for(int i = 0; i < SIZE; i++){
			boardSet[i] = new Board(BOARDSIZE);
		}
		fullSet = new double[SIZE][BOARDSIZE*BOARDSIZE];
		//trainingSet = new double[(int)(SIZE * TRAININGRATIO)][BOARDSIZE*BOARDSIZE];
		//testingSet = new double[(int)(SIZE * (1 - TRAININGRATIO))][BOARDSIZE*BOARDSIZE];
		ideal = new double[SIZE][1];
		Board tempBoard = new Board(BOARDSIZE);

		Game game = null;
		if(gameType.equals("go")){
			game = new GoGame();
		}else if(gameType.equals("hex")){
			game = new HexGame();
		}else{
			System.out.println("Invalid gametype");
			System.exit(-1);
		}

		GameAgent agent = new MCTSAgent(gameType, false);

		// Done initializing
		//-------------



		// Create random boards using helper from game class
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
			moveTracker += 5;
			
		}

		System.out.println("Done creating boards");
		//System.out.println("score est: " + agent.estimateNodesScore(boardSet[2500], 5000, 25));

		// Find estimated value of boards for ideal output,
		// Turn boards into network friendly form
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < SIZE; i++){
			System.out.print(i);
			double temp = agent.estimateNodesScore(boardSet[i], 1000, i / numSizes);
			ideal[i][0] = temp;

			if(temp < min){
				min = temp;
			}
			if(temp > max){
				max = temp;
			}

			System.out.println("!");
			setTraining(i, boardSet[i]);
		}

		//System.out.println(min + " : " + max);
		//try{ System.in.read(); } catch(IOException e){}


		train();

	}

	// Train the networks on the training data
	// until a low enough error is reached
	public static void train(){
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,BOARDSIZE*BOARDSIZE));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,(BOARDSIZE*BOARDSIZE) / 3));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();

		// create training data
		MLDataSet trainingSet = new BasicMLDataSet(fullSet, ideal);
		//MLDataSet testSet = new BasicMLDataSet(TEST, IDEAL);
		
		// train the neural network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

		int epoch = 1;
		double err = 0.0;
		double[] lastTen = new double[10];
		do {
			train.iteration();
			err = train.getError();
			System.out.println("Epoch #" + epoch + " Error:" + err);
			lastTen[(epoch-1) % 10] = err;
			epoch++;

			if(epoch > 1500){
				if(range(lastTen) < 0.000001){
					break;
				}
			}

		} while((train.getError() > 0.0001));
		train.finishTraining();

		try{ System.in.read(); } catch(IOException e){}

		// test the neural network
		System.out.println("Neural Network Results:");
		int count = 1;
		for(MLDataPair pair: trainingSet ) {
			final MLData output = network.compute(pair.getInput());
			System.out.println("input num " + count + ": actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
			count++;
		}

		serializeNetwork(network, err);

		Encog.getInstance().shutdown();
	}

	// Save the network to the harddisk
	public static void serializeNetwork(BasicNetwork net, double finalErr){
		System.out.println("Serializing");
		String path = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/";
		int err = (int)(finalErr*10000.0);
		try{
			File file = new File(path + "agents/" + BOARDSIZE + gameType + err + ".ser");
			file.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(file);
         	ObjectOutputStream out = new ObjectOutputStream(fileOut);
         	out.writeObject(net);
         	out.close();
         	fileOut.close();
         	System.out.println("wrote network to: " + "/agents/" + BOARDSIZE + gameType + err + ".ser");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	// Finds the range of set of doubles.
	// Used to exit training when we are converging to an error above
	// what we want from training (so we aren't training forever)
	public static double range(double[] d){
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for(int i = 0; i < d.length; i++){
			if(d[i] < min) { min = d[i]; }
			if(d[i] > max) { max = d[i]; }
		}

		return max - min;
	}

	// Puts the board into a network friendly format (i.e. doubles) and adds to
	// the training data array.
	public static void setTraining(int index, Board b){
		int count = 0;
		for(int i = 0; i < BOARDSIZE; i++){
			for(int j = 0; j < BOARDSIZE; j++){
				if(b.getBoard()[i][j] == 'X'){
					fullSet[index][count] = 1.0;
				}else if(b.getBoard()[i][j] == 'O'){
					fullSet[index][count] = -1.0;
				}else{
					fullSet[index][count] = 0.0;
				}
				count++;
			}
		}
	}
}
