package edu.bsu.cs639.eeclone.sprites;

/**
 * Tests if sprites collide.
 * 
 * @author pvg
 */
public class CollisionTester {

  public static boolean collidesBoundingOval(Sprite s1, Sprite s2) {
    float s1CenterX = s1.x() + s1.width() / 2f;
    float s1CenterY = s1.y() + s1.height() / 2f;
  
    // assume the other sprite is round too
    float s2CenterX = s2.x() + s2.width() / 2f;
    float s2CenterY = s2.y() + s2.height() / 2f;
  
    float distance = (float) Math.sqrt(
        square(s1CenterX - s2CenterX)
        + square(s1CenterY - s2CenterY));

    return distance < s1.width() / 2f + s2.width() / 2f; 
  }

  private static float square(float f) {
    return f * f;
  }

}
