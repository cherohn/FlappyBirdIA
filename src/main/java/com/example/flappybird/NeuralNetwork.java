package com.example.flappybird;

import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork {
    private int inputCount;   // sem bias (usamos internamente o bias)
    private int hiddenLayers;
    private int hiddenSize;   // por camada (assumimos todas iguais como no C)
    private int outputCount;

    // estruturas: pesos por camada (matriz: neurons x inputs)
    // camadas escondidas: hiddenLayers arrays
    public double[][][] weightsHidden; // [layer][neuron][weightIndex]
    public double[][] weightsOut;      // [neuronOut][weightIndex]

    private static final Random rnd = new Random();

    public NeuralNetwork(int hiddenLayers, int inputCount, int hiddenSize, int outputCount) {
        this.hiddenLayers = hiddenLayers;
        this.inputCount = inputCount;
        this.hiddenSize = hiddenSize;
        this.outputCount = outputCount;

        // cria arrays, lembrando de adicionar BIAS internamente: cada camada de entrada espera +1 bias
        weightsHidden = new double[hiddenLayers][][];
        for (int L = 0; L < hiddenLayers; L++) {
            int inCount = (L == 0) ? inputCount + 1 : hiddenSize + 1; // +1 para bias
            weightsHidden[L] = new double[hiddenSize][inCount];
            for (int i = 0; i < hiddenSize; i++) {
                for (int j = 0; j < inCount; j++) {
                    weightsHidden[L][i][j] = rnd.nextDouble() * 2000 - 1000; // similar ao C
                }
            }
        }
        // saída: cada neuron de saída ligado aos neurônios ocultos + bias
        int outInCount = hiddenSize + 1;
        weightsOut = new double[outputCount][outInCount];
        for (int i = 0; i < outputCount; i++) {
            for (int j = 0; j < outInCount; j++) {
                weightsOut[i][j] = rnd.nextDouble() * 2000 - 1000;
            }
        }
    }

    // ReLU (igual ao C)
    private double relu(double x) {
        if (x < 0) return 0;
        if (x < 10000) return x;
        return 10000;
    }

    // feedforward: entrada já normalizada
    public double[] compute(double[] input) {
        // input length must be inputCount
        // primeira camada: consider bias com valor 1
        double[] prevOutputs = new double[inputCount + 1];
        System.arraycopy(input, 0, prevOutputs, 0, inputCount);
        prevOutputs[inputCount] = 1.0; // bias

        // camadas escondidas
        for (int L = 0; L < hiddenLayers; L++) {
            double[] outLayer = new double[hiddenSize + 1]; // + bias
            for (int n = 0; n < hiddenSize; n++) {
                double sum = 0;
                double[] w = weightsHidden[L][n];
                for (int k = 0; k < w.length; k++) {
                    sum += prevOutputs[k] * w[k];
                }
                outLayer[n] = relu(sum);
            }
            outLayer[hiddenSize] = 1.0; // bias
            prevOutputs = outLayer;
        }

        // saída
        double[] outputs = new double[outputCount];
        for (int o = 0; o < outputCount; o++) {
            double sum = 0;
            for (int k = 0; k < weightsOut[o].length; k++) {
                sum += prevOutputs[k] * weightsOut[o][k];
            }
            outputs[o] = sigmoid(sum);
        }
        return outputs;
    }

    private double sigmoid(double x) {
        // prevenção overflow leve
        if (x < -60) return 0.0;
        if (x > 60) return 1.0;
        return 1.0 / (1.0 + Math.exp(-x));
    }

    // tamanho total dos pesos (DNA length)
    public int totalWeights() {
        int sum = 0;
        for (int L = 0; L < hiddenLayers; L++) {
            sum += hiddenSize * ( (L==0 ? inputCount+1 : hiddenSize+1) );
        }
        sum += outputCount * (hiddenSize + 1);
        return sum;
    }

    // converter para vetor (flat) - mesma ordem do C: todas camadas escondidas (layer by layer), depois saída
    public double[] toGenome() {
        int len = totalWeights();
        double[] genome = new double[len];
        int idx = 0;
        for (int L = 0; L < hiddenLayers; L++) {
            for (int n = 0; n < hiddenSize; n++) {
                for (int w = 0; w < weightsHidden[L][n].length; w++) {
                    genome[idx++] = weightsHidden[L][n][w];
                }
            }
        }
        for (int o = 0; o < outputCount; o++) {
            for (int w = 0; w < weightsOut[o].length; w++) {
                genome[idx++] = weightsOut[o][w];
            }
        }
        return genome;
    }

    // aplicar genome (copiar vetor para pesos)
    public void fromGenome(double[] genome) {
        int idx = 0;
        for (int L = 0; L < hiddenLayers; L++) {
            for (int n = 0; n < hiddenSize; n++) {
                for (int w = 0; w < weightsHidden[L][n].length; w++) {
                    weightsHidden[L][n][w] = genome[idx++];
                }
            }
        }
        for (int o = 0; o < outputCount; o++) {
            for (int w = 0; w < weightsOut[o].length; w++) {
                weightsOut[o][w] = genome[idx++];
            }
        }
    }

    // criar rede clonada (pesos copiados)
    public NeuralNetwork cloneNetwork() {
        NeuralNetwork n = new NeuralNetwork(hiddenLayers, inputCount, hiddenSize, outputCount);
        n.fromGenome(this.toGenome());
        return n;
    }
}

