# Flappy Bird AI — Neural Network with Genetic Algorithm in Java

A Flappy Bird clone where the bird is controlled by a neural network trained through a genetic algorithm. The AI learns to navigate pipes by evolving across generations — no backpropagation, no manual training data.

---

## How It Works

Each generation starts with a population of birds, each controlled by its own neural network with random weights. Birds that survive longer score higher fitness. The best performers are selected, their weights are combined (crossover) and slightly mutated, and a new generation is born.

```
Generation N
    │
    ▼
Population of birds (each with unique neural network)
    │
    ▼
Simulate: birds play until they all die
    │
    ▼
Evaluate fitness (distance traveled / pipes cleared)
    │
    ▼
Selection → Crossover → Mutation
    │
    ▼
Generation N+1 (smarter population)
```

**Network architecture:**
```
Input:   Bird Y position, velocity, distance to next pipe, pipe gap Y
Hidden:  Configurable hidden layer
Output:  1 neuron → flap or don't flap
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI | JavaFX |
| Build | Maven |
| ML | Custom neural net + genetic algorithm (no libraries) |

---

## Running Locally

**Requirements:** Java 17+, Maven 3.8+

```bash
git clone https://github.com/cherohn/FlappyBirdIA.git
cd FlappyBirdIA
mvn clean compile exec:java
```

Watch the AI improve in real time across generations.

---

## What I Learned

- How genetic algorithms differ from gradient-based learning (no error signal, only survival)
- How fitness function design directly shapes what the AI learns to optimize
- Why diversity in the initial population matters for avoiding local optima
- How to run a real-time game loop alongside an evolutionary training cycle

---

## Author

**Matheus Garcez** — [github.com/cherohn](https://github.com/cherohn)
