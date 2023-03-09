package jcheese.swing_ui;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Latch {
	private static class Sync extends AbstractQueuedSynchronizer {
	  private static final long serialVersionUID = 0xBEEFBABE;
	  
	  public final int startCount;
	  
	  public Sync(int count) {
	    startCount = count;
	    setState(count);
	  }
	  
	  public int getCount() { return getState(); }
	  
	  public int tryAcquireShared(int acquires) {
	    return getState() == 0 ? 1 : -1;
	  }
	  
	  public boolean tryReleaseShared(int releases) {
	    for (;;) {
	      int count = getState();
	      if (count == 0) return false;
	      int nextCount = count - 1;
	      if (compareAndSetState(count, nextCount)) {
	        return nextCount == 0;
	      }
	    }
	  }
	  
	  public void reset() {
	    setState(startCount);
	  }
	} // class Sync
	
	private final Sync sync;
	
	public Latch(int count) {
	  if (count < 0) throw new IllegalArgumentException("Count must be a non-negative integer");
	  sync = new Sync(count);
	}
	
	public void await() throws InterruptedException {
	  sync.acquireSharedInterruptibly(1);
	}
	
	public void reset() { sync.reset(); }
	
	public boolean await(long timeout, TimeUnit tu) throws InterruptedException {
	  return sync.tryAcquireSharedNanos(1, tu.toNanos(timeout));
	}
	
	public void countDown() { sync.releaseShared(1); }
	
	public long getCount() { return sync.getCount(); }
	
	@Override
	public String toString() {
	  return String.format("%s[Count = %d]", super.toString(), sync.getCount());
	}
}