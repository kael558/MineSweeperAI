package players.ai;

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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipException;

public class CNN_5x5_Player extends AI_Player {

    /*
  Supervised learning
  Training q(s, a) to achieve optimal policy
   */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CNN_5x5_Player cnn_5x5_player = new CNN_5x5_Player();
        cnn_5x5_player.generateTrainingData();
    }

    public CNN_5x5_Player(){
        super();
        modelVersion = ModelVersion.AI_5x5_v1;
        init();
    }

    public void generateTrainingData() throws IOException {
        int numDataCount = 5000000;
        int numGames = Integer.MAX_VALUE;
        File file = new File(modelVersion.trainingFilename);
        if (file.createNewFile())
            System.out.println("Created new file: " + file.getName());
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
                            boolean[][] actionScore = {{trainingBoard.getCell(row, col) != CellType.BOMB}};
                            ActionValue av = new ActionValue(state, actionScore);
                            oos.writeObject(av);
                            dataCount++;

                            //making sure it doesn't lose just to improve state diversity
                            if (r.nextInt(2)==0) { //50% of the time do an action
                                Action action =  new Action(
                                        trainingBoard.getCell(row, col) == CellType.BOMB
                                                ? ActionType.FLAG : ActionType.CLICK,
                                        row, col);
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
        File trainingFilename = new File(modelVersion.trainingFilename);
        FileInputStream fis = new FileInputStream(trainingFilename);
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
                    else if (val < 0.1)
                        return new Action(ActionType.FLAG, row, col);
                }
            }
        }

        return new Action(board.ROWS,board.COLUMNS); //random action lul
    }
}
