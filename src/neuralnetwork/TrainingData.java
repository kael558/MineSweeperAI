package neuralnetwork;

import interfaces.CellType;
import interfaces.PlayerType;
import main.GetConfig;
import mechanics.Board;
import org.apache.commons.lang3.ArrayUtils;
import java.io.*;
import java.util.*;

public class TrainingData {

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        String filename = GetConfig.getInstance().getPropertyAsString("trainingdata");
        TrainingData td = new TrainingData(filename, Integer.MAX_VALUE, 3000000); //3000000 * 300bytes ~ 1gb of data
        td.generate();
        td.printTrainingData();
    }

    private final int numDataCount;
    private final int numGames;
    private final File file;
    private final Random r;

    public TrainingData(String filename, int numGames, int numDataCount) throws IOException {
        this.numDataCount = numDataCount;
        this.numGames = numGames;
        this.file = new File(filename);
        if (file.createNewFile())
            System.out.println("Created new file: " + filename);
        this.r = new Random();
    }

    public void generate() throws IOException {
        int dataCount = 0;
        FileOutputStream fos = new FileOutputStream(file, true);

        for (int i = 0; i < numGames && dataCount < numDataCount; i++){
            Board trainingBoard = new Board(16, 30, 99);

            //initialize board with random click
            trainingBoard.clickCellInitial(r.nextInt(16), r.nextInt(30));

            while (trainingBoard.isRunning()) {
                for (int row = 0; row < trainingBoard.ROWS; row++) {
                    for (int col = 0; col < trainingBoard.COLUMNS; col++) {
                        if (trainingBoard.getObservableCell(row, col).getCellType() == CellType.HIDDEN
                                || trainingBoard.getObservableCell(row, col).getCellType() == CellType.FLAGGED) {

                            //serialize entire board, with rewards for each action
                            byte[] state = trainingBoard.serializeState();
                            byte[] qArray = serializeActionRewards(trainingBoard);
                            byte[] bytes = ArrayUtils.addAll(state, qArray);
                            fos.write(bytes); //each state: 241 bytes + 60 bytes
                            dataCount++;

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
        fos.close();
    }

    /**
     * Pre-condition : rows*columns must be multiple of 8 (otherwise is truncated)
     * 1 bit per square (0 for flag, 1 for click) -> 8 squares per byte
     * ~ 480 squares -> 60 bytes
     * @param board
     * @return
     */
    public byte[] serializeActionRewards(Board board){
        double squareToByteRatio = 1/8.0;
        byte[] bytes = new byte[(int)(board.ROWS*board.COLUMNS*squareToByteRatio)];

        int row = 0;
        int col = 0;
        for (int i = 0; i < bytes.length; i++){
            byte b = 0;
            for (int j = 7; j >= 0; j--){
                if (board.getCell(row, col).getSecretStatus() != CellType.BOMB)
                    b |= 1 << j;
                col++;
                if (col % board.COLUMNS == 0){
                    row++;
                    col = 0;
                }
            }
            bytes[i] = b;
        }

        return bytes;
    }

    public void printTrainingData() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        int byteCount = fis.available();

        System.out.println("Byte count: " + byteCount);
        System.out.println("Data count: " + byteCount/301);
        fis.close();
    }

}

