package players;

import mechanics.Action;
import mechanics.ObservableBoard;

public class RandomPlayer extends Player {
    @Override
    public Action chooseAction(ObservableBoard board) {
        return new Action(board.ROWS, board.COLUMNS);
    }
}
