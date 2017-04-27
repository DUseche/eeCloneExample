/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.bsu.cs639.eeclone;

import edu.bsu.cs639.eeclone.sprites.Sprite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author david
 */
public interface Score {

 /**
   * Adjust the score for having collected a powerup
   * 
   * @param powerup
   * @return the number of points added
   */
  int add(Sprite.Powerup powerup);

  /**
   * Add points for adding one more to the given chain.
   * 
   * @param chain
   */
  public void add(Chain chain);

  /**
   * Notify the score that the player has lost a life
   */
  public void playerLostLife();

  public void draw(Graphics2D g);
  
  /**
   * Get the max chain recorded by this score.
   * @return max chain
   */
  public int getMaxChain();
  
  /**
   * Get the integer form of this score 
   * @return score as integer
   */
  public int toInt();   
}
