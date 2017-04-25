package edu.bsu.cs639.eeclone;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import edu.bsu.cs639.eeclone.audio.OggPlayer;
import edu.bsu.cs639.eeclone.sprites.PlayerSprite;
import edu.bsu.cs639.eeclone.sprites.PlayerSprite.MovementDirection;

/**
 * The game logic.
 * A singleton of sorts that is initialized when the constructor is called
 * for the first time.
 * 
 * @author pvg
 */
public final class Game {
  
  private static Game SINGLETON;
  
  public static Game instance() { return SINGLETON; }
  
  /** The panel on which this game is installed */
  private final EEClonePanel panel;
  
  /** The state of the game */
  private GameState state; 
  
  private final ObstacleGenerator obstacleGenerator = new ObstacleGenerator();
  
  /** The sprite manager.  Though it is a singleton, we keep a reference
   * for convenience.
   * This manages all of the sprites used in the game <em>except</em>
   * for the player's sprite.
   */
  private final SpriteManager spriteManager = SpriteManager.instance();
  
  /**
   * the player sprite
   */
  private PlayerSprite player;
  
  /** The score object, which will be null if a game is not happening */
  private Score score;
  
  /**
   * Create a new game instance
   * @param panel the panel on which the game is running
   */
  public Game(EEClonePanel panel) {
    if (SINGLETON!=null)
      throw new IllegalStateException("There can only be one game object!");
    else
      SINGLETON = this;
    
    assert panel!=null;
    this.panel = panel;
    
    // Set the initial state
    setState(MENU_STATE);
  }
  
  /**
   * Set the state of the game.
   * This will cause the old state to be uninstalled and the new state to be
   * installed.
   * @param state the new state of the game
   */
  protected void setState(GameState state) {
    assert state!=null;
    assert !state.equals(this.state) : "Setting the same state!";
    
    // Uninstall the old state.
    // this.state will be null the first time we call this method.
    if (this.state!=null) this.state.uninstall();
    
    // Install the new state.
    this.state = state;
    this.state.install();
  }
  
  /**
   * Called when a game is first started
   */
  private void startGame() {
    player = new PlayerSprite();
    score = new Score();
    spriteManager.reset();
    setState(PLAYING_STATE);
  }
  
  /**
   * Update this game's state.
   */
  public void update() {
    state.update();
  }

  /**
   * Render this game
   * @param buffer the image on which to render
   */
  public void render(Image buffer) {
    state.render(buffer);
  }
  
  /**
   * Immediately quit this game.
   */
  public void quit() {
    // Do other cleanup here, like saving high scores.
    System.exit(0);
  }
  
  /**
   * Called to indicate that the game is over
   */
  public void gameOver() {
    OggPlayer.instance().stop();
    setState(END_OF_GAME_STATE);
  }
  
  /**
   * Get the score object for this game.
   * This wil return null if there is no game happening.
   * @return score
   */
  public Score score() { return score; }
  
  /**
   * The main menu state
   */
  private final GameState MENU_STATE = new GameState() {

  	private final Image bg = ResourceLoader.instance().getImage("menu_bg");
  	
    private boolean firstRender = true;
    
    /**
     * The key listener for this state.
     */
    private final KeyListener keyListener = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() ==KeyEvent.VK_ESCAPE)
          quit();
        else {
          startGame();
        }
      }
    };
    
    public void install() {
      panel.addKeyListener(keyListener);
    }

    public void uninstall() {
      panel.removeKeyListener(keyListener);
    }

    public void update() {
      // Nothing to do on updates.
    }

    public void render(Image buffer) {
      assert buffer!=null;
      
      Graphics2D g = (Graphics2D)buffer.getGraphics();
      
      // There is a noticible delay the first time the font metrics
      // for a font are computed for a text layout, so this code ensures
      // that the delay is only when the menu is first being drawn.
      if (firstRender) {
        new TextLayout("x", Constants.POWERUP_TEXT_FONT, g.getFontRenderContext());
        firstRender=false;
      }
      
      g.drawImage(bg, 0, 0, null);
    }
  };
  
  /**
   * The state when the game is actively being played
   */
  private final GameState PLAYING_STATE = new GameState() {

    /** Keep track of presses and releases for movement keys */
    private boolean upActive, downActive, leftActive, rightActive;
    
    private Image bg = ResourceLoader.instance().getImage("game_bg");
    
    private final KeyListener keyListener = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          upActive=true; break;
        case KeyEvent.VK_DOWN:
          downActive=true; break;
        case KeyEvent.VK_LEFT:
          leftActive=true; break;
        case KeyEvent.VK_RIGHT:
          rightActive=true; break;
        case KeyEvent.VK_SPACE:
          player.explode();
        }
      }
      @Override
      public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          upActive=false; break;
        case KeyEvent.VK_DOWN:
          downActive=false; break;
        case KeyEvent.VK_LEFT:
          leftActive=false; break;
        case KeyEvent.VK_RIGHT:
          rightActive=false; break;
        }
      }
    };
    
    public void install() {
      // Reset the key actions
      upActive = downActive = leftActive = rightActive = false;
      
      panel.addKeyListener(keyListener);
      OggPlayer.instance().play(
          ResourceLoader.instance().getOggStream("game_music"));
    }

    public void render(Image buffer) {
      Graphics2D g = (Graphics2D)buffer.getGraphics();
      g.drawImage(bg, 0, 0, null);
      spriteManager.drawAll(g);
      player.draw(g);
      score.draw(g);
      drawLivesRemaining(g);
    }

    public void uninstall() {
      panel.removeKeyListener(keyListener);
    }

    public void update() {
      //
      // Set the player's motion vector based on keyboard commands.
      //
      // There is potential for synchronization bugs between this code
      // and the input processing code, but frankly, we don't care.
      // It might mean that the player goes the wrong way for one update,
      // and that should be barely noticable.
      //
      if (upActive) {
        if (leftActive) player.move(MovementDirection.UP_LEFT);
        else if (rightActive) player.move(MovementDirection.UP_RIGHT);
        else player.move(MovementDirection.UP);
      }
      else if (downActive) {
        if (leftActive) player.move(MovementDirection.DOWN_LEFT);
        else if (rightActive) player.move(MovementDirection.DOWN_RIGHT);
        else player.move(MovementDirection.DOWN);
      }
      else if (rightActive) player.move(MovementDirection.RIGHT);
      else if (leftActive) player.move(MovementDirection.LEFT);
      else player.move(MovementDirection.STOP);
      
      // Generate obstacles
      obstacleGenerator.update();
      
      // Update the player's location and animation
      player.update();
      
      // Update all the other sprites
      spriteManager.update();
    }
    
    /**
     * Draw the number of lives remaining for the player
     * @param g
     */
    private void drawLivesRemaining(Graphics2D g) {
      // Currently we reuse the constants for the score.
      // This could be customized, but this is sufficient for now.
      TextLayout tl = new TextLayout(String.valueOf(player.livesRemaining()),
          Constants.SCORE_FONT, g.getFontRenderContext());
      g.setPaint(java.awt.Color.WHITE);
      Rectangle2D rect = tl.getBounds();
      tl.draw(g, Constants.SCORE_RIGHT - (float)rect.getWidth(),
          Constants.BOARD_HEIGHT - Constants.SCORE_TOP);
    }
  };

  /**
   * The end-of-game state.
   */
  private final GameState END_OF_GAME_STATE = new GameState() {

    /**
     * Listens for the press of any key, which signals a change back to
     * the playing state.
     */
    private final KeyListener keyListener = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        startGame();
      }
    };
    
    public void install() {
      panel.addKeyListener(keyListener);
    }

    public void render(Image buffer) {
      Graphics2D g = (Graphics2D)buffer.getGraphics();
      g.setFont(Constants.SCORE_FONT);
      
      g.drawString("Score: " + score.toInt(), 50,100);
      g.drawString("Max Chain: " + score.getMaxChain(), 50,200);
      g.drawString("Press any key to play again", 50, 300);
    }

    public void uninstall() {
      panel.removeKeyListener(keyListener);
    }

    public void update() {
      // Nothing changes here.
    }
    
  };
  
}

/**
 * A game state.
 * 
 * @author pvg
 */
interface GameState {

  /**
   * Install this game state
   */
  public void install();
  
  /**
   * Uninstall this game state
   */
  public void uninstall();
  
  /**
   * Update the game's status, given that this is the current state.
   */
  public void update();
  
  /**
   * Render the game, given that this is the current state.
   * @param buffer the image buffer on which to draw
   */
  public void render(Image buffer);
}
