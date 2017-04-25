package edu.bsu.cs639.util;

import java.awt.geom.Dimension2D;

/**
 * Implementation of {@link java.awt.geom.Dimension2D} using doubles.
 * 
 * @author pvg
 */
public class FloatDimension extends Dimension2D {

  private float w, h;
  
  /**
   * Create a new dimension.
   * @param w width
   * @param h height
   */
  public FloatDimension(float w, float h){
    setSize(w,h);
  }
  
  @Override
  public double getHeight() {
    return h;
  }

  @Override
  public double getWidth() {
    return w;
  }

  @Override
  public void setSize(double width, double height) {
    this.w=(float)width;
    this.h=(float)height;
  }

}
