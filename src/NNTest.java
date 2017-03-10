import java.util.Random;
import java.io.IOException;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationStep;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class NNTest{
	public static double[][] INPUT;
	public static double[][] IDEAL;
	public static double[][] TEST;
	public static int size = 2000;

	public static void main(String[] args){
		
		INPUT = new double[size][81];
		IDEAL = new double[size][1];
		TEST = new double[size][81];
		
		createInput(size);

		// create a neural network
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,81));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,40));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();

		// create training data
		MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
		MLDataSet testSet = new BasicMLDataSet(TEST, IDEAL);
		
		// train the neural network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

		int epoch = 1;

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);
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

		// TEST-----------------
		try{ System.in.read(); } catch(IOException e){}
		System.out.println("Test Results:");
		count = 1;
		for(MLDataPair pair: testSet ) {
			final MLData output = network.compute(pair.getInput());
			System.out.println("input num " + count + ": actual=" + output.getData(0));
			count++;
		}
		
		Encog.getInstance().shutdown();
	}

	public static void createInput(int size){
		Random r = new Random();
		double in = 0.0;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < 81; j++){
				int temp = r.nextInt(3);
				if(temp == 0){
					in = -1.0;
				}else if(temp == 1){
					in = 0.0;
				}else{
					in = 1.0;
				}
				
				INPUT[i][j] = in;
				TEST[i][j] = in * -1;
			}
		}

		for(int i = 0; i < size; i++){
			if(r.nextInt(2) == 0){ 
				IDEAL[i][0] = 1.0; 
			}else{ 
				IDEAL[i][0] = 0.0;
			}
		}
	}
}
