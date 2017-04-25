package edu.bsu.cs639.eeclone.sprites;

import java.awt.Graphics2D;

import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.anim.Animation;

/**
 * A sprite that moves with constant velocity along a linear path. 
 * 
 * @author pvg
 */
public abstract class LinearMotionSprite extends AbstractSprite implements Sprite {

  private final Animation animation;
  protected final float dx, dy;

  /**
   * 
   * @param x initial x 
   * @param y initial y
   * @param w width
   * @param h height
   * @param a animation (may be null if the sprite is manually rendered)
   * @param dx delta-x (horizontal speed in pixels-per-update)
   * @param dy delta-y (vertical speed in pixels-per-update)
   */
  public LinearMotionSprite(float x, float y, float w, float h, Animation a,
      float dx, float dy) {
    this.x=x;
    this.y=y;
    this.w=w;
    this.h=h;
    this.animation=a;
    this.dx=dx;
    this.dy=dy;
  }
  
  public void draw(Graphics2D g) {
    if (animation!=null) animation.draw(g, x, y);
  }

  public void update() {
    x += dx;
    y += dy;
    if (animation!=null) animation.update();
  }
  
  /**
   * Test if this sprite is visible
   * 
   * @return true if this is on the screen
   */
  protected boolean onScreen() {
    return x > 0 
    && y > 0
    && x + w < Constants.BOARD_WIDTH
    && y + h < Constants.BOARD_HEIGHT;
  }
  
}
