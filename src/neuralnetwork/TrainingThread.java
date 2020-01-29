package neuralnetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.apache.http.annotation.ThreadingBehavior;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.nd4j.linalg.primitives.Pair;

import main.PlayGame;
import mechanics.Board;
import mechanics.ObservableBoard;

public class TrainingThread implements Runnable {

	static Queue<ArrayList<Pair<INDArray, INDArray>>> queue = new LinkedList<ArrayList<Pair<INDArray, INDArray>> >();
	public static boolean pause = false;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	public TrainingThread(Board board, int epochs, double gamma, double epsilon){
		//System.out.println("made class");
		
		new TrainingParameters(board, epochs,  gamma, epsilon);
		long startTime = System.nanoTime();
		
		ThreadGroup tg1 = new ThreadGroup("Group A");   
		Thread t1 = new Thread(tg1,this,"one");;     
		Thread t2 = new Thread(tg1,this,"two");     
		Thread t3 = new Thread(tg1,this,"three");
		//Thread t4 = new Thread(tg1,this,"four");
		
		t1.setPriority(Thread.MIN_PRIORITY);
		t2.setPriority(Thread.MIN_PRIORITY);
		t3.setPriority(Thread.MIN_PRIORITY);
		
		
		t1.start();
		t2.start();
		t3.start();
		//t4.start();
		
		
		Thread queueHandler = new Thread("QueueHandler"){
			@Override
	        public void run(){
			
				
				int count = 0;
				whileloop:
	        	while (true){
	        		while (queue.size() > 0){
	        			count++;
	        			//if (queue.size()%50==0){
	        			System.out.println("Fitting data	" + queue.size());
	        			
	        			if (queue.peek().size()>0){
		        			DataSetIterator iterator = new INDArrayDataSetIterator(queue.peek(), queue.peek().size());
		        			TrainingParameters.model.fit(iterator);
	        				queue.remove();	
	        			}
        				if (queue.size() > 300){
        					if (!pause){
	        					System.out.println("Threads paused");
	        				}
        					pause = true;
    	        		} else if (queue.size() < 50 && pause){
    	        			System.out.println("Threads resumed");
    	        			pause = false;
    	        		}
        				
        				if (count%100000 ==0 && count!=0){
        					TrainingParameters.saveModel();
        				}
	        		}
	        		
	        		if (!t1.isAlive() && !t2.isAlive() && !t3.isAlive()){// && !t3.isAlive() && !t4.isAlive()){
	        			System.out.println("All threads are finished");
	        			if (count!=0){
	        				TrainingParameters.saveModel();
	        			}
	        			long endTime = System.nanoTime();
	        			System.out.println(endTime-startTime + " TIME");
	        			break whileloop;
	        		}	
	        	}
	        }

	    };
	    queueHandler.setPriority(Thread.MAX_PRIORITY);
	    queueHandler.start();
	}
	
	public TrainingThread(int epochs, double gamma, double epsilon){
		//System.out.println("made class");
		new TrainingParameters(null, epochs,  gamma, epsilon);
		long startTime = System.nanoTime();
		
		ThreadGroup tg1 = new ThreadGroup("Group A");   
		Thread t1 = new Thread(tg1,this,"one");;     
		Thread t2 = new Thread(tg1,this,"two");     
		Thread t3 = new Thread(tg1,this,"three");
		//Thread t4 = new Thread(tg1,this,"four");
		
		t1.setPriority(Thread.MIN_PRIORITY);
		t2.setPriority(Thread.MIN_PRIORITY);
		t3.setPriority(Thread.MIN_PRIORITY);
		
		
		t1.start();
		t2.start();
		t3.start();
		//t4.start();
		
		
		Thread queueHandler = new Thread("QueueHandler"){
			@Override
	        public void run(){
			
				
				int count = 0;
				whileloop:
	        	while (true){
	        		while (queue.size() > 0){
	        			count++;
	        			//if (queue.size()%50==0){
	        				System.out.println("Fitting data	" + queue.size());
	        			//}
	        			
	        			DataSetIterator iterator = new INDArrayDataSetIterator(queue.peek(), queue.peek().size());
	        			TrainingParameters.model.fit(iterator);
        				queue.remove();	
        				
        				if (queue.size() > 300){
        					if (!pause){
	        					System.out.println("Threads paused");
	        				}
        					pause = true;
    	        		} else if (queue.size() < 50 && pause){
    	        			System.out.println("Threads resumed");
    	        			pause = false;
    	        		}
        				
        				if (count%100000 ==0 && count!=0){
        					TrainingParameters.saveModel();
        				}
	        		}
	        		
	        		if (!t1.isAlive() && !t2.isAlive() && !t3.isAlive()){// && !t3.isAlive() && !t4.isAlive()){
	        			System.out.println("All threads are finished");
	        			if (count!=0){
	        				TrainingParameters.saveModel();
	        			}
	        			long endTime = System.nanoTime();
	        			System.out.println(endTime-startTime + " TIME");
	        			break whileloop;
	        		}	
	        	}
	        }

	    };
	    queueHandler.setPriority(Thread.MAX_PRIORITY);
	    queueHandler.start();
	}
	
	@Override
	public void run() {
		IntSummaryStatistics averageSquaresRevealedCount = new IntSummaryStatistics();
		System.out.println("Thread: " + Thread.currentThread().getName() + " Begin Training");	
		Board trainingGame =  (Board) TrainingParameters.initialBoard.cloneInitializedBoard();
		
		for (int epoch = 1; epoch < TrainingParameters.epochs; epoch++){
			trainingGame.resetObservableBoard();
			//Board trainingGame = new Board();

			int action = 0;
			//int sameActionCounter = 0;
			
			long epochStartTime = System.nanoTime();
			
			ArrayList<Pair<INDArray, INDArray>> iter = new ArrayList<Pair<INDArray, INDArray>>();
			
			//trainingloop:
			//State boardState = new State(trainingGame);
			while (!trainingGame.isBoardInitialized() || trainingGame.isRunning()){
				
				if (!pause){
					
			        INDArray state = Nd4j.create(trainingGame.getState());
			        
			        //accessing model (could just synchronize this)
			        
			        INDArray qval = TrainingParameters.model.output(state); 
			
					 
				//	int previousAction = action;
					
					if (TrainingParameters.randomDouble(1)<TrainingParameters.epsilon){
						action =  TrainingParameters.randomInt(959); 
					} else {
						action =  qval.argMax(0).getInt(0);
					}
					
					//trainingGame..drawObservableBoard();
				/*	if (previousAction == action){
						sameActionCounter++;
					}
					
					if  (sameActionCounter>10){
						System.out.println("Looped action");
						break trainingloop;
					}*/
					/*
					int initialFlagCount = trainingGame.getFlagCount();
					int initialSquareRevealedCount = trainingGame.getSquaresRevealedCount();
					
					TrainingParameters.playMove(trainingGame, action);
					
					int reward = TrainingParameters.getReward(trainingGame, initialSquareRevealedCount, initialFlagCount);
					
					INDArray new_State = Nd4j.create(trainingGame.getState());
					
					//accessing model (could just synchronize this)
					INDArray newQ = TrainingParameters.model.output(new_State);
			
					double maxQ = newQ.max(0).getDouble(0);
					
					INDArray y = qval.dup();
					
					double update;
					if (trainingGame.isRunning()){
						update = (reward+ (TrainingParameters.gamma * maxQ));
					}  else {
						update = reward;
					}
					
					
					y.putScalar(action, update);
					
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Pair p = new Pair(state, y);
					iter.add(p);
					
					//(could just synchronize this)
					//TrainingParameters.model.fit(state, y); 

				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//System.out.println("Thread: " + Thread.currentThread().getName() + "is paused" +pause);
			}
			addToQueue(iter);
			
			iter.clear();
			
			long epochEndTime   = System.nanoTime();
			averageSquaresRevealedCount.accept(trainingGame.getSquaresRevealedCount());
			System.out.println("Thread: " + Thread.currentThread().getName() + "	Epoch: " + epoch + "	Time Taken: " + (epochEndTime-epochStartTime)/1000000000 + "	Squares Revealed: " + trainingGame.getSquaresRevealedCount() +  "	Average: " + averageSquaresRevealedCount.getAverage() + "	Highest: " + averageSquaresRevealedCount.getMax());
			
			if (TrainingParameters.epsilon > 0.1){
			//	TrainingParameters.epsilon -= ((double)1/(double)TrainingParameters.epochs);
			}
		}
		System.out.println("Thread: " + Thread.currentThread().getName() + " End Training");		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void addToQueue(ArrayList<Pair<INDArray, INDArray>> iter){
		//System.out.println("Data added to queue	" + queue.size());
		queue.add(iter);
	}
	
	public static class TrainingParameters {
		private static MultiLayerNetwork model;
		static int epochs;
		static double gamma;
		static double epsilon;
		static Board initialBoard;
		
		public TrainingParameters(Board board, int epochs, double gamma, double epsilon){
			modelInitialization();
			TrainingParameters.initialBoard = board;
			TrainingParameters.epochs = epochs;
			TrainingParameters.gamma = gamma;
			TrainingParameters.epsilon = epsilon;
		}
		
		
		private static int getReward(ObservableBoard trainingBoard, int initialSquareRevealedCount, int initialFlagCount) {
			if (trainingBoard.getGameCondition().equals("Loser")){
				return -100;
			} else if (trainingBoard.getGameCondition().equals("Winner")){
				return 100;
			}
			
			if (trainingBoard.getFlagCount() - initialFlagCount==0){ //no flags placed
				if (trainingBoard.getSquaresRevealedCount() - initialSquareRevealedCount == 0){ //no squares made
					return -10;
				} else {
					return trainingBoard.getSquaresRevealedCount() - initialSquareRevealedCount;
				}
			} else { // flag placed/removed
				return 0;
			}
		}

		private static void playMove(Board board, int actionIndex) {
			int [] action = {0, 0, 0};
			
			if (actionIndex >= 480){
				actionIndex-=480;
				action[0] = actionIndex/board.COLUMNS;
				action[1] = actionIndex%board.COLUMNS;
				action[2] = 1;
				board.flagCell(action[0], action[1]);
				
			} else {
				action[0] = actionIndex/board.COLUMNS;
				action[1] = actionIndex%board.COLUMNS;
				action[2] = 0;
				board.clickCellInitial(action[0], action[1]);
			} 
		}
		
		

		public static double evaluateState(Board board){

	        INDArray allXYPoints = Nd4j.create(board.getState());
	 
			return model.output(allXYPoints).getDouble(0);
		}
		
		private static int randomInt(int maxInclusive) {
			Random r = new Random();
			return r.nextInt(maxInclusive+1);
		} 
		
		private static double randomDouble(double max) {
			Random r = new Random();
			return r.nextDouble()*max;
		}
		
		private static void saveModel() {
			File locationToSave = new File("MyMultiLayerNetwork.zip");      
                                           
	        try {
	        	System.out.println("Saving Model... Do Not close.");
				model.save(locationToSave, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        System.out.println("Model Saved");
	       
			
		}

		private static void modelInitialization() {
			File file = new File("MyMultiLayerNetwork.zip");
			
			try {
				if (file.createNewFile()){ 
					
					int seed = 3;
					double learningRate = 0.001;
					int numInputs = 5761;
					int numHiddenNodes = 8000;
					int numOutputs = 960;
					
					MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				            .seed(seed)
				            .weightInit(WeightInit.XAVIER)
				            .updater(new Adam(learningRate))
				            .list()
				            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
				                    .activation(Activation.RELU)
				                    .build())
				            .layer(new OutputLayer.Builder(LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR)
				                    .activation(Activation.IDENTITY)
				                    .nIn(numHiddenNodes).nOut(numOutputs).build())
				            		.build();
					
					model = new MultiLayerNetwork(conf);
				    model.init();
				    System.out.println("New model created.");
				} else { 
					boolean loadUpdater = true;
					model = MultiLayerNetwork.load(file, loadUpdater);
					System.out.println("Model loaded.");
				}
			} catch (IOException e) {
			
				e.printStackTrace();
			}
			
		}
	}

	
	
	*/
	
}
