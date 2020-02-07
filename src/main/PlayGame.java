package main;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;

import java.io.ObjectOutputStream;
import java.util.Scanner;

import mechanics.Board;
import mechanics.ObservableBoard;
import interfaces.StatusConstants;
import players.AI_Player;
import players.Agent;
import players.ManualPlayer;
import screenMechanics.ScreenReader;

public class PlayGame implements StatusConstants{

	ObservableBoard board;
	
	Agent agent;
	boolean printBoard;
	boolean pause;
	boolean play_browser;
	boolean stochastic;
	
	public PlayGame(int agentType, boolean printBoard, boolean pause, boolean play_browser, boolean train, int threadcount, boolean stochastic){
		this.pause = pause;
		this.printBoard = printBoard;
		this.play_browser = play_browser;
		this.stochastic = stochastic;
			
		if (play_browser){
			board = new ScreenReader();
		} else {
			if (stochastic){ //stochastic board
				board = new Board();
			} else { //deterministic board 
				ObjectInputStream in;
				try {
					in = new ObjectInputStream(new FileInputStream("board.txt"));
					board  = (Board) in.readObject();
					
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Could not find board");
					board = new Board(); //contains mechanics of game
					board.clickCellInitial(10, 10);
					System.out.println("Writing new board...");
					ObjectOutputStream out;
					try {
						out = new ObjectOutputStream(new FileOutputStream("board.txt"));
						out.writeObject(board);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} 
			}
		}
		
		if (agentType == 0){
			agent = new AI_Player(train, threadcount, stochastic);
		} else if (agentType == 1){
			agent = new ManualPlayer(); 
		} else {
			agent = new Agent(); //default is random agent
		}
	}
	
	public void start() {
		long start = System.currentTimeMillis();
		
		if (printBoard)
			board.drawObservableBoard();

		while (board.isRunning()){
			if (pause){
				pause();
			} else{
			//	sleep();
			}
			
			int action = agent.chooseAction(board);

			board.playMove(action);
			
			if (printBoard)
				printBoard(action);
		}
		System.out.println(board.getGameCondition());
		System.out.println("Time: " + ((System.currentTimeMillis() - start)/1000) + " seconds.");
	}
	
	public void reset(){
		board.resetObservableBoard();
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
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
		if (action >= 480){ 
			action-=480;
			System.out.print("Flagged ");
		} 
		else {
			System.out.print("Clicked ");
		}
		System.out.println("cell at row: " + action/board.COLUMNS + " col: " + action%board.COLUMNS);
		board.drawObservableBoard();	
		System.out.println("Squares Revealed: " + board.getSquaresRevealedCount());
	}
	
	public Board getBoard(){
		return (Board) board;
	}
	
	public void writeToFile(){
		System.out.println("Writing...");
		 
		try {
			XMLEncoder e;
			e = new XMLEncoder(
			         new BufferedOutputStream(
			             new FileOutputStream("Test.xml")));
			e.writeObject(board);
			System.out.println("Successfully written");
			e.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public Board readFromFile(){
		System.out.println("Reading...");
		
		try {
			XMLDecoder d;
			d = new XMLDecoder(
			           new BufferedInputStream(
			               new FileInputStream("Test.xml")));
			Board result = (Board)d.readObject();
			d.close();
			System.out.println("Successfully read");
			return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Object does not yet exist.");
		return null;
	}   
	
	
	
}
