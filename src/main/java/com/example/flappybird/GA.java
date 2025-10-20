package com.example.flappybird;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class GA {
    public NeuralNetwork[] population;
    public double[][] genomes; // flat genomes for quick mutate/copy
    public double[] fitness;
    private Random rnd = new Random();

    public GA(int popSize, int hiddenLayers, int inputCount, int hiddenSize, int outputCount) {
        population = new NeuralNetwork[popSize];
        genomes = new double[popSize][];
        fitness = new double[popSize];
        for (int i = 0; i < popSize; i++) {
            population[i] = new NeuralNetwork(hiddenLayers, inputCount, hiddenSize, outputCount);
            genomes[i] = population[i].toGenome();
            fitness[i] = 0;
        }
    }

    public int size() { return population.length; }

    public void setFitness(int idx, double f) { fitness[idx] = f; }

    // selection + clonagem + mutação inspired by RandomMutations() do C
    public void evolve() {
        int N = size();
        // create array of indices sorted by fitness descending
        Integer[] idxs = new Integer[N];
        for (int i = 0; i < N; i++) idxs[i] = i;
        Arrays.sort(idxs, Comparator.comparingDouble((Integer k) -> fitness[k]).reversed());

        // keep top K clones
        int Step = Math.max(1, 5); // keep first `Step` as templates
        // clone: for i in 0..Step-1 make copies across population in pattern from C
        for (int i = 0; i < Step; i++) {
            for (int j = Step + i; j < N; j += Step) {
                // copy genome of ranked i to ranked j
                int src = idxs[i];
                int dst = idxs[j];
                System.arraycopy(genomes[src], 0, genomes[dst], 0, genomes[src].length);
            }
        }

        // mutate the others (following C logic)
        double RangeRandom = genomes[0].length;
        for (int r = Step; r < N; r++) {
            int index = idxs[r];
            int mutations = rnd.nextInt((int)RangeRandom) + 1;
            for (int m = 0; m < mutations; m++) {
                int tipo = rnd.nextInt(3);
                int gidx = rnd.nextInt(genomes[index].length);
                switch (tipo) {
                    case 0:
                        genomes[index][gidx] = rnd.nextDouble()*2000.0 - 1000.0;
                        break;
                    case 1:
                        double num = (rnd.nextInt(10001)/10000.0) + 0.5;
                        genomes[index][gidx] = genomes[index][gidx] * num;
                        break;
                    case 2:
                        double n = (rnd.nextDouble()*2000.0 - 1000.0)/100.0;
                        genomes[index][gidx] = genomes[index][gidx] + n;
                        break;
                }
            }
        }

        // copy genomes back into networks
        for (int i = 0; i < N; i++) {
            population[i].fromGenome(genomes[i]);
            fitness[i] = 0; // reset
        }
    }

    // optional: get best index
    public int bestIndex() {
        int best = 0;
        for (int i = 1; i < fitness.length; i++) if (fitness[i] > fitness[best]) best = i;
        return best;
    }
}