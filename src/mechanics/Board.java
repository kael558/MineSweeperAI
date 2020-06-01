package mechanics;

import java.io.Serializable;
import java.util.ArrayList;

//Environment
public class Board extends ObservableBoard implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4244819984696003737L;

	private Cell[][] board;	
	
	public static final int SECRET_STATUS_BOMB = -1;
	private boolean isBoardInitialized;
	
	public Board(){
		super();
		isBoardInitialized = false;
		board = new Cell[super.ROWS][super.COLUMNS];
	}
	
	public Board(int rows, int columns, int number_of_bombs){
		super(rows, columns, number_of_bombs);
		isBoardInitialized = false;
		board = new Cell[super.ROWS][super.COLUMNS];
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
				board[row][col] = new Cell(SECRET_STATUS_BOMB);
				bombCount++;
			}
		}
		
		
		for (int i = 0; i < ROWS; i++){
			for (int j = 0; j < COLUMNS; j++){
				if (board[i][j] == null){
					board[i][j] = new Cell(getNumberOfSurroundingBombs(i, j));
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
					if (row>=0 && row<ROWS && col>=0 && col<COLUMNS && board[row][col]!=null && board[row][col].getSecretStatus()==SECRET_STATUS_BOMB){
						count++;
					}
				}
			}
		}
			
		return count;
	}
	
	public void drawSecretBoard(){
		System.out.printf("  ");
		for (int i = 0; i < COLUMNS; i++){
			System.out.printf("%3d", i);
		}
		
		System.out.println();
		for (int i = 0; i < ROWS; i++){
			System.out.printf("%-2d", i);
			for (int j = 0; j < COLUMNS; j++){
				System.out.printf("%3d", board[i][j].getSecretStatus());
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
	
	public Cell[][] getBoard(){
		return board;
	}
	
	public Cell getCell(int row, int col){
		return board[row][col];
	}
	
	public Board cloneInitializedBoard(){
		Board board = new Board();
		board.setBoardInitialized();
		
		for (int row = 0; row < ROWS; row++){
			for (int col = 0; col < COLUMNS; col++){
				board.getBoard()[row][col] = new Cell(this.board[row][col].getSecretStatus());
			}
		}
		return board;
	}

	
	
	/* ACTIONS*/
	public void playMove(int actionIndex){
		int [] action = {0, 0, 0};
		
		if (actionIndex >= 480){
			actionIndex-=480;
			action[0] = actionIndex/COLUMNS;
			action[1] = actionIndex%COLUMNS;
			action[2] = 1; 
			//System.out.println("flagged" + action[0] + " " + action[1]);
			flagCell(action[0], action[1]);
			
		} else {
			action[0] = actionIndex/COLUMNS;
			action[1] = actionIndex%COLUMNS;
			action[2] = 0;
			//System.out.println("clicked" + action[0] + " " + action[1]);
			clickCellInitial(action[0], action[1]);
		} 
		updateGameCondition();
	}
	
	public void clickCellInitial(int selectedRow, int selectedCol){
		if (!isBoardInitialized()){
			initializeBoard(selectedRow, selectedCol);
		} 
		clickCell(selectedRow, selectedCol);
	}
	
	private void clickCell(int selectedRow, int selectedCol){
		if (getObservableCell(selectedRow, selectedCol).getStatus()!=STATUS_HIDDEN){
		//	System.out.println("You can't click an already revealed cell");
			return;
		}
		
		
		getObservableCell(selectedRow, selectedCol).setStatus(getCell(selectedRow, selectedCol).getSecretStatus());
		
		if (getObservableCell(selectedRow, selectedCol).getStatus()==STATUS_BOMB){
		//	System.out.println("Clicked Bomb at" + selectedRow + " " + selectedColumn);
			return;
		}
		
		incrementSquaresRevealedCount();
		if (getObservableCell(selectedRow, selectedCol).getStatus()==STATUS_SQUARE0){
			for (int row = selectedRow-1; row <= selectedRow+1; row++){
				for (int col = selectedCol-1; col <= selectedCol+1; col++){
					if (!(row==selectedRow && col==selectedCol)){
						if (row>=0 && row<ROWS && col>=0 && col<COLUMNS){
							if (getObservableCell(row, col).getStatus()==STATUS_HIDDEN){
								if (getCell(row, col).getSecretStatus()!=STATUS_BOMB && getCell(row, col).getSecretStatus()!=STATUS_SQUARE0){
									getObservableCell(row, col).setStatus(getCell(row, col).getSecretStatus());
									incrementSquaresRevealedCount();
								} else if (getCell(row, col).getSecretStatus()==STATUS_SQUARE0){
									clickCell(row, col);
								}
							}
							
						} 
					}
				}
			}
		}
	}
	
	
	
	/* Unnecessary
	 The double click show option:
	 
	public boolean doubleClickCell(int selectedRow, int selectedColumn){
	 
		if (board[selectedRow][selectedColumn].getStatus()!=STATUS_REVEALED){
			System.out.println("You can't double click an unrevealed cell");
			return false;
		}
		if (board[selectedRow][selectedColumn].getStatus()==STATUS_FLAGGED){
			System.out.println("You can't double click a flagged cell");
			return false;
		}
			
		if (getNumberOfSurroundingFlags(selectedRow, selectedColumn) == board[selectedRow][selectedColumn].getSecretStatus()){
		
			/* if cell is revealed
			 * if revealed bombs match number
			 * then check adjacent cells that are not revealed and not flagged
			 * 	if adjacent cell is 1 - 8, reveal just that number
			 *  if adjacent cell is 0, then click that cell 
			 * 
			 */
		/*	
			for (int row = selectedRow-1; row <= selectedRow+1; row++){
				for (int col = selectedColumn-1; col <= selectedColumn+1; col++){
					
					if (!(row==selectedRow && col==selectedColumn)){
						
						if (row>=0 && row<ROWS && col>=0 && col<COLUMNS){
							
							if (board[row][col].getStatus()!=STATUS_REVEALED && board[row][col].getStatus()!=STATUS_FLAGGED){
								if (board[row][col].getSecretStatus()>0 && board[row][col].getSecretStatus()<9){
									board[row][col].setStatus(STATUS_REVEALED);
									SquaresRevealedCount++;
								}else if (board[row][col].getSecretStatus()==0){
									clickCell(row, col);
								}
							}
						} 
					}

				}
			}
		
		}
		return true;
	}
	
	public int getNumberOfSurroundingFlags(int selectedRow, int selectedColumn){
		int count = 0;
		
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedColumn-1; col <= selectedColumn+1; col++){	
				if (!(row==selectedRow && col==selectedColumn)){
					if (row>=0 && row<ROWS && col>=0 && col<COLUMNS && board[row][col].getStatus()==STATUS_FLAGGED){
						count++;
					}
				}
			}
		}
			
		return count;
	}
	
	*/
}
