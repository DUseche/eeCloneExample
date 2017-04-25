package edu.bsu.cs639.eeclone.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * Manages sound playback.
 *  The SoundManager is a
 * ThreadPool, with each thread playing back one sound at a time. This allows
 * the SoundManager to easily limit the number of simultaneous sounds being
 * played.
 * 
 * <p>
 * Based on the implementation by David Brackeen for
 * <ul>
 * Developing Games in Java</ul>, provided under a BSD license.
 * 
 */
public class SoundManager extends edu.bsu.cs639.util.ThreadPool {

  /** The format used by all threads in this pool */
  private AudioFormat playbackFormat;

  private ThreadLocal<SourceDataLine> localLine;

  private ThreadLocal<byte[]> localBuffer;

  /** Lock object */
  private Object pausedLock = new Object();

  private boolean paused;

  /**
   * Creates a new SoundManager using the maximum number of simultaneous sounds.
   * @param playbackFormat playback format for this manager
   */
  public SoundManager(AudioFormat playbackFormat) {
    this(playbackFormat, getMaxSimultaneousSounds(playbackFormat));
  }

  /**
   * Creates a new SoundManager with the specified maximum number of
   * simultaneous sounds.
   * @param playbackFormat playback format 
   * @param maxSimultaneousSounds
   */
  public SoundManager(AudioFormat playbackFormat, int maxSimultaneousSounds) {
    // Commented out original impl since Mandriva always says this is one.
    super(//Math.min(maxSimultaneousSounds,getMaxSimultaneousSounds(playbackFormat)));
        maxSimultaneousSounds);
    this.playbackFormat = playbackFormat;
    localLine = new ThreadLocal<SourceDataLine>();
    localBuffer = new ThreadLocal<byte[]>();

    // notify threads in pool it's ok to start
    synchronized (this) {
      notifyAll();
    }
  }

  /**
   * Gets the maximum number of simultaneous sounds with the specified
   * AudioFormat that the default mixer can play.
   * <p>
   * <strong>WARNING</strong>: This does not seem to work on Mandriva 2006,
   * where it always returns 1.
   * 
   * @param playbackFormat playback format
   * @return max simultaneous sounds for the given format
   */
  public static int getMaxSimultaneousSounds(AudioFormat playbackFormat) {
    DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
        playbackFormat);
    Mixer mixer = AudioSystem.getMixer(null);
    return mixer.getMaxLines(lineInfo);
  }

  /**
   * Does any clean up before closing.
   */
  protected void cleanUp() {
    // signal to unpause
    setPaused(false);

    // close the mixer (stops any running sounds)
    Mixer mixer = AudioSystem.getMixer(null);
    if (mixer.isOpen()) {
      mixer.close();
    }
  }

  @Override
  public void close() {
    cleanUp();
    super.close();
  }

  @Override
  public void join() {
    cleanUp();
    super.join();
  }

  /**
   * Sets the paused state. Sounds may not pause immediately.
   * @param paused desired paused state
   */
  public void setPaused(boolean paused) {
    if (this.paused != paused) {
      synchronized (pausedLock) {
        this.paused = paused;
        if (!paused) {
          // restart sounds
          pausedLock.notifyAll();
        }
      }
    }
  }

  /**
   * Returns the paused state.
   * @return true if paused, false if not
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * Loads a Sound from the file system. Returns null if an error occurs.
   */
  //public Sound getSound(String filename) {
  //  return getSound(getAudioInputStream(filename));
 // }

  /**
   * Loads a Sound from an input stream. Returns null if an error occurs.
   */
  //public Sound getSound(InputStream is) {
  //  return getSound(getAudioInputStream(is));
  //}

  /**
   * Loads a Sound from an AudioInputStream.
   */
//  public Sound getSound(AudioInputStream audioStream) {
//    if (audioStream == null) {
//      return null;
//    }
//
//    // get the number of bytes to read
//    int length = (int) (audioStream.getFrameLength() * audioStream.getFormat()
//        .getFrameSize());
//
//    // read the entire stream
//    byte[] samples = new byte[length];
//    DataInputStream is = new DataInputStream(audioStream);
//    try {
//      is.readFully(samples);
//      is.close();
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    }
//
//    // return the samples
//    return new Sound(samples);
//  }

//  /**
//   * Creates an AudioInputStream from a sound from the file system.
//   */
//  public AudioInputStream getAudioInputStream(String filename) {
//    try {
//      return getAudioInputStream(new FileInputStream(filename));
//    } catch (IOException ex) {
//      ex.printStackTrace();
//      return null;
//    }
//  }

//  /**
//   * Creates an AudioInputStream from a sound from an input stream
//   */
//  public AudioInputStream getAudioInputStream(InputStream is) {
//
//    try {
//      if (!is.markSupported()) {
//        is = new BufferedInputStream(is);
//      }
//      // open the source stream
//      AudioInputStream source = AudioSystem.getAudioInputStream(is);
//
//      // convert to playback format
//      return AudioSystem.getAudioInputStream(playbackFormat, source);
//    } catch (UnsupportedAudioFileException ex) {
//      ex.printStackTrace();
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    } catch (IllegalArgumentException ex) {
//      ex.printStackTrace();
//    }
//
//    return null;
//  }

  /**
   * Plays a sound. This method returns immediately.
   * @param sound the sound to play
   */
  public void play(Sound sound) {
    play(sound, false);
  }

  /**
   * Plays a sound, optionally looping. This method returns immediately.
   * @param sound the sound to play
   * @param loop desired looping status
   */
  public void play(Sound sound, /*SoundFilter filter,*/ boolean loop) {
    assert sound!=null;
    
    InputStream is;
    // If we are looping, set up a looping input stream.
    // Otherwise, use a normal input stream.
    if (loop) {
      is = sound.asLoopingStream();
    } else {
      is = sound.asStream();
    }

    play(is);
  }

  /**
   * Plays a sound from an InputStream.
   * This method returns immediately.
   * @param is an input stream
   */
  public void play(InputStream is/*, SoundFilter filter*/) {
    assert is!=null;
    //TODO: make the 50ms fudge a parameter of this object or the method
    runTask(new SoundPlayer(is), System.currentTimeMillis()+50);
  }

  /**
   * Signals that a PooledThread has started. Creates the Thread's line and
   * buffer.
   */
  @Override
  protected void threadStarted() {
    // wait for the SoundManager constructor to finish
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException ex) {
      }
    }

    // use a short, 100ms (1/10th sec) buffer for filters that
    // change in real-time
    int bufferSize = playbackFormat.getFrameSize()
        * Math.round(playbackFormat.getSampleRate() / 10);

    // create, open, and start the line
    SourceDataLine line;
    DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
        playbackFormat);
    try {
      line = (SourceDataLine) AudioSystem.getLine(lineInfo);
      line.open(playbackFormat, bufferSize);
    } catch (LineUnavailableException ex) {
      // the line is unavailable - signal to end this thread
      Thread.currentThread().interrupt();
      return;
    }

    line.start();

    // create the buffer
    byte[] buffer = new byte[bufferSize];

    // set this thread's locals
    localLine.set(line);
    localBuffer.set(buffer);
  }

  /**
   * Signals that a PooledThread has stopped. Drains and closes the Thread's
   * Line.
   */
  @Override
  protected void threadStopped() {
    SourceDataLine line = (SourceDataLine) localLine.get();
    if (line != null) {
      line.drain();
      line.close();
    }
  }

  /**
   * The SoundPlayer class is a task for the PooledThreads to run. It receives
   * the threads's Line and byte buffer from the ThreadLocal variables and plays
   * a sound from an InputStream.
   * <p>
   * This class only works when called from a PooledThread.
   */
  private class SoundPlayer implements Runnable {

    private InputStream source;

    public SoundPlayer(InputStream source) {
      this.source = source;
    }

    public void run() {
      // get line and buffer from ThreadLocals
      SourceDataLine line =  localLine.get();
      byte[] buffer = localBuffer.get();
      if (line == null || buffer == null) {
        // the line is unavailable
        return;
      }

      // copy data to the line
      try {
        int numBytesRead = 0;
        while (numBytesRead != -1) {
          // if paused, wait until unpaused
          synchronized (pausedLock) {
            if (paused) {
              try {
                pausedLock.wait();
              } catch (InterruptedException ex) {
                return;
              }
            }
          }
          // copy data
          numBytesRead = source.read(buffer, 0, buffer.length);
          if (numBytesRead != -1) {
            line.write(buffer, 0, numBytesRead);
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }

    }
  }

}
