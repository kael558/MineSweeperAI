package mechanics;

import interfaces.ActionType;
import interfaces.CellType;

import java.io.Serial;
import java.io.Serializable;

public class Board extends ObservableBoard implements Serializable{
	@Serial
	private static final long serialVersionUID = 4244819984696003737L;

	private CellType[][] board;

	private boolean isBoardInitialized;
	
	public Board(){
		super();
		isBoardInitialized = false;
		board = new CellType[super.ROWS][super.COLUMNS];
	}
	
	public Board(int rows, int columns, int number_of_bombs){
		super(rows, columns, number_of_bombs);
		isBoardInitialized = false;
		board = new CellType[super.ROWS][super.COLUMNS];
	}
	
	public void initializeBoard(int selectedRow, int selectedCol){ //initalized after first click
		int bombCount = 0;
		
		//ArrayList <Integer> indexes = new ArrayList<Integer>(super.ROWS*super.COLUMNS);
		//for (int i = 0; i < super.ROWS*super.COLUMNS; i++){
		//	indexes.add(i);
		//}
		
		while (bombCount<super.getNumberOfBombs()){
			//int bombIndex = (int) (Math.random()*indexes.size());
			int row = (int) (Math.random()*ROWS);
			int col = (int) (Math.random()*COLUMNS);
		
			//int row = indexes.get(bombIndex)/COLUMNS;
			//int col = indexes.get(bombIndex)%COLUMNS;
			
			if (board[row][col] == null && !(row >= selectedRow-1 && row <= selectedRow+1 && col >= selectedCol-1 && col <= selectedCol+1)){
				board[row][col] = CellType.BOMB;
				bombCount++;
			}
		}
		
		
		for (int i = 0; i < ROWS; i++){
			for (int j = 0; j < COLUMNS; j++){
				if (board[i][j] == null){
					board[i][j] = CellType.values()[getNumberOfSurroundingBombs(i, j)];
				}
			}
		}
		isBoardInitialized = true;
	}
	
	
	
	public int getNumberOfSurroundingBombs(int selectedRow, int selectedColumn){
		int count = 0;
		
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedColumn-1; col <= selectedColumn+1; col++){	
				if (!(row==selectedRow && col==selectedColumn)){
					if (row>=0 && row<ROWS && col>=0 && col<COLUMNS && board[row][col]!=null && board[row][col]== CellType.BOMB){
						count++;
					}
				}
			}
		}
			
		return count;
	}
	
	public void drawSecretBoard(){
		System.out.print("  ");
		for (int i = 0; i < COLUMNS; i++){
			System.out.printf("%3d", i);
		}
		
		System.out.println();
		for (int i = 0; i < ROWS; i++){
			System.out.printf("%-2d", i);
			for (int j = 0; j < COLUMNS; j++){
				System.out.printf("%3d", board[i][j].ordinal());
			}
			System.out.println();
		}
	}
	
	public boolean isBoardInitialized(){
		return isBoardInitialized;
	}
	
	public void setBoardInitialized(){
		isBoardInitialized = true;
	}



	/* ACTIONS*/
	public void playMove(Action action){
		if (action.actionType == ActionType.CLICK)
			clickCellInitial(action.row, action.col);
		else
			flagCell(action.row, action.col);
		updateGameCondition();
	}
	
	public void clickCellInitial(int selectedRow, int selectedCol){
		if (!isBoardInitialized()){
			initializeBoard(selectedRow, selectedCol);
		} 
		clickCell(selectedRow, selectedCol);
	}
	
	private void clickCell(int selectedRow, int selectedCol){
		if (getObservableCell(selectedRow, selectedCol)!= CellType.HIDDEN){
		//	System.out.println("You can't click an already revealed cell");
			return;
		}
		
		observableBoard[selectedRow][selectedCol] = getCell(selectedRow, selectedCol);
		
		if (getObservableCell(selectedRow, selectedCol)== CellType.BOMB){
		//	System.out.println("Clicked Bomb at" + selectedRow + " " + selectedColumn);
			return;
		}
		
		incrementSquaresRevealedCount();
		if (getObservableCell(selectedRow, selectedCol)== CellType.SQUARE0){
			for (int row = selectedRow-1; row <= selectedRow+1; row++){
				for (int col = selectedCol-1; col <= selectedCol+1; col++){
					if (!(row==selectedRow && col==selectedCol)){
						if (row>=0 && row<ROWS && col>=0 && col<COLUMNS){
							if (getObservableCell(row, col)== CellType.HIDDEN){
								if (getCell(row, col)!= CellType.BOMB && getCell(row, col)!= CellType.SQUARE0){
									observableBoard[row][col] = getCell(row, col);
									incrementSquaresRevealedCount();
								} else if (getCell(row, col)== CellType.SQUARE0){
									clickCell(row, col);
								}
							}
							
						} 
					}
				}
			}
		}
	}

	public CellType getCell(int row, int col) {
		return board[row][col];
	}
}
