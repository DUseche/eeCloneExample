package edu.bsu.cs639.eeclone;

import edu.bsu.cs639.eeclone.sprites.FlyingBlockSprite;
import edu.bsu.cs639.eeclone.sprites.SpecialFlyingBlockSprite;

/**
 * The class responsible for populating the game with obstacles.
 * 
 * @author pvg
 */
public class ObstacleGenerator {

  /** The radius of the circle that envelops the game board. */
  private static final float BOARD_RADIUS = 
    Math.max(Constants.BOARD_HEIGHT, Constants.BOARD_WIDTH) / 2f
    + Constants.FLYING_BLOCK_SIZE;
  
  private static final float CENTER_X = Constants.BOARD_WIDTH / 2f;
  private static final float CENTER_Y = Constants.BOARD_HEIGHT / 2f;
  
  // experimentally derived
  private static final float RADIAL_FLOCK_OFFSET = (float)Math.PI / 40f; 
  private static final float DISTANCE_FLOCK_OFFSET = 
    Constants.FLYING_BLOCK_SIZE * 0.8f;
  
  /** Probability of generating an obstacle each tick */
  private float probabilityPerTick = 1f / Constants.FPS;
  
  /**
   * Called at each tick to update the generator.
   * This may result in obstacles being added to the game.
   */
  public void update() {

    // TODO: make this vary as the game progresses.
    if (Math.random() < probabilityPerTick)
      generateFlyingFlock((int)(Math.random() * 3)*2+3);
    
  }
  
  private static final float WIGGLE = (float)(0.25 * Math.PI);
  private static final float HALF_WIGGLE = WIGGLE / 2f;
  
  /**
   * Generates a flock of flying blocks.
   * @param blocks the number of blocks, which must be odd
   */
  private void generateFlyingFlock(int blocks) {
    assert blocks % 2 == 1;
    
    // The starting point of the obstacle can be described in radians 
    float startingPosRadians = (float)(Math.random() * Constants.TWO_PI);
    
    // float speed = 100 / Constants.FPS; // TODO: randomize
    // TODO: make the speed get faster as the game progresses
    float speed = ((float)Math.random() * 40f + 80f) / Constants.FPS; //80-120 ppu
  
    
    // To determine the motion vector for the thing, it should shoot towards
    // the center, with a little wiggle to keep life interesting.
    float xWiggle = (float)Math.random() * WIGGLE - HALF_WIGGLE;
    float yWiggle = (float)Math.random() * WIGGLE - HALF_WIGGLE;
    float dx = (float)Math.cos(startingPosRadians - Math.PI + xWiggle) * speed;
    float dy = (float)Math.sin(startingPosRadians - Math.PI + yWiggle) * speed;
    
    // We now have the motion vector and speed and centered starting point.
    // Calculate the position of the center block
    float x = (float)Math.cos(startingPosRadians) * BOARD_RADIUS + CENTER_X;
    float y = (float)Math.sin(startingPosRadians) * BOARD_RADIUS + CENTER_Y;
    
    SpriteManager m = SpriteManager.instance();
    m.add(new SpecialFlyingBlockSprite(x,y,dx,dy));
    
    // Create the blocks to the left
    for (int i=1; i<=blocks/2; i++) {
      float tx = 
        (float)Math.cos(startingPosRadians - RADIAL_FLOCK_OFFSET * i)
            * (BOARD_RADIUS + DISTANCE_FLOCK_OFFSET * i) + CENTER_X;
      float ty = 
        (float)Math.sin(startingPosRadians - RADIAL_FLOCK_OFFSET * i)
            * (BOARD_RADIUS + DISTANCE_FLOCK_OFFSET * i) + CENTER_Y;
      
      m.add(new FlyingBlockSprite(tx,ty,dx,dy));
    }
    // and right
    for (int i=1; i<=blocks/2; i++) {
      float tx = 
        (float)Math.cos(startingPosRadians + RADIAL_FLOCK_OFFSET * i)
            * (BOARD_RADIUS + DISTANCE_FLOCK_OFFSET * i) + CENTER_X;
      float ty = 
        (float)Math.sin(startingPosRadians + RADIAL_FLOCK_OFFSET * i)
            * (BOARD_RADIUS + DISTANCE_FLOCK_OFFSET * i) + CENTER_Y;
      
      m.add(new FlyingBlockSprite(tx,ty,dx,dy));
    }
  }
  
}
