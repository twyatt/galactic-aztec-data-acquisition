package edu.sdsu.rocket.helpers;

public abstract class RateLimitedRunnable implements Runnable {

	private long sleep;
	
	/**
	 * Sets the duration runnable sleeps per loop (in nanoseconds).
	 * 
	 * @param sleep (in nanoseconds)
	 */
	public void setSleep(long sleep) {
		this.sleep = sleep;
	}
	
	/**
	 * Returns the duration runnable sleeps per loop (in nanoseconds).
	 * 
	 * @return
	 */
	public long getSleep() {
		return sleep;
	}
	
	@Override
	public final void run() {
		while (!Thread.currentThread().isInterrupted()) {
			loop();
			
			if (sleep != 0) {
				try {
					// http://stackoverflow.com/q/4300653/196486
					if (sleep > 999999) {
						long ms = sleep / 1000000;
						long ns = sleep % 1000000;
						Thread.sleep(ms, (int) ns);
					} else {
						Thread.sleep(0, (int) sleep);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
	
	public abstract void loop();

}
