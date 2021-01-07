package neuralnetwork;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.Serializable;
import java.util.Arrays;

public class ActionValue implements Serializable {
    private final boolean [] stateBooleanArray;
    private final int actionScore;

    public ActionValue(INDArray state, int actionScore) {
        int[] intVector = state.toIntVector();
        stateBooleanArray = new boolean[intVector.length];
        for (int i = 0; i < intVector.length; i++)
            this.stateBooleanArray[i] = intVector[i] == 1;
        this.actionScore = actionScore;
    }

    public boolean[] getStateBooleanArray(){
        return stateBooleanArray;
    }

    public INDArray getStateINDArray(){
        boolean[][] vector = new boolean[1][stateBooleanArray.length];
        vector[0] = stateBooleanArray;
        return Nd4j.create(vector);
    }

    public int getActionScore(){
        return actionScore;
    }

    public INDArray getActionScoreINDArray(){
        boolean[][] y = new boolean[1][1];
        y[0][0] = actionScore == 1;
        return Nd4j.create(y);
    }

    public String toString(){
        return Arrays.toString(stateBooleanArray).replace("true", "1").replace("false", "0") + ": " + actionScore;
    }
}
