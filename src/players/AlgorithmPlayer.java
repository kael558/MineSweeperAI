package players;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import interfaces.StatusConstants;
import main.Main.HiddenCell;
import mechanics.ObservableBoard;

public class AlgorithmPlayer extends Agent implements StatusConstants {

	Random r;
	
	public AlgorithmPlayer(){
		r = new Random();
	}
	
	public class HiddenCell{
		int row, column, count;
		boolean flagged;
		
		public HiddenCell(int row, int column){
			this.row = row;
			this.column = column;
			count = 0;
			flagged = false;
		}
		
		public int getRow(){
			return row;
		}
		
		public int getColumn(){
			return column;
		}
		
		public int getCount(){
			return count;
		}
		
		public void resetFlag(){
			flagged = false;
		}
		public boolean getFlagged(){
			return flagged;
		}
		
		public void setFlagged(){
			flagged = true;
		}
		
		public void incrementCount(){
			count++;
		}
		public String output(){
			return  row + ", " + column;
		}
		public String toString(){
			return  row + ", " + column + ", " + count;
		}
	}
	
	
	
	
	public int chooseAction(ObservableBoard board){
		int action = r.nextInt(board.ROWS*board.COLUMNS);

		boolean isEmpty = true;
		
		/*KNOWN MOVES*/
		for (int row = 0; row < board.ROWS; row++){
			for (int col = 0; col < board.COLUMNS; col++){
				if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN){
					if (satisfyNumbered(board, row, col)){
						return getAction(row, col, board.COLUMNS, true);
					} 
					if (satisfyFlagged(board, row, col)){
						return getAction(row, col, board.COLUMNS, false);
					}
				} else {
					isEmpty = false;
				}
			}
		}
		
		if (isEmpty)
			return action;
		
		
		/*PROBABILITY BASED*/
		ArrayList<HiddenCell> hiddenCellPerimeter = new ArrayList<HiddenCell>();;
		for (int row = 0; row < board.ROWS; row++){
			for (int col = 0; col < board.COLUMNS; col++){
				if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN){
					if (adjacentIsNumbered(board, row, col)){
						//System.out.println("row: " + row + " col: " + col);
						if (!contains(hiddenCellPerimeter, row, col)){
							
							hiddenCellPerimeter = populateHiddenCellPerimeter(hiddenCellPerimeter, board, row, col);

						}
						
					}
				}
			}
		}
		System.out.println(hiddenCellPerimeter + " size: " + hiddenCellPerimeter.size());
		
		for (int i = 0; i < hiddenCellPerimeter.size(); i++){
			board.flagCell(hiddenCellPerimeter.get(i).getRow(), hiddenCellPerimeter.get(i).getColumn());
			hiddenCellPerimeter.get(i).setFlagged();
			System.out.println("Set Origin Flag: " + hiddenCellPerimeter.get(i).output());
			
			//have to use recursion
			for (int j = i+1; j < hiddenCellPerimeter.size(); j++){
				board.drawObservableBoard();
				System.out.println("Checking if cell: " + hiddenCellPerimeter.get(j).output() + " is a possible flag");
				if (possibleFlag(board, hiddenCellPerimeter.get(j).getRow(), hiddenCellPerimeter.get(j).getColumn())){
					board.flagCell(hiddenCellPerimeter.get(j).getRow(), hiddenCellPerimeter.get(j).getColumn());
					hiddenCellPerimeter.get(j).setFlagged();
					hiddenCellPerimeter.get(j).incrementCount();
					hiddenCellPerimeter.get(i).incrementCount();
				//	pause();
				}
			}
			
			for (int j = 0; j < hiddenCellPerimeter.size(); j++){
				if (hiddenCellPerimeter.get(j).getFlagged()){
					board.flagCell(hiddenCellPerimeter.get(j).getRow(), hiddenCellPerimeter.get(j).getColumn());
				}
			}
		}
		
		System.out.println(hiddenCellPerimeter);
		
		return action;
	}

	private boolean possibleFlag(ObservableBoard board, int selectedRow, int selectedCol){
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (!(row==selectedRow && col==selectedCol)){
					//if one of the surrounding squares is satisfied, then dont flag
					if (board.getObservableCell(row, col).getStatus()!=STATUS_FLAGGED && board.getObservableCell(row, col).getStatus()!=STATUS_HIDDEN){
						if (isNumberSatisfied(board, row, col)){
							System.out.println("Cell: " + row + " " + col + " is satisfied");
							pause();
							return false;
						}
						System.out.println("Cell: " + row + " " + col + " is not satisfied");
						pause();
					}
						
				}
			}
		}
		return true;
	}



	private boolean isNumberSatisfied(ObservableBoard board, int selectedRow, int selectedCol) {
		int count = 0;
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus()==STATUS_FLAGGED){
							count++;
						}
						if (count == board.getObservableCell(selectedRow, selectedCol).getStatus()){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private ArrayList<HiddenCell> populateHiddenCellPerimeter(ArrayList<HiddenCell> hiddenCellPerimeter, ObservableBoard board, int selectedRow, int selectedCol) {
		hiddenCellPerimeter.add(new HiddenCell(selectedRow, selectedCol));

		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN){
							if (adjacentIsNumbered(board, row, col)){
								if (!contains(hiddenCellPerimeter, row, col)){
									//System.out.println("row: " + row + " col: " + col);
									//pause();
									hiddenCellPerimeter = populateHiddenCellPerimeter(hiddenCellPerimeter, board, row, col);
	
								}

							}
						}
					}
				}
			}
		}
		
		
		return hiddenCellPerimeter;
	}

	
	private boolean contains(ArrayList<HiddenCell> hiddenCellPerimeter, int row, int col) {
		HiddenCell newHiddenCell = new HiddenCell(row, col);
		for (HiddenCell hiddenCell: hiddenCellPerimeter){
			if (newHiddenCell.getRow() == hiddenCell.getRow() && newHiddenCell.getColumn() == hiddenCell.getColumn()){
				return true;
			}
		}
		
		return false;
	}

	private void pause() {
		System.out.println("Paused: Please enter any character to continue.");

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
	}
	
	private boolean adjacentIsNumbered(ObservableBoard board, int selectedRow, int selectedCol) {
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus()!=STATUS_HIDDEN && board.getObservableCell(row, col).getStatus()!=STATUS_FLAGGED){
							return true;
						}
					}
				}
			}
		}
	
		
		return false;
	}

	private boolean satisfyFlagged(ObservableBoard board, int selectedRow, int selectedCol) {
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus()!=STATUS_HIDDEN && board.getObservableCell(row, col).getStatus()!=STATUS_FLAGGED){
							if (board.getObservableCell(row, col).getStatus() == countHiddenFlagged(board, row, col)){
								return true;
							}
						}
					}
				}
			}
		}
	
		return false;
	}
	
	private int countHiddenFlagged(ObservableBoard board, int selectedRow, int selectedCol) {
		int count = 0;
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus() == STATUS_HIDDEN || board.getObservableCell(row, col).getStatus() == STATUS_FLAGGED){
							count++;
						}
					}
				}
			}
		
		}
		return count;
	}


	private boolean satisfyNumbered(ObservableBoard board, int selectedRow, int selectedCol) {
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus()!=STATUS_HIDDEN && board.getObservableCell(row, col).getStatus()!=STATUS_FLAGGED){
							if (board.getObservableCell(row, col).getStatus() == countBombs(board, row, col)){
								return true;
							}
						}
					}
				}
			}
		
		}
		return false;
	}

	private int countBombs(ObservableBoard board, int selectedRow, int selectedCol) {
		int count = 0;
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col).getStatus() == STATUS_FLAGGED){
							count++;
						}
					}
				}
			}
		
		}
		return count;
	}

	public int getAction(int row, int col, int COLUMNS, boolean clicked){
		if (clicked)
			return row * COLUMNS + col;
		
		return row * COLUMNS + col + 480;
	}
	

	
}

