package neuralnetwork;

import mechanics.Action;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.Serializable;
import java.util.Arrays;

public class ActionValue implements Serializable {
    private final boolean [][] stateBooleanArray;
    private final boolean [][] actionScore;


    public ActionValue(boolean[][] stateBooleanArray, int actionScore) {
        this.stateBooleanArray = stateBooleanArray;
        this.actionScore = new boolean[1][1];
        this.actionScore[0][0] = actionScore==1;
    }

    public boolean[][] getStateBooleanArray(){
        return stateBooleanArray;
    }

    public INDArray getStateINDArray(){
        return Nd4j.create(stateBooleanArray);
    }

    public boolean[][] getActionScore(){
        return actionScore;
    }

    public INDArray getActionScoreINDArray(){
        return Nd4j.create(actionScore);
    }

    public String toString(){
        return Arrays.toString(stateBooleanArray).replace("true", "1").replace("false", "0") + ": " + actionScore;
    }
}
