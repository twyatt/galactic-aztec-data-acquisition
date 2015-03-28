package edu.sdsu.rocket.models;

import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.math.Vector3;

public class Accelerometer {

	private float scalingFactor = 1.0f;
	
	private final AtomicInteger rawX = new AtomicInteger();
	private final AtomicInteger rawY = new AtomicInteger();
	private final AtomicInteger rawZ = new AtomicInteger();
	
	public void setScalingFactor(float scalingFactor) {
		this.scalingFactor = scalingFactor;
	}
	
	public float getScalingFactor() {
		return scalingFactor;
	}
	
	public void setRawX(int value) {
		this.rawX.set(value);
	}
	
	public int getRawX() {
		return rawX.get();
	}
	
	public void setRawY(int value) {
		this.rawY.set(value);
	}
	
	public int getRawY() {
		return rawY.get();
	}
	
	public void setRawZ(int value) {
		this.rawZ.set(value);
	}
	
	public int getRawZ() {
		return rawZ.get();
	}
	
	/**
	 * Sets the latest accelerometer values in the provided Vector3 object.
	 * 
	 * Units are in G's (1 G = 9.8 m/s^2).
	 * 
	 * @param v
	 */
	public void get(Vector3 v) {
		v.set(getRawX(), getRawY(), getRawZ()).scl(scalingFactor);
	}
	
}
