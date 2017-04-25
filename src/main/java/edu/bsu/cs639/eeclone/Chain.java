package edu.bsu.cs639.eeclone;

import edu.bsu.cs639.eeclone.sprites.Sprite;

/**
 * A chain of objects exploding.
 * 
 * @author pvg
 */
public class Chain {
  
  /** The number of objects blown up in this chain.
   * Start at neg 1 since the player's explosion will be explosion 1, which
   * is worth nothing.  */
  private int count = -1;

  /**
   * Add an obstacle to the chain.
   * @param e the thing exploded in this chain
   */
  public void add(Sprite.Explosion e) {
    count++;
    Game.instance().score().add(this);
  }
  
  /** 
   * Get the size of this chain
   * @return chain size
   */
  public int size() {
    return count;
  }
}
