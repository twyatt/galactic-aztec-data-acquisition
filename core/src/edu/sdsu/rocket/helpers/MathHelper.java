package edu.sdsu.rocket.helpers;

public class MathHelper {

	/**
	 * Translates a values from one range of numbers to another range.
	 * 
	 * http://stackoverflow.com/a/1969274/196486
	 * 
	 * @param value
	 * @param leftMin Lower bound of source range.
	 * @param leftMax Upper bound of source range.
	 * @param rightMin Lower bound of target range.
	 * @param rightMax Upper bound of target range.
	 * @return
	 */
	public static float translate(float value, float leftMin, float leftMax, float rightMin, float rightMax) {
		// figure out how 'wide' each range is
		float leftSpan = leftMax - leftMin;
		float rightSpan = rightMax - rightMin;
		
		// convert the left range into a 0-1 range (float)
		float valueScaled = (value - leftMin) / leftSpan;
		
		// convert the 0-1 range into a value in the right range
		return rightMin + (valueScaled * rightSpan);
	}
	
}
