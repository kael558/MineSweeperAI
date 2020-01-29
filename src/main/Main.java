package main;


/* cd /c/Users/Rahel/workspace/MineSweeperAI
 * git add .
 * git commit -m "message"
 * git push origin master
 */

public class Main {
	
	/*PLAYERS*/
	final static int AI_PLAYER = 0;
	final static int MANUAL_PLAYER = 1;
	final static int RANDOM_PLAYER = 2;
	
	/*PARAMETERS*/
	final static boolean PRINT_BOARD = true;
	final static boolean PAUSE = false;
	final static boolean PLAY_BROWSER = false;
	final static boolean TRAIN = false;
	final static int THREAD_COUNT = 1;
	
	public static void main(String[] args) {
		PlayGame game = new PlayGame(AI_PLAYER, PRINT_BOARD, PAUSE, PLAY_BROWSER, TRAIN, THREAD_COUNT);
		game.start();

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