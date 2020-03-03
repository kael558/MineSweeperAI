package main;

import java.util.ArrayList;

import neuralnetwork.TestModel;
import players.AlgorithmPlayer.HiddenCell;

/* cd /c/Users/Rahel/workspace/MineSweeperAI
 * git add .
 * git commit -m "message"
 * git push origin master
 */


/*TODO
 * add multithreading
 * set-up test game
 * 	- save game state
 *  - play game state
 *  - model.output of previous game state to see whether an impact was made.
 */
public class Main {
	
	/*PLAYERS*/
	final static int AI_PLAYER = 0;
	final static int MANUAL_PLAYER = 1;
	final static int ALGORITHM_PLAYER = 2;
	final static int RANDOM_PLAYER = 3;
	
	/*PARAMETERS*/
	final static boolean PRINT_IN_CONSOLE = true;
	final static boolean PAUSE = true;
	final static boolean PLAY_BROWSER = false;
	
	final static int ROWS = 16;
	final static int COLUMNS = 30;
	final static int NUMBER_OF_BOMBS = 99;
	
	public static void main(String[] args) {
		new TestModel(ROWS, COLUMNS, NUMBER_OF_BOMBS, ALGORITHM_PLAYER);
	
		//PlayGame game = new PlayGame(ALGORITHM_PLAYER, PRINT_IN_CONSOLE, PAUSE, PLAY_BROWSER, ROWS, COLUMNS, NUMBER_OF_BOMBS);
		//game.start();
		
		
		//new Main();
		
		
		/*
		int wins = 0;
		for (int i = 0; i < 100; i++){
			System.out.println("Game " + i);
			game.start();
			if (game.getBoard().getGameCondition().equals("Winner")){
				wins++;
			}
			game.reset();
		}
		System.out.println("Won " + wins + "/100");*/
	}
	public Main(){
		ArrayList<HiddenCell> hiddenCellPerimeter = new ArrayList<HiddenCell>();
		HiddenCell hc = new HiddenCell(1, 1);
		hiddenCellPerimeter.add(hc);
		
		System.out.println(hiddenCellPerimeter);
		
		
		for (HiddenCell HC: hiddenCellPerimeter){
			if (hc.getRow() == HC.getRow() && hc.getColumn() == HC.getColumn()){
				System.out.println("It contains it");
			}
		}
			
	}
	
	
	public class HiddenCell{
		int row, column, count;
		
		public HiddenCell(int row, int column){
			this.row = row;
			this.column = column;
			count = 0;
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
		
		public void incrementCount(){
			count++;
		}
		
		public String toString(){
			return  row + ", " + column + ", " + count;
		} 
	}
}
/*
	0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
0   2  2  1  0  0  1 -1 -1  1  0  1  1  1  1 -1  2  1  0  0  0  0  1  1  2  1  2  2 -1  1  0
1  -1 -1  2  0  0  1  2  2  1  1  2 -1  2  3  3 -1  1  0  0  1  2  4 -1  4 -1  3 -1  2  1  0
2   4 -1  3  1  0  1  1  2  1  2 -1  3 -1  4 -1  4  3  2  1  1 -1 -1 -1  4 -1  3  1  1  0  0
3  -1  3 -1  1  0  1 -1  3 -1  3  2  3  2 -1 -1  4 -1 -1  2  1  2  4  4  5  3  2  1  1  1  0
4   1  2  1  1  0  1  2 -1  3  3 -1  2  2  2  3  4 -1 -1  4  2  2  2 -1 -1 -1  1  2 -1  3  1
5   0  0  0  1  1  1  1  1  2 -1  3 -1  2  1  1 -1  5 -1  4 -1 -1  2  2  4  3  2  2 -1  3 -1
6   1  1  1  1 -1  1  0  0  2  3  4  3 -1  1  2  2  4 -1  3  2  2  1  0  1 -1  1  2  2  3  1
7   1 -1  1  1  2  2  1  0  1 -1 -1  2  1  1  2 -1  4  2  1  0  1  1  2  2  2  1  1 -1  1  0
8   1  1  1  0  1 -1  3  2  2  2  2  1  0  0  2 -1 -1  1  0  0  1 -1  3 -1  1  0  1  1  1  0
9   1  1  0  0  1  2 -1 -1  2  1  0  0  0  0  1  2  2  2  1  2  2  4 -1  4  2  1  1  1  1  0
10 -1  1  0  0  1  3  4  4 -1  1  0  0  0  0  0  0  1  2 -1  2 -1  3 -1  3 -1  3  3 -1  1  0
11  1  1  1  2  3 -1 -1  4  2  1  0  0  0  1  1  1  1 -1  4  5  3  3  1  2  3 -1 -1  2  2  1
12  0  0  1 -1 -1  4 -1 -1  2  0  0  0  1  2 -1  1  1  2 -1 -1 -1  1  1  1  3 -1  3  2  3 -1
13  0  0  1  2  2  2  3 -1  2  0  0  0  1 -1  3  2  1  2  3  5  4  4  3 -1  2  1  1  1 -1 -1
14  0  0  0  1  1  1  1  2  2  1  0  1  2  3  3 -1  1  1 -1  3 -1 -1 -1  3  2  1  1  2  3  2
15  0  0  0  1 -1  1  0  1 -1  1  0  1 -1  2 -1  2  1  1  2 -1  3  3  2  2 -1  1  1 -1  1  0
*/