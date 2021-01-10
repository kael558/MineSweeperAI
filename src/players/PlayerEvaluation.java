package players;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import enumerations.PlayerType;
import main.PlayGame;
import mechanics.Board;

public class PlayerEvaluation {

	Board board;
	PlayGame play;

	int ROWS, COLUMNS, TOTAL_NUMBER_OF_BOMBS;
	int agentType;
	
	
	public PlayerEvaluation(int rows, int cols, int bombs, int agentType) {
		ROWS = rows;
		COLUMNS = cols;
		TOTAL_NUMBER_OF_BOMBS = bombs;
		this.agentType = agentType;
		
	//	makeTestCases();
		testTestCases();
	}

	private void makeTestCases() {

		Random r = new Random();

		for (int i = 1; i <= 10; i++) {
			Board temp = new Board(ROWS, COLUMNS, TOTAL_NUMBER_OF_BOMBS);
			temp.clickCellInitial(r.nextInt(ROWS), r.nextInt(COLUMNS));
			ObjectOutputStream out;

			try {
				out = new ObjectOutputStream(new FileOutputStream("testcases//" + ROWS + "x" + COLUMNS + "b" + TOTAL_NUMBER_OF_BOMBS + "_" + i + ".txt"));
				out.writeObject(temp);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
	
	private void testTestCases() {
		play = new PlayGame(null, PlayerType.AI_5x5, false, false);
		for (int i = 2; i <= 10; i++) {
			ObjectInputStream in;

			try {
				in = new ObjectInputStream(new FileInputStream("testcases//" + ROWS + "x" + COLUMNS + "b" + TOTAL_NUMBER_OF_BOMBS + "_" + i + ".txt"));
				board = (Board) in.readObject();
				board.drawSecretBoard();
				play.setBoard(board);
				play.start();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
