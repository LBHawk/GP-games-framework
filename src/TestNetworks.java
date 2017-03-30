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

public class TestNetworks{
	public static void main(String [] args){
		//public static double[][] trainingSet;
		//public static double[][] testingSet;
		double[][] boardRep = new double[1][49];
		double[][] ideal = new double[1][1];

		Game game = new GoGame();
		GameAgent agent = new MCTSAgent("go", false);

		Board b = game.randomBoardAfterXMoves(7, 40);
		double score = agent.estimateNodesScore(b, 1, 240);
		System.out.println("SCORE: " + score);
		
		b.printBoard();

		int count = 0;
		//as
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 7; j++){
				if(b.getBoard()[i][j] == 'X'){
					boardRep[0][count] = 1.0;
				}else if(b.getBoard()[i][j] == 'O'){
					boardRep[0][count] = -1.0;
				}else{
					boardRep[0][count] = 0.0;
				}
				count++;
			}
		}

		ideal[0][0] = score;

		BasicNetwork network = new BasicNetwork();
		String path = "/home/h/hawkl/Documents/Thesis/thesis-code-hawkl/src/agents/";
		String netName = "7Go90.ser";

		try {
			File file = new File(path + "" + netName);
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			network = (BasicNetwork)in.readObject();
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

		MLDataSet trainingSet = new BasicMLDataSet(boardRep, ideal);
		//MLDataSet testSet = new BasicMLDataSet(TEST, IDEAL);

		// test the neural network
		System.out.println("Neural Network Results:");
		int counter = 1;
		for(MLDataPair pair: trainingSet ) {
			final MLData output = network.compute(pair.getInput());
			System.out.println("input num " + counter + ": actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
			counter++;
		}


	}
}
