package players;

import java.io.File;

import java.io.IOException;

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

import akka.actor.typed.ActorSystem;
import interfaces.StatusConstants;
import mechanics.Board;
import mechanics.ObservableBoard;
import neuralnetwork.NeuralNetwork;

public class AI_Player extends Agent implements StatusConstants {

	private static MultiLayerNetwork model;
	int epochs = 10000;
	int update = 1000;
	int numGames = 5;
	
	int height = 5, width = 5;
	int bitWidth = 12;

	double epsilon = 1.0;

	Random r;

	int ROWS, COLUMNS, NUMBER_OF_BOMBS;
	String fileName = "";

	public AI_Player(int rows, int columns, int numBombs, String temp) {
		ROWS = rows;
		COLUMNS = columns;
		NUMBER_OF_BOMBS = numBombs;

		r = new Random();
		model = denseModel();
		model.init();
	}

	public AI_Player(int rows, int columns, int numBombs) {
		ROWS = rows;
		COLUMNS = columns;
		NUMBER_OF_BOMBS = numBombs;

		r = new Random();

		// model = alexnetModel();
		model = denseModel();
		model.init();

		System.out.print("Enter number of epochs to train: ");
		Scanner scan = new Scanner(System.in);
		epochs = scan.nextInt();
		// scan.close();
		//train();
		
		ActorSystem.create(NeuralNetwork.create(model, update, epochs, numGames, fileName), "Neural Network");
	}

	public int chooseAction(ObservableBoard board) {
		for (int row = 0; row < board.ROWS; row++) {
			for (int col = 0; col < board.COLUMNS; col++) {
				if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN) {
					INDArray state = Nd4j.create(board.getState(row, col));
					INDArray qval = model.output(state);
					
					//System.out.print("action: " + qval.argMax(0).getInt(0) + " | row: " + row + " col:" + col + " | ");
				//	print(qval, "qval: ");
					// print(state, "state: ");
					if (qval.getDouble(0) > 0.9)
						return row * board.COLUMNS + col;
					else if (qval.getDouble(1) > 0.9)
						return row * board.COLUMNS + col
								+ (board.ROWS * board.COLUMNS);
					
					// qval.argMax(0).getInt(0);
					// ArrayList<ActionValueIndex> allowedActions =
					// getAllowedActions(qval, board);
					// int action = argmax(allowedActions);
					// return action;
				}
			}
		}

		return r.nextInt(board.ROWS * board.COLUMNS * 2);
	}

	public void train() {
		System.out.println("Begin Training");
		IntSummaryStatistics averageSquaresRevealedCount = new IntSummaryStatistics();
		int actionLimit = 2000;
		
		for (int epoch = 1; epoch <= epochs; epoch++) {
			Board trainingBoard = new Board(ROWS, COLUMNS, NUMBER_OF_BOMBS);
			long epochStartTime = System.nanoTime();
			// ArrayList<Pair<INDArray, INDArray>> iter = new ArrayList<Pair<INDArray, INDArray>>();
			int actionCount = 0;
			
			gameloop: while (!trainingBoard.isBoardInitialized() || trainingBoard.isRunning()) {
				for (int row = 0; row < trainingBoard.ROWS; row++) {
					for (int col = 0; col < trainingBoard.COLUMNS; col++) {
						if (trainingBoard.getObservableCell(row, col).getStatus() == STATUS_HIDDEN
								|| trainingBoard.getObservableCell(row, col).getStatus() == STATUS_FLAGGED) {
							INDArray state = Nd4j.create(trainingBoard.getState(row, col));
							INDArray qval = model.output(state);

							// System.out.print (row + " " + col + ": ");
							// print(qval, "qval: ");

							int action = -1;

							if (r.nextDouble() < epsilon) {
								double a = r.nextDouble();

								if (a < 0.333)
									action = row * trainingBoard.COLUMNS + col;
								else if (a < 0.666)
									action = row * trainingBoard.COLUMNS + col
											+ (trainingBoard.ROWS * trainingBoard.COLUMNS);
							} else {
								if (qval.getDouble(0) > 0.9)
									action = row * trainingBoard.COLUMNS + col;
								else if (qval.getDouble(1) > 0.9)
									action = row * trainingBoard.COLUMNS + col
											+ (trainingBoard.ROWS * trainingBoard.COLUMNS);
	
							}

							if (action != -1) {
								actionCount++;
								int initialFlagCount = trainingBoard.getFlagCount();
								trainingBoard.playMove(action);

								// trainingBoard.drawObservableBoard();

								INDArray y = qval.dup();

								double updateClick = getRewardClick(trainingBoard, row, col) + qval.getDouble(0);
								double updateFlag = getRewardFlag(trainingBoard, initialFlagCount, row, col)
										+ qval.getDouble(1);

								y.putScalar(0, updateClick);
								y.putScalar(1, updateFlag);
								// print(y, "y: ");
								// Pair<INDArray, INDArray> p = new
								// Pair<INDArray,
								// INDArray>(state, y);
								// iter.add(p);

								model.fit(state, y);

								// pause();

								if (!trainingBoard.isRunning() || actionCount >= actionLimit)
									break gameloop;
							}
						}
					}
				}

			}
			// DataSetIterator iterator = new INDArrayDataSetIterator(iter,
			// iter.size());
			// model.fit(iterator);
			// iter.clear();

			averageSquaresRevealedCount.accept(trainingBoard.getSquaresRevealedCount());
			if (epoch % 10 == 0)
				System.out.printf(
						"Epoch: %-4d Time Taken: %-3.2fs Squares Revealed: %-3d Average: %-3.2f Highest: %-3d%n", epoch,
						(System.nanoTime() - epochStartTime) / 1000000000.0, trainingBoard.getSquaresRevealedCount(),
						averageSquaresRevealedCount.getAverage(), averageSquaresRevealedCount.getMax());

			if (epoch == epochs || epoch % 10000 == 0) 
				saveModel();
			

			if (epsilon > 0.1) 
				epsilon -= 1 / (double) epochs;
			
		}
		System.out.println("End Training");
	}

	public void print(INDArray arr, String str) {
		System.out.println(str + " " + arr);
	}

	/*
	 * HAVE KNOWLEDGE OF BOMBS CLICKED 
	 * if clicked is bomb, decrease click score, increase flag score. 
	 * if clicked is not bomb, increase click score, decrease flag score. 
	 * 
	 * FLAGGED 
	 * if flag is placed on bomb, then increase flag score, decrease click score. 
	 * if flag is not placed on bomb, then decrease flag score, increase click score.
	 * 
	 * REMOVE FLAG 
	 * if flag was not placed on bomb, increase flag score, decrease click score 
	 * if flag was placed on bomb, decrease score
	 * 
	 * if there are no squares surrounding 
	 * 
	 */
	private double getRewardClick(ObservableBoard trainingBoard, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() != STATUS_BOMB && trainingBoard.getObservableCell(row, col).getStatus() != STATUS_FLAGGED)
			return 1;

		return -1;
	}

	private double getRewardFlag(ObservableBoard trainingBoard, int initialFlagCount, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() == STATUS_BOMB) // bomb
			return 1;

		return -1;
		/*
		
		
		if (trainingBoard.getFlagCount() != initialFlagCount) { // flag												// placed/removed
			if (trainingBoard.getObservableCell(row, col).getStatus() == STATUS_FLAGGED) { // flag																		// placed
				if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() == STATUS_BOMB) // bomb
					return 1;
				else
					return -1;
			} else { // flag removed
				if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() == STATUS_BOMB) // bomb
					return -1;
				else
					return 1;
			}
		} else { // no flags placed/removed
			if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() == STATUS_BOMB) // bomb
				return 1;
			else
				return -1;
		}
	*/
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

	private MultiLayerNetwork denseModel() {
		int seed = 3;
		double learningRate = 0.00001;
		int numInputs = 5 * 5 * 12;
		int numHiddenNodes = 1000;
		int numOutputs = 2;

		fileName = "5x5sigmoid" +  "lr" + learningRate + "nodes" + numHiddenNodes + ".zip";
		File file = new File(fileName);

		try {

			if (file.createNewFile()) {
				return createNewDenseModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);
			} else {
				boolean loadUpdater = true;
				try {
					System.out.println("Model loaded.");
					return MultiLayerNetwork.load(file, loadUpdater);
				} catch (ZipException e) {
					return createNewDenseModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public MultiLayerNetwork createNewDenseModel(int seed, double learningRate, int numInputs, int numHiddenNodes,
			int numOutputs) {
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).weightInit(WeightInit.XAVIER)
				.updater(new Adam(learningRate)).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU).build())
				.layer(new OutputLayer.Builder(LossFunction.MSE).activation(Activation.SIGMOID).nIn(numHiddenNodes)
						.nOut(numOutputs).build())
				.build();

		System.out.println("New Model Created: " + fileName);
		return new MultiLayerNetwork(conf);

	}

	/*
	 * private ConvolutionLayer convInit(String name, int in, int out, int[]
	 * kernel, int[] stride, int[] pad, double bias) { return new
	 * ConvolutionLayer.Builder(kernel, stride,
	 * pad).name(name).nIn(in).nOut(out).biasInit(bias).build(); }
	 * 
	 * private ConvolutionLayer conv3x3(String name, int out, double bias) {
	 * return new ConvolutionLayer.Builder(new int[] { 3, 3 }, new int[] { 1, 1
	 * }, new int[] { 1, 1 }).name(name) .nOut(out).biasInit(bias).build(); }
	 * 
	 * private ConvolutionLayer conv5x5(String name, int out, int[] stride,
	 * int[] pad, double bias) { return new ConvolutionLayer.Builder(new int[] {
	 * 5, 5 }, stride, pad).name(name).nOut(out).biasInit(bias) .build(); }
	 * 
	 * private SubsamplingLayer maxPool(String name, int[] kernel) { return new
	 * SubsamplingLayer.Builder(kernel, new int[] { 2, 2 }).name(name).build();
	 * }
	 * 
	 * private DenseLayer fullyConnected(String name, int out, double bias,
	 * double dropOut, Distribution dist) { return new
	 * DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut)
	 * .weightInit((IWeightInit) new WeightInitDistribution(dist)).build(); }
	 * private MultiLayerNetwork alexnetModel() { int seed = 42; double
	 * learningRate = 0.1; int numInputs = 5 * 5 * 12 + 1; int numHiddenNodes =
	 * 1000; int numLabels = 3;
	 * 
	 * double nonZeroBias = 1; double dropOut = 0.5;
	 * 
	 * MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	 * .seed(seed) .weightInit(new NormalDistribution(0.0, 0.01))
	 * .activation(Activation.RELU) .updater(new AdaDelta())
	 * .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) //
	 * normalize to prevent vanishing or exploding gradients .l2(5 * 1e-4)
	 * .list() .layer(convInit("cnn1", bitWidth, 96, new int[]{11, 11}, new
	 * int[]{4, 4}, new int[]{5, 5}, 0)) .layer(new
	 * LocalResponseNormalization.Builder().name("lrn1").build())
	 * .layer(maxPool("maxpool1", new int[]{3,3})) .layer(conv5x5("cnn2", 256,
	 * new int[] {1,1}, new int[] {2,2}, nonZeroBias)) .layer(new
	 * LocalResponseNormalization.Builder().name("lrn2").build())
	 * .layer(maxPool("maxpool2", new int[]{3,3})) .layer(conv3x3("cnn3", 384,
	 * 0)) .layer(conv3x3("cnn4", 384, nonZeroBias)) .layer(conv3x3("cnn5", 256,
	 * nonZeroBias)) .layer(maxPool("maxpool3", new int[]{3,3}))
	 * .layer(fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new
	 * NormalDistribution(0, 0.005))) .layer(fullyConnected("ffn2", 4096,
	 * nonZeroBias, dropOut, new NormalDistribution(0, 0.005))) .layer(new
	 * OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	 * .name("output") .nOut(numLabels) .activation(Activation.SOFTMAX)
	 * .build()) .setInputType(InputType.convolutional(height, width, bitWidth))
	 * .build();
	 * 
	 * return new MultiLayerNetwork(conf);
	 * 
	 * } /* private void pause() { System.out.println("Paused");
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

	private void pause() {
		System.out.println("Paused: Please enter any character to continue.");

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
	}

	/*
	 * public void train() { IntSummaryStatistics averageSquaresRevealedCount =
	 * new IntSummaryStatistics(); System.out.println("Begin Training");
	 * 
	 * for (int epoch = 1; epoch <= epochs; epoch++) { Board trainingBoard = new
	 * Board(ROWS, COLUMNS, NUMBER_OF_BOMBS); long epochStartTime =
	 * System.nanoTime(); // ArrayList<Pair<INDArray, INDArray>> iter = new //
	 * ArrayList<Pair<INDArray, INDArray>>();
	 * 
	 * gameloop: while (!trainingBoard.isBoardInitialized() ||
	 * trainingBoard.isRunning()) { for (int row = 0; row <trainingBoard.ROWS;
	 * row++){ for (int col = 0; col < trainingBoard.COLUMNS; col++){ if
	 * (trainingBoard.getObservableCell(row, col).getStatus() == STATUS_HIDDEN){
	 * INDArray state = Nd4j.create(trainingBoard.getState(row, col)); INDArray
	 * qval = model.output(state);
	 * 
	 * int action = 0; ArrayList<ActionValueIndex> allowedActions =
	 * getAllowedActions(qval, trainingBoard);
	 * 
	 * if (r.nextDouble() < epsilon) { action =
	 * allowedActions.get(r.nextInt(allowedActions.size())).getIndex(); } else {
	 * action = argmax(allowedActions); }
	 * 
	 * int initialFlagCount = trainingBoard.getFlagCount(); int
	 * initialSquareRevealedCount = trainingBoard.getSquaresRevealedCount();
	 * 
	 * trainingBoard.playMove(action); // trainingBoard.drawObservableBoard();
	 * 
	 * 
	 * 
	 * double update = getReward(trainingBoard, initialSquareRevealedCount,
	 * initialFlagCount);
	 * 
	 * INDArray y = qval.dup(); y.putScalar(action, update);
	 * 
	 * // Pair<INDArray, INDArray> p = new Pair<INDArray, // INDArray>(state,
	 * y); // iter.add(p);
	 * 
	 * model.fit(state, y);
	 * 
	 * 
	 * if (!trainingBoard.isRunning()) break gameloop;
	 * 
	 * } } }
	 * 
	 * 
	 * } // DataSetIterator iterator = new INDArrayDataSetIterator(iter, //
	 * iter.size()); // model.fit(iterator); // iter.clear();
	 * 
	 * long epochEndTime = System.nanoTime();
	 * 
	 * averageSquaresRevealedCount.accept(trainingBoard.getSquaresRevealedCount(
	 * )); System.out.
	 * printf("Epoch: %-4d Time Taken: %-3.2fs Squares Revealed: %-3d Average: %-3.2f Highest: %-3d%n"
	 * , epoch, (epochEndTime - epochStartTime) / 1000000000.0,
	 * trainingBoard.getSquaresRevealedCount(),
	 * averageSquaresRevealedCount.getAverage(),
	 * averageSquaresRevealedCount.getMax());
	 * 
	 * if (epoch == epochs || epoch % 1000 == 0) { saveModel(); }
	 * 
	 * if (epsilon > 0.1) { epsilon -= 1 / (double) epochs; } }
	 * 
	 * System.out.println("End Training"); }
	 * 
	 * private int argmax(ArrayList<ActionValueIndex> allowedActions) { int
	 * index = 0; double highest = Integer.MIN_VALUE;
	 * 
	 * for (ActionValueIndex avi : allowedActions) { if (avi.getValue() >
	 * highest) { highest = avi.getValue(); index = avi.getIndex(); } }
	 * 
	 * return index; }
	 * 
	 * private ArrayList<ActionValueIndex> getAllowedActions(INDArray qval,
	 * ObservableBoard board) { int count = 0;
	 * 
	 * ArrayList<ActionValueIndex> availableActions = new
	 * ArrayList<ActionValueIndex>();
	 * 
	 * for (int row = 0; row < board.ROWS; row++) { for (int col = 0; col <
	 * board.COLUMNS; col++) { if (board.getObservableCell(row, col).getStatus()
	 * == STATUS_HIDDEN) { // clickable ActionValueIndex avi = new
	 * ActionValueIndex(qval.getDouble(0, count), count);
	 * availableActions.add(avi); }
	 * 
	 * count++; } }
	 * 
	 * return availableActions; }
	 */
}
