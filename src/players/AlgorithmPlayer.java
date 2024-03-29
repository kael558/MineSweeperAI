package players;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import enumerations.ActionType;
import enumerations.CellType;
import mechanics.Action;
import mechanics.Board;
import mechanics.ObservableBoard;

public class AlgorithmPlayer extends Player {

	Random r;
	
	public AlgorithmPlayer(){
		r = new Random();
	}
	
	public static class HiddenCell{
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

	
	public Action chooseAction(ObservableBoard board){
		Action action = new Action(ActionType.CLICK, r.nextInt(board.ROWS), r.nextInt(board.COLUMNS));

		boolean isEmpty = true;
		
		/*KNOWN MOVES*/
		for (int row = 0; row < board.ROWS; row++){
			for (int col = 0; col < board.COLUMNS; col++){
				if (board.getObservableCell(row, col) == CellType.HIDDEN){
					if (satisfyNumbered(board, row, col)){
						System.out.println("Definite Action");
						return new Action(ActionType.CLICK, row, col);
					} 
					if (satisfyFlagged(board, row, col)){
						System.out.println("Definite Action");
						return new Action(ActionType.FLAG, row, col);
					}
				} else if (board.getObservableCell(row, col) != CellType.FLAGGED){ //not hidden && not flagged
					isEmpty = false; 
				}
			}
		}
		
		if (isEmpty){
			System.out.println("Random Action");
			return action;
		}

		/*PROBABILITY BASED*/
		ArrayList<HiddenCell> hiddenCellPerimeter = new ArrayList<>();
		int clearAmount = 0;
		int minimumCount = Integer.MAX_VALUE;
		int minimumCountRow = 999;
		int minimumCountCol = 999;
		
		ObservableBoard temp = new Board(board);
		
		for (int row = 0; row < temp.ROWS; row++){
			for (int col = 0; col < temp.COLUMNS; col++){
				if (temp.getObservableCell(row, col) == CellType.HIDDEN){
					if (adjacentIsNumbered(temp, row, col)){
						if (!contains(hiddenCellPerimeter, row, col)){
							populateHiddenCellPerimeter(hiddenCellPerimeter, temp, row, col);
							for (int i = 0; i < clearAmount; i++){
								hiddenCellPerimeter.remove(0);
							}
							if (hiddenCellPerimeter.size() < 30){
								tryFlagCombination(temp, hiddenCellPerimeter, 0);
								
								for (int i = 0; i < hiddenCellPerimeter.size(); i++){
									if (hiddenCellPerimeter.get(i).getCount() < minimumCount){
										minimumCount = hiddenCellPerimeter.get(i).getCount();
										minimumCountRow= hiddenCellPerimeter.get(i).getRow();
										minimumCountCol= hiddenCellPerimeter.get(i).getColumn();
									}
									
									if (hiddenCellPerimeter.get(i).getCount()==0){
										System.out.println("Definite Probability Action");
										System.out.println(hiddenCellPerimeter);
										return new Action(ActionType.CLICK, hiddenCellPerimeter.get(i).getRow(), hiddenCellPerimeter.get(i).getColumn());
									}
								}
	
							} else {
								System.out.println(hiddenCellPerimeter.size() + " " + hiddenCellPerimeter);
							}

							clearAmount = hiddenCellPerimeter.size();
						}
					}
				}
			}
		}
	
		if (minimumCountRow!=999 && minimumCountCol!=999){
			System.out.println("Minimum Count Probability Action");
			System.out.println(hiddenCellPerimeter);
			return new Action(ActionType.CLICK, minimumCountRow, minimumCountCol);
		}
		
		System.out.println("Random Action as hiddenCellPerimeter is too large");
		return action;
	}

	private void tryFlagCombination(ObservableBoard board, ArrayList<HiddenCell> hiddenCellPerimeter, int index) {
		//board.fl
		if (index == hiddenCellPerimeter.size()){
		
			//take into account the flag count, e.g. must be lower flag count, if < 0
			boolean allSatisfied = true;
			
			perimeterSatisfiedLoop:
			for (int i = 0; i < hiddenCellPerimeter.size(); i++){
				int selectedRow = hiddenCellPerimeter.get(i).getRow();
				int selectedCol = hiddenCellPerimeter.get(i).getColumn();
				for (int row = selectedRow-1; row <= selectedRow+1; row++){
					for (int col = selectedCol-1; col <= selectedCol+1; col++){
						if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
							if (!(row==selectedRow && col==selectedCol)){
								if (board.getObservableCell(row, col) != CellType.HIDDEN && board.getObservableCell(row, col) != CellType.FLAGGED){
									if (!isNumberSatisfied(board, row, col)){
										allSatisfied = false;
										break perimeterSatisfiedLoop;
									}
								}
							}
						}
					}
				}
				
		
			}
			
			if (allSatisfied){
				//board.drawObservableBoard();
				for (int i = 0; i < hiddenCellPerimeter.size(); i++){
					if (hiddenCellPerimeter.get(i).getFlagged()){
						hiddenCellPerimeter.get(i).incrementCount();
						
					}
				}
			}
			
			//System.out.println(hiddenCellPerimeter);
			//System.out.println("reached end of combination");
			return;
		}
		
		
		
		if (possibleFlag(board, hiddenCellPerimeter.get(index).getRow(), hiddenCellPerimeter.get(index).getColumn())){
			board.flagCell(hiddenCellPerimeter.get(index).getRow(), hiddenCellPerimeter.get(index).getColumn());
			hiddenCellPerimeter.get(index).setFlagged();
			//board.drawObservableBoard();
		}
		
		//System.out.println("index: " + index);
		//pause();
		
		tryFlagCombination(board, hiddenCellPerimeter, index+1);
		

		//System.out.println("index to unflag: " + index);
		
		if (hiddenCellPerimeter.get(index).getFlagged()){
			board.flagCell(hiddenCellPerimeter.get(index).getRow(), hiddenCellPerimeter.get(index).getColumn());
			hiddenCellPerimeter.get(index).resetFlag();
			//board.drawObservableBoard();
			tryFlagCombination(board, hiddenCellPerimeter, index+1);
		}
		
	
	}

	private boolean possibleFlag(ObservableBoard board, int selectedRow, int selectedCol){
		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						
						//if one of the surrounding squares is satisfied, then dont flag
						if (board.getObservableCell(row, col)!= CellType.FLAGGED && board.getObservableCell(row, col)!= CellType.HIDDEN){
							if (isNumberSatisfied(board, row, col)){
								//System.out.println("Cell: " + row + " " + col + " is satisfied");
								//pause();
								return false;
							}
							//System.out.println("Cell: " + row + " " + col + " is not satisfied");
							//pause();
						}
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
						if (board.getObservableCell(row, col)== CellType.FLAGGED){
							count++;
						}
						if (CellType.values()[count] == board.getObservableCell(selectedRow, selectedCol)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private void populateHiddenCellPerimeter(ArrayList<HiddenCell> hiddenCellPerimeter, ObservableBoard board, int selectedRow, int selectedCol) {
		hiddenCellPerimeter.add(new HiddenCell(selectedRow, selectedCol));

		for (int row = selectedRow-1; row <= selectedRow+1; row++){
			for (int col = selectedCol-1; col <= selectedCol+1; col++){
				if (row >= 0 && row < board.ROWS && col >= 0 && col < board.COLUMNS){
					if (!(row==selectedRow && col==selectedCol)){
						if (board.getObservableCell(row, col) == CellType.HIDDEN){
							if (adjacentIsNumbered(board, row, col)){
								if (!contains(hiddenCellPerimeter, row, col)){
									//System.out.println("row: " + row + " col: " + col);
									//pause();
									populateHiddenCellPerimeter(hiddenCellPerimeter, board, row, col);
	
								}

							}
						}
					}
				}
			}
		}
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
						if (board.getObservableCell(row, col)!= CellType.HIDDEN && board.getObservableCell(row, col)!= CellType.FLAGGED){
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
						if (board.getObservableCell(row, col)!= CellType.HIDDEN && board.getObservableCell(row, col)!= CellType.FLAGGED){
							if (board.getObservableCell(row, col) == CellType.values()[countHiddenFlagged(board, row, col)]){
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
						if (board.getObservableCell(row, col) == CellType.HIDDEN || board.getObservableCell(row, col) == CellType.FLAGGED){
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
						if (board.getObservableCell(row, col)!= CellType.HIDDEN && board.getObservableCell(row, col)!= CellType.FLAGGED){
							if (board.getObservableCell(row, col) == CellType.values()[countBombs(board, row, col)]){
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
						if (board.getObservableCell(row, col) == CellType.FLAGGED){
							count++;
						}
					}
				}
			}
		
		}
		return count;
	}
}

