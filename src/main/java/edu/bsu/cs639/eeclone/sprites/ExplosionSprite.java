package edu.bsu.cs639.eeclone.sprites;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

import edu.bsu.cs639.eeclone.Chain;
import edu.bsu.cs639.eeclone.Constants;
import edu.bsu.cs639.eeclone.ResourceLoader;
import edu.bsu.cs639.eeclone.audio.Sound;
import edu.bsu.cs639.eeclone.audio.SoundManagerFactory;
import edu.bsu.cs639.util.FloatDimension;

/**
 * An explosion sprite
 * 
 * @author pvg
 */
public class ExplosionSprite implements Sprite.Explosion {

  private static final float MAX_DIAMETER = 125f;
  private static final float MIN_DIAMETER = 0.0001f;
  private static final float INITIAL_DIAMETER = Constants.FLYING_BLOCK_SIZE;
  private static final float GROWTH_RATE = 250f / Constants.FPS;
  
  /** The current diameter of this sprite */
  private float diameter = INITIAL_DIAMETER;
  
  /** The center of this sprite */
  private final float x, y;
  
  /** Indicates if we are currently getting bigger or smaller */
  private boolean increasing = true;
  
  private boolean markedForRemoval = false;
  
  /** The chain of which this explosion is a part */
  private final Chain chain;
  
  private static final Sound EXPLOSION_SOUND = ResourceLoader.instance().getSound("explosion");
  
  /**
   * @param x center of the explosion
   * @param y center of the explosion
   * @param chain the chain of which this explosion is a part
   */
  public ExplosionSprite(float x, float y, Chain chain) {
    assert chain!=null;
    
    this.x=x;
    this.y=y;
    this.chain = chain;
    chain.add(this);
    //SoundPlayer.instance().play(ResourceLoader.instance().getSound("explosion"));
    SoundManagerFactory.instance().get(EXPLOSION_SOUND.format()).play(EXPLOSION_SOUND);
  }
  
  public Object accept(Visitor v, Object arg) {
    return v.visit(this,arg);
  }
  
  private static final Paint FILL_PAINT = Color.YELLOW;
  private static final Paint OUTLINE_PAINT = Color.ORANGE;
  private static final Stroke OUTER_STROKE = new BasicStroke(3f);

  public void draw(Graphics2D g) {
    float radius = diameter / 2f;
    
    Paint oldPaint = g.getPaint();
    Stroke oldStroke = g.getStroke();
    
    g.setPaint(FILL_PAINT);
    g.fillOval((int)(x-radius), (int)(y-radius), (int)diameter, (int)diameter);
    
    g.setPaint(OUTLINE_PAINT);
    g.setStroke(OUTER_STROKE);
    g.drawOval((int)(x-radius), (int)(y-radius), (int)diameter, (int)diameter);
    
    // Reset the state
    g.setPaint(oldPaint);
    g.setStroke(oldStroke);
  }

  public float height() {
    return diameter;
  }

  public Point2D location() {
    return new Point2D.Float(x,y);
  }

  public Dimension2D size() {
    return new FloatDimension(diameter,diameter);
  }

  public void update() {
    if (increasing) {
      diameter += GROWTH_RATE;
      if (diameter >= MAX_DIAMETER) increasing = false;
    }
    else {
      diameter -= GROWTH_RATE;
      // If we shrunk too small, remove from the sprite manager.
      if (diameter <= MIN_DIAMETER) markForRemoval();
    }
  }

  public float width() {
    return diameter;
  }

  public float x() {
    return x - width() / 2;
  }

  public float y() {
    return y - height() / 2;
  }
  
  /**
   * Check if this explosion is done exploding.
   * @return true if done
   */
  public boolean isDone() { return diameter <= MIN_DIAMETER; }

  public void markForRemoval() {
    markedForRemoval = true;
  }

  public boolean isMarkedForRemoval() { return markedForRemoval; }
  
  /**
   * Get the chain of which this is a part
   * @return chain
   */
  public Chain chain() { return chain; }
}

