package players.ai;

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
    public int chooseAction(ObservableBoard board) {
        return 0;
    }

    @Override
    public void train()  {

    }
}
