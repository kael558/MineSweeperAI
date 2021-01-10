package mechanics;

import enumerations.ActionType;

import java.util.Random;

public class Action {
    public ActionType actionType;
    public int row;
    public int col;

    public Action(ActionType actionType, int row, int col) {
        this.actionType = actionType;
        this.row = row;
        this.col = col;
    }

    //random action
    public Action(int ROWS, int COLUMNS){
        Random r = new Random();
        this.actionType = r.nextInt(2) == 0 ? ActionType.CLICK : ActionType.FLAG;
        this.row = r.nextInt(ROWS);
        this.col = r.nextInt(COLUMNS);
    }

    public String toString(){
        return actionType + " (" + row + ", " + col + ")";
    }


}
