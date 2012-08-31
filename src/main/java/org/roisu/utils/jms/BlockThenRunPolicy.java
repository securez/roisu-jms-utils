package org.roisu.utils.jms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Class BlockThenRunPolicy wait on full queue before reject the task, a callback
 * permits to decide to wait again. 
 * 
 * 
 * @author marcos.lois
 */
public class BlockThenRunPolicy implements RejectedExecutionHandler {

	/** The block timeout. */
	private long blockTimeout;
	
	/** The bloc timeout unit. */
	private TimeUnit blocTimeoutUnit;
	
	/** The block timeout callback. */
	private Callable<Boolean> blockTimeoutCallback;

	/**
	 * Instantiates a new block then run policy.
	 *
	 * @param maxBlockingTime the max blocking time
	 * @param unit the time unit
	 * @param blockTimeoutCallback the block timeout callback
	 */
	public BlockThenRunPolicy(long maxBlockingTime, TimeUnit timeUnit, Callable<Boolean> blockTimeoutCallback) {
		this.blockTimeout = maxBlockingTime;
		this.blocTimeoutUnit = timeUnit;
		this.blockTimeoutCallback = blockTimeoutCallback;
	}
	
	/**
	 * Instantiates a new block then run policy.
	 *
	 * @param maxBlockingTime the max blocking time
	 * @param unit the unit
	 */
	public BlockThenRunPolicy(long maxBlockingTime, TimeUnit timeUnit) {
		this.blockTimeout = maxBlockingTime;
		this.blocTimeoutUnit = timeUnit;
		this.blockTimeoutCallback = null;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
	 */
	@Override
	public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
		BlockingQueue<Runnable> queue = executor.getQueue();
		boolean taskSent = false;

		while (!taskSent) {

			if (executor.isShutdown()) {
				throw new RejectedExecutionException(
				        "ThreadPoolExecutor has shutdown while attempting to offer a new task.");
			}

			try {
				// offer the task to the queue, for a blocking-timeout
				if (queue.offer(task, blockTimeout, blocTimeoutUnit)) {
					taskSent = true;
				} else {
					if (blockTimeoutCallback != null) {
						// task was not accepted - call the user's Callback
						Boolean result = null;
						try {
							result = blockTimeoutCallback.call();
						} catch (Exception e) {
							// wrap the Callback exception and re-throw
							throw new RejectedExecutionException(e);
						}
						// check the Callback result
						if (result == false) {
							throw new RejectedExecutionException("User decided to stop waiting for task insertion");
						} else {
							// user decided to keep waiting (may log it)
							continue;
						}
					} else {
						throw new RejectedExecutionException("Timeout waiting for task insertion");
					}
				}
			} catch (InterruptedException e) {
				// we need to go back to the offer call...
			}

		}

	}
}
