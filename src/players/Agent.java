package players;

import java.util.Random;

import mechanics.Board;
import mechanics.ObservableBoard;


public class Agent {

	

	public Agent(){
	
	}
	
	
	public int chooseAction(ObservableBoard board){
		Random r = new Random();
		
		
		int action = r.nextInt(board.ROWS*board.COLUMNS*2);

		
		return action;
	}
	

}
