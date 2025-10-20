package com.example.flappybird;

import com.example.flappybird.FlappyBirdInit; // ajuste pacote se necessário

public class AgentBird {
    public FlappyBirdInit.Bird bird; // referência ao Bird do seu jogo
    public NeuralNetwork brain;
    public double fitness;
    public double[] genome;

    public AgentBird(FlappyBirdInit.Bird b, NeuralNetwork brain) {
        this.bird = b;
        this.brain = brain;
        this.genome = brain.toGenome();
        this.fitness = 0;
    }

    // compute inputs normalized and get outputs
    // você precisa implementar um método no jogo para retornar próximos canos (index etc).
    public boolean decide(double distH, double distV, double pipeVy, double gapSize, double windowW, double windowH) {
        // normalize (exemplos)
        double in0 = distH / windowW; // 0..1 (ou maior)
        double in1 = distV / windowH; // -1..1 maybe
        double in2 = pipeVy / 5.0;     // ajuste denom conforme escala do seu jogo
        double in3 = gapSize / windowH;

        double[] input = new double[] { in0, in1, in2, in3 };
        double[] out = brain.compute(input);

        boolean jump = out[0] > 0.0;
        boolean parachute = out[1] > 0.0;
        // aplique efeitos ao bird no loop externo (ex: if (jump) bird.flap();)
        return jump;
    }
}
