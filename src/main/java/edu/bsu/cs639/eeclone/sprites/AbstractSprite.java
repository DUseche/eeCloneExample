package edu.bsu.cs639.eeclone.sprites;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

import edu.bsu.cs639.util.FloatDimension;

/**
 * Abstract superclass for sprites that share implementation of common methods.
 * 
 * @author pvg
 */
public abstract class AbstractSprite implements Sprite {

  protected float x, y, w, h;

  private boolean markedForRemoval = false;

  public float height() {
    return h;
  }

  public Point2D location() {
    return new Point2D.Float(x, y);
  }

  public Dimension2D size() {
    return new FloatDimension(w, h);
  }

  public float width() {
    return w;
  }

  public float x() {
    return x;
  }

  public float y() {
    return y;
  }

  public void markForRemoval() {
    markedForRemoval = true;
  }
  
  public boolean isMarkedForRemoval() { return markedForRemoval; }

}
