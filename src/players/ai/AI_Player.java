package players.ai;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipException;

import enumerations.ActionType;
import enumerations.CellType;
import enumerations.ModelVersion;
import mechanics.Action;
import mechanics.Board;
import mechanics.ObservableBoard;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.learning.config.Adam;
import players.Player;

public abstract class AI_Player extends Player {
	protected MultiLayerNetwork model;
	protected Random r;
	protected ModelVersion modelVersion;


	public void init(){
		model = denseModel();
		r = new Random();
	}

	public abstract Action chooseAction(ObservableBoard board);

	public abstract void generateTrainingData() throws IOException;

	public void train() throws IOException, ClassNotFoundException {
		System.out.println("Begin Training");
		File trainingFilename = new File(modelVersion.trainingFilename);
		FileInputStream fis = new FileInputStream(trainingFilename);
		ObjectInputStream ois = new ObjectInputStream(fis);

		for (int i = 0; fis.available()!=0; i++){
			ActionValue av = (ActionValue) ois.readObject();
			if (av == null)
				break;
			model.fit(av.getStateINDArray(), av.getActionScoreINDArray());

			if (i%100==0)
				System.out.println("Game: " + i);
		}

		saveModel();
		System.out.println("End Training");
	}

	private MultiLayerNetwork denseModel() {
		File modelFilename = new File(modelVersion.modelFilename);

		try {
			if (modelFilename.createNewFile()) {
				System.out.println("New Model Created: " + modelFilename.getName());
				return createNewDenseModel();
			} else {
				try {
					System.out.println("Model loaded: " + modelFilename.getName());
					return MultiLayerNetwork.load(modelFilename, true);
				} catch (ZipException e) {
					System.out.println("New Model Created: " + modelFilename.getName());
					return createNewDenseModel();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return null;
	}

	private MultiLayerNetwork createNewDenseModel() {
		ArrayList<Layer> layers = new ArrayList<>();
		for (ModelVersion.Layer layerConfig: modelVersion.layers){
			layers.add(layerConfig instanceof ModelVersion.DenseLayer ?
					new DenseLayer.Builder().nIn(layerConfig.inputs).nOut(layerConfig.outputs).activation(layerConfig.activation).build():
					new OutputLayer.Builder(((ModelVersion.OutputLayer) layerConfig).lossFunction).activation(layerConfig.activation).nIn(layerConfig.inputs).nOut(layerConfig.outputs).build()
			);
		}

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(modelVersion.seed).weightInit(modelVersion.weightInit)
				.updater(new Adam(modelVersion.learningRate)).optimizationAlgo(modelVersion.optimizationAlgorithm)
				.list(layers.toArray(new Layer[0]))
				.build();

		return new MultiLayerNetwork(conf);
	}

	private void saveModel() {
		System.out.println("Saving model. Do not close.");
		File locationToSave = new File(modelVersion.modelFilename);
		try {
			model.save(locationToSave, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Model Saved");
	}


}
