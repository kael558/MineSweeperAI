package mechanics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import interfaces.CellType;
import interfaces.GameStatus;

//Partially Observable Environment
public class ObservableBoard implements  Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6927416557556575516L;
	public int ROWS;
	public int COLUMNS;

	public CellType[][] observableBoard;

	private GameStatus gameStatus;
	private int squaresRevealedCount;
	private int flagCount;
	public int totalBombs;

	public ObservableBoard() {
		ROWS = 16;
		COLUMNS = 30;

		totalBombs = 99;

		gameStatus = GameStatus.IN_PROGRESS;
		flagCount = 99;
		squaresRevealedCount = 0;

		observableBoard = new CellType[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = CellType.HIDDEN;
			}
		}
	}

	public ObservableBoard(int rows, int columns, int number_of_bombs) {
		ROWS = rows;
		COLUMNS = columns;

		totalBombs = number_of_bombs;

		gameStatus = GameStatus.IN_PROGRESS;
		flagCount = number_of_bombs; // flags remaining
		squaresRevealedCount = 0;

		observableBoard = new CellType[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = CellType.HIDDEN;
			}
		}
	}

	public ObservableBoard(ObservableBoard board) {
		ROWS = board.ROWS;
		COLUMNS = board.COLUMNS;

		totalBombs = board.totalBombs;

		gameStatus = GameStatus.IN_PROGRESS;
		flagCount = board.totalBombs; // flags remaining
		squaresRevealedCount = board.getSquaresRevealedCount();

		observableBoard = new CellType[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = CellType.HIDDEN;
			}
		}
	}


	public void drawObservableBoard() {
		System.out.print("  ");
		for (int i = 0; i < COLUMNS; i++) {
			System.out.printf("%3d", i);
		}

		System.out.println();
		for (int i = 0; i < ROWS; i++) {
			System.out.printf("%-3d", i);
			for (int j = 0; j < COLUMNS; j++) {
				if (observableBoard[i][j] == CellType.HIDDEN) {
					System.out.print("|_|");
				} else if (observableBoard[i][j] == CellType.FLAGGED) {
					System.out.print(" F ");
				} else if (observableBoard[i][j] == CellType.BOMB) {
					System.out.print("-1 ");
				} else if (observableBoard[i][j] == CellType.SQUARE0) {
					System.out.print("   ");
				} else {
					System.out.printf("%2d", observableBoard[i][j].ordinal());
					System.out.print(" ");
				}
			}
			System.out.println();
		}
	}


	public void updateGameCondition() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (observableBoard[i][j] == CellType.BOMB) {
					gameStatus = GameStatus.LOST;
					return;
				}
			}
		}

		if (ROWS * COLUMNS - totalBombs == squaresRevealedCount) {
			gameStatus = GameStatus.WIN;
		}
	}

	public boolean isRunning() {
		if (gameStatus == GameStatus.IN_PROGRESS) {
			return true;
		}
		return false;
	}

	public void incrementFlagCount() {
		flagCount++;
	}

	public void decrementFlagCount() {
		flagCount--;
	}

	public void incrementSquaresRevealedCount() {
		squaresRevealedCount++;
	}

	public void resetSquaresRevealedCount() {
		squaresRevealedCount = 0;
	}

	/* GETTERS */
	public CellType getObservableCell(int row, int col) {
		return observableBoard[row][col];
	}

	public GameStatus getGameCondition() {
		return gameStatus;
	}

	public int getNumberOfBombs() {
		return totalBombs;
	}

	public int getFlagCount() {
		return flagCount;
	}

	public int getSquaresRevealedCount() {
		return squaresRevealedCount;
	}

	/*
	public boolean[][] serializeState(){
		int bitsPerSquare = 4;
		int flagBits = 7;
		boolean[][] state = new boolean[1][ROWS * COLUMNS * bitsPerSquare + flagBits];

		int bitCount = 0;
		for (int row = 0; row < ROWS; row++)
			for (int col = 0; col < COLUMNS; col++) {
				appendToBooleanArray(state, bitCount, observableBoard[row][col].ordinal());
				bitCount+=4;
			}

		appendToBooleanArray(state, bitCount, flagCount);
		return state;
	}
*/

	/**
	 * 24 squares * 4 bits = 96 bits
	 * +1 bit for middle square hidden or flagged
	 */
	public boolean[][] serializeState5x5(int selectedRow, int selectedCol) {
		boolean hasNonHidden = false;
		int bitsPerSquare = 4;
		boolean[][] state = new boolean[1][24 * bitsPerSquare + 1];

		int bitCount = 0;
		for (int row = selectedRow - 2; row <= selectedRow + 2; row++) {
			for (int col = selectedCol - 2; col <= selectedCol + 2; col++) {
				if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS) {
					if (!hasNonHidden && observableBoard[row][col] != CellType.HIDDEN)
						hasNonHidden = true;
					if (!(selectedRow == row && selectedCol == col)) {
						appendToBooleanArray(state, bitCount, observableBoard[row][col].ordinal());
						bitCount += 4;
					}
				} else {
					appendToBooleanArray(state, bitCount, CellType.INVALID.ordinal());
					bitCount += 4;
				}
			}
		}

		if (!hasNonHidden) //return null if state is all hidden cells.
			return null;
		state[0][bitCount] = observableBoard[selectedRow][selectedCol] == CellType.HIDDEN;
		return state;
	}

	private void appendToBooleanArray(boolean[][] arr, int index, int value){
		for (int i = index+3; i>=index; i--, value/=2)
			arr[0][i] = value%2==1;
	}


	/* Overridden Methods */
	/* Actions */
	public void playMove(Action action) {

	}

	public void flagCell(int selectedRow, int selectedCol) {
		if (observableBoard[selectedRow][selectedCol] == CellType.HIDDEN) {
			observableBoard[selectedRow][selectedCol] = CellType.FLAGGED;
			decrementFlagCount();

		} else if (observableBoard[selectedRow][selectedCol] == CellType.FLAGGED) {
			observableBoard[selectedRow][selectedCol] = CellType.HIDDEN;
			incrementFlagCount();

		} else {
			// System.out.println("Cannot flag a revealed cell");

		}
	}

	public void clickCellInitial(int selectedRow, int selectedCol) {

	}

	public void initializeBoard(int row, int col) {

	}

	public boolean isBoardInitialized() {
		return false;
	}

	public void drawSecretBoard() {
		// TODO REMOVE
	}

	public void updateObservableBoard() {

	}
}
