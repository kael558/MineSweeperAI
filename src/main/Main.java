package main;

import neuralnetwork.TestModel;

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

/*
 * 5x
 * 
 * 
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
	//	new TestModel(ROWS, COLUMNS, NUMBER_OF_BOMBS, AI_PLAYER);
	
		/*
		Board temp = new Board(ROWS, COLUMNS, NUMBER_OF_BOMBS);
		temp.getState(0, 0);
		temp.drawObservableBoard();
		*/
		//PlayGame game = new PlayGame(AI_PLAYER, PRINT_IN_CONSOLE, PAUSE, PLAY_BROWSER, ROWS, COLUMNS, NUMBER_OF_BOMBS);
	//	game.start();
		
		
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
		System.out.println(isIn("AAAAB"));
	}
	/*
<word> = es | <word> <dol> | <ch> <word> <ch>
<ch> = A | B
<dol> = $


	 */
	
	public static boolean isCh(char c){
		return c=='A' || c=='B';
	}
	public static boolean isIn(String str){
		int size = str.length();
		if (str.equals(""))
			return true;
		if (str.equals("$"))
			return true;
		if (size == 2)
			if(isCh(str.charAt(0)) && isCh(str.charAt(size-1)))
				return true;
						
		if(isCh(str.charAt(0)) && isCh(str.charAt(size-1)))
			return isIn(str.substring(1, size-1));
		else if (str.charAt(size-1) == '$')
			return isIn(str.substring(0, size-1));
		else
			return false;
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