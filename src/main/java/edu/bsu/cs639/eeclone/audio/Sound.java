package edu.bsu.cs639.eeclone.audio;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import edu.bsu.cs639.util.LoopingByteInputStream;

/**
 * A buffered sound effect. Note that this should be used only for small audio
 * samples, not music.
 * <p>
 * This is based in part 
 * on Steven Fletcher's code from the cowcodgames.com tutorial on
 * java sound.
 * 
 * @author pvg
 */
public class Sound {
  
  //TODO: add support for killing sounds

  /** The format of the audio */
  private AudioFormat format;

  /** The audio data buffer */
  private byte[] buffer;
  
  /**
   * Create a sound object from the data on the input stream.
   * This is a synchronous method; it will block until the data is loaded.
   * 
   * @param in the input stream containing the sound effect data
   */
  public Sound(InputStream in) {
    // load the data
    try {
      AudioInputStream stream = AudioSystem.getAudioInputStream(in);
      format = stream.getFormat();
      loadAudioStream(stream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 

  /**
   * Converts the Sound's AudioInputStream to a ByteArrayInputStream.
   * 
   * @param audioStream 
   *          the AudioInputStream to convert
   * @throws IOException
   *           when the AudioInputStream can't be read.
   */
  private void loadAudioStream(AudioInputStream audioStream) throws IOException {
    // get the number of bytes to read
    int length = (int) (audioStream.getFrameLength() * format.getFrameSize());

    // read the entire stream
    buffer = new byte[length];
    DataInputStream is = new DataInputStream(audioStream);
    is.readFully(buffer);
    is.close();
  } // end loadAudioStream
  
  /**
   * Get the format of this sound
   * @return sound format
   */
  public AudioFormat format() { return format; }

  /**
   * Create a line that is compatible with the data in this sound
   * @return line
   */
  public SourceDataLine createCompatibleLine() {
    SourceDataLine line;
    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      line = (SourceDataLine)AudioSystem.getLine(info);
      line.open(format, buffer.length);
      line.start();
      return line;
    } catch(LineUnavailableException exception) {
      exception.printStackTrace();
      return null;
    }
  }
  
  /**
   * Play this sound on the given line.
   * This is a synchronous method that will return when the sound is
   * finished playing.
   * 
   * @param line an initialized line
   */
  public void play(SourceDataLine line) {
    
    line.write(buffer, 0, buffer.length);
    
    // wait until the sound is finished playing
    line.drain();
  }
  
  /**
   * Play this sound on its own line.
   */
  public void play() {
    SourceDataLine line = createCompatibleLine();
    if (line!=null)
      play(line);
    
    // Close the line we just made
    line.close();
  }
  
  /**
   * Get an input stream that provides the raw data of this sound.
   * @return input stream that reads this sound
   */
  public InputStream asStream() { return new ByteArrayInputStream(buffer); }
  
  /**
   * Get an input stream that provides this sound in an endless loop.
   * @return looping input stream for this sound
   */
  public InputStream asLoopingStream() 
  { return new LoopingByteInputStream(buffer); }
  
  /**
   * Test this class
   * @param args ignored
   */
  public static void main(String[] args) {
    InputStream in = Sound.class.getClassLoader().getResourceAsStream("resources/sounds/explosion.wav");
    InputStream in2 = Sound.class.getClassLoader().getResourceAsStream("resources/sounds/aspirin.wav");
    assert in!=null;
    final Sound s = new Sound(in);
    final Sound s2 = new Sound(in2);
    
    Thread[] t = new Thread[] {
        new Thread() {
      @Override public void run() {
        s.play();
      }
    },
    new Thread() {
      @Override public void run() {
        s2.play();
      }
    },
    new Thread() {
      @Override public void run() {
        s.play();
      }
    },
    new Thread() {
      @Override public void run() {
        s2.play();
      }
    }};
    for (int i=0; i<t.length; i++) {
      try {
      Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      t[i].start();
    }
    
    s.play();
    
  }
}
