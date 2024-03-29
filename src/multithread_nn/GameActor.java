package multithread_nn;

import java.util.ArrayList;
import java.util.Random;

import enumerations.ActionType;
import enumerations.CellType;
import mechanics.Action;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import mechanics.Board;

public class GameActor extends AbstractBehavior<GameActor.Command>  {

	/*
	 * runs game of minesweeper sends data to neural network, sends message to
	 * itself to start new game receives - updated neural network - terminate
	 * command - start new game
	 */

	interface Command {
	}

	private MultiLayerNetwork model;
	private int ROWS, COLUMNS, NUMBER_OF_BOMBS;
	private double epsilon;
	private Random r;

	private ActorRef<ModelActor.ReceiveData> neuralNetworkActor;
	
	// Types of messages received
	public enum StartGame implements Command {
		INSTANCE
	}

	public static class UpdateNeuralNetwork implements Command {
		public final MultiLayerNetwork newModel;

		public UpdateNeuralNetwork(MultiLayerNetwork model) {
			super();
			newModel = model;
		}
	}

	// choosing which behavior in response to message
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessageEquals(StartGame.INSTANCE, this::onStartGame)
				.onMessage(UpdateNeuralNetwork.class, this::onUpdateNeuralNetwork)
				.build();
	}

	// Behaviors
	private Behavior<Command> onStartGame() {
		//getContext().getLog().info("{} is starting a new game", getContext().getSelf().path().name());
		
		Board trainingBoard = new Board(ROWS, COLUMNS, NUMBER_OF_BOMBS);

		ArrayList<INDArray> features = new ArrayList<INDArray>();
		ArrayList<INDArray> labels = new ArrayList<INDArray>();
		
		int actionCount = 0;

		gameloop: 
		while (!trainingBoard.isBoardInitialized() || trainingBoard.isRunning()) {
			for (int row = 0; row < trainingBoard.ROWS; row++) {
				for (int col = 0; col < trainingBoard.COLUMNS; col++) {
					if (trainingBoard.getObservableCell(row, col) == CellType.HIDDEN
							|| trainingBoard.getObservableCell(row, col) == CellType.FLAGGED) {
						INDArray state = Nd4j.create(trainingBoard.serializeState5x5(row, col));
						INDArray qval = model.output(state);

						Action action = null;

						if (r.nextDouble() < epsilon) {
							double a = r.nextDouble();

							if (a < 0.333)
								action = new Action(ActionType.CLICK, row, col);
							else if (a < 0.666)
								action = new Action(ActionType.FLAG, row, col);
						} else {
							if (qval.getDouble(0) > 0.9)
								action = new Action(ActionType.CLICK, row, col);
							else if (qval.getDouble(1) > 0.9)
								action = new Action(ActionType.FLAG, row, col);
						}

						if (action != null) {
							actionCount++;
							int initialFlagCount = trainingBoard.getFlagCount();
							trainingBoard.playMove(action);

							INDArray y = qval.dup();

							double updateClick = getRewardClick(trainingBoard, row, col) + qval.getDouble(0);
							double updateFlag = getRewardFlag(trainingBoard, initialFlagCount, row, col) + qval.getDouble(1);

							y.putScalar(0, updateClick);
							y.putScalar(1, updateFlag);

							features.add(state);
							labels.add(y);


							if (!trainingBoard.isRunning() || actionCount >= 100)
								break gameloop;
						}
					}
				}
			}
		}

		if (epsilon > 0.1) 
			epsilon -= 1 / (double) 100000;
		
		ArrayList<INDArray> [] data = new ArrayList[2];
		data[0] = features;
		data[1] = labels;

		//getContext().getLog().info("{} has finished a game", getContext().getSelf().path().name());
		
		neuralNetworkActor.tell(new ModelActor.ReceiveData(data));
		
		getContext().getSelf().tell(GameActor.StartGame.INSTANCE);
		
		return this;
	}

	private double getRewardClick(Board trainingBoard, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col) != CellType.BOMB
				&& trainingBoard.getObservableCell(row, col) != CellType.FLAGGED)
			return 1;
		return -1;
	}

	private double getRewardFlag(Board trainingBoard, int initialFlagCount, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col) == CellType.BOMB) // bomb
			return 1;
		return -1;
	}

	private Behavior<Command> onUpdateNeuralNetwork(UpdateNeuralNetwork command) {
		model = command.newModel.clone();
		
		getContext().getLog().info("{} has updated their model", getContext().getSelf().path().name());
		
		return this;
	}

	private GameActor(ActorContext<Command> context, MultiLayerNetwork model, ActorRef<ModelActor.ReceiveData> neuralNetwork, int rows, int columns, int number_of_bombs) {
		super(context);
		this.model = model;
		this.neuralNetworkActor = neuralNetwork;
		this.ROWS = rows;
		this.COLUMNS = columns;
		this.NUMBER_OF_BOMBS = number_of_bombs;
		this.r = new Random();
		this.epsilon = 1.0;
		getContext().getLog().info("Creating game actor");
	}

	public static Behavior<Command> create(MultiLayerNetwork model, ActorRef<ModelActor.ReceiveData> neuralNetwork, int rows, int columns, int number_of_bombs) {
		return Behaviors.setup(context -> new GameActor(context, model, neuralNetwork, rows, columns, number_of_bombs));
	}

}
