package edu.bsu.cs639.eeclone;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import edu.bsu.cs639.eeclone.sprites.Sprite.Powerup;

/**
 * The player's score.
 * 
 * @author pvg
 */
public class Score {

  private static final int BASE_POWERUP_VALUE = 200;

  private static final int POWERUP_SCALE = 200;

  private static final int CHAIN_FACTOR = 1;

  private static final Paint PAINT = java.awt.Color.WHITE;

  private static final Composite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

  /** The actual current score of the player */
  private int score;

  /** The number of points to assign for the next powerup collected */
  private int nextPowerupValue = BASE_POWERUP_VALUE;

  private int maxChain = 0;

  /**
   * Adjust the score for having collected a powerup
   * 
   * @param powerup
   * @return the number of points added
   */
  public int add(Powerup powerup) {
    int toAdd = nextPowerupValue;
    score += toAdd;
    nextPowerupValue += POWERUP_SCALE;
    return toAdd;
  }

  /**
   * Add points for adding one more to the given chain.
   * 
   * @param chain
   */
  public void add(Chain chain) {
    score += chain.size() * CHAIN_FACTOR;
    if (chain.size() > maxChain)
      maxChain = chain.size();
  }

  /**
   * Notify the score that the player has lost a life
   */
  public void playerLostLife() {
    nextPowerupValue = BASE_POWERUP_VALUE;
  }

  public void draw(Graphics2D g) {
    Paint oldPaint = g.getPaint();
    Composite oldComp = g.getComposite();

    // Draw score
    g.setPaint(PAINT);
    g.setComposite(TRANSPARENT);
    TextLayout tl = new TextLayout("" + score, Constants.SCORE_FONT, 
        g.getFontRenderContext());
    Rectangle2D rect = tl.getBounds();
    tl.draw(g, Constants.SCORE_RIGHT - (int) rect.getWidth(),
        Constants.SCORE_TOP + (int) rect.getHeight());

    // Draw max chain
    TextLayout mc = new TextLayout("" + maxChain,
        Constants.SCORE_FONT, g.getFontRenderContext());
    Rectangle2D rect2 = mc.getBounds();
    mc.draw(g, Constants.SCORE_RIGHT - (float)rect2.getWidth(),
        Constants.SCORE_TOP + (float)rect.getHeight() + 10 
        + (float)rect2.getHeight());

    g.setPaint(oldPaint);
    g.setComposite(oldComp);
  }
  
  /**
   * Get the max chain recorded by this score.
   * @return max chain
   */
  public int getMaxChain() { return maxChain; }
  
  /**
   * Get the integer form of this score 
   * @return score as integer
   */
  public int toInt() { return score; } 
}
