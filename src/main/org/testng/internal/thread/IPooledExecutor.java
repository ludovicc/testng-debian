package org.testng.internal.thread;

import java.util.List;

/**
 * Reduced interface to mimic PooledExecutor.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IPooledExecutor {
   void execute(Runnable run);

   void shutdown();

   void awaitTermination(long timeout) throws InterruptedException;

  /**
   * @return
   */
  boolean isTerminated();
}
