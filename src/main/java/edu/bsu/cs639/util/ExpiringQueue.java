package edu.bsu.cs639.util;



/**
 * A linked list queue that supports expiration of elements.
 * 
 * @author pvg
 * @param <T> the type of element contained in this queue
 */
public final class ExpiringQueue<T>{ 
  // This could implement List<T>, but that requires implementing
  // a bunch of silly methods that I don't feel like implementing now.
  
  /** head of the queue */
  private Node head;
  
  /** tail of the queue */
  private Node tail;

  public ExpiringQueue() {}
  
  /**
   * Add a non-expiring element to the queue.
   * @param data
   */
  public void add(T data) {
    add(data, Node.NON_EXPIRING);
  }
  
  /**
   * Add an element to the queue
   * @param data 
   * @param expiry expiration time, in milliseconds 
   */
  public void add(T data, long expiry) {
    if (head==null) head = tail = new Node(data,expiry);
    else {
      tail.next = new Node(data,expiry);
      tail = tail.next;
    }
  }

  /**
   * Check if the queue is empty
   * @return true if empty
   */
  public boolean isEmpty() { return head==null; }
  
  /**
   * Remove an element from the queue.
   * Expired elements will not be returned.
   * If there are no elements left, null is returned.
   * @return the next non-expired element or null if the queue is empty
   */
  public T get() {
    while (head!=null) {
      // Check if the head should expire
      if (head.expiry!=Node.NON_EXPIRING
          && head.expiry < System.currentTimeMillis()) {
        // Expire the head
        head = head.next;
        if (isEmpty()) {
          tail=null;
        }
      }
      else {
        // return the head and update the list
        T result = head.data;
        head = head.next;
        if (isEmpty()) tail=null;
        return result;
      }
    }
    assert head==null;
    assert tail==null;
    return null;
  }
  
  /**
   * Clear this queue.
   */
  public void clear() {
    head = tail = null;
  }
  
  /**
   * Linked list node type.
   * 
   * @author pvg
   */
  private final class Node {
    // Implementation note: these could be pooled
    
    /** Sentinel value for nonexpiring nodes */
    private static final int NON_EXPIRING = -1;
    
    /**
     * The time, in millis, at which this node expires.
     * May also be {@link #NON_EXPIRING}.
     */
    private long expiry;
    
    /** The data element of this node */
    private T data;
    
    /** A link to the next element in the queue */
    private Node next;
    
    public Node(T data, long expiry) {
      assert expiry >= -1;
      this.data=data;
      this.expiry = expiry;
    }
  }
}
