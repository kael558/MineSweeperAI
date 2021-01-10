package multithread_nn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ModelActor extends AbstractBehavior<ModelActor.ReceiveData> {

	private MultiLayerNetwork model;
	int count;
	int update;
	int terminate;
	String fileName;
	
	List<ActorRef<GameActor.Command>> games;

	public static class ReceiveData  {
		public final ArrayList<INDArray> [] data;

		public ReceiveData(ArrayList<INDArray> [] data) {
			this.data = data;
		}

	}

	@Override
	public Receive<ReceiveData> createReceive() {
		return newReceiveBuilder()
				.onMessage(ReceiveData.class, this::onReceiveData).build();
	}

	private Behavior<ReceiveData> onReceiveData(ReceiveData command) {
		for (int i = 0; i < command.data[0].size(); i++)
			model.fit(command.data[0].get(i), command.data[1].get(i));
		
		count++;

		if (count % 10 == 0)
			getContext().getLog().info("NN received data count: {}", count);
		
		if (count % update == 0) // send model to all games to update
			for (ActorRef<GameActor.Command> game: games)
				game.tell(new GameActor.UpdateNeuralNetwork(model));
		else if (count >= terminate) { // terminate all actors and return the final model.
			saveModel();
			getContext().stop(getContext().getSelf());
		}

		return this;
	}

	public static Behavior<ModelActor.ReceiveData> create(MultiLayerNetwork model, int update, int terminate, int numGames, String fileName) {
		return Behaviors.setup(context -> new ModelActor(context, model, update, terminate, numGames, fileName));
	}

	public ModelActor(ActorContext<ReceiveData> context, MultiLayerNetwork model, int update, int terminate, int numGames, String fileName) {
		super(context);
		getContext().getLog().info("Creating neural network actor");
		this.model = model;
		this.count = 0;
		this.update = update;
		this.terminate = terminate;
		this.fileName = fileName;
		this.games = IntStream.rangeClosed(1, numGames).mapToObj(i -> context.spawn(GameActor.create(model.clone(), getContext().getSelf(), 16, 30, 99), "Game" + i)).collect(Collectors.toList());
		for (ActorRef<GameActor.Command> game: games)
			game.tell(GameActor.StartGame.INSTANCE);
	}
	
	private void saveModel() {
		getContext().getLog().info("Saving model. Do not close...");
		File locationToSave = new File(fileName);
		try {
			model.save(locationToSave, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getContext().getLog().info("Model saved");
	}
}
