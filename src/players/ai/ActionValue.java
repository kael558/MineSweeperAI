package players.ai;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.Serializable;
import java.util.Arrays;

public class ActionValue implements Serializable {
    private final boolean [][] serializedState;
    private final boolean [][] serializedActionScore;

    public ActionValue(boolean[][] serializedState, boolean[][] serializedActionScore) {
        this.serializedState = serializedState;
        this.serializedActionScore = serializedActionScore;
    }

    public boolean[][] getSerializedState(){
        return serializedState;
    }

    public boolean[][] getSerializedActionScore(){
        return serializedActionScore;
    }

    public INDArray getStateINDArray(){
        return Nd4j.create(serializedState);
    }


    public INDArray getActionScoreINDArray(){
        return Nd4j.create(serializedActionScore);
    }

    public String toString(){
        return Arrays.toString(serializedState).replace("true", "1").replace("false", "0") + ": " + serializedActionScore;
    }
}
