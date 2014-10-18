package edu.sdsu.rocket.data.helpers;

public class Stopwatch {

	private long start;
	
	public Stopwatch() {
		reset();
	}
	
	public void reset() {
		start = System.nanoTime();
	}
	
	/**
	 * Returns number of seconds since reset() was called.
	 * 
	 * @return Time elapsed since reset (seconds).
	 */
	public float secondsElapsed() {
		return nanoSecondsElapsed() / 1000000000f;
	}
	
	/**
	 * Returns number of nanoseconds since reset() was called.
	 * 
	 * @return Time elapsed since reset (nanoseconds).
	 */
	public long nanoSecondsElapsed() {
		return System.nanoTime() - start;
	}
	
}
