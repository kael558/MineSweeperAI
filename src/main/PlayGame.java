package main;

import java.util.Scanner;

import interfaces.PlayerType;
import mechanics.Action;
import mechanics.Board;
import mechanics.ObservableBoard;
import players.*;
import players.ai.CNN_5x5_Player;
import players.ai.CNN_Full_Player;
import players.ai.MCTS_Player;

public class PlayGame  {
	ObservableBoard board;

	Player player;
	boolean printBoard;
	boolean pause;

	public static void main(String[] args){
		PlayGame pg = new PlayGame(new Board(), PlayerType.AI_5x5, false, false);
		pg.start();
	}

	public PlayGame(Board board, PlayerType playerType, boolean printBoard, boolean pause){
		this.board = board;
		this.pause = pause;
		this.printBoard = printBoard;

		switch(playerType){
			case AI_5x5 -> player = new CNN_5x5_Player();
			case AI_Full -> player = new CNN_Full_Player();
			case AI_MCTS -> player = new MCTS_Player();
			case HUMAN -> player = new ManualPlayer();
			case ALGORITHM -> player = new AlgorithmPlayer();
			case RANDOM -> player = new RandomPlayer();
		}
	}


	public void start() {
		long start = System.currentTimeMillis();
		if (printBoard)
			board.drawObservableBoard();

		while (board.isRunning()) {
			if (pause) 
				pause();
			else 
				sleep();

			Action action = player.chooseAction(board);
			board.playMove(action);

			if (printBoard)
				printBoard(action);
		}
		System.out.println(board.getGameCondition());
		System.out.println("Time: " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		
		if (pause) 
			pause();
	}

	private void sleep() {
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void pause() {
		System.out.println("Paused: Please enter any character to continue.");

		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
	}

	public void printBoard(Action action) {
		System.out.println(action);
		board.drawObservableBoard();
		System.out.println("Squares Revealed: " + board.getSquaresRevealedCount());
	}

	public Board getBoard() {
		return (Board) board;
	}
	
	public void setBoard(Board board){
		this.board = board;
	}
	
}
