package com.example.flappybird;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class FlappyBirdInit extends Application {

    // Window
    static final int WINDOW_W = 576; // 2x 288 background width to look nicer
    static final int WINDOW_H = 512;

    // Game world
    private GraphicsContext gc;
    private Canvas canvas;

    // Sprites
    private Image IMG_BG_DAY, IMG_BG_NIGHT;
    private Image IMG_BASE;
    private Image IMG_PIPE_GREEN, IMG_PIPE_RED;
    private Image[] IMG_BIRD = new Image[3];
    private Image IMG_MESSAGE, IMG_GAMEOVER;

    // World entities
    private Bird player;
    private List<PipePair> pipes = new ArrayList<>();
    private double baseX = 0;
    private boolean nightMode = false;
    private boolean showMessage = true;
    private boolean gameOver = false;

    // Controls
    private boolean spacePressed = false;

    // Timing / random
    private long lastNano = 0;
    private double spawnTimer = 0;
    private final Random rnd = new Random();

    // Constants (tweak if desired)
    private static final double GRAVITY = 900;          // px/s^2
    private static final double FLAP_IMPULSE = -300;   // px/s
    private static final double PIPE_SPEED = -120;     // px/s (negative = move left)
    private static final double PIPE_GAP = 100;        // vertical gap between top & bottom
    private static final double PIPE_SPACING = 120;    // horizontal distance between pipes (reduzido)
    private static final double BASE_SCROLL_SPEED = 180; // chão mais rápido


    @Override
    public void start(Stage stage) {
        loadSprites();

        canvas = new Canvas(WINDOW_W, WINDOW_H);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // Input
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.SPACE) {
                spacePressed = true;
                if (showMessage) {
                    showMessage = false; // start game on first flap
                }
                if (!gameOver && player != null) player.flap();
                if (gameOver) restart();
            }
        });
        scene.setOnKeyReleased(ev -> {
            if (ev.getCode() == KeyCode.SPACE) spacePressed = false;
        });

        stage.setScene(scene);
        stage.setTitle("Flappy Bird - JavaFX");
        stage.show();

        initGame();

        lastNano = System.nanoTime();
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - lastNano) / 1e9;
                if (dt > 0.05) dt = 0.05; // clamp large dt
                update(dt);
                render();
                lastNano = now;
            }
        };
        loop.start();
    }

    private void initGame() {
        player = new Bird(WINDOW_W * 0.25, WINDOW_H * 0.45);
        pipes.clear();
        baseX = 0;
        spawnTimer = 0;
        gameOver = false;
        showMessage = true;
        nightMode = false;
        // preload a few pipes offscreen to the right
        double startX = WINDOW_W + 50;
        for (int i = 0; i < 3; i++) {
            spawnPipeAt(startX + i * (PIPE_SPACING + 52));
        }
    }

    private void restart() {
        initGame();
    }

    private void loadSprites() {
        try {
            IMG_BG_DAY = new Image(getClass().getResourceAsStream("/sprites/background-day.png"));
        } catch (Exception e) { IMG_BG_DAY = null; System.out.println("Não encontrou background-day.png"); }
        try {
            IMG_BG_NIGHT = new Image(getClass().getResourceAsStream("/sprites/background-night.png"));
        } catch (Exception e) { IMG_BG_NIGHT = null; System.out.println("Não encontrou background-night.png"); }

        try { IMG_BASE = new Image(getClass().getResourceAsStream("/sprites/base.png")); } catch (Exception e) { IMG_BASE = null; System.out.println("Não encontrou base.png"); }
        try { IMG_PIPE_GREEN = new Image(getClass().getResourceAsStream("/sprites/pipe-green.png")); } catch (Exception e) { IMG_PIPE_GREEN = null; System.out.println("Não encontrou pipe-green.png"); }
        try { IMG_PIPE_RED = new Image(getClass().getResourceAsStream("/sprites/pipe-red.png")); } catch (Exception e) { IMG_PIPE_RED = null; /* optional */ }

        try { IMG_BIRD[0] = new Image(getClass().getResourceAsStream("/sprites/yellowbird-upflap.png")); } catch (Exception e) { IMG_BIRD[0] = null; System.out.println("Não encontrou yellowbird-upflap.png"); }
        try { IMG_BIRD[1] = new Image(getClass().getResourceAsStream("/sprites/yellowbird-midflap.png")); } catch (Exception e) { IMG_BIRD[1] = null; System.out.println("Não encontrou yellowbird-midflap.png"); }
        try { IMG_BIRD[2] = new Image(getClass().getResourceAsStream("/sprites/yellowbird-downflap.png")); } catch (Exception e) { IMG_BIRD[2] = null; System.out.println("Não encontrou yellowbird-downflap.png"); }

        try { IMG_MESSAGE = new Image(getClass().getResourceAsStream("/sprites/message.png")); } catch (Exception e) { IMG_MESSAGE = null; System.out.println("Não encontrou message.png"); }
        try { IMG_GAMEOVER = new Image(getClass().getResourceAsStream("/sprites/gameover.png")); } catch (Exception e) { IMG_GAMEOVER = null; System.out.println("Não encontrou gameover.png"); }

        System.out.println("Sprites carregados:");
        System.out.println("BG day: " + (IMG_BG_DAY != null) + " night: " + (IMG_BG_NIGHT != null));
        System.out.println("Base: " + (IMG_BASE != null));
        System.out.println("Pipe green: " + (IMG_PIPE_GREEN != null));
        System.out.println("Bird frames: " + (IMG_BIRD[0]!=null) + "," + (IMG_BIRD[1]!=null) + "," + (IMG_BIRD[2]!=null));
        System.out.println("Message: " + (IMG_MESSAGE != null) + " Gameover: " + (IMG_GAMEOVER != null));
    }

    private void moveGround() {
        baseX -= 0;
        if (baseX <= -IMG_BASE.getWidth()) {
            baseX = 0;
        }
    }

    private void update(double dt) {
        if (showMessage) {
            moveGround();
            return;
        }

        if (!gameOver) {
            spawnTimer += dt;

            if (spawnTimer > 10.0) {
                nightMode = !nightMode;
                spawnTimer = 0;
            }

            player.update(dt);
            moveGround();

            // spawn pipes
            // spawn logic: keep at least 2-3 ahead
            if (pipes.isEmpty() || pipes.get(pipes.size()-1).x < WINDOW_W - (PIPE_SPACING + 52)) {
                spawnPipeAt(WINDOW_W + 50);
            }

            // move pipes and check collisions
            Iterator<PipePair> it = pipes.iterator();
            while (it.hasNext()) {
                PipePair p = it.next();
                p.x += PIPE_SPEED * dt;
                // remove if offscreen left
                if (p.x + 52 < -100) it.remove();
                // collision
                if (p.collidesWith(player)) {
                    player.alive = false;
                    gameOver = true;
                }
                // scoring (optional) - could implement later
            }

            // ground collision
            double baseTop = WINDOW_H - (IMG_BASE != null ? IMG_BASE.getHeight() : 100);
            if (player.y + player.h/2.0 >= baseTop) {
                player.y = baseTop - player.h/2.0;
                player.alive = false;
                gameOver = true;
            }
        } else {
            // on game over: let the bird fall gently
            player.update(dt);
        }
    }

    private void render() {
        // ==== BACKGROUND ====
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

        // ==== PIPES ====
        for (PipePair p : pipes) {
            Image pipeImg = IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN : IMG_PIPE_RED;

            if (pipeImg != null) {
                double pipeW = pipeImg.getWidth();
                double pipeH = pipeImg.getHeight();

                // TOP (flipped)
                gc.drawImage(pipeImg, p.x, p.topY, pipeW, -pipeH);

                // BOTTOM (normal)
                gc.drawImage(pipeImg, p.x, p.bottomY, pipeW, pipeH);
            } else {
                gc.setFill(Color.GREEN);
                gc.fillRect(p.x, p.topY - 200, 52, 200);
                gc.fillRect(p.x, p.bottomY, 52, 200);
            }
        }

        // ==== GROUND (ANTES DO PLAYER) ====
        if (IMG_BASE != null) {
            double w = IMG_BASE.getWidth();
            double baseY = WINDOW_H - IMG_BASE.getHeight();

            gc.drawImage(IMG_BASE, baseX, baseY);
            gc.drawImage(IMG_BASE, baseX + w, baseY);
        } else {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(0, WINDOW_H - 100, WINDOW_W, 100);
        }

        // ==== PLAYER ====
        player.render(gc);

        // ==== HUD DEBUG ====
        gc.setFill(Color.BLACK);
        gc.fillText("Pipes: " + pipes.size(), 10, 20);

        // ==== MESSAGE ====
        if (showMessage && IMG_MESSAGE != null) {
            double mx = WINDOW_W/2.0 - IMG_MESSAGE.getWidth()/2.0;
            double my = WINDOW_H*0.45 - IMG_MESSAGE.getHeight()/2.0;
            gc.drawImage(IMG_MESSAGE, mx, my);
        }

        // ==== GAME OVER ====
        if (gameOver && IMG_GAMEOVER != null) {
            double gx = WINDOW_W/2.0 - IMG_GAMEOVER.getWidth()/2.0;
            double gy = WINDOW_H*0.25 - IMG_GAMEOVER.getHeight()/2.0;
            gc.drawImage(IMG_GAMEOVER, gx, gy);
        }
    }

    private void spawnPipeAt(double x) {
        double minY = 80;
        double maxY = WINDOW_H - 160 - (IMG_BASE != null ? IMG_BASE.getHeight() : 100);
        double centerY = minY + rnd.nextDouble() * (maxY - minY);
        pipes.add(new PipePair(x, centerY, PIPE_GAP));
    }

    private double mod(double a, double m) {
        if (m == 0) return 0;
        double r = a % m;
        if (r < 0) r += m;
        return r;
    }

    // ----------------- INNER CLASSES -----------------

    private class Bird {
        double x, y;
        double w = 34, h = 24; // logical size (matches sprites)
        double vy = 0;
        double rotation = 0; // degrees
        int animIndex = 0;
        double animTimer = 0;
        boolean alive = true;

        Bird(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void flap() {
            vy = FLAP_IMPULSE;
            rotation = -25;
        }

        void update(double dt) {
            if (alive) {
                vy += GRAVITY * dt;
                y += vy * dt;
                // rotate gradually down when falling
                rotation += 200 * dt;
                if (rotation > 90) rotation = 90;
            } else {
                // when dead, just fall faster
                vy += GRAVITY * dt;
                y += vy * dt;
                rotation += 200 * dt;
                if (rotation > 90) rotation = 90;
            }

            // animation: change flap frame every 0.12s
            animTimer += dt;
            if (animTimer >= 0.12) {
                animIndex = (animIndex + 1) % 3;
                animTimer = 0;
            }
        }

        void render(GraphicsContext gc) {
            Image frame = IMG_BIRD[animIndex];
            if (frame != null) {
                double drawW = frame.getWidth();
                double drawH = frame.getHeight();
                gc.save();
                // translate to bird center
                gc.translate(x, y);
                gc.rotate(rotation);
                gc.drawImage(frame, -drawW/2.0, -drawH/2.0);
                gc.restore();
            } else {
                gc.setFill(Color.YELLOW);
                gc.save();
                gc.translate(x, y);
                gc.rotate(rotation);
                gc.fillOval(-w/2.0, -h/2.0, w, h);
                gc.restore();
            }
        }

        // bounding box for collision
        double left() { return x - (w/2.0); }
        double right() { return x + (w/2.0); }
        double top() { return y - (h/2.0); }
        double bottom() { return y + (h/2.0); }
    }

    private class PipePair {
        double x;
        double centerY;
        double gap;
        double topY;    // y coordinate of bottom of top pipe
        double bottomY; // y coordinate of top of bottom pipe

        PipePair(double x, double centerY, double gap) {
            this.x = x;
            this.centerY = centerY;
            this.gap = gap;
            computeYs();
        }

        void computeYs() {
            double halfGap = gap/2.0;
            // top pipe bottom edge at centerY - halfGap (we will draw top flipped)
            topY = centerY - halfGap;
            // bottom pipe top edge at centerY + halfGap
            bottomY = centerY + halfGap;
        }

        boolean collidesWith(Bird b) {
            // simple AABB collision using pipe rectangles
            double pipeW = (IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN.getWidth() : 52);
            double pipeH = (IMG_PIPE_GREEN != null ? IMG_PIPE_GREEN.getHeight() : 320);

            // top pipe rectangle (we consider the pipe's visible rect bottom aligned at topY)
            double topRectX = x;
            double topRectY = topY - pipeH; // top pipe drawn with bottom at topY
            double topRectW = pipeW;
            double topRectH = pipeH;

            // bottom pipe rectangle
            double botRectX = x;
            double botRectY = bottomY;
            double botRectW = pipeW;
            double botRectH = pipeH;

            // bird rect
            double bx = b.left();
            double by = b.top();
            double bw = b.w;
            double bh = b.h;

            if (rectOverlap(bx, by, bw, bh, topRectX, topRectY, topRectW, topRectH)) return true;
            if (rectOverlap(bx, by, bw, bh, botRectX, botRectY, botRectW, botRectH)) return true;
            return false;
        }

        private boolean rectOverlap(double x1, double y1, double w1, double h1, double x2, double y2, double w2, double h2) {
            if (x1 + w1 <= x2) return false;
            if (x1 >= x2 + w2) return false;
            if (y1 + h1 <= y2) return false;
            if (y1 >= y2 + h2) return false;
            return true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
