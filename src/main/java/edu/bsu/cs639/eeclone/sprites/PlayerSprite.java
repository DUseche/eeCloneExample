package edu.bsu.cs639.eeclone.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

import edu.bsu.cs639.eeclone.Chain;
import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.Game;
import edu.bsu.cs639.eeclone.SpriteManager;
import edu.bsu.cs639.eeclone.anim.Animation;
import edu.bsu.cs639.eeclone.anim.SpinningCircleAnimation;
import edu.bsu.cs639.util.FloatDimension;

/**
 * The player's sprite
 * 
 * @author pvg
 */
public final class PlayerSprite implements Sprite.Player {
  
  private static final float ANGLED = (float) Math.sin(Math.PI * 0.25);

  /** The directions in which the player can move */
  public enum MovementDirection {
    UP(0, -1), UP_LEFT(-ANGLED, -ANGLED), LEFT(-1, 0), DOWN_LEFT(-ANGLED,
        ANGLED), DOWN(0, 1), DOWN_RIGHT(ANGLED, ANGLED), RIGHT(1, 0), UP_RIGHT(
        ANGLED, -ANGLED), STOP(0, 0);

    private final float xFactor;

    private final float yFactor;

    private MovementDirection(float xFactor, float yFactor) {
      this.xFactor = xFactor;
      this.yFactor = yFactor;
    }

    /**
     * Get the vector produced by multiplying the given scalar by this vector.
     * 
     * @param scalar
     * @return product
     */
    public Point2D multiply(float scalar) {
      return new Point2D.Float(scalar * xFactor, scalar * yFactor);
    }

    /**
     * Get the product of the given scalar and this vector's x component.
     * 
     * @param scalar
     * @return product
     */
    public float multiplyX(float scalar) {
      return scalar * xFactor;
    }

    /**
     * Get the product of the given scalar and this vector's y component
     * 
     * @param scalar
     * @return product
     */
    public float multiplyY(float scalar) {
      return scalar * yFactor;
    }
  }

  private static final int NORMAL_DIAMETER = 20;

  private static final Point STARTING_LOCATION 
  = new Point(Constants.BOARD_WIDTH / 2 - NORMAL_DIAMETER / 2,
      Constants.BOARD_HEIGHT /2  -NORMAL_DIAMETER / 2);

  /** The initial speed for a player sprite */
  private static final float DEFAULT_SPEED_PPU = // 80fps 
    80f / Constants.FPS;

  /**
   * The interface for states of this sprite.
   * 
   * @author pvg
   */
  private interface State {
    public void install();
    public void uninstall();
    public void draw(Graphics2D g);
    public void update();
    public void explode();
  }

  /**
   * The state of the sprite when it is moving around the screen, not exploded.
   * 
   * @author pvg
   */
  private final State MOVING_STATE = new State() {
    private final Animation circle = new SpinningCircleAnimation(20, 20,
        Color.BLUE, Color.WHITE, 0.5f * (float)Math.PI /Constants.FPS);

    public void install() {
    }

    public void uninstall() {
    }

    public void draw(Graphics2D g) {
      circle.draw(g, x, y);
    }
    
    public void explode() {
      deductLife();
      setState(EXPLODING_STATE);
    }

    public synchronized void update() {
      // Update the animation
      circle.update();

      // Update the location if we must
      updateLocation();
      
      // Process collisions for the player sprite
      synchronized (SpriteManager.instance()) {
      for (Sprite s : SpriteManager.instance()) {
        s.accept(playerCollisionProcessor, PlayerSprite.this);
      }
      }
    }
    
    private final Sprite.Visitor playerCollisionProcessor = new Sprite.Visitor.Abstract() {
      @Override
      public Object visit(Obstacle o, Object arg) {
        if (CollisionTester.collidesBoundingOval((PlayerSprite)arg,o)) {
          deductLife();
          setState(DEATH_STATE);
        }
        return null;
      }
      @Override
      public Object visit(Powerup p, Object arg) {
        if (CollisionTester.collidesBoundingOval((PlayerSprite)arg, p)) {
          p.collect();
        }
        return null;
      }
    };
    
  };
  
  private final State EXPLODING_STATE = new State() {

    private ExplosionSprite explosion;
    
    private static final int TICKS_BEFORE_RESPAWN = Constants.FPS / 2;
    
    private int ticks;
    
    public void draw(Graphics2D g) {
      // The explosion is automatically drawn by the sprite manager.
    }

    public void install() {
      explosion = new ExplosionSprite(x+diameter/2f,y+diameter/2f,new Chain());
      SpriteManager.instance().add(explosion);
    }

    public void uninstall() {
      explosion = null;
      ticks = 0;
    }
    
    public void explode() { //ignored
    }

    public void update() {
      // It's possible that the installation isnt done yet.
      // Check for that here
      if (explosion==null) return;
      
      assert state == this;
      assert explosion!=null; // If we are in this state (it was installed),
                              // then we have an  explosion
      
      // Note that the explosion sprite itself is managed by the sprite
      // manager, so we should not call its update function here.
      if (explosion.isDone()) {
        ticks ++;
        if (ticks >= TICKS_BEFORE_RESPAWN) {
          if (lives>0) setState(SPAWNING_STATE);
          else Game.instance().gameOver();
        }
      }
    }
  };
  
  private final State DEATH_STATE = new State() {

    private static final int MIN_TICKS_BEFORE_RESPAWN = (int)(Constants.FPS * 1.5);
    
    /** The number of ticks since the death animation stopped */ 
    private int ticks = 0;
    
    public void draw(Graphics2D g) {
      // TODO: replace with death animation
      g.setColor(Color.BLACK);
      g.fillOval((int)x,(int)y,(int)diameter,(int)diameter);
    }

    public void install() {
      // Reset ticks
      ticks = 0;
    }

    public void uninstall() {
    }
    
    public void explode() { //ignored
    }

    public void update() {
      // TODO: update a death animation
      
      // Respawn after time elapses
      ticks++;
      if (ticks > MIN_TICKS_BEFORE_RESPAWN) {
        if (lives > 0)
          setState(SPAWNING_STATE);
        else 
          Game.instance().gameOver();
      }
    }
    
  };
  
  private final State SPAWNING_STATE = new State() {
    // The number of ticks of the spawn animation
    private static final int SPAWN_DURATION = Constants.FPS;
    
    private int ticks = 0;
    
    public void draw(Graphics2D g) {
      // TODO: replace with spawning animation
      g.setColor(Color.CYAN);
      g.fillOval((int)x,(int)y,(int)diameter,(int)diameter);
    }

    public void install() {
      x = STARTING_LOCATION.x;
      y = STARTING_LOCATION.y;
    }

    public void uninstall() {
      ticks = 0;
    }
    
    public void explode() { //ignored
    }

    public void update() {
      
      // Allow movement while spawning
      updateLocation();
      
      ticks++;
      if (ticks > SPAWN_DURATION)
        setState(MOVING_STATE);
    }
  };
  
  /** The current state of the sprite */
  private State state;

  private float x = STARTING_LOCATION.x, y = STARTING_LOCATION.y,
      diameter = NORMAL_DIAMETER;
  
  /** 
   * The number of lives the player has.
   * @see #deductLife() 
   */
  private int lives = Constants.STARTING_LIVES;

  /** This sprite's current speed, in pixels per update */
  private float speedPPU = DEFAULT_SPEED_PPU;

  /** The direction in which the player is moving. */
  private MovementDirection direction = MovementDirection.STOP;

  private boolean markedForRemoval = false;

  public PlayerSprite() {
    // Initialize to the spawning state
    setState(SPAWNING_STATE);
  }

  public Object accept(Sprite.Visitor v, Object arg) {
    return v.visit(this,arg);
  }
  
  /**
   * Change the speed of the player's sprite.
   * 
   * @param speedPPU
   *          the new speed, in pixels per update
   */
  public void setSpeed(float speedPPU) {
    this.speedPPU = speedPPU;
  }

  /**
   * Update this sprite's state.
   * 
   * @param state
   */
  private void setState(State state) {
    if (this.state != null)
      this.state.uninstall();
    this.state = state;
    this.state.install();
  }
  
  /**
   * ISsue the explode command.
   * (Ignored in many states.)
   */
  public void explode() {
    state.explode();
  }

  public void draw(Graphics2D g) {
    // Draw according to the current state
    state.draw(g);
  }

  public void update() {
    // Update according to the current state
    state.update();
  }

  public float height() {
    return diameter;
  }

  public final Point2D location() {
    return new Point2D.Float(x(), y());
  }

  public final Dimension2D size() {
    return new FloatDimension(width(), height());
  }

  public float width() {
    return diameter;
  }

  public float x() {
    return x;
  }

  public float y() {
    return y;
  }

  /**
   * Get the number of lives remaining for this player
   * @return lives remaining
   */
  public int livesRemaining() { return lives; }
  
  /**
   * Tell the player to move in the designated direction.
   * 
   * @param d
   */
  public void move(MovementDirection d) {
    this.direction = d;
  }
  
  /**
   * Update the location based on player's input
   */
  private void updateLocation() {
    MovementDirection d = direction; // atomic copy to prevent synchro probs.
    if (d != MovementDirection.STOP) {
      // Update location based on the direction being moved
      x += d.multiplyX(speedPPU);
      y += d.multiplyY(speedPPU);

      // Make sure we are still on the board
      x = Math.max(x, 0);
      y = Math.max(y, 0);
      x = Math.min(Constants.BOARD_WIDTH - diameter, x);
      y = Math.min(Constants.BOARD_HEIGHT - diameter, y);
    }
  }

  public void markForRemoval() {
    markedForRemoval = true;
  }

  public boolean isMarkedForRemoval() { return markedForRemoval; }
  
  /** 
   * Deduct a life from the player.
   * This should be the only playce lives are deducted
   */
  private final void deductLife() {
    lives--;
    Game.instance().score().playerLostLife();//TODO: should be observer
  }
}
