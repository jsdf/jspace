package jspace;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

public class Main extends JFrame implements KeyListener {
    Game game;
    JPanel viewport;
    double lastUpdateTimeInNanoseconds;
    HashSet<String> keysDown;

    Main() {
        this.setTitle("Spaaaaace");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.game = new Game();
        this.viewport = new Viewport(this);
        this.add(viewport);
        this.setLocationRelativeTo(null); // center window
        this.setVisible(true);

        this.keysDown = new HashSet<String>();
        this.game.keysDown = this.keysDown;
        this.addKeyListener(this);

        this.runGameMainLoop();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.setVisible(true);
    }

    public void draw(Graphics g) {
        this.game.graphics = g;
        this.game.screenWidth = this.viewport.getWidth();
        this.game.screenHeight = this.viewport.getHeight();
        this.game.draw();
    }

    public void keyPressed(KeyEvent e) {
        this.keysDown.add(KeyEvent.getKeyText(e.getKeyCode()));
    }

    public void keyReleased(KeyEvent e) {
        this.keysDown.remove(KeyEvent.getKeyText(e.getKeyCode()));
    }

    public void keyTyped(KeyEvent e) {
        // ignore
    }

    private void runGameMainLoop() {
        this.lastUpdateTimeInNanoseconds = System.nanoTime();
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Thread.sleep can throw this exception, but shouldn't in our program
                e.printStackTrace();
            }

            double currentTimeInNanoseconds = System.nanoTime();
            double deltaTimeInNanoseconds = currentTimeInNanoseconds - this.lastUpdateTimeInNanoseconds;
            double deltaTimeInSeconds = (deltaTimeInNanoseconds) / 1000000000; // convert nanoseconds to seconds
            if (deltaTimeInSeconds > 0.016) {
                game.update(deltaTimeInSeconds);
                this.viewport.repaint();
                this.lastUpdateTimeInNanoseconds = currentTimeInNanoseconds;
            }
        }
    }

    class Viewport extends JPanel {
        Main mainApp;

        Viewport(Main mainApp) {
            this.mainApp = mainApp;
        }

        @Override
        public void paintComponent(Graphics g) {
            this.mainApp.draw(g);
        }
    }
}
