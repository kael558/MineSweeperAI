package players;

import mechanics.ObservableBoard;


public abstract class Player {
	public abstract int chooseAction(ObservableBoard board);
}
