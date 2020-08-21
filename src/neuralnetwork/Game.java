package neuralnetwork;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Random;

import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import interfaces.StatusConstants;
import mechanics.Board;
import mechanics.ObservableBoard;
import neuralnetwork.NeuralNetwork.Command;

public class Game extends AbstractBehavior<Game.Command> implements StatusConstants {

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

	private ActorRef<NeuralNetwork.Command> neuralNetworkActor;
	
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
		getContext().getLog().info("{} is starting a new game", getContext().getSelf().path().name());
		
		Board trainingBoard = new Board(ROWS, COLUMNS, NUMBER_OF_BOMBS);
		long epochStartTime = System.nanoTime();

		Iterable<Pair<INDArray, INDArray>> iter = new ArrayList<Pair<INDArray, INDArray>>();
		int actionCount = 0;

		gameloop: while (!trainingBoard.isBoardInitialized() || trainingBoard.isRunning()) {
			for (int row = 0; row < trainingBoard.ROWS; row++) {
				for (int col = 0; col < trainingBoard.COLUMNS; col++) {
					if (trainingBoard.getObservableCell(row, col).getStatus() == STATUS_HIDDEN
							|| trainingBoard.getObservableCell(row, col).getStatus() == STATUS_FLAGGED) {
						INDArray state = Nd4j.create(trainingBoard.getState(row, col));
						INDArray qval = model.output(state);

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

							Pair<INDArray, INDArray> p = new Pair<INDArray, INDArray>(state, y);
							((ArrayList<Pair<INDArray, INDArray>>) iter).add(p);

							if (!trainingBoard.isRunning() || actionCount >= 100)
								break gameloop;
						}
					}
				}
			}
		}

		DataSetIterator iterator = new INDArrayDataSetIterator((Iterable) iter,
				((ArrayList<Pair<INDArray, INDArray>>) iter).size());
		((ArrayList<Pair<INDArray, INDArray>>) iter).clear();

		getContext().getLog().info("{} has finished a game", getContext().getSelf().path().name());
		
		neuralNetworkActor.tell(new NeuralNetwork.ReceiveData(iterator));
		
		getContext().getSelf().tell(Game.StartGame.INSTANCE);
		
		return this;
	}

	private double getRewardClick(ObservableBoard trainingBoard, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() != STATUS_BOMB
				&& trainingBoard.getObservableCell(row, col).getStatus() != STATUS_FLAGGED)
			return 1;
		return -1;
	}

	private double getRewardFlag(ObservableBoard trainingBoard, int initialFlagCount, int row, int col) {
		if (trainingBoard.isBoardInitialized() && trainingBoard.getCell(row, col).getSecretStatus() == STATUS_BOMB) // bomb
			return 1;
		return -1;
	}

	private Behavior<Command> onUpdateNeuralNetwork(UpdateNeuralNetwork command) {
		model = command.newModel.clone();
		
		getContext().getLog().info("{} has updated their model", getContext().getSelf().path().name());
		
		return this;
	}

	private Game(ActorContext<Command> context, MultiLayerNetwork model, ActorRef<NeuralNetwork.Command> neuralNetwork) {
		super(context);
		this.model = model;
		this.neuralNetworkActor = neuralNetwork;
	}

	public static Behavior<Command> create(MultiLayerNetwork model, ActorRef<NeuralNetwork.Command> neuralNetwork) {
		return Behaviors.setup(context -> new Game(context, model, neuralNetwork));
	}

}
