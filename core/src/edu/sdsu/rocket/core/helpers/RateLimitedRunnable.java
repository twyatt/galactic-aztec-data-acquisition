package edu.sdsu.rocket.core.helpers;

import java.util.concurrent.TimeUnit;

public abstract class RateLimitedRunnable implements Runnable {
	
	private static final long NANOSECONDS_PER_MILLISECOND = 1000000L;

	private long sleep_ms;
	private long sleep_ns;
	
	/**
	 * Sets the duration runnable sleeps per loop (in milliseconds).
	 * 
	 * @param sleep (in milliseconds)
	 */
	public void setSleep(long milliseconds) {
		sleep_ms = milliseconds;
		sleep_ns = 0;
	}
	
	/**
	 * Sets the duration runnable sleeps per loop (in nanoseconds).
	 * 
	 * @param sleep (in nanoseconds)
	 */
	public void setSleepNanoseconds(long nanoseconds) {
		// http://stackoverflow.com/q/4300653/196486
		if (nanoseconds > 999999) {
			sleep_ms = nanoseconds / NANOSECONDS_PER_MILLISECOND;
			sleep_ns = TimeUnit.NANOSECONDS.toMillis(nanoseconds);
		} else {
			sleep_ms = 0;
			sleep_ns = nanoseconds;
		}
	}
	
	/**
	 * Returns the duration runnable sleeps per loop (in milliseconds).
	 * 
	 * @return
	 */
	public long getSleep() {
		return sleep_ms;
	}
	
	public long getSleepNanoseconds() {
		return sleep_ms * NANOSECONDS_PER_MILLISECOND + sleep_ns;
	}
	
	@Override
	public final void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				loop();
				
				if (sleep_ms != 0 || sleep_ns != 0) {
					if (sleep_ns == 0) {
						Thread.sleep(sleep_ms);
					} else {
						Thread.sleep(sleep_ms, (int) sleep_ns);
					}
				}
			} catch (InterruptedException e) {
				Console.error(e);
				return;
			}
		}
	}
	
	public abstract void loop() throws InterruptedException;

}
