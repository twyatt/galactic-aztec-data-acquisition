package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Status {

	private final AtomicInteger temperature = new AtomicInteger();
	private final AtomicBoolean isPowerGood = new AtomicBoolean();
	
	public void setRawTemperature(int temperature) {
		this.temperature.set(temperature);
	}
	
	public int getRawTemperature() {
		return temperature.get();
	}
	
	public void setIsPowerGood(boolean isPowerGood) {
		this.isPowerGood.set(isPowerGood);
	}
	
	public boolean getIsPowerGood() {
		return isPowerGood.get();
	}
	
	/**
	 * System temperature (in degrees C).
	 * 
	 * @return
	 */
	public float getTemperatureC() {
		return getRawTemperature() / 1000f;
	}
	
	/**
	 * System temperature (in degrees F).
	 * 
	 * @return
	 */
	public float getTemperatureF() {
		return getTemperatureC() * 9f / 5f + 32f;
	}
	
}
