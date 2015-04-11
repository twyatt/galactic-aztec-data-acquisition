package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicInteger;

public class Status {

	private final AtomicInteger temperature = new AtomicInteger();
	
	public void setRawTemperature(int temperature) {
		this.temperature.set(temperature);
	}
	
	public int getRawTemperature() {
		return temperature.get();
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
