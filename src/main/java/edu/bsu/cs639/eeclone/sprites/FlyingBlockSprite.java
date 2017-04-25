package edu.bsu.cs639.eeclone.sprites;

import java.awt.Color;

import edu.bsu.cs639.eeclone.Chain;
import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.SpriteManager;
import edu.bsu.cs639.eeclone.anim.SpinningSquareAnimation;

/**
 * A flying block sprite.  This is an obstacle that destroys the player if
 * he hits it, and it blows up if caught in an explosion.
 * 
 * @author pvg
 */
public final class FlyingBlockSprite extends LinearMotionSprite 
implements Sprite.Obstacle {

  /** 
   * Indicates if this block was exploded, meaning that it should be
   * replaced with an explosion sprite.
   */
  private boolean exploded = false;
  
  /** 
   * Keeps track of whether this flying block has ever been shown on the 
   * screen or not.
   */
  private boolean wasOnScreen = false;
  
  /** Record the chain so that a proper explosion can be created */
  private Chain chain;
  
  public void explode(Explosion e) {
    exploded = true;  // It will explode on the next update
    this.chain = e.chain();
  }

  public FlyingBlockSprite(float x, float y, float dx, float dy) {
    super(x,y,Constants.FLYING_BLOCK_SIZE,Constants.FLYING_BLOCK_SIZE, 
        new SpinningSquareAnimation(Constants.FLYING_BLOCK_SIZE,Constants.FLYING_BLOCK_SPIN_SPEED,
            (float)(Math.random() * Math.PI),
            Color.LIGHT_GRAY),
              dx, dy);
  }
  
  public Object accept(Visitor v, Object arg) {
    return v.visit(this,arg);
  }

  @Override
  public void update() {
    super.update();    
    
    // Check if we need to explode
    if (exploded) {
      SpriteManager.instance().add(new ExplosionSprite(x+w/2f,y+h/2f,chain));
      markForRemoval();
    }

    // Check if this is on the screen.
    // This needs to remove itself if it has flown offscreen.
    if (!onScreen()) {
      // If we've been on the screen and are now off, this sprite can be removed
      if (wasOnScreen) {
        markForRemoval();
      }
    }
    else {
      // We're on the screen, so make sure it's recorded as true.
      wasOnScreen = true;
    }
  }
}
