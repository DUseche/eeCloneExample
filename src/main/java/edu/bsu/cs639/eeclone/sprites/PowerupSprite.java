package edu.bsu.cs639.eeclone.sprites;

import java.awt.Graphics2D;

import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.Game;
import edu.bsu.cs639.eeclone.SpriteManager;

/**
 * A general powerup sprite.
 * 
 * @author pvg
 */
public final class PowerupSprite extends LinearMotionSprite implements Sprite.Powerup {
  
  public PowerupSprite(float x, float y, float dx, float dy) {
    super(x,y,Constants.FLYING_BLOCK_SIZE,Constants.FLYING_BLOCK_SIZE,
        null, dx, dy);
  }
  
  public void collect() {
    int points = Game.instance().score().add(this);
    SpriteManager.instance().add(new TextSprite(""+points, x + w/2, y+h/2));
    
    // remove myself
    markForRemoval();
  }

  public Object accept(Visitor v, Object arg) {
    return v.visit(this,arg);
  }

  @Override
  public void draw(Graphics2D g) {
    g.setColor(java.awt.Color.GREEN);
    g.fillOval((int)x, (int)y, (int)w, (int)h);
  }
}