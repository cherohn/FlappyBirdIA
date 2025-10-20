package com.example.flappybird;

import com.example.flappybird.AIManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FlappyBirdInit extends Application {

    // Window
    public static final int WINDOW_W = 576;
    public static final int WINDOW_H = 512;

    // Game world
    private GraphicsContext gc;
    private Canvas canvas;

    // Sprites
    private Image IMG_BG_DAY, IMG_BG_NIGHT;
    private Image IMG_BASE;
    private Image IMG_PIPE_GREEN, IMG_PIPE_RED;
    private final Image[][] BIRD_COLORS = new Image[3][]; // 0=yellow, 1=blue, 2=red
    private Image IMG_MESSAGE, IMG_GAMEOVER;

    // World entities
    public List<PipePair> pipes = new ArrayList<>();
    private double baseX = 0;
    private boolean nightMode = false;

    // AI
    private AIManager ai;
    public long framesAlive = 0;

    // Timing / random
    private long lastNano = 0;
    private double spawnTimer = 0;
    private final Random rnd = new Random();

    // Constants
    private static final double GRAVITY = 900;
    private static final double FLAP_IMPULSE = -300;
    private static final double PIPE_SPEED = -120;
    private static final double PIPE_GAP = 100;
    private static final double PIPE_SPACING = 120;
    private static final double BASE_SCROLL_SPEED = 180;

    @Override
    public void start(Stage stage) {
        loadSprites();

        canvas = new Canvas(WINDOW_W, WINDOW_H);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // (input manual desabilitado para IA)
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) System.exit(0);
        });

        stage.setScene(scene);
        stage.setTitle("Flappy Bird - Neural AI");
        stage.show();

        initGame();

        lastNano = System.nanoTime();
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - lastNano) / 1e9;
                if (dt > 0.05) dt = 0.05;
                update(dt);
                render();
                lastNano = now;
            }
        };
        loop.start();
    }

    private void initGame() {
        if (ai == null) ai = new AIManager(this);
        ai.initGeneration();
        pipes.clear();
        baseX = 0;
        spawnTimer = 0;
        framesAlive = 0;
        nightMode = false;

        // atribui cores aleatórias para os pássaros
        Random rnd = new Random();
        for (FlappyBirdInit.Bird b : ai.birds) {
            b.colorIndex = rnd.nextInt(3); // 0 amarelo, 1 azul, 2 vermelho
        }

        double startX = WINDOW_W + 50;
        for (int i = 0; i < 3; i++) {
            spawnPipeAt(startX + i * (PIPE_SPACING + 52));
        }
    }

    public void restartPipes() {
        pipes.clear();
        double startX = WINDOW_W + 50;
        for (int i = 0; i < 3; i++) {
            spawnPipeAt(startX + i * (PIPE_SPACING + 52));
        }
    }

    private void loadSprites() {
        try { IMG_BG_DAY = new Image(getClass().getResourceAsStream("/sprites/background-day.png")); } catch (Exception e) { IMG_BG_DAY = null; }
        try { IMG_BG_NIGHT = new Image(getClass().getResourceAsStream("/sprites/background-night.png")); } catch (Exception e) { IMG_BG_NIGHT = null; }
        try { IMG_BASE = new Image(getClass().getResourceAsStream("/sprites/base.png")); } catch (Exception e) { IMG_BASE = null; }
        try { IMG_PIPE_GREEN = new Image(getClass().getResourceAsStream("/sprites/pipe-green.png")); } catch (Exception e) { IMG_PIPE_GREEN = null; }
        try { IMG_PIPE_RED = new Image(getClass().getResourceAsStream("/sprites/pipe-red.png")); } catch (Exception e) { IMG_PIPE_RED = null; }

        // Amarelo
        BIRD_COLORS[0] = new Image[]{
                new Image(getClass().getResourceAsStream("/sprites/yellowbird-upflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/yellowbird-midflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/yellowbird-downflap.png"))
        };
        // Azul
        BIRD_COLORS[1] = new Image[]{
                new Image(getClass().getResourceAsStream("/sprites/bluebird-upflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/bluebird-midflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/bluebird-downflap.png"))
        };
        // Vermelho
        BIRD_COLORS[2] = new Image[]{
                new Image(getClass().getResourceAsStream("/sprites/redbird-upflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/redbird-midflap.png")),
                new Image(getClass().getResourceAsStream("/sprites/redbird-downflap.png"))
        };

        try { IMG_MESSAGE = new Image(getClass().getResourceAsStream("/sprites/message.png")); } catch (Exception e) { IMG_MESSAGE = null; }
        try { IMG_GAMEOVER = new Image(getClass().getResourceAsStream("/sprites/gameover.png")); } catch (Exception e) { IMG_GAMEOVER = null; }
    }

    private void moveGround() {
        baseX -= 0;
        if (baseX <= -IMG_BASE.getWidth()) baseX = 0;
    }

    private void update(double dt) {
        framesAlive++;

        ai.update(dt);

        // Atualiza cada pássaro e verifica limites (teto/chão)
        for (int i = 0; i < ai.birds.size(); i++) {
            Bird b = ai.birds.get(i);
            if (!ai.alive.get(i)) continue;

            b.update(dt);

            double baseTop = WINDOW_H - (IMG_BASE != null ? IMG_BASE.getHeight() : 100);

            // Teto
            if (b.y - b.h / 2.0 <= 0) {
                b.y = b.h / 2.0;
                b.alive = false;
                ai.markDead(i);
                continue;
            }

            // Chão
            if (b.y + b.h / 2.0 >= baseTop) {
                b.y = baseTop - b.h / 2.0;
                b.alive = false;
                ai.markDead(i);
                continue;
            }
        }

        moveGround();

        // Spawn de canos
        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < WINDOW_W - (PIPE_SPACING + 52)) {
            spawnPipeAt(WINDOW_W + 50);
        }

        // Move canos + colisões
        Iterator<PipePair> it = pipes.iterator();
        while (it.hasNext()) {
            PipePair p = it.next();
            p.x += PIPE_SPEED * dt;
            if (p.x + 52 < -100) it.remove();

            for (int i = 0; i < ai.birds.size(); i++) {
                Bird b = ai.birds.get(i);
                if (!ai.alive.get(i)) continue;
                if (p.collidesWith(b)) {
                    b.alive = false;
                    ai.markDead(i);
                }
            }
        }

        // Reinicia se todos morreram
        if (ai.allDead()) {
            ai.evolveAndRestart();
            framesAlive = 0;
        }
    }

    private void render() {
        // fundo
        gc.setFill(Color.web("#4ec0d8"));
        gc.fillRect(0, 0, WINDOW_W, WINDOW_H);

        Image bg = nightMode && IMG_BG_NIGHT != null ? IMG_BG_NIGHT : IMG_BG_DAY;
        if (bg != null) {
            double scale = WINDOW_H / bg.getHeight();
            double w = bg.getWidth() * scale;
            for (double x = 0; x < WINDOW_W + w; x += w) {
                gc.drawImage(bg, x, 0, w, WINDOW_H);
            }
        }

        // pipes
        for (PipePair p : pipes) {
            Image pipeImg = IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN : IMG_PIPE_RED;
            if (pipeImg != null) {
                double pipeW = pipeImg.getWidth();
                double pipeH = pipeImg.getHeight();
                gc.drawImage(pipeImg, p.x, p.topY, pipeW, -pipeH);
                gc.drawImage(pipeImg, p.x, p.bottomY, pipeW, pipeH);
            } else {
                gc.setFill(Color.GREEN);
                gc.fillRect(p.x, p.topY - 200, 52, 200);
                gc.fillRect(p.x, p.bottomY, 52, 200);
            }
        }

        // chão
        if (IMG_BASE != null) {
            double w = IMG_BASE.getWidth();
            double baseY = WINDOW_H - IMG_BASE.getHeight();
            gc.drawImage(IMG_BASE, baseX, baseY);
            gc.drawImage(IMG_BASE, baseX + w, baseY);
        } else {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(0, WINDOW_H - 100, WINDOW_W, 100);
        }

        // pássaros IA
        for (Bird b : ai.birds) {
            if (b.alive) b.render(gc);
        }

        // HUD
        gc.setFill(Color.BLACK);
        gc.fillText("Geração: " + ai.generation, 10, 20);
        gc.fillText("Melhor fitness: " + (int) ai.bestFitness, 10, 40);
        gc.fillText("Vivos: " + ai.aliveCount, 10, 60);
    }

    public int procurarProximoObstaculo(double birdX) {
        double menor = Double.POSITIVE_INFINITY;
        int idx = 0;
        for (int i = 0; i < pipes.size(); i++) {
            PipePair p = pipes.get(i);
            if (p.x + 52 >= birdX && p.x < menor) {
                menor = p.x;
                idx = i;
            }
        }
        return idx;
    }

    private void spawnPipeAt(double x) {
        double minY = 80;
        double maxY = WINDOW_H - 160 - (IMG_BASE != null ? IMG_BASE.getHeight() : 100);
        double centerY = minY + rnd.nextDouble() * (maxY - minY);
        pipes.add(new PipePair(x, centerY, PIPE_GAP));
    }

    // ----------------- INNER CLASSES -----------------

    public class Bird {
        double x, y;
        double w = 34, h = 24;
        double vy = 0;
        double rotation = 0;
        int animIndex = 0;
        double animTimer = 0;
        public boolean alive = true;
        int colorIndex = 0; // 0 amarelo, 1 azul, 2 vermelho

        Bird(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void flap() {
            vy = FLAP_IMPULSE;
            rotation = -25;
        }

        void update(double dt) {
            vy += GRAVITY * dt;
            y += vy * dt;

            if (vy < 0) rotation = -25;
            else if (vy < 200) rotation = 0;
            else rotation = 90;

            animTimer += dt;
            if (animTimer >= 0.12) {
                animIndex = (animIndex + 1) % 3;
                animTimer = 0;
            }
        }

        void render(GraphicsContext gc) {
            Image frame = BIRD_COLORS[colorIndex][animIndex];
            gc.save();
            gc.translate(x, y);
            gc.rotate(rotation);

            if (frame != null) {
                double drawW = frame.getWidth();
                double drawH = frame.getHeight();
                gc.drawImage(frame, -drawW / 2.0, -drawH / 2.0);
            } else {
                gc.setFill(Color.YELLOW);
                gc.fillOval(-w / 2.0, -h / 2.0, w, h);
            }

            gc.restore();
        }

        double left() { return x - (w / 2.0); }
        double right() { return x + (w / 2.0); }
        double top() { return y - (h / 2.0); }
        double bottom() { return y + (h / 2.0); }
    }

    public class PipePair {
        double x;
        double centerY;
        double gap;
        double topY;
        double bottomY;

        PipePair(double x, double centerY, double gap) {
            this.x = x;
            this.centerY = centerY;
            this.gap = gap;
            computeYs();
        }

        void computeYs() {
            double halfGap = gap / 2.0;
            topY = centerY - halfGap;
            bottomY = centerY + halfGap;
        }

        boolean collidesWith(Bird b) {
            double pipeW = (IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN.getWidth() : 52);
            double pipeH = (IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN.getHeight() : 320);

            double topRectX = x;
            double topRectY = topY - pipeH;
            double topRectW = pipeW;
            double topRectH = pipeH;

            double botRectX = x;
            double botRectY = bottomY;
            double botRectW = pipeW;
            double botRectH = pipeH;

            double bx = b.left();
            double by = b.top();
            double bw = b.w;
            double bh = b.h;

            if (rectOverlap(bx, by, bw, bh, topRectX, topRectY, topRectW, topRectH)) return true;
            if (rectOverlap(bx, by, bw, bh, botRectX, botRectY, botRectW, botRectH)) return true;
            return false;
        }

        private boolean rectOverlap(double x1, double y1, double w1, double h1,
                                    double x2, double y2, double w2, double h2) {
            return !(x1 + w1 <= x2 || x1 >= x2 + w2 || y1 + h1 <= y2 || y1 >= y2 + h2);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
