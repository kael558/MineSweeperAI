package players.ai;

import interfaces.ActionType;
import interfaces.CellType;
import main.GetConfig;
import mechanics.Action;
import mechanics.Board;
import mechanics.ObservableBoard;
import neuralnetwork.ActionValue;
import org.apache.commons.lang3.ArrayUtils;
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

import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipException;

public class CNN_5x5_Player extends AI_Player {

    /*
  Supervised learning
  Training q(s, a) to achieve optimal policy
   */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CNN_5x5_Player cnn_5x5_player = new CNN_5x5_Player();
        cnn_5x5_player.train();
    }

    private final MultiLayerNetwork model;
    private final Random r;


    public CNN_5x5_Player(){
        model = denseModel();
        r = new Random();
    }

    public void generate() throws IOException {
        String filename = GetConfig.getInstance().getPropertyAsString("trainingdata5x5");
        int numDataCount = 5000000;
        int numGames = Integer.MAX_VALUE;
        File file = new File(filename);
        if (file.createNewFile())
            System.out.println("Created new file: " + filename);
        Random r = new Random();

        int dataCount = 0;
        FileOutputStream fos = new FileOutputStream(file, true);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        for (int i = 0; i < numGames && dataCount < numDataCount; i++){
            Board trainingBoard = new Board(16, 30, 99);

            //initialize board with random click
            trainingBoard.clickCellInitial(r.nextInt(16), r.nextInt(30));

            while (trainingBoard.isRunning()) {
                for (int row = 0; row < trainingBoard.ROWS; row++) {
                    for (int col = 0; col < trainingBoard.COLUMNS; col++) {
                        if (trainingBoard.getObservableCell(row, col) == CellType.HIDDEN
                                || trainingBoard.getObservableCell(row, col) == CellType.FLAGGED) {

                            boolean[][] state = trainingBoard.serializeState5x5(row, col);
                            if (state == null) //this means state is just all hidden cells
                                continue;
                            int actionScore = trainingBoard.getCell(row, col) != CellType.BOMB ? 1 : 0;
                            ActionValue av = new ActionValue(state, actionScore);
                            oos.writeObject(av);
                            dataCount++;



                            //making sure it doesn't lose just to improve state diversity
                            if (r.nextInt(2)==0) { //50% of the time do an action
                                Action action =  new Action(
                                        trainingBoard.getCell(row, col) == CellType.BOMB ? ActionType.FLAG: ActionType.CLICK,
                                        row,
                                        col);
                                trainingBoard.playMove(action);
                            }
                        }
                    }
                }
            }
            if (i%100==0)
                System.out.println("Game: " + i + " -> Size: " + dataCount + " -> " + trainingBoard.getSquaresRevealedCount());
        }
        System.out.println("Training Set Size: " + dataCount);
        oos.close();
    }

    public void printData() throws IOException, ClassNotFoundException {
        File file = new File(GetConfig.getInstance().getPropertyAsString("trainingdata5x5"));
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        int i = 0;

        while (fis.available()!=0){
            ActionValue av = (ActionValue) ois.readObject();
            INDArray state = av.getStateINDArray();
            INDArray actionScore = av.getActionScoreINDArray();
            System.out.println(state);
            System.out.println(actionScore);
            i++;
            if (i == 5)
                System.exit(0);
        }
    }


    public Action chooseAction(ObservableBoard board) {
        for (int row = 0; row < board.ROWS; row++) {
            for (int col = 0; col < board.COLUMNS; col++) {
                if (board.getObservableCell(row, col) == CellType.HIDDEN) {
                    INDArray state = Nd4j.create(board.serializeState5x5(row, col));
                    INDArray qval = model.output(state);

                    double val = qval.getDouble(0);
                    if (val > 0.9)
                        return new Action(ActionType.CLICK, row, col);
                    else if (val < -0.9)
                        return new Action(ActionType.FLAG, row, col);
                }
            }
        }

        return new Action(board.ROWS,board.COLUMNS); //random action lul
    }

    public void train() throws IOException, ClassNotFoundException {
        saveModel();

        System.out.println("Begin Training");
        File file = new File(GetConfig.getInstance().getPropertyAsString("trainingdata"));
        FileInputStream fis = new FileInputStream(file);
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
        GetConfig gc = GetConfig.getInstance();

        int seed = gc.getPropertyAsInt("seed5x5");
        double learningRate = gc.getPropertyAsDouble("learningRate5x5");
        int numInputs = gc.getPropertyAsInt("numInputs5x5");
        int numHiddenNodes = gc.getPropertyAsInt("numHiddenNodes5x5");
        int numOutputs = gc.getPropertyAsInt("numOutputs5x5");
        File file = new File(gc.getPropertyAsString("modelname5x5"));

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
