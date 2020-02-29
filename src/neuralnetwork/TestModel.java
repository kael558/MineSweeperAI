package neuralnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import main.PlayGame;
import mechanics.Board;

public class TestModel {

	Board board;
	PlayGame play;

	int ROWS, COLUMNS, TOTAL_NUMBER_OF_BOMBS;

	public TestModel() {
		ROWS = 9;
		COLUMNS = 9;
		TOTAL_NUMBER_OF_BOMBS = 10;

		// makeTestCases();

		testTestCases();
	}

	private void testTestCases() {
		play = new PlayGame(ROWS, COLUMNS, TOTAL_NUMBER_OF_BOMBS);
		for (int i = 1; i <= 10; i++) {
			ObjectInputStream in;

			try {
				in = new ObjectInputStream(new FileInputStream("testcases//" + i + ".txt"));
				board = (Board) in.readObject();
				play.setBoard(board);
				play.start();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void makeTestCases() {

		Random r = new Random();

		for (int i = 1; i <= 10; i++) {
			Board temp = new Board(ROWS, COLUMNS, TOTAL_NUMBER_OF_BOMBS);
			temp.drawObservableBoard();
			temp.clickCellInitial(r.nextInt(ROWS), r.nextInt(COLUMNS));
			temp.drawObservableBoard();
			temp.drawSecretBoard();

			ObjectOutputStream out;

			try {
				out = new ObjectOutputStream(new FileOutputStream("testcases//" + i + ".txt"));
				out.writeObject(temp);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
