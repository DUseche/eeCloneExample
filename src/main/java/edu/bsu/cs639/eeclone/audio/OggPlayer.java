package edu.bsu.cs639.eeclone.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * Plays ogg streams as background music.
 * This is a singleton.
 * 
 * @author pvg
 */
public final class OggPlayer {
  
  //TODO: implement pause/continue
  
  private static final OggPlayer SINGLETON = new OggPlayer();
  /**
   * Get the singleton oggplayer for this runtime.
   * @return singletone ogg player
   */
  public static OggPlayer instance() { return SINGLETON; }
  private OggPlayer() {}

  private PlayThread thread;
  
  /**
   * Play the provided ogg stream.
   * @param in
   */
  public void play(OggInputStream in) {
    if(thread!=null)
      throw new IllegalStateException("Cannot start; another thread is running");
    else {
      thread = new PlayThread(in);
      thread.start();
    }
  }
  
  /**
   * Stop playing music.
   */
  public void stop() {
    thread.requestStop();
    thread = null;
  }
  
  /**
   * Plays an ogg stream on a separate thread.
   * 
   * @author pvg
   */
  private final class PlayThread extends Thread {
    
    private final OggInputStream oggStream;
    
    private boolean stopRequested = false;
    
    public PlayThread(OggInputStream in) {
      this.oggStream = in;
    }
    
    public void requestStop() { stopRequested = true; }
    
    @Override
    public void run() {
      byte[] buffer = new byte[512]; 
      
      try {
        /*
        AudioFormat decodedFormat = new AudioFormat(
          AudioFormat.Encoding.PCM_SIGNED, 
          oggStream.getRate(), 
          16,
          oggStream.getFormat()==OggInputStream.FORMAT_MONO16 ? 1 : 2,
          oggStream.getFormat()==OggInputStream.FORMAT_MONO16 ? 2 : 4,
          oggStream.getRate(), 
          false);
          */
        AudioFormat decodedFormat = new AudioFormat(
            oggStream.getRate(),
            16,
            oggStream.getFormat()==OggInputStream.FORMAT_MONO16 ? 1 : 2,
            true,
            false);
        DataLine.Info lineInfo = 
          new DataLine.Info(SourceDataLine.class, decodedFormat);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(lineInfo);
      
        line.open(decodedFormat);
        line.start();
        
        
        int nBytesRead = 0;
        while (nBytesRead != -1 && !stopRequested) {
          
          // Check if we should pause
          /*
          synchronized (this) {
            if (paused) {
              try {
                line.stop();
                wait();
                line.start();
              } catch (InterruptedException e) {
              }
            }
            assert !paused;
          }*/
          
          nBytesRead = oggStream.read(buffer, 0, buffer.length);
          if (nBytesRead != -1 && !stopRequested) {
            line.write(buffer, 0, nBytesRead);
          }
        }

        // Stop
        if (!stopRequested) line.drain();
        line.stop();
        line.close();

        oggStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  /** 
   * Test this class.
   * @param args ignored
   * @throws Exception if anything goes wrong
   */
  public static void main(String[] args) throws Exception {
    java.io.InputStream rawInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("music/cheese.ogg");
    assert rawInputStream!=null;
    OggInputStream oggStream = new OggInputStream(rawInputStream);
    System.out.println(oggStream.toString());
    System.out.println(oggStream.getRate());
    OggPlayer player = new OggPlayer();
    player.play(oggStream);
  }
}
