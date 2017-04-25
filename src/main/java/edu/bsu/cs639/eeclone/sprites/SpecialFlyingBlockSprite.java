package edu.bsu.cs639.eeclone.sprites;

import java.awt.Color;

import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.SpriteManager;
import edu.bsu.cs639.eeclone.anim.SpinningSquareAnimation;

/**
 * A flying block that drops powerups.
 * 
 * @author pvg
 */
public final class SpecialFlyingBlockSprite extends LinearMotionSprite
    implements Sprite.Obstacle {

  private static final Color COLOR = Color.GREEN.brighter();

  /**
   * The explosion in which this sprite was caught. If it was not caught in an
   * explosion (is not exploded) then this is null
   */
  private Sprite.Explosion explosion;

  /**
   * Keeps track of whether this flying block has ever been shown on the screen
   * or not.
   */
  private boolean wasOnScreen = false;

  public void explode(Explosion e) {
    this.explosion = e;
  }

  public SpecialFlyingBlockSprite(float x, float y, float dx, float dy) {
    super(x, y, Constants.FLYING_BLOCK_SIZE, Constants.FLYING_BLOCK_SIZE,
        new SpinningSquareAnimation(Constants.FLYING_BLOCK_SIZE,
            Constants.FLYING_BLOCK_SPIN_SPEED,
            (float) (Math.random() * Math.PI), COLOR), dx, dy);
  }

  public Object accept(Visitor v, Object arg) {
    return v.visit(this, arg);
  }

  @Override
  public void update() {
    super.update();

    // Check if we need to explode
    if (explosion != null) {
      SpriteManager.instance().add(
          new ExplosionSprite(x + w / 2f, y + h / 2f, explosion.chain()));

      // Compute center points
      Sprite p = explosion;
      float px = p.x() + p.width() / 2f;
      float py = p.y() + p.height() / 2f;
      float cx = x + w / 2f;
      float cy = y + h / 2f;

      // Make sure we're not dividing by zero
      float numerator = cy - py;
      float denominator = cx - px;
      if (denominator == 0f)
        denominator = Constants.NEARLY_ZERO;

      float theta = (float) Math.atan(numerator / denominator);

      // Adjust for the proper quadrant
      // (Let's be honest. I am not quite sure why this works, but it does.)
      if (denominator < 0)
        theta -= (float) Math.PI;

      // compute dx, dy
      float dx = (float) Math.cos(theta) * Constants.POWERUP_SPEED;
      float dy = (float) Math.sin(theta) * Constants.POWERUP_SPEED;

      SpriteManager.instance().add(new PowerupSprite(x, y, dx, dy));
      markForRemoval();
    }

    // Check if this is on the screen.
    // This needs to remove itself if it has flown offscreen.
    if (!onScreen()) {
      // If we've been on the screen and are now off, this sprite can be removed
      if (wasOnScreen) {
        markForRemoval();
      }
    } else {
      // We're on the screen, so make sure it's recorded as true.
      wasOnScreen = true;
    }
  }

}
