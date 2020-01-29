package players;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;

import java.util.Random;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;

import org.nd4j.linalg.activations.Activation;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.nd4j.linalg.primitives.Pair;

import interfaces.StatusConstants;
import mechanics.Board;
import mechanics.ObservableBoard;
import neuralnetwork.TrainingThread;

public class AI_Player extends Agent implements StatusConstants{

	private static MultiLayerNetwork model;
	private Board initialBoard;
	
	int epochs = 10000;
	double gamma = 0;
	double epsilon = 0;
	int threadcount;
	
	public AI_Player(boolean train, int threadcount){
		modelInitialization();
		this.threadcount = threadcount;
		//long stat = System.nanoTime();
		if (train){
			train();
			//new TrainingThread(epochs, gamma, epsilon);	
		}
		//System.out.println(System.nanoTime() - stat);
	}

	
	
	public AI_Player(Board board, boolean train) { //for training with specific board
		modelInitialization();
		initialBoard = board.cloneInitializedBoard();
		//new TrainingThread(board, epochs, gamma, epsilon);
		if (train)
			train();
	}
	
	public int chooseAction(ObservableBoard board){
        INDArray state = Nd4j.create(board.getState());
        
		INDArray qval = model.output(state);
		INDArray allowedActions = getAllowedActions(qval, board);
	
		int actionIndex = Nd4j.argMax(allowedActions,1).getInt(0);
		return actionIndex;
	}
	
	/*implement catastrophic failure for different boards*/
	/*implement akka for multithreading*/
	public void train(){
		IntSummaryStatistics averageSquaresRevealedCount = new IntSummaryStatistics();
		System.out.println("Begin Training");
		
		Board trainingBoard = initialBoard.cloneInitializedBoard();
		int firstValue = 1;
		for (int epoch = 1; epoch <= epochs; epoch++){
			trainingBoard.resetObservableBoard();
			
			long epochStartTime = System.nanoTime();
			
			
			//ArrayList<Pair<INDArray, INDArray>> iter = new ArrayList<Pair<INDArray, INDArray>>();
			
			while (!trainingBoard.isBoardInitialized() || trainingBoard.isRunning()){
		        INDArray state = Nd4j.create(trainingBoard.getState());
				INDArray qval = model.output(state); 
		       
				int action = 0;
				INDArray allowedActions = getAllowedActions(qval, trainingBoard);
				
				/*
				if (randomDouble(1)<epsilon){
					ArrayList<Integer> availableActions = getAvailableActions(allowedActions);
					action = availableActions.get(randomInt(availableActions.size())); 
				} else {
					action =  Nd4j.argMax(allowedActions,1).getInt(0);
				}
				*/
				action =  Nd4j.argMax(allowedActions,1).getInt(0);
				
				int initialFlagCount = trainingBoard.getFlagCount();
				int initialSquareRevealedCount = trainingBoard.getSquaresRevealedCount();
				
				trainingBoard.playMove(action);
				//trainingBoard.drawObservableBoard();
				
				if (!trainingBoard.isRunning()){
					double update = getReward(trainingBoard, initialSquareRevealedCount, initialFlagCount);
				
					INDArray y = qval.dup();
					y.putScalar(action, update);
				
					
			//		Pair p = new Pair(state, y);
			//		iter.add(p);
		
					//System.out.println("FIT");
					model.fit(state, y);
				}
				//model.fit(iterator);

			}
			
			//DataSetIterator iterator = new INDArrayDataSetIterator(iter, iter.size());
		//	model.fit(iterator);
		//	iter.clear();
			
			long epochEndTime   = System.nanoTime();
			
			if (epoch == 1){
				firstValue = trainingBoard.getSquaresRevealedCount();
			}
			averageSquaresRevealedCount.accept(trainingBoard.getSquaresRevealedCount());
			System.out.printf("Epoch: %-4d	Time Taken: %-3d	Squares Revealed: %-3d	Average: %-3.2f	Highest: %-3d	AverageROC: %-3.2f%n" , epoch, (epochEndTime-epochStartTime)/1000000000, trainingBoard.getSquaresRevealedCount(), averageSquaresRevealedCount.getAverage(), averageSquaresRevealedCount.getMax(), ((double)trainingBoard.getSquaresRevealedCount()-(double)firstValue)/(double)epoch );

			if (epoch%250==0 || epoch == epochs || epoch%1000==0){
				System.out.println("Saving model. Do not close.");
				saveModel();
				System.out.println("Model Saved");
			}
			
			if (epsilon > 0.1){
				epsilon -= ((double)1/(double)epochs);
			}
		}
		
		System.out.println("End Training");
		
	}
	
	private ArrayList<Integer> getAvailableActions(INDArray allowed){
		ArrayList<Integer> availableActions = new ArrayList<Integer>();
		
		for (int i = 0; i < allowed.length(); i++){
			if (allowed.getInt(i) != Integer.MIN_VALUE){
				availableActions.add(i);
			}
		}
		return availableActions;
	}

	private INDArray getAllowedActions(INDArray qval, ObservableBoard board) {
		INDArray allowedActions = qval.dup();
		int count = 0;
		
		
		for (int row = 0; row < board.ROWS; row++){
			for (int  col = 0; col < board.COLUMNS; col++){
				
				if (board.getObservableCell(row, col).getStatus()!=STATUS_HIDDEN){ //numbered or flagged
					allowedActions.putScalar(count, Integer.MIN_VALUE);	//For click
				
					if (board.getObservableCell(row, col).getStatus()!=STATUS_FLAGGED){ //just numbered
						allowedActions.putScalar(count+480, Integer.MIN_VALUE); // for flagged
					}
				}
				count++;
			}
		}
		
		/*REMOVE*/
		//TODO
		for (int i = 480; i < 960; i++){
			allowedActions.putScalar(i, Integer.MIN_VALUE);
		}
		
		return allowedActions;
	}

	public void print(INDArray arr, String str){
        System.out.println(str + " " + arr);
	}
	
	
	private int getReward(ObservableBoard trainingBoard, int initialSquareRevealedCount, int initialFlagCount) {
		if (trainingBoard.getGameCondition().equals("Loser")){
			return -10;
		} else if (trainingBoard.getGameCondition().equals("Winner")){
			return 10;
		}
		
		if (trainingBoard.getFlagCount() - initialFlagCount==0){ //no flags placed
			if (trainingBoard.getSquaresRevealedCount() - initialSquareRevealedCount == 0){ //no squares made
				return -1;
			} else {
				return trainingBoard.getSquaresRevealedCount() - initialSquareRevealedCount;
			}
		} else { // flag placed/removed
			return -1;
		}
	}

	

	public double evaluateState(Board board){	
        INDArray allXYPoints = Nd4j.create(board.getState());
		return model.output(allXYPoints).getDouble(0);
	}
	
	private int randomInt(int maxExclusive) {
		Random r = new Random();
		return r.nextInt(maxExclusive);
	} 
	
	private double randomDouble(double max) {
		Random r = new Random();
		return r.nextDouble()*max;
	}
	
	private void saveModel() {
	/*	try {
			
			FileWriter configSave = new FileWriter("configuration.json");
			configSave.write(model.getDefaultConfiguration().toJson());
			configSave.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			File file = new File("Multi.zip");
			boolean saveUpdater = true;
			ModelSerializer.writeModel(model, file, saveUpdater);
			
			/*OutputStream os = new OutputStream("parameters.json");
			DataOutputStream outputStream = new DataOutputStream(os);
			INDArray params = model.params();
			Nd4j.write(outputStream, params);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		*/
		
		
		File locationToSave = new File("Deterministic.zip");      
        boolean saveUpdater = true;                                            
        try {
			model.save(locationToSave, saveUpdater);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void modelInitialization() {
		File file = new File("Deterministic.zip");
		//File file = new File("test.zip");
		//File configFile = new File("configuration.json");
		
		try {
			if (file.createNewFile()){ 
				//File paramFile = new File("parameters.json");
				//paramFile.createNewFile();
				System.out.println("New Files created.");
				
				int seed = 3;
				double learningRate = 0.1;
				int numInputs = 5761;
				int numHiddenNodes = 8000;
				int numOutputs = 960;

				MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
			            .seed(seed)
			            .weightInit(WeightInit.XAVIER)
			            .updater(new Adam(learningRate))
			            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
			            .list()
			            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
			                    .activation(Activation.RELU) 
			                    .build())
			            .layer(new OutputLayer.Builder(LossFunction.MSE)
			                    .activation(Activation.IDENTITY)
			                    .nIn(numHiddenNodes).nOut(numOutputs).build())
			            		.build();
				
				model = new MultiLayerNetwork(conf);
			    model.init();
			    System.out.println("New model created.");
			    
		
			} else {
				
				
				/*FileReader configReader = new FileReader("configuration.json");
				JSONParser jsonParser = new JSONParser();
				
				MultiLayerConfiguration conf = null;
				try {
					conf = MultiLayerConfiguration.fromJson((String) jsonParser.parse(configReader));
					InputStream inputStream = new FileInputStream("parameters.json");
					INDArray parameters = Nd4j.readTxtString(inputStream);
	
					model = new MultiLayerNetwork(conf, parameters);
					model.init(); //maybe not necessary
						
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
				
				*/
				
				
				boolean loadUpdater = true;
				model = MultiLayerNetwork.load(file, loadUpdater);
				
				
				System.out.println("Model loaded.");
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}


	
	/*private void pause() {
		System.out.println("Paused");
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
	
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}*/
}
