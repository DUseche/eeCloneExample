package edu.bsu.cs639.eeclone.anim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.bsu.cs639.eeclone.Constants;

/**
 * An animation of a spinning circle-within-a-circle. Actually, it's currently a
 * circle with a line rotating inside it since that's easier to test. TODO: redo
 * the docs or rename the class
 * 
 * @author pvg
 */
public class SpinningCircleAnimation implements Animation {

  private static final float DEFAULT_SPEED_RPU = // 20 radians per second
  20 / Constants.FPS;

  /** Dimensions of the circle */
  private final int width, height;

  /** Paints for the inner and outer circle */
  private final Paint inner, outer;

  /** Speed of the animation, in radians per update. */
  private float speed = DEFAULT_SPEED_RPU;

  /**
   * Create a new spinning circle animation.
   * 
   * @param width
   *          width of the circle
   * @param height
   *          height of the circle
   * @param inner
   *          color for the inner circle
   * @param outer
   *          color for the outer circle
   * @param speed
   *          speed of the animation, in radians per second
   */
  public SpinningCircleAnimation(int width, int height, Paint inner,
      Paint outer, float speed) {
    this.width = width;
    this.height = height;
    this.inner = inner;
    this.outer = outer;
    this.speed = speed;
  }

  public void reset() {
    // TODO Auto-generated method stub

  }

  public void start() {
    // TODO Auto-generated method stub

  }

  public void stop() {
    // TODO Auto-generated method stub

  }

  public void update() {
    this.radians = (radians + speed) % Constants.TWO_PI;
  }

  private float radians = 0;

  public  void draw(Graphics2D g, float x, float y) {
    Paint oldPaint = g.getPaint();

    // Draw the outer circle. Easy.
    g.setPaint(outer);
    g.fillOval((int) x, (int) y, width, height);

    // Translate to the middle
    float centerX = x + width / 2f;
    float centerY = y + height / 2f;
    g.translate(centerX, centerY);

    // Draw a line from the center to the edge.
    final float localRadians = radians; // atomic copy to prevent synchro probs
    float endX = width / 2f * (float) Math.cos(localRadians);
    float endY = height / 2f * (float) Math.sin(localRadians);
    g.setPaint(inner);
    g.drawLine(0, 0, (int) endX, (int) endY);

    // System.out.println(endX + ", " + endY);

    // translate back
    g.translate(-centerX, -centerY);

    // Reset original paint
    g.setPaint(oldPaint);
  }

  @SuppressWarnings("serial")
  public static void main(String[] args) {
    final JFrame f = new JFrame("SpinningCircle Test");
    final int FPS = 10;
    final SpinningCircleAnimation anim = new SpinningCircleAnimation(30, 30,
        Color.WHITE, Color.BLACK, (float)Math.PI / 2f / FPS);
    Timer t = new Timer();
    t.schedule(new TimerTask() {
      @Override
      public void run() {
        anim.update();
        f.repaint();
      }
    }, 0, 1000 / FPS);
    f.getContentPane().add(new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        anim.draw((Graphics2D) g, 0f, 0f);
      }
    });
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(400, 400);
    f.setVisible(true);
  }
}
