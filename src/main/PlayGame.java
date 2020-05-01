package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import interfaces.StatusConstants;
import mechanics.Board;
import mechanics.ObservableBoard;
import players.AI_Player;
import players.Agent;
import players.AlgorithmPlayer;
import players.ManualPlayer;
import screenMechanics.ScreenReader;

public class PlayGame implements StatusConstants {
	ObservableBoard board;

	Agent agent;
	boolean printBoard = true;
	boolean pause = true;

	public PlayGame(int rows, int columns, int bombs, int agentType) {
		if (agentType == 0) {
			agent = new AI_Player(rows, columns, bombs, "no train");
		} else if (agentType == 1) {
			agent = new ManualPlayer();
		} else if (agentType == 2){
			agent = new AlgorithmPlayer(); // default is random agent
		} else {
			agent = new Agent(); // default is random agent
		}
	}
	


	public PlayGame(int agentType, boolean printBoard, boolean pause, boolean playBrowser, int rows, int columns,
			int numBombs) {
		this.pause = pause;
		this.printBoard = printBoard;

		if (playBrowser) {
			board = new ScreenReader();
		} else {
			board = new Board(rows, columns, numBombs);
		}

		if (agentType == 0) {
			agent = new AI_Player(rows, columns, numBombs);
		} else if (agentType == 1) {
			agent = new ManualPlayer();
		} else if (agentType == 2){
			agent = new AlgorithmPlayer(); // default is random agent
		} else {
			agent = new Agent(); // default is random agent
		}
	}

	public void start() {
		long start = System.currentTimeMillis();
		if (printBoard)
			board.drawObservableBoard();

		while (board.isRunning()) {
			if (pause) {
				pause();
			} else {
				sleep();
			}

			int action = agent.chooseAction(board);

			board.playMove(action);

			if (printBoard)
				printBoard(action);
		}
		System.out.println(board.getGameCondition());
		System.out.println("Time: " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
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

	public void printBoard(int action) {
		if (action >= 480) {
			action -= 480;
			System.out.print("Flagged ");
		} else {
			System.out.print("Clicked ");
		}
		System.out.println("cell at row: " + action / board.COLUMNS + " col: " + action % board.COLUMNS);
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
