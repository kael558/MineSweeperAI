package mechanics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import interfaces.CellType;

//Partially Observable Environment
public class ObservableBoard implements  Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6927416557556575516L;
	public int ROWS;
	public int COLUMNS;

	public ObservableCell observableBoard[][];

	private String gameCondition;
	private int squaresRevealedCount;
	private int flagCount;
	public int totalBombs;

	public ObservableBoard() {
		ROWS = 16;
		COLUMNS = 30;

		totalBombs = 99;

		gameCondition = "In Progress";
		flagCount = 99;
		squaresRevealedCount = 0;

		observableBoard = new ObservableCell[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = new ObservableCell();
			}
		}
	}

	public ObservableBoard(int rows, int columns, int number_of_bombs) {
		ROWS = rows;
		COLUMNS = columns;

		totalBombs = number_of_bombs;

		gameCondition = "In Progress";
		flagCount = number_of_bombs; // flags remaining
		squaresRevealedCount = 0;

		observableBoard = new ObservableCell[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = new ObservableCell();
			}
		}
	}

	public ObservableBoard(ObservableBoard board) {
		ROWS = board.ROWS;
		COLUMNS = board.COLUMNS;

		totalBombs = board.totalBombs;

		gameCondition = "In Progress";
		flagCount = board.totalBombs; // flags remaining
		squaresRevealedCount = board.getSquaresRevealedCount();

		observableBoard = new ObservableCell[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col] = new ObservableCell(board.getObservableCell(row, col).getCellType());
			}
		}
	}

	public void drawObservableBoard() {
		System.out.printf("  ");
		for (int i = 0; i < COLUMNS; i++) {
			System.out.printf("%3d", i);
		}

		System.out.println();
		for (int i = 0; i < ROWS; i++) {
			System.out.printf("%-3d", i);
			for (int j = 0; j < COLUMNS; j++) {
				if (observableBoard[i][j].getCellType() == CellType.HIDDEN) {
					System.out.print("|_|");
				} else if (observableBoard[i][j].getCellType() == CellType.FLAGGED) {
					System.out.print(" F ");
				} else if (observableBoard[i][j].getCellType() == CellType.BOMB) {
					System.out.print("-1 ");
				} else if (observableBoard[i][j].getCellType() == CellType.SQUARE0) {
					System.out.print("   ");
				} else {
					System.out.printf("%2d", observableBoard[i][j].getCellType());
					System.out.print(" ");
				}
			}
			System.out.println();
		}
	}

	public void resetObservableBoard() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col].setCellType(CellType.HIDDEN);
			}
		}
		gameCondition = "In Progress";
		flagCount = 99;
		squaresRevealedCount = 0;
	}

	public void updateGameCondition() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (observableBoard[i][j].getCellType() == CellType.BOMB) {
					gameCondition = "Loser";
				}
			}
		}

		if (ROWS * COLUMNS - totalBombs == squaresRevealedCount) {
			gameCondition = "Winner";
		}
	}

	public boolean isRunning() {
		if (gameCondition.equals("In Progress")) {
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
	public ObservableCell getObservableCell(int row, int col) {
		return observableBoard[row][col];
	}

	public String getGameCondition() {
		return gameCondition;
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


	public boolean[][] serializeState(){
		int bitsPerSquare = 4;
		int flagBits = 7;
		boolean[][] state = new boolean[1][ROWS * COLUMNS * bitsPerSquare + flagBits];

		int bitCount = 0;
		for (int row = 0; row < ROWS; row++)
			for (int col = 0; col < COLUMNS; col++) {
				appendToBooleanArray(state, bitCount, observableBoard[row][col].getCellType().ordinal());
				bitCount+=4;
			}

		appendToBooleanArray(state, bitCount, flagCount);
		return state;
	}

	/**
	 * 24 squares * 4 bits = 96 bits
	 * +1 bit for middle square hidden or flagged
	 */
	public boolean[][] serializeState5x5(int selectedRow, int selectedCol) {
		int bitsPerSquare = 4;
		boolean[][] state = new boolean[1][24 * bitsPerSquare + 1];

		int bitCount = 0;
		for (int row = selectedRow - 2; row <= selectedRow + 2; row++)
			for (int col = selectedCol - 2; col <= selectedCol + 2; col++) {
				if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS)
					if (!(selectedRow == row && selectedCol == col))
						appendToBooleanArray(state, bitCount, observableBoard[row][col].getCellType().ordinal());
					else
						appendToBooleanArray(state, bitCount, CellType.INVALID.ordinal());
				bitCount+=4;
			}
		state[0][bitCount] = observableBoard[selectedRow][selectedCol].getCellType() == CellType.HIDDEN;
		return state;
	}

	private void appendToBooleanArray(boolean[][] arr, int index, int value){
		for (int i = index+3; i>=index; i--, value/=2)
			arr[0][i] = value%2==1;
	}


	public List<CellType> get5x5StateAsList(int selectedRow, int selectedCol){
		List<CellType> state = new ArrayList<>();

		for (int row = selectedRow - 2; row <= selectedRow + 2; row++) {
			for (int col = selectedCol - 2; col <= selectedCol + 2; col++) {
				if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS)
					state.add(observableBoard[row][col].getCellType());
				else
					state.add(CellType.HIDDEN);
			}
		}
		return state;
	}


	//public boolean[][] get


	/* Overridden Methods */
	/* Actions */
	public void playMove(int action) {

	}

	public void flagCell(int selectedRow, int selectedCol) {
		if (getObservableCell(selectedRow, selectedCol).getCellType() == CellType.HIDDEN) {
			getObservableCell(selectedRow, selectedCol).setCellType(CellType.FLAGGED);
			decrementFlagCount();

		} else if (getObservableCell(selectedRow, selectedCol).getCellType() == CellType.FLAGGED) {
			getObservableCell(selectedRow, selectedCol).setCellType(CellType.HIDDEN);
			incrementFlagCount();

		} else {
			// System.out.println("Cannot flag a revealed cell");

		}
	}

	public void clickCellInitial(int selectedRow, int selectedCol) {

	}

	public void initializeBoard(int row, int col) {

	}

	public Cell getCell(int row, int col) {
		return null;
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
