package edu.bsu.cs639.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * The LoopingByteInputStream is a ByteArrayInputStream that loops indefinitly.
 * The looping stops when the close() method is called.
 * <p>
 * Based on the implementation by David Brackeen for
 * <ul>
 * Developing Games in Java
 * </ul>, provided under a BSD license.
 *
 * @author pvg
 */
public class LoopingByteInputStream extends ByteArrayInputStream {

  private boolean closed;

  /**
   * Creates a new LoopingByteInputStream with the specified byte array. The
   * array is not copied.
   * @param buffer the source buffer
   */
  public LoopingByteInputStream(byte[] buffer) {
    super(buffer);
    closed = false;
  }

  /**
   * Reads <code>length</code> bytes from the array. If the end of the array
   * is reached, the reading starts over from the beginning of the array.
   * Returns -1 if the array has been closed.
   * @param buffer the buffer in which to put the result
   * @param offset initial offset
   * @param length the number of bytes to read
   * @return -1 if the array has been closed, otherwise the number of
   * bytes read.
   */
  @Override
  public int read(byte[] buffer, int offset, int length) {
    if (closed) {
      return -1;
    }
    int totalBytesRead = 0;

    while (totalBytesRead < length) {
      int numBytesRead = super.read(buffer, offset + totalBytesRead, length
          - totalBytesRead);

      if (numBytesRead > 0) {
        totalBytesRead += numBytesRead;
      } else {
        reset();
      }
    }
    return totalBytesRead;
  }

  /**
   * Closes the stream. Future calls to the read() methods will return 1.
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    super.close();
    closed = true;
  }

}
