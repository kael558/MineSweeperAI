package players;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;

import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipException;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;

import org.nd4j.linalg.activations.Activation;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import interfaces.StatusConstants;
import mechanics.Board;
import mechanics.ObservableBoard;
import neuralnetwork.ActionValueIndex;

public class AI_Player extends Agent implements StatusConstants {

	private static MultiLayerNetwork model;
	int epochs = 10000;

	double epsilon = 1.0;

	Random r;

	int ROWS, COLUMNS, NUMBER_OF_BOMBS;
	String fileName = "";

	public AI_Player(int rows, int columns, int numBombs, String temp) {
		ROWS = rows;
		COLUMNS = columns;
		NUMBER_OF_BOMBS = numBombs;

		r = new Random();
		modelInitialization();
	}

	public AI_Player(int rows, int columns, int numBombs) {
		ROWS = rows;
		COLUMNS = columns;
		NUMBER_OF_BOMBS = numBombs;

		r = new Random();
		modelInitialization();

		System.out.print("Enter number of epochs to train: ");
		Scanner scan = new Scanner(System.in);
		epochs = scan.nextInt();
		
		train();
	}

	public int chooseAction(ObservableBoard board) {
		INDArray state = Nd4j.create(board.getState());

		INDArray qval = model.output(state);
		ArrayList<ActionValueIndex> allowedActions = getAllowedActions(qval, board);

		int action = argmax(allowedActions);
		return action;
	}

	/* implement catastrophic failure for different boards */
	/* implement akka for multithreading */
	public void train() {
		IntSummaryStatistics averageSquaresRevealedCount = new IntSummaryStatistics();
		System.out.println("Begin Training");

		for (int epoch = 1; epoch <= epochs; epoch++) {
			Board trainingBoard = new Board(ROWS, COLUMNS, NUMBER_OF_BOMBS);
			long epochStartTime = System.nanoTime();
			// ArrayList<Pair<INDArray, INDArray>> iter = new
			// ArrayList<Pair<INDArray, INDArray>>();

			while (!trainingBoard.isBoardInitialized() || trainingBoard.isRunning()) {
				INDArray state = Nd4j.create(trainingBoard.getState());
				INDArray qval = model.output(state);

				int action = 0;
				ArrayList<ActionValueIndex> allowedActions = getAllowedActions(qval, trainingBoard);

				if (r.nextDouble() < epsilon) {
					action = allowedActions.get(r.nextInt(allowedActions.size())).getIndex();
				} else {
					action = argmax(allowedActions);
				}

				int initialFlagCount = trainingBoard.getFlagCount();
				int initialSquareRevealedCount = trainingBoard.getSquaresRevealedCount();

				trainingBoard.playMove(action);
				// trainingBoard.drawObservableBoard();

				double update = getReward(trainingBoard, initialSquareRevealedCount, initialFlagCount);

				INDArray y = qval.dup();
				y.putScalar(action, update);

				// Pair<INDArray, INDArray> p = new Pair<INDArray,
				// INDArray>(state, y);
				// iter.add(p);

				model.fit(state, y);
			}
			// DataSetIterator iterator = new INDArrayDataSetIterator(iter, iter.size());
			// model.fit(iterator);
			// iter.clear();

			long epochEndTime = System.nanoTime();

			averageSquaresRevealedCount.accept(trainingBoard.getSquaresRevealedCount());
			System.out.printf("Epoch: %-4d Time Taken: %-3.2fs Squares Revealed: %-3d Average: %-3.2f Highest: %-3d%n",
					epoch, (epochEndTime - epochStartTime) / 1000000000.0, trainingBoard.getSquaresRevealedCount(),
					averageSquaresRevealedCount.getAverage(), averageSquaresRevealedCount.getMax());
 
			if (epoch == epochs || epoch % 1000 == 0) {
				saveModel();
			}

			if (epsilon > 0.1) {
				epsilon -= 1 / (double) epochs;
			}
		}

		System.out.println("End Training");
	}

	private int argmax(ArrayList<ActionValueIndex> allowedActions) {
		int index = 0;
		double highest = Integer.MIN_VALUE;

		for (ActionValueIndex avi : allowedActions) {
			if (avi.getValue() > highest) {
				highest = avi.getValue();
				index = avi.getIndex();
			}
		}

		return index;
	}

	private ArrayList<ActionValueIndex> getAllowedActions(INDArray qval, ObservableBoard board) {
		int count = 0;

		ArrayList<ActionValueIndex> availableActions = new ArrayList<ActionValueIndex>();

		for (int row = 0; row < board.ROWS; row++) {
			for (int col = 0; col < board.COLUMNS; col++) {
				if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN) { // clickable
					ActionValueIndex avi = new ActionValueIndex(qval.getDouble(0, count), count);
					availableActions.add(avi);
				}

				count++;
			}
		}

		return availableActions;
	}

	public void print(INDArray arr, String str) {
		System.out.println(str + " " + arr);
	}

	private int getReward(ObservableBoard trainingBoard, int initialSquareRevealedCount, int initialFlagCount) {
		if (trainingBoard.getGameCondition().equals("Loser")) {
			return -10;
		} 
		return 1;
	}

	private void saveModel() {
		System.out.println("Saving model. Do not close.");
		File locationToSave = new File(fileName);
		try {
			model.save(locationToSave, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Model Saved");
	}

	private void modelInitialization() {
		int seed = 3;
		double learningRate = 0.1;
		int numInputs = ROWS * COLUMNS * 12 + 1;
		int numHiddenNodes = 1000;
		int numOutputs = ROWS * COLUMNS;

		fileName = ROWS + "x" + COLUMNS + "b" + NUMBER_OF_BOMBS + "lr" + learningRate + "nodes" + numHiddenNodes + ".zip";
		File file = new File(fileName);

		try {
			
			if (file.createNewFile()) {
				createNewModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);
			} else {
				boolean loadUpdater = true;
				try {
					model = MultiLayerNetwork.load(file, loadUpdater);
					System.out.println("Model loaded.");
				} catch (ZipException e) {
					createNewModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);	
				}
				
				
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	public void createNewModel(int seed, double learningRate, int numInputs, int numHiddenNodes, int numOutputs){
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed)
				.weightInit(WeightInit.XAVIER).updater(new Adam(learningRate))
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU)
						.build())
				.layer(new OutputLayer.Builder(LossFunction.MSE).activation(Activation.IDENTITY)
						.nIn(numHiddenNodes).nOut(numOutputs).build())
				.build();

		model = new MultiLayerNetwork(conf);
		model.init();
		System.out.println("New Model Created: " + fileName);

	}
	

	/*
	 * private void pause() { System.out.println("Paused");
	 * 
	 * @SuppressWarnings("resource") Scanner scan = new Scanner(System.in);
	 * scan.nextLine();
	 * 
	 * }
	 * 
	 * private void sleep(int millis) { try { Thread.sleep(millis); } catch
	 * (InterruptedException e) {
	 * 
	 * e.printStackTrace(); } }
	 */
}
