package players.ai;

import enumerations.ActionType;
import enumerations.CellType;
import enumerations.ModelVersion;
import mechanics.Action;
import mechanics.Board;
import mechanics.ObservableBoard;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.Random;

public class CNN_Full_Player extends AI_Player{
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new CNN_Full_Player().generateTrainingData();
    }

    public CNN_Full_Player(){
        super();
        modelVersion = ModelVersion.AI_Full;
        r = new Random();
        //init();
    }

    public long countData() throws IOException, ClassNotFoundException {
        File trainingFilename = new File(modelVersion.trainingFilename);
        FileInputStream fis = new FileInputStream(trainingFilename);
        ObjectInputStream ois = new ObjectInputStream(fis);

        long count = 0;
        for (; fis.available()!=0; count++){
            ActionValue av = (ActionValue) ois.readObject();
            if (av == null)
                break;
            if (count%100000==0)
                System.out.println("Count: " + count);
        }
        return count;
    }

    public Action chooseAction(ObservableBoard board) {
        INDArray state = Nd4j.create(board.serializeState());
        double[] qval = model.output(state).toDoubleVector();
        for (int i = 0; i < qval.length; i++)
            if (qval[i] > 0.9)
                return new Action(ActionType.CLICK, i/board.COLUMNS, i%board.COLUMNS);
            else if (qval[i] < 0.1)
                return new Action(ActionType.FLAG, i/board.COLUMNS, i%board.COLUMNS);
        return new Action(board.ROWS,board.COLUMNS); //random action lul
    }

    public void generateTrainingData() throws IOException {
        int rows = 16, columns = 30, num_bombs = 99;

        int numDataCount = 5000000;
        int numGames = Integer.MAX_VALUE;
        File file = new File(modelVersion.trainingFilename);
        if (file.createNewFile())
            System.out.println("Created new file: " + file.getName());
        Random r = new Random();

        int dataCount = 0;
        FileOutputStream fos = new FileOutputStream(file, true);
        ObjectOutputStream oos = new ObjectOutputStream(fos){
            @Override
            protected void writeStreamHeader() throws IOException {
                reset();
            }
        };

        for (int i = 0; i < numGames && dataCount < numDataCount; i++) {
            Board trainingBoard = new Board(rows, columns, num_bombs);

            //initialize board with random click
            trainingBoard.clickCellInitial(r.nextInt(rows), r.nextInt(columns));

            while (trainingBoard.isRunning()) {
                boolean[][] state = trainingBoard.serializeState();
                boolean[][] actionScore = new boolean[1][rows * columns];
                int index = 0;
                for (int row = 0; row < trainingBoard.ROWS; row++)
                    for (int col = 0; col < trainingBoard.COLUMNS; col++)
                        actionScore[0][index++] = trainingBoard.getCell(row, col) != CellType.BOMB;

                ActionValue av = new ActionValue(state, actionScore);
                oos.writeObject(av);
                dataCount++;

                boolean foundMove = false;
                int row = r.nextInt(rows), col = r.nextInt(columns);
                if (trainingBoard.getSquaresRevealedCount() < 200) {
                    while (trainingBoard.getObservableCell(row, col) != CellType.HIDDEN) {
                        row = r.nextInt(rows);
                        col = r.nextInt(columns);
                    }
                    foundMove = true;
                } else{
                    searchForHiddenCell:
                    for (; row < trainingBoard.ROWS; row++)
                        for (; col < trainingBoard.COLUMNS; col++)
                            if (trainingBoard.getObservableCell(row, col) == CellType.HIDDEN) {
                                foundMove = true;
                                break searchForHiddenCell;
                            }
                }

                if (foundMove) {
                    Action action = new Action(
                            trainingBoard.getCell(row, col) == CellType.BOMB
                                    ? ActionType.FLAG : ActionType.CLICK,
                            row, col);

                    trainingBoard.playMove(action);
                }
            }

            if (i%100==0)
                System.out.println("Game: " + i + " -> Size: " + dataCount + " -> " + trainingBoard.getSquaresRevealedCount());
        }
    }
}
