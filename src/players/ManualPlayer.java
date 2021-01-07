package players;

import java.util.Scanner;

import interfaces.ActionType;
import mechanics.Action;
import mechanics.ObservableBoard;

public class ManualPlayer  extends Player {

	public ManualPlayer(){
	
	}

	public Action chooseAction(ObservableBoard board){
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		
		int row = 0;
		int col = 0;
		int click = 0;

		do{
			System.out.println("Choose an action:");
			System.out.print("Enter Row (0 - 15): ");
			row = scan.nextInt();
			System.out.print("Enter Col (0 - 29): ");
			col = scan.nextInt();
			
			System.out.print("Enter Action (0 - click, 1 - flag): ");
			click = scan.nextInt();
		
		} while (row < 0 || row >= board.ROWS || col < 0 || col >= board.COLUMNS || click < 0 || click > 1);

		if (click == 0)
			return new Action(ActionType.CLICK, row, col);

		return new Action(ActionType.FLAG, row, col);
	}
}
