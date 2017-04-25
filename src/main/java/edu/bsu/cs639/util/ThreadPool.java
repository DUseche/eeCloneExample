package edu.bsu.cs639.util;


/**
 * A group of a limited number of threads that are used to execute tasks.
 * <p>
 * Based on the implementation by David Brackeen for
 * <ul>
 * Developing Games in Java
 * </ul>, provided under a BSD license.
 * This version includes expirable tasks.
 * 
 * @author pvg (expiring tasks)
 * @author David Brackeen (original design)
 */
public class ThreadPool extends ThreadGroup {

  private boolean isAlive;

  /** The queue of tasks to be completed */
  private ExpiringQueue<Runnable> taskQueue = new ExpiringQueue<Runnable>();

  /** The next available thread ID for a thread in this pool. */
  private int threadID;

  /** The next available thread pool ID */
  private static int threadPoolID;

  /**
   * Creates a new ThreadPool.
   * 
   * @param numThreads
   *          The number of threads in the pool.
   */
  public ThreadPool(int numThreads) {
    super("ThreadPool-" + (threadPoolID++));
    setDaemon(true);

    isAlive = true;

    // create all of the pooled threads
    for (int i = 0; i < numThreads; i++) {
      new PooledThread().start();
    }
  }

  /**
   * Requests a new task to run. This method returns immediately, and the task
   * executes on the next available idle thread in this ThreadPool.
   * The task never expires.
   * <p>
   * Tasks start execution in the order they are received.
   * 
   * @param task
   *          The task to run. If null, no action is taken.
   * @throws IllegalStateException
   *           if this ThreadPool is already closed.
   */
  public synchronized void runTask(Runnable task) {
    if (!isAlive) {
      throw new IllegalStateException();
    }
    if (task != null) {
      taskQueue.add(task);
      notify();
    }
  }
  
  /**
   * Requests a new task to run.
   * This method returns immediately, and the task executes on the next 
   * available idle thread in this pool.
   * The task expires at the given time.
   * @param task the task to run
   * @param expiry a time, specified in milliseconds, after which the
   *  task has expired and should not be run
   */
  public synchronized void runTask(Runnable task, long expiry) {
    if (!isAlive) throw new IllegalStateException();
    if (task!=null) {
      taskQueue.add(task,expiry);
      notify();
    }
  }
  

  /**
   * Get a task from the queue
   * @return the next task or null if all tasks have expired.
   * @throws InterruptedException
   */
  private synchronized Runnable getTask() throws InterruptedException {
    while (taskQueue.isEmpty()) {
      if (!isAlive) {
        return null;
      }
      wait();
    }
    return taskQueue.get();
  }

  /**
   * Closes this ThreadPool and returns immediately. All threads are stopped,
   * and any waiting tasks are not executed. Once a ThreadPool is closed, no
   * more tasks can be run on this ThreadPool.
   */
  public synchronized void close() {
    if (isAlive) {
      isAlive = false;
      taskQueue.clear();
      interrupt();
    }
  }

  /**
   * Closes this ThreadPool and waits for all running threads to finish. Any
   * waiting tasks are executed.
   */
  public void join() {
    // notify all waiting threads that this ThreadPool is no
    // longer alive
    synchronized (this) {
      isAlive = false;
      notifyAll();
    }

    // wait for all threads to finish
    Thread[] threads = new Thread[activeCount()];
    int count = enumerate(threads);
    for (int i = 0; i < count; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException ex) {
      }
    }
  }

  /**
   * Signals that a PooledThread has started. This method does nothing by
   * default; subclasses should override to do any thread-specific startup
   * tasks.
   */
  protected void threadStarted() {
    // do nothing
  }

  /**
   * Signals that a PooledThread has stopped. This method does nothing by
   * default; subclasses should override to do any thread-specific cleanup
   * tasks.
   */
  protected void threadStopped() {
    // do nothing
  }

  /**
   * A thread in a ThreadPool group.
   * @author pvg
   */
  private final class PooledThread extends Thread {

    public PooledThread() {
      super(ThreadPool.this, "PooledThread-" + (threadID++));
    }

    @Override
    public void run() {
      // signal that this thread has started
      threadStarted();

      while (!isInterrupted()) {
        // get a task to run
        Runnable task = null;
        try {
          task = getTask();
        } catch (InterruptedException ex) {
        }

        // brackeen;
        // if getTask() returned null or was interrupted,
        // close this thread.
        //if (task == null) {
        //  break;
        // }
        
        // if the task is null, then the queue must have expired all tasks,
        // so we'll wait for a new one
        if (task==null) continue;

        // run the task, and eat any exceptions it throws
        try {
          task.run();
        } catch (Throwable t) {
          uncaughtException(this, t);
        }
      }
      
      // signal that this thread has stopped
      threadStopped();
    }
  }
}