package edu.bsu.cs639.eeclone;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.bsu.cs639.eeclone.sprites.CollisionTester;
import edu.bsu.cs639.eeclone.sprites.Sprite;
import edu.bsu.cs639.eeclone.sprites.Sprite.Explosion;
import edu.bsu.cs639.eeclone.sprites.Sprite.Noninteractive;
import edu.bsu.cs639.eeclone.sprites.Sprite.Obstacle;
import edu.bsu.cs639.eeclone.sprites.Sprite.Player;
import edu.bsu.cs639.eeclone.sprites.Sprite.Powerup;

/**
 * Handles all of the sprites in the game. This is a singleton.
 * 
 * @author pvg
 */
public class SpriteManager implements Iterable<Sprite> {

  private static final SpriteManager SINGLETON = new SpriteManager();

  public static SpriteManager instance() {
    return SINGLETON;
  }

  private SpriteManager() {
  }

  /**
   * The list of all active sprites
   */
  private final List<Sprite> sprites = new java.util.LinkedList<Sprite>();
  
  /*{
    private final Log log = LogFactory.getLog(getClass());
    @Override
    public boolean add(Sprite s) {
      log.debug("Adding: " + s);
      return super.add(s);
    }
    @Override
    public boolean remove(Object s) {
      log.debug("Removing: " + s);
      return super.remove(s);
    }
    public void add(int i, Sprite s) {
      log.debug("Adding: " + s + " at " + i);
      super.add(i,s);
    }
  };*/
  
  
  private final Sprite.Visitor spriteAdder = new Sprite.Visitor() {
    public Object visit(Explosion e, Object arg) {
      // Add explosions in front
      sprites.add(0,e);
      return null;
    }

    public Object visit(Obstacle o, Object arg) {
      sprites.add(o);
      return null;
    }

    public Object visit(Player p, Object arg) {
      throw new IllegalArgumentException(
          "Player sprites are not handled by this manager");
    }

    public Object visit(Powerup p, Object arg) {
      sprites.add(p);
      return null;
    }
    
    public Object visit(Noninteractive ni, Object arg) {
      sprites.add(ni);
      return null;
    }
  };
  
  private final Sprite.Visitor spriteRemover = new Sprite.Visitor() {
    public Object visit(Explosion e, Object arg) {
      sprites.remove(e);
      return null;
    }

    public Object visit(Obstacle o, Object arg) {
      sprites.remove(o);
      return null;
    }

    public Object visit(Player p, Object arg) {
      throw new IllegalArgumentException(
          "Player sprites are not handled by this manager");
    }

    public Object visit(Powerup p, Object arg) {
      sprites.remove(p);
      return null;
    }
    
    public Object visit(Noninteractive ni, Object arg) {
      sprites.remove(ni);
      return null;
    }
  };

  // private Log log = LogFactory.getLog(this.getClass());
  
  /** Lits of sprites to add on the next update */
  private final List<Sprite> toAdd = new ArrayList<Sprite>();
  
  /**
   * Add a sprite to the sprite manager.
   * 
   * @param s
   */
  public void add(Sprite s) {
    // Queue for addition on next update
    toAdd.add(s);
  }

  /**
   * Update all the sprites
   */
  public synchronized void update() {
    // Add necessary sprites
    while (toAdd.size()>0)
      toAdd.remove(0).accept(spriteAdder,null);
    
    for (int i = 0; i < sprites.size();) {
      // Update each sprite and check if it should be removed.
      Sprite s = sprites.get(i);
      
      // The sprite could have been marked for removal on another thread,
      // so check that before updating its state.
      if (s.isMarkedForRemoval()) {
        remove(s);
        continue;
      }
      
      // Update the sprite
      s.update();

      // Maybe it is now marked for removal, so doublecheck.
      // (If this takes too long, this block can be removed, and the sprite
      // would only be drawn for one frame.)
      if (s.isMarkedForRemoval()) 
        remove(s);
      else
        i++;
    }
    // Check if blocks have hit any explosions
    for (Sprite s : sprites) 
      s.accept(collisionTester,null);
  }

  /**
   * Draw all the sprites
   * 
   * @param g
   *          graphics context
   */
  public synchronized void drawAll(Graphics2D g) {
    for (Sprite s : sprites)
      s.draw(g);
  }

  /**
   * Remove a sprite from the sprite manager
   * 
   * @param s
   *          the sprite to remove
   */
  private void remove(Sprite s) {
    assert sprites.contains(s);
    //if (log.isDebugEnabled())
    //  log.debug("Removing sprite: " + s);
    s.accept(spriteRemover,null);
  }
  
  /**
   * Reset this sprite manager, emptying it of all sprites
   */
  public void reset() {
    sprites.clear();
  }

  public Iterator<Sprite> iterator() {
    return sprites.iterator();
  }
  
  /**
   * The visitor responsible for checking if any obstacles are caught 
   * in an explosion.
   */
  private final Sprite.Visitor collisionTester = new Sprite.Visitor.Abstract() {

    @Override
    public Object visit(Explosion e, Object arg) {
      // Check if this explosion hits any obstacle
      for (Sprite s : sprites) {
        s.accept(testerHelper, e);
      }
      return null;
    }
    
    private final Sprite.Visitor testerHelper = new Sprite.Visitor.Abstract() {
      @Override
      public Object visit(Obstacle o, Object arg) {
        Explosion e = (Explosion)arg;
        if (CollisionTester.collidesBoundingOval(e,o)) {
          o.explode(e);
        }
        return null;
      }
    };
    
  };
}
