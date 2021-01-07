package players;

import mechanics.ObservableBoard;

import java.util.Random;

public class RandomPlayer extends Player {
    @Override
    public int chooseAction(ObservableBoard board) {
        Random r = new Random();
        return r.nextInt(board.ROWS*board.COLUMNS*2);
    }
}
