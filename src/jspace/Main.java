package jspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.concurrent.locks.*;

public class Main extends JFrame {
    Game game;
    JPanel viewport;
    KeyboardEventListener keyListener;
    HashSet<String> keysDown;
    int fps = 0;
    private Lock redrawLock = new ReentrantLock();

    static final int NANOSECONDS_IN_1SEC = 1000000000;
    static final double NANOSECONDS_IN_1_60TH_OF_A_SEC = NANOSECONDS_IN_1SEC / 60;

    Main() {
        this.game = new Game();

        // set up JFrame window junk
        this.setTitle("Spaaaaace");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.viewport = new Viewport(this);
        this.add(viewport);
        this.setLocationRelativeTo(null); // center window
        this.setVisible(true);

        // set up AWT KeyListener to listen for keypresses and make this.keysDown always contain
        // the set of currently pressed keys so the game can just check that a key is pressed
        // by doing keysDown.contains(key)
        this.keysDown = new HashSet<String>();
        this.keyListener = new KeyboardEventListener(this.keysDown);
        this.addKeyListener(this.keyListener);
        this.game.keysDown = this.keysDown;

        this.runGameMainLoop();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.setVisible(true);
    }

    public void draw(Graphics g) {
        // lock to prevent update and draw from happening at the same time
        this.redrawLock.lock();

        this.game.graphics = g;
        this.game.screenWidth = this.viewport.getWidth();
        this.game.screenHeight = this.viewport.getHeight();
        this.game.draw();

        this.redrawLock.unlock();
    }


    private void runGameMainLoop() {
        double lastUpdateTimeInNanoseconds = System.nanoTime();
        int frameCounter = 0;
        double frameStartTime = lastUpdateTimeInNanoseconds;

        while (true) {
            try {
                // sleep for about one millisecond. Note, it's *about* 1ms, not exactly 1ms, because
                // Thread.sleep isn't guaranteed to wait the exact amount of time you ask for.
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Thread.sleep can throw this exception, but shouldn't in our program, so we just have
                // this try/catch here to stop the Java compiler complaining
            }

            double currentTimeInNanoseconds = System.nanoTime();
            // 'delta time' or 'dt' the time since the last time the game world was
            // updated and the view was rendered. it's useful for working out how much stuff should move
            // each time game.update is called
            double deltaTimeInNanoseconds = currentTimeInNanoseconds - lastUpdateTimeInNanoseconds;
            // has 1/60th of a second passed?
            if (deltaTimeInNanoseconds > NANOSECONDS_IN_1_60TH_OF_A_SEC) {
                // make sure draw is not happening while the game world is being updated
                this.redrawLock.lock();

                // update the game world
                this.game.time = currentTimeInNanoseconds / NANOSECONDS_IN_1SEC; // nanoseconds to seconds
                this.game.update(deltaTimeInNanoseconds / NANOSECONDS_IN_1SEC);

                // tell the viewport JPanel to paint, which will in turn cause this.draw to be called,
                // which then calls this.game.draw (which draws all the game objects on the screen)
                this.viewport.repaint();

                lastUpdateTimeInNanoseconds = currentTimeInNanoseconds;
                frameCounter++;
                this.redrawLock.unlock();
            }

            if ((currentTimeInNanoseconds - frameStartTime) >= NANOSECONDS_IN_1SEC) {
                this.fps = frameCounter;
                frameStartTime += NANOSECONDS_IN_1SEC;
                frameCounter = 0;
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

            g.setColor(Color.WHITE);
            g.drawString("fps: " + String.valueOf(this.mainApp.fps), 10, 30);
        }
    }

    class KeyboardEventListener implements KeyListener {
        private HashSet<String> keysDown;
        KeyboardEventListener(HashSet<String> keysDown) {
            this.keysDown = keysDown;
        }
        public void keyPressed(KeyEvent e) {
            // we add keypresses to the keysDown set so the game can just check that a key is pressed
            // by doing keysDown.contains(key)
            this.keysDown.add(KeyEvent.getKeyText(e.getKeyCode()));
        }

        public void keyReleased(KeyEvent e) {
            // remove them again once they are no longer pressed
            this.keysDown.remove(KeyEvent.getKeyText(e.getKeyCode()));
        }

        public void keyTyped(KeyEvent e) {
            // not used, but KeyListener requires that we define this
        }
    }
}
