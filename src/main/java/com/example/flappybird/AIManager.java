package com.example.flappybird;

import com.example.flappybird.FlappyBirdInit;
import java.util.ArrayList;
import java.util.List;

public class AIManager {

    public GA ga;
    public List<FlappyBirdInit.Bird> birds = new ArrayList<>();
    public List<Boolean> alive = new ArrayList<>();
    public int generation = 1;
    public double bestFitness = 0;
    public int aliveCount = 0;

    private static final int POP_SIZE = 1000;
    private static final int INPUTS = 4;
    private static final int HIDDEN = 6;
    private static final int OUTPUTS = 2;

    private FlappyBirdInit game;

    public AIManager(FlappyBirdInit game) {
        this.game = game;
        ga = new GA(POP_SIZE, 1, INPUTS, HIDDEN, OUTPUTS);
    }

    public void initGeneration() {
        birds.clear();
        alive.clear();
        for (int i = 0; i < ga.size(); i++) {
            double baseX = FlappyBirdInit.WINDOW_W * 0.25;
            double baseY = FlappyBirdInit.WINDOW_H * 0.45;
            double yOffset = (Math.random() - 0.5) * 40; // ±20 px aleatório
            double xOffset = (Math.random() - 0.5) * 10; // ±5 px aleatório (opcional)
            FlappyBirdInit.Bird b = game.new Bird(baseX + xOffset, baseY + yOffset);
            birds.add(b);
            alive.add(true);
        }
        aliveCount = birds.size();
    }

    public void update(double dt) {
        for (int i = 0; i < birds.size(); i++) {
            FlappyBirdInit.Bird b = birds.get(i);
            if (!alive.get(i)) continue;

            // Se morreu, marca como morto
            if (!b.alive) {
                markDead(i);
                continue;
            }

            // Procura o próximo cano
            int idx = game.procurarProximoObstaculo(b.x);
            if (idx >= game.pipes.size()) continue;
            FlappyBirdInit.PipePair p = game.pipes.get(idx);

            // Calcula distância horizontal e vertical normalizada
            double distH = (p.x + 26) - b.x;
            double distV = ((p.bottomY + p.topY) / 2.0) - b.y;
            double gap = p.gap;

            // Inputs para a rede neural: distH, distV, velocidade vertical do pássaro, gap
            double[] input = new double[]{
                    distH / FlappyBirdInit.WINDOW_W,
                    distV / FlappyBirdInit.WINDOW_H,
                    b.vy / 500.0, // normaliza a velocidade vertical
                    gap / FlappyBirdInit.WINDOW_H
            };

            // Saída da rede neural
            double[] out = ga.population[i].compute(input);

            // Decisão de flap
            // Para teste inicial, força flap se a rede neural der qualquer valor positivo
            if (out[0] > 0.5) b.flap(); // começa com mais flaps



        }
    }


    public void markDead(int i) {
        if (alive.get(i)) {
            alive.set(i, false);
            ga.setFitness(i, game.framesAlive);
            aliveCount--;
        }
    }

    public boolean allDead() {
        return aliveCount <= 0;
    }

    public void evolveAndRestart() {
        double maxFit = 0;
        for (double f : ga.fitness) if (f > maxFit) maxFit = f;
        if (maxFit > bestFitness) bestFitness = maxFit;

        System.out.println("Geração " + generation + " - Melhor fitness: " + maxFit);

        ga.evolve();
        generation++;
        initGeneration();
        game.restartPipes();
    }
}
