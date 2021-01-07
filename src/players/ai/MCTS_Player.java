package players.ai;

import mechanics.Action;
import mechanics.ObservableBoard;

import java.io.IOException;

public class MCTS_Player extends AI_Player{

    /*
    uses binary file to store states (as byte indexes)
    State:
    bombs 0 - 25 gives a statespace of ~64 million

    e.g. state 111011111 is stored at line

     */

    public static void main(String[] args)  {
        new MCTS_Player().train();
    }

    @Override
    public Action chooseAction(ObservableBoard board) {
        return null;
    }

    @Override
    public void train()  {

    }
}
