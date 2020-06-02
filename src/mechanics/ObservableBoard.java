package mechanics;

import java.io.Serializable;

import interfaces.StatusConstants;

//Partially Observable Environment
public class ObservableBoard implements StatusConstants, Serializable {
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
				this.observableBoard[row][col] = new ObservableCell(board.getObservableCell(row, col).getStatus());
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
				if (observableBoard[i][j].getStatus() == STATUS_HIDDEN) {
					System.out.print("|_|");
				} else if (observableBoard[i][j].getStatus() == STATUS_FLAGGED) {
					System.out.print(" F ");
				} else if (observableBoard[i][j].getStatus() == STATUS_BOMB) {
					System.out.print("-1 ");
				} else if (observableBoard[i][j].getStatus() == STATUS_SQUARE0) {
					System.out.print("   ");
				} else {
					System.out.printf("%2d", observableBoard[i][j].getStatus());
					System.out.print(" ");
				}
			}
			System.out.println();
		}
	}

	public void resetObservableBoard() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				this.observableBoard[row][col].setStatus(STATUS_HIDDEN);
			}
		}
		gameCondition = "In Progress";
		flagCount = 99;
		squaresRevealedCount = 0;
	}

	public void updateGameCondition() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (observableBoard[i][j].getStatus() == STATUS_BOMB) {
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

	public boolean[][] getState(int selectedRow, int selectedCol) {
		int bitWidth = 12;
		boolean[][] state = new boolean[1][5 * 5 * bitWidth];

		for (int i = 0; i < 5 * 5 * bitWidth; i++) {
			state[0][i] = false;
		}

		int count = 0;
		for (int row = selectedRow - 2; row <= selectedRow + 2; row++) {
			for (int col = selectedCol - 2; col <= selectedCol + 2; col++) {
				if (row >= 0 && row < ROWS && col >= 0 && col < COLUMNS) {
					switch (observableBoard[row][col].getStatus()) {
					case STATUS_BOMB: // case bomb
						state[0][count * 12 + 11] = true;
						break;
					case STATUS_SQUARE0:
						state[0][count * 12 + 10] = true;
						break;
					case STATUS_SQUARE1:
						state[0][count * 12 + 9] = true;
						break;
					case STATUS_SQUARE2:
						state[0][count * 12 + 8] = true;
						break;
					case STATUS_SQUARE3:
						state[0][count * 12 + 7] = true;
						break;
					case STATUS_SQUARE4:
						state[0][count * 12 + 6] = true;
						break;
					case STATUS_SQUARE5:
						state[0][count * 12 + 5] = true;
						break;
					case STATUS_SQUARE6:
						state[0][count * 12 + 4] = true;
						break;
					case STATUS_SQUARE7:
						state[0][count * 12 + 3] = true;
						break;
					case STATUS_SQUARE8:
						state[0][count * 12 + 2] = true;
						break;
					case STATUS_HIDDEN:
						state[0][count * 12 + 1] = true;
						break;
					case STATUS_FLAGGED:
						state[0][count * 12 + 0] = true;
						break;
					default:
						throw new IllegalArgumentException("Unknown Case");
					}
				}
				count++;
			}

		}

		/*
		System.out.println("ROW: " + selectedRow + " COL:" + selectedCol);
		for (int i = 0; i < 5 * 5 * bitWidth; i++) {
			if (i % 60 == 0 && i != 0)
				System.out.println("");
			else if (i % 12 == 0 && i != 0)
				System.out.print(" ");

			if (state[0][i])
				System.out.print("1");
			else
				System.out.print("0");
		}
		System.out.println();
		*/
		return state;
	}

	/* Overridden Methods */
	/* Actions */
	public void playMove(int action) {

	}

	public void flagCell(int selectedRow, int selectedCol) {
		if (getObservableCell(selectedRow, selectedCol).getStatus() == STATUS_HIDDEN) {
			getObservableCell(selectedRow, selectedCol).setStatus(STATUS_FLAGGED);
			decrementFlagCount();

		} else if (getObservableCell(selectedRow, selectedCol).getStatus() == STATUS_FLAGGED) {
			getObservableCell(selectedRow, selectedCol).setStatus(STATUS_HIDDEN);
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
