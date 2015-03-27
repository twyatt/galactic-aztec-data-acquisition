package edu.sdsu.rocket.server.devices;

import java.io.IOException;

public class ADS1115Wrapper {
	
	public static final int STALE   = -1;
	public static final int FRESH   =  0;
	public static final int TIMEOUT =  1;
	
	private static final boolean DEBUG = true;
	private static final int CHANNELS = 4;
	
	enum State {
		POWERED_DOWN,
		WAITING_FOR_ASSERT,
		CONVERSION_IN_PROGRESS,
		CONVERSION_COMPLETE,
	};
	
	float a[] = new float[4];
	
	State state = State.POWERED_DOWN;
	long start = System.nanoTime();
	int i = 0;

	private long timeout;
	
	private final ADS1115 ads1115;

	public ADS1115Wrapper(ADS1115 ads1115) {
		this.ads1115 = ads1115;
	}
	
	public ADS1115 getDevice() {
		return ads1115;
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Performs an analog read of the specified channel (blocks until read is
	 * complete).
	 * 
	 * @param channel 0-3
	 * @return Reading (mV)
	 * @throws IOException
	 */
	public float read(int channel) throws IOException {
		start = System.nanoTime();
		ads1115.setSingleEnded(channel).begin();
		
		boolean ready = false, waiting = true;
		while (!ready) {
			if (ads1115.isPerformingConversion()) {
				ready = true;
			} else if (System.nanoTime() - start > timeout) {
				/*
				 * Timed out waiting for assert, so we'll assume we
				 * missed the assert and the conversion is already
				 * complete.
				 */
				ready = true;
				waiting = false;
				if (DEBUG) System.out.print("?");
			}
		}
		
		// wait for conversion
		while (waiting) {
			if (!ads1115.isPerformingConversion()) {
				waiting = false;
			} else if (System.nanoTime() - start > timeout) {
				if (DEBUG) System.out.print("!");
				waiting = false;
			}
		}
		
		return ads1115.readMillivolts();
	}
	
	// non-blocking
	public int read(float[] raw) throws IOException {
		if (State.POWERED_DOWN.equals(state)) {
			start = System.nanoTime();
			
			int channel = i;
			ads1115.setSingleEnded(channel).begin();
			
			state = State.WAITING_FOR_ASSERT;
			if (DEBUG) System.out.print("_");
		}
		
		if (State.WAITING_FOR_ASSERT.equals(state)) {
			if (ads1115.isPerformingConversion()) {
				state = State.CONVERSION_IN_PROGRESS;
				if (DEBUG) System.out.print("^");
			} else {
//				System.out.println("Waiting for assert.");
				if (DEBUG) System.out.print(",");
				if (System.nanoTime() - start > timeout) {
					/*
					 * Timed out waiting for assert, so we'll assume we
					 * missed the assert and the conversion is already
					 * complete.
					 */
					state = State.CONVERSION_COMPLETE;
					if (DEBUG) System.out.print("?");
				}
			}
		}
		
		if (State.CONVERSION_IN_PROGRESS.equals(state)) {
			if (!ads1115.isPerformingConversion()) {
//				System.out.println("Conversion complete.");
				state = State.CONVERSION_COMPLETE;
				if (DEBUG) System.out.print("-");
			} else {
//				System.out.println("Waiting for conversion.");
				if (DEBUG) System.out.print(".");
			}
		}
		
		if (State.CONVERSION_COMPLETE.equals(state)) {
			if (DEBUG) System.out.print(i);
			state = State.POWERED_DOWN;
			a[i++] = ads1115.readMillivolts();
			if (i == CHANNELS) {
				if (DEBUG) System.out.println();
//				System.out.println("A0=" + a[0] + " mV,\tA1=" + a[1] + " mV,\tA2=" + a[2] + " mV,\tA3=" + a[3] + " mV");
				System.arraycopy(a /* src */, 0 /* srcPos */, raw /* dest */, 0 /* destPos */, CHANNELS /* length */);
				i = 0;
				return FRESH;
			}
			return STALE;
		}
		
		if (System.nanoTime() - start > timeout) {
			if (DEBUG) System.out.print("!");
			state = State.POWERED_DOWN; // timed out, force restart
			return TIMEOUT;
		}
		
		return STALE;
	}

}
