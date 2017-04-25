package edu.bsu.cs639.eeclone.anim;

import java.awt.Graphics2D;

/**
 * A sequence of images that change with update ticks.
 * 
 * @author pvg
 */
public interface Animation {

  /**
   * Update this animation.
   */
  public void update();
  
  /**
   * Start this animation.
   * If the animation was previously stopped, this will continue from where
   * it was stopped, unless it was reset.
   * @see #stop()
   * @see #reset()
   */
  public void start();
  
  /**
   * Halt this animation.
   */
  public void stop();
  
  /**
   * Go back to the beginning of the animation.
   */
  public void reset();
  
  /**
   * Draw the current frame of the animation on the given graphics context.
   * @param g graphics context
   * @param x 
   * @param y
   */
  public void draw(Graphics2D g, float x, float y);
}
