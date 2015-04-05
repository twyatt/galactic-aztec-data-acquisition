package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.math.Vector3;

public class Gyroscope {
	
	private float scalingFactor = 1.0f;
	
	private final AtomicInteger rawX = new AtomicInteger();
	private final AtomicInteger rawY = new AtomicInteger();
	private final AtomicInteger rawZ = new AtomicInteger();

	public Gyroscope setScalingFactor(float scalingFactor) {
		this.scalingFactor = scalingFactor;
		return this;
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
	 * Sets the latest gyroscope values in the provided Vector3 object.
	 * 
	 * Units are in degrees/second.
	 * 
	 * @param v
	 */
	public void get(Vector3 v) {
		v.set(getRawX(), getRawY(), getRawZ()).scl(scalingFactor);
	}
	
}
