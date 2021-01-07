package neuralnetwork;

import interfaces.CellType;
import interfaces.PlayerType;
import main.GetConfig;
import mechanics.Board;
import mechanics.ObservableBoard;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import players.ai.AI_Player;
import players.ai.CNN_5x5_Player;

import java.io.*;
import java.util.*;

public class TrainingData {

    public static void main(String args[]) throws IOException, ClassNotFoundException {

        String filename = GetConfig.getInstance().getPropertyAsString("trainingdata5x5");
        TrainingData td = new TrainingData(filename, Integer.MAX_VALUE);
        td.generateDataFor(PlayerType.AI_5x5);

    }


    private final int numGames;
    private final File file;
    private final Random r;

    public TrainingData(String filename, int numGames) throws IOException{
        this.numGames = numGames;
        this.file = new File(filename);
        this.r = new Random();

        FileOutputStream fos = new FileOutputStream(file, true);
        //ObjectOutputStream oos = new ObjectOutputStream(fos);

        ObjectOutputStream oos = new ObjectOutputStream(fos) {
            protected void writeStreamHeader() throws IOException {
                reset();
            }
        };
    }



    public void generateDataFor(PlayerType playerType) throws IOException {
        int dataCount = 0;

        FileOutputStream fos = new FileOutputStream(file, true);
        //ObjectOutputStream oos = new ObjectOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(fos) {
            protected void writeStreamHeader() throws IOException {
                reset();
            }
        };

        //only applicable for 5x5
        List<CellType> emptyState = new ArrayList<>(Collections.nCopies(25, CellType.HIDDEN));

        for (int i = 0; i < numGames && dataCount < 5000000; i++){
            Board trainingBoard = new Board(16, 30, 16*15); //half of all squares are bombs to remove bias.

            //initialize board with random click
            trainingBoard.clickCellInitial(r.nextInt(16), r.nextInt(30));

            while (trainingBoard.isRunning()) {
                for (int row = 0; row < trainingBoard.ROWS; row++) {
                    for (int col = 0; col < trainingBoard.COLUMNS; col++) {
                        if (trainingBoard.getObservableCell(row, col).getCellType() == CellType.HIDDEN
                                || trainingBoard.getObservableCell(row, col).getCellType() == CellType.FLAGGED) {

                            if (playerType == PlayerType.AI_5x5){
                                List<CellType> stateAsList = trainingBoard.get5x5StateAsList(row, col);
                                if (!stateAsList.equals(emptyState)){
                                    boolean[][] state = trainingBoard.serializeState5x5(row, col);
                                    byte[] bytes = toByteArray(state);
                                   // fos.write(bytes);
                                    //dont forget to write score in there as well

                                 /*   INDArray state = Nd4j.create();
                                    int actionScore = getActionScore5x5(trainingBoard, row, col);
                                    ActionValue av = new ActionValue(state, actionScore);
                                    oos.writeObject(av);
                                    oos.flush();

                                  */
                                    dataCount++;
                                }
                            } else {
                                INDArray state = Nd4j.create(trainingBoard.serializeState());
                                //int actionScore = getActionScore(trainingBoard, row, col);
                                //not sure what i want to save

                            }

                            //making sure it doesn't lose just to improve state diversity
                            if (r.nextInt(2)==0) { //50% of the time do an action
                                int action = row * trainingBoard.COLUMNS + col;
                                if (trainingBoard.getCell(row, col).getSecretStatus() != CellType.BOMB){ //click
                                    trainingBoard.playMove(action);
                                } else {//flag
                                    trainingBoard.playMove(action + (trainingBoard.ROWS * trainingBoard.COLUMNS));
                                }
                            }
                        }
                    }
                }
            }
            if (i%100==0)
                System.out.println("Game: " + i + " -> Size: " + dataCount);
        }
        System.out.println("Training Set Size: " + dataCount);
        oos.close();
    }


    public byte[] toByteArray(boolean[][] input) {
        int totalBytes = (int) Math.ceil(input[0].length / 8.0);
        byte[] bytes = new byte[totalBytes];

        for (int i = 0; i < totalBytes; i++) {
            for (int bit = 0; bit < 8; bit++) {
                int bitIndex = i * 8 + bit;
                if (input[0].length > bitIndex && input[0][bitIndex])
                    bytes[i] |= (128 >> bit);
            }
        }

        return bytes;
    }


    public void printTrainingData() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        int i = 0;
        while (fis.available()!=0){
            ActionValue av = (ActionValue) ois.readObject();
            System.out.println(av.getActionScore());
            System.out.println(av.getActionScoreINDArray());
            System.exit(0);
            i++;
        }

        System.out.println("Data Count: " + i);
        ois.close();
    }

    /*
    -1 means flag, 1 means click (uncertain will be 0)
    Problem with this is that most of the time, the cells will be case - no bomb, hidden. So
    the reward for all empty squares will be skewed towards clicking... right???
     */
    private int getActionScore5x5(ObservableBoard trainingBoard, int row, int col){
        if (trainingBoard.getCell(row, col).getSecretStatus() == CellType.BOMB){
            if (trainingBoard.getObservableCell(row, col).getCellType() == CellType.FLAGGED){
                return 1; //bomb, flagged
            } else {
                return -1; //bomb, unflagged
            }
        } else {
            if (trainingBoard.getObservableCell(row, col).getCellType() == CellType.FLAGGED){
                return -1; //no bomb, flagged
            } else {
                return 1; //no bomb, hidden
            }
        }
    }

}

