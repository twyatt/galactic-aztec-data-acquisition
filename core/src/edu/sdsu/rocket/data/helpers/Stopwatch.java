package edu.sdsu.rocket.data.helpers;

public class Stopwatch {

	private static long start = System.nanoTime();
	
	public static void reset() {
		start = System.nanoTime();
	}
	
	/**
	 * Returns number of seconds since reset() was called.
	 * 
	 * @return Time elapsed since reset (seconds).
	 */
	public static float secondsElapsed() {
		return nanoSecondsElapsed() / 1000000000f;
	}
	
	/**
	 * Returns number of nanoseconds since reset() was called.
	 * 
	 * @return Time elapsed since reset (nanoseconds).
	 */
	public static long nanoSecondsElapsed() {
		return System.nanoTime() - start;
	}
	
}
