package edu.bsu.cs639.eeclone;

import java.awt.Font;

/**
 * Miscellaneous constants for this game.
 * 
 * @author pvg
 */
public interface Constants {

  /**
   * The ideal number of frames per second.
   */
  public static final int FPS = 60;
  
  /**
   * The width of the game area
   */
  public static final int BOARD_WIDTH = 640;
  
  /**
   * The height of the game area
   */
  public static final int BOARD_HEIGHT = 480;
  
  /**
   * The size (diameter or leg) of a "flying block". 
   */
  public static final int FLYING_BLOCK_SIZE = 20;
  
  /**
   * The rotational speed, in radians per update, of a spinning block.
   */
  public static final float FLYING_BLOCK_SPIN_SPEED = 
    (float)(0.25 * Math.PI / FPS);
  
  /**
   * Nearly zero
   */
  public static final float NEARLY_ZERO = 0.001f;
  
  /**
   * The number of lives a player has to start
   */
  public static final int STARTING_LIVES = 6;
  
  public static final float TWO_PI = (float)(2.0 * Math.PI);
  
  public static final String RESOURCE_INDEX = "resources/resources.index";
  
  /** The speed at which powerups move across the screen */
  public static final float POWERUP_SPEED =
    40f / FPS;
  
  /** The font used by powerup text sprites */
  public static final Font POWERUP_TEXT_FONT = Font.decode("fixed-PLAIN-12");
  // TODO: replace with loaded resource
  
  public static final Font SCORE_FONT = Font.decode("fixed-BOLD-32");
  
  /** RIGHT side of hte score string */
  public static final int SCORE_RIGHT = BOARD_WIDTH-10;
  /** top side of the score string */
  public static final int SCORE_TOP = 10;
}
