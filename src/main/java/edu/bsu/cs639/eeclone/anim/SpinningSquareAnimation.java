package edu.bsu.cs639.eeclone.anim;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

import edu.bsu.cs639.eeclone.Constants;

/**
 * An animation of a rotating square.
 * 
 * @author pvg
 */
public class SpinningSquareAnimation implements Animation {

  /** The current rotation of the square in radians */
  private float rotation; 
  
  /** The current rotation speed of the square, in radians per update */
  private float rotSpeed;
  
  /** The size of the square. */
  private float size;
  
  /** Keeps track of half the size so that it does not have to be 
   * recomputed often
   */
  private float halfSize;
  
  private Paint paint;
  
  /**
   * Create an animation
   * @param size the size of a leg of the square
   * @param speed the speed of rotation in radians per update
   * @param rotation the initial rotation in radians
   * @param paint
   */
  public SpinningSquareAnimation(float size, float speed, float rotation, Paint paint) {
    this.size=size;
    this.halfSize = size/2f;
    this.rotSpeed = speed;
    this.rotation=rotation;
    this.paint=paint;
  }
  
  public void draw(Graphics2D g, float x, float y) {
    Paint oldPaint = g.getPaint();
    AffineTransform oldXForm = g.getTransform();
    
    // Set the paint
    g.setPaint(paint);
    
    // translate to the square's logical center.
    g.translate(x + halfSize, y+halfSize);
    
    // rotate
    g.rotate(rotation);
    
    // draw the square
    g.fillRect((int)(-halfSize), (int)(-halfSize), (int)size, (int)size);
    
    // with a black outline
    g.setPaint(java.awt.Color.BLACK);
    g.drawRect((int)(-halfSize), (int)(-halfSize), (int)size, (int)size);
    
    // reset old state
    g.setTransform(oldXForm);
    g.setPaint(oldPaint);
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
    rotation = (rotation + rotSpeed) % Constants.TWO_PI;
  }

  
}
