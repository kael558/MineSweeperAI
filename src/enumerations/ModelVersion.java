package enumerations;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.deeplearning4j.nn.weights.WeightInit;

public enum ModelVersion {
    AI_5x5_v1("training_data5x5.txt", "model5x5.zip", WeightInit.XAVIER, 0.00001, OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,
            new DenseLayer(97, 300, Activation.RELU), new OutputLayer(300, 1, Activation.SIGMOID, LossFunction.MSE)),
    AI_Full("training_dataFull.txt", "modelFull.zip", WeightInit.XAVIER, 0.00001, OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,
            new DenseLayer(97, 300, Activation.RELU), new OutputLayer(300, 1, Activation.SIGMOID, LossFunction.MSE));

    public String trainingFilename, modelFilename;
    public int seed;
    public WeightInit weightInit;
    public double learningRate;
    public OptimizationAlgorithm optimizationAlgorithm;
    public Layer[] layers;

    ModelVersion(String trainingFilename, String modelFilename, WeightInit weightInit, double learningRate, OptimizationAlgorithm optimizationAlgorithm, Layer... layers) {
        this.seed = 3;
        this.trainingFilename = trainingFilename;
        this.modelFilename = modelFilename;
        this.weightInit = weightInit;
        this.learningRate = learningRate;
        this.optimizationAlgorithm = optimizationAlgorithm;
        this.layers = layers;
    }

    public static class Layer{
        public int inputs, outputs;
        public Activation activation;
        public Layer(int inputs, int outputs, Activation activation) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.activation = activation;
        }
    }

    public static class DenseLayer extends Layer{
        public DenseLayer(int inputs, int outputs, Activation activation) {
            super(inputs, outputs, activation);
        }
    }

    public static class OutputLayer extends Layer{
        public LossFunction lossFunction;
        public OutputLayer(int inputs, int outputs, Activation activation,  LossFunction lossFunction) {
            super(inputs, outputs, activation);
            this.lossFunction = lossFunction;
        }
    }
}
