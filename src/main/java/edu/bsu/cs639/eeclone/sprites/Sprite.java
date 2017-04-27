package edu.bsu.cs639.eeclone.sprites;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

import edu.bsu.cs639.eeclone.Chain;

public interface Sprite {
  
  /**
   * An interface for obstacles.  If the player hits an obstacle, he is killed.
   * 
   * @author pvg
   */
  public interface Obstacle extends Sprite {
    
    /**
     * Notify this object that it has been caught in the provided explosion.
     * @param e explosion
     */
    public void explode(Explosion e);
  }
  
  /**
   * An interface for powerups.  When the player hits them, something good 
   * happens.
   * @author pvg
   */
  public interface Powerup extends Sprite {
    /**
     * Called when this powerup is collected by the player.
     */
    public void collect();
  }
  
  /**
   * An interface for exposions. Explosions blow things up.
   * @author pvg
   */
  public interface Explosion extends Sprite{
    /**
     * Get the chain of which this explosion is a part.
     * @return chain
     */
    public Chain chain();
  }
  
  /**
   * The player's sprite
   * @author pvg
   */
  public interface Player extends Sprite {
      public void explode();
      public void move(PlayerSprite.MovementDirection d);
      public int livesRemaining();
  }
  
  /**
   * A noninteractive sprite, such as HUD elements.
   * @author pvg
   */
  public interface Noninteractive extends Sprite {}
  
  /**
   * A visitor interface for sprites
   * @author pvg
   */
  public interface Visitor {
    public Object visit(Obstacle o, Object arg);
    public Object visit(Powerup p, Object arg);
    public Object visit(Explosion e, Object arg);
    public Object visit(Player p, Object arg);
    public Object visit(Noninteractive ni, Object arg);
    
    /**
     * Provides default, empty implementations of all visitor methods.
     * 
     * @author pvg
     */
    public abstract class Abstract implements Visitor {
      public Object visit(Obstacle o, Object arg) {return null;}
      public Object visit(Powerup p, Object arg) {return null;}
      public Object visit(Explosion e, Object arg) {return null;}
      public Object visit(Player p, Object arg) {return null;}
      public Object visit(Noninteractive ni, Object arg) {return null;}
    }
  }
  
  /**
   * Accept a visitor
   * @param v
   * @param arg
   * @return visitation argument
   */
  public Object accept(Visitor v, Object arg);

  /**
   * Draw this sprite on the given graphics context.
   * @param g
   */
  public void draw(Graphics2D g);
  
  /**
   * Update this sprite.
   * If the sprite has a motion vector, it will be moved along that vector.
   */
  public void update();
  
  /**
   * Get the current location of the sprite
   * @return location
   */
  public Point2D location();
  
  /**
   * Get the x-coordinate of this sprite
   * @return x-coordinate
   */
  public float x();
  
  /**
   * Get the y-coordinate of this sprite
   * @return y-coordinate
   */
  public float y();
  
  /**
   * Get the current size of this sprite.
   * Note that some sprites can change size (e.g. explosions).
   * @return current size
   */
  public Dimension2D size();
  
  /**
   * Get the width of this sprite
   * @return width
   */
  public float width();
  
  /**
   * Get the height of this sprite
   * @return height
   */
  public float height();
  
  /**
   * Mark this sprite for removal during the next update cycle.
   */
  public void markForRemoval();
  
  /**
   * Check if this sprite was marked for removal.
   * @see #markForRemoval()
   * @return true if marked for removal, false otherwise
   */
  public boolean isMarkedForRemoval();
}
