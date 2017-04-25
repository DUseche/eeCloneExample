package edu.bsu.cs639.eeclone.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import edu.bsu.cs639.eeclone.Constants;

/**
 * A noninteractive sprite that shows some text. 
 * 
 * @author pvg
 */
public class TextSprite extends AbstractSprite implements Sprite.Noninteractive{
  
  private final String text;
  
  private static final Color COLOR = Color.BLUE;
  
  private static final int LIFECYCLE_UPDATES = edu.bsu.cs639.eeclone.Constants.FPS * 2;
  
  private int ticks = 0;
  
  /**
   * Create a text sprite centered at the given point
   * @param text the text to show 
   * @param cx center x
   * @param cy center y
   */
  public TextSprite(String text, float cx, float cy) {
    x = cx;
    y = cy;
    this.text=text;
  }
  
  public Object accept(Visitor v, Object arg) {
    return v.visit(this,arg);
  }

  public void draw(Graphics2D g) {
    Color oldColor = g.getColor();
    
    g.setColor(COLOR);
    TextLayout tl = new TextLayout(text, Constants.POWERUP_TEXT_FONT, g.getFontRenderContext());
    Rectangle2D rect = tl.getBounds();
    tl.draw(g, x - (float)rect.getWidth()/2f, y-(float)rect.getHeight()/2f);
    
    g.setColor(oldColor);
  }

  public void update() {
    if (ticks++ > LIFECYCLE_UPDATES) markForRemoval();
  }



}
