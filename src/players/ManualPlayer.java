package players;

import java.util.Scanner;

import mechanics.Board;
import mechanics.ObservableBoard;

public class ManualPlayer  extends Agent{

	public ManualPlayer(){
	
	}
	
	
	public int chooseAction(ObservableBoard board){
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		
		int[] action = {0, 0, 0};

		do{
			System.out.println("Choose an action:");
			System.out.print("Enter Row (0 - 15): ");
			action[0] = scan.nextInt();
			System.out.print("Enter Col (0 - 29): ");
			action[1] = scan.nextInt();
			
			System.out.print("Enter Action (0 - click, 1 - flag): ");
			action[2] = scan.nextInt();
		
		} while (action[0] < 0 || action[0] >= board.ROWS || action[1] < 0 || action[1] >= board.COLUMNS || action[2] < 0 || action[2] > 1);
		
		
		if (action[2] == 0)
			return action[0]*board.COLUMNS + action[1];
		
		return action[0]*board.COLUMNS + action[1] + 480;
		
	}
}
