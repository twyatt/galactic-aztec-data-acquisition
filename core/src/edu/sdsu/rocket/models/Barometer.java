package edu.sdsu.rocket.models;

import java.util.concurrent.atomic.AtomicInteger;

public class Barometer {

	private final AtomicInteger rawTemperature = new AtomicInteger();
	private final AtomicInteger rawPressure = new AtomicInteger();

	public void setRawTemperature(int value) {
		this.rawTemperature.set(value);
	}
	
	public int getRawTemperature() {
		return rawTemperature.get();
	}
	
	public void setRawPressure(int value) {
		this.rawPressure.set(value);
	}
	
	public int getRawPressure() {
		return rawPressure.get();
	}
	
	/**
	 * Returns the latest barometer temperature reading.
	 * 
	 * @return Barometer temperature (C)
	 */
	public float getTemperature() {
		return (float) getRawTemperature() / 100f;
	}
	
	/**
	 * Returns the latest barometer pressure.
	 * 
	 * @return Barometer pressure (mbar)
	 */
	public float getPressure() {
		return (float) getRawPressure() / 100f;
	}
	
}
