package players.ai;

import interfaces.CellType;
import main.GetConfig;
import mechanics.ObservableBoard;
import neuralnetwork.ActionValue;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.zip.ZipException;

public class CNN_Full_Player extends AI_Player{
    private final MultiLayerNetwork model;
    private final Random r;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new CNN_Full_Player().train();
    }

    public CNN_Full_Player(){
        model = denseModel();
        r = new Random();
    }

    public int chooseAction(ObservableBoard board) {
        for (int row = 0; row < board.ROWS; row++) {
            for (int col = 0; col < board.COLUMNS; col++) {
                if (board.getObservableCell(row, col).getCellType() == CellType.HIDDEN) {
                    INDArray state = Nd4j.create(board.serializeState5x5(row, col));
                    INDArray qval = model.output(state);

                    double val = qval.getDouble(0);
                    if (val > 0.9)
                        return row * board.COLUMNS + col;
                    else if (val < -0.9)
                        return row * board.COLUMNS + col + (board.ROWS * board.COLUMNS);
                }
            }
        }

        return r.nextInt(board.ROWS * board.COLUMNS * 2); //random action lul
    }

    public void train() throws IOException, ClassNotFoundException {
        saveModel();

        System.out.println("Begin Training");
        File file = new File(GetConfig.getInstance().getPropertyAsString("trainingdata"));
        System.out.println(file.getName());
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        for (int i = 0; fis.available()!=0; i++){
            ActionValue av = (ActionValue) ois.readObject();
            model.fit(av.getStateINDArray(), av.getActionScoreINDArray());
            if (i%100000==0)
                System.out.println("Game: "  + i);
        }

        saveModel();
        ois.close();
        System.out.println("End Training");
    }

    private MultiLayerNetwork denseModel() {
        GetConfig gc = GetConfig.getInstance();

        int seed = gc.getPropertyAsInt("seed");
        double learningRate = gc.getPropertyAsDouble("learningRate");
        int numInputs = gc.getPropertyAsInt("numInputs");
        int numHiddenNodes = gc.getPropertyAsInt("numHiddenNodes");
        int numOutputs = gc.getPropertyAsInt("numOutputs");
        File file = new File(gc.getPropertyAsString("modelname"));

        try {
            if (file.createNewFile()) {
                System.out.println("New Model Created: " + file.getName());
                return createNewDenseModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);
            } else {
                try {
                    System.out.println("Model loaded: " + file.getName());
                    return MultiLayerNetwork.load(file, true);
                } catch (ZipException e) {
                    System.out.println("New Model Created: " + file.getName());
                    return createNewDenseModel(seed, learningRate, numInputs, numHiddenNodes, numOutputs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        return null;
    }

    private MultiLayerNetwork createNewDenseModel(int seed, double learningRate, int numInputs, int numHiddenNodes,
                                                  int numOutputs) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate)).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunction.MSE).activation(Activation.TANH).nIn(numHiddenNodes)
                        .nOut(numOutputs).build())
                .build();


        return new MultiLayerNetwork(conf);
    }

    private void saveModel() {
        System.out.println("Saving model. Do not close.");
        String fileName = GetConfig.getInstance().getPropertyAsString("modelname");
        File locationToSave = new File(fileName);
        try {
            model.save(locationToSave, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Model Saved");
    }
}
