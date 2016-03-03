package edu.sdsu.rocket.server.io.radio;

import edu.sdsu.rocket.core.helpers.RateLimitedRunnable;
import edu.sdsu.rocket.server.io.radio.api.RFModuleStatus;
import edu.sdsu.rocket.server.io.radio.api.RXPacket;
import edu.sdsu.rocket.server.io.radio.api.TXStatus;

public class Watchdog implements APIFrameListener {
	
	public interface WatchdogListener {
		public void triggered();
	}
	private WatchdogListener listener;
	
	private static final long MILLISECONDS_PER_SECOND = 1000L;
	private static final long CHECK_INTERVAL = 1L * MILLISECONDS_PER_SECOND;
	
	private boolean timeoutEnabled;
	private volatile long timeoutStart;
	private final long timeoutDuration; // milliseconds
	
	private boolean countdownEnabled;
	private long countdownStart;
	private long countdownDuration; // milliseconds
	
	private Thread thread;
	private RateLimitedRunnable runnable;

	public Watchdog(long timeout) {
		this.timeoutDuration = timeout * MILLISECONDS_PER_SECOND;
	}
	
	public void setListener(WatchdogListener listener) {
		this.listener = listener;
	}
	
	public void enable() {
		timeoutStart = System.currentTimeMillis();
		timeoutEnabled = true;
	}
	
	public void disable() {
		timeoutEnabled = false;
	}
	
	public void startCountdown(long seconds) {
		countdownStart = System.currentTimeMillis();
		countdownDuration = seconds * MILLISECONDS_PER_SECOND;
		countdownEnabled = true;
	}
	
	public void stopCountdown() {
		countdownEnabled = false;
	}
	
	/**
	 * Number of seconds remaining until timeout.
	 * 
	 * @return
	 */
	public long getTimeoutTimeRemaining() {
		long elapsed = System.currentTimeMillis() - timeoutStart;
		return (timeoutDuration - elapsed) / MILLISECONDS_PER_SECOND;
	}
	
	/**
	 * Number of seconds remaining on countdown.
	 * 
	 * @return
	 */
	public long getCountdownTimeRemaining() {
		long elapsed = System.currentTimeMillis() - countdownStart;
		return (countdownDuration - elapsed) / MILLISECONDS_PER_SECOND;
	}

	public void start() {
		runnable = new RateLimitedRunnable(CHECK_INTERVAL) {
			@Override
			public void loop() throws InterruptedException {
				if (countdownEnabled) {
					long elapsed = System.currentTimeMillis() - countdownStart;
					if (elapsed >= countdownDuration) {
						if (listener != null) {
							listener.triggered();
						}
					}
				}
				
				if (timeoutEnabled) {
					long elapsed = System.currentTimeMillis() - timeoutStart;
					if (elapsed >= timeoutDuration) {
						if (listener != null) {
							listener.triggered();
						}
					}
				}
			}
		};
		thread = new Thread(runnable);
		thread.setName(getClass().getSimpleName());
		thread.start();
	}
	
	public void stop() {
		if (thread != null && runnable != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
			runnable.setRunning(false);
			thread = null;
		}
	}

	@Override
	public void onRXPacket(RXPacket rxPacket) {
		timeoutStart = System.currentTimeMillis();
	}

	@Override
	public void onRFModuleStatus(RFModuleStatus rfModuleStatus) {
		timeoutStart = System.currentTimeMillis();
	}

	@Override
	public void onTXStatus(TXStatus txStatus) {
		timeoutStart = System.currentTimeMillis();
	}

}
