package players;

import mechanics.Action;
import mechanics.ObservableBoard;


public abstract class Player {
	public abstract Action chooseAction(ObservableBoard board);
}
