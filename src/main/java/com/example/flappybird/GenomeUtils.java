package com.example.flappybird;

import java.util.Random;

public class GenomeUtils {
    private static final Random rnd = new Random();

    // mutation types inspired by C:
    // 0: replace with random value in [-1000,1000]
    // 1: multiply by random [0.5,1.5]
    // 2: add small random value (getRandomValue()/100)
    public static void mutate(double[] genome) {
        // choose number of mutations randomly up to genome.length
        int mutations = rnd.nextInt(genome.length) + 1;
        for (int m = 0; m < mutations; m++) {
            int tipo = rnd.nextInt(3);
            int idx = rnd.nextInt(genome.length);
            switch (tipo) {
                case 0:
                    genome[idx] = rnd.nextDouble()*2000.0 - 1000.0;
                    break;
                case 1:
                    double factor = (rnd.nextInt(10001)/10000.0) + 0.5; // [0.5,1.5]
                    genome[idx] = genome[idx] * factor;
                    break;
                case 2:
                    double number = (rnd.nextDouble()*2000.0 - 1000.0)/100.0;
                    genome[idx] = genome[idx] + number;
                    break;
            }
        }
    }
}