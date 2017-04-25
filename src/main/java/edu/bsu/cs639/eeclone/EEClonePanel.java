package edu.bsu.cs639.eeclone;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The swing panel that contains the EEClone game.
 * <p>
 * This class uses some concepts and code from 
 * Andrew Davison's <ul>Killer Game Programming in Java</ul>.
 * 
 * @see <a href="http://fivedots.coe.psu.ac.th/~ad/jg">
 *   Killer Game Programming in Java</a>
 * 
 * @author pvg
 */
public class EEClonePanel extends JPanel implements Runnable {

  /**
   * generated serial version uid
   */
  private static final long serialVersionUID = 4073389367777748837L;

  /**
   * The most frames that can be skipped. That is, the number of game updates
   * that can take place without rendering a frame.
   */
  private static final int MAX_FRAME_SKIPS = 5;

  /**
   * The number of consecutive updates that we allow without sleeping before the
   * thread yields.
   */
  private static final int MAX_FRAMES_WITHOUT_YIELDING = 16;

  /**
   * Indicates if the game is and should continue running. This should be set to
   * false when the user chooses to quit.
   */
  private volatile boolean running = true;

  private final Game game;

  /** The optimal period for frame refreshes, in nanoseconds. */
  private final long period;

  /**
   * Create an EEClone panel.
   * 
   * @param period
   *          the desired amount of time (ns) between screen refreshes
   */
  public EEClonePanel(long period) {
    super();

    assert period > 0;
    this.period = period;

    // Turn off Swing's default double-buffering, since we'll be handling
    // it ourselves.
    setDoubleBuffered(false);

    // Use non-default background so we know this is working.
    setBackground(Color.WHITE);

    // This panel can and should have input focus
    setFocusable(true);
    requestFocus();

    // Set the size of the panel
    setPreferredSize(new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT));

    // Create the game
    game = new Game(this);
  }

  /**
   * Repeat the update-render-sleep cycle to achieve desired FPS. This method
   * handles sleep inaccuracies.
   */
  public void run() {
    // Times in nanoseconds
    long beforeTime, afterTime, timeDiff = 0, timeToSleep;
    long overSleepTime = 0;
    int framesWithoutSleeping = 0;
    long excess = 0;

    // Make sure we have a back buffer.
    // (It can only be created once this panel has been validated, so we
    // cannot do this in the contructor.)
    if (backBuffer == null)
      backBuffer = createImage(getWidth(), getHeight());

    beforeTime = System.nanoTime();

    while (running) {
      game.update();
      game.render(backBuffer);
      paintScreen();

      afterTime = System.nanoTime(); // Time after update&render
      timeDiff = afterTime - beforeTime; // Actual elapsed time
      timeToSleep = period - timeDiff - overSleepTime;// Time available for
                                                      // sleep

      // If there is time left in this cycle, sleep for a bit.
      if (timeToSleep > 0) {
        try {
          Thread.sleep(timeToSleep / 1000000L); // ns -> ms
        } catch (InterruptedException ie) {
          // We don't expect to be interrupted, but if we do, there's nothing
          // special to be done, just carry on with the game.
        }

        // Determine how long we slept in (due to overhead of sleep() above)
        overSleepTime = System.nanoTime() - afterTime - timeToSleep;
      }
      // Otherwise, update/render/draw took more time than the period.
      else {
        excess -= timeToSleep; // Store excess time value
        overSleepTime = 0;
        framesWithoutSleeping++;

        // If we have been continuously drawing without sleeping, make sure
        // to yield from time to time.
        if (framesWithoutSleeping >= MAX_FRAMES_WITHOUT_YIELDING) {
          Thread.yield();
          framesWithoutSleeping = 0;
        }
      }

      beforeTime = System.nanoTime();

      // If rendering is taking too long, add extra updates without rendering.
      for (int skips = 0; (excess > period) && (skips < MAX_FRAME_SKIPS); skips++) {
        excess -= period;
        game.update();
      }
    }

    assert !running;
  }

  /**
   * The back buffer on which the game state is rendered.
   */
  private Image backBuffer;

  /**
   * Paint the back drawing surface to the screen. This method uses &quot;active
   * rendering&quot;.
   */
  private void paintScreen() {
    Graphics g = null;
    try {
      g = this.getGraphics();
      if ((g != null) && (backBuffer != null))
        g.drawImage(backBuffer, 0, 0, null);

      // Ensure that the OS' graphics buffer is up to date.
      Toolkit.getDefaultToolkit().sync();
    } catch (Exception e) {
      System.err.println("Graphics context error.");
      e.printStackTrace();
    } finally {
      // We're done with this graphics context, so dispose of it to take some
      // load off of the garbage collector later.
      if (g != null)
        g.dispose();
    }
  }

  /**
   * Paint this panel. This will draw the back buffer to the given graphics
   * context.
   * 
   * @param g
   *          the graphics context on which to draw
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (backBuffer != null)
      g.drawImage(backBuffer, 0, 0, null);
  }

  /**
   * Test this panel.
   * 
   * @param args
   *          command-line arguments (ignored)
   */
  public static void main(String[] args) {
    //BasicConfigurator.configure();
    long period = 1000000000L / Constants.FPS; // 1000ms == 1s, convert to ns

    // Set up the enclosing frame
    JFrame f = new JFrame("Test");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Set up the game panel
    EEClonePanel panel = new EEClonePanel(period);
    panel.setPreferredSize(new java.awt.Dimension(Constants.BOARD_WIDTH,
        Constants.BOARD_HEIGHT));
    f.getContentPane().add(panel);
    f.pack();
    f.setResizable(false);
    Thread gameRunner = new Thread(panel);
    gameRunner.start();

    // Show the frame
    f.setVisible(true);
  }

}
