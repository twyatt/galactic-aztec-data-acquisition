package edu.sdsu.rocket.data.models;

import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;

public class Sensors {

	private float accelerometerScalingFactor;
	private float gyroscopeScalingFactor;
	
	public final short[] accelerometer = new short[3];
	public final short[] gyroscope = new short[3];
	public final int[] barometer = new int[2];
	public final float[] analog = new float[4]; // mV
	
	public void setAccelerometerScalingFactor(float scale) {
		accelerometerScalingFactor = scale;
	}
	
	public void setGryroscopeScalingFactor(float scale) {
		gyroscopeScalingFactor = scale;
	}
	
	/**
	 * Sets the latest accelerometer values in the provided Vector3 object.
	 * 
	 * Units are in G's (1 G = 9.8 m/s^2).
	 * 
	 * @param v
	 */
	public void getAccelerometer(Vector3 v) {
		v.set(accelerometer[0], accelerometer[1], accelerometer[2])
			.scl(accelerometerScalingFactor);
	}
	
	/**
	 * Sets the latest gyroscope values in the provided Vector3 object.
	 * 
	 * Units are in degrees/second.
	 * 
	 * @param v
	 */
	public void getGryoscope(Vector3 v) {
		v.set(gyroscope[0], gyroscope[1], gyroscope[2])
			.scl(gyroscopeScalingFactor);
	}
	
	/**
	 * Returns the latest barometer temperature reading.
	 * 
	 * @return Barometer temperature (C)
	 */
	public float getBarometerTemperature() {
		return (float) barometer[0] / 100f;
	}
	
	/**
	 * Returns the latest barometer pressure.
	 * 
	 * @return Barometer pressure (mbar)
	 */
	public float getBarometerPressure() {
		return (float) barometer[1] / 100f;
	}
	
	public void toByteBuffer(ByteBuffer buffer) {
		buffer.putFloat(analog[0]);
		buffer.putFloat(analog[1]);
		buffer.putFloat(analog[2]);
		buffer.putFloat(analog[3]);
		buffer.putInt(barometer[0]);
		buffer.putInt(barometer[1]);
		buffer.putShort(accelerometer[0]);
		buffer.putShort(accelerometer[1]);
		buffer.putShort(accelerometer[2]);
		buffer.putFloat(accelerometerScalingFactor);
		buffer.putShort(gyroscope[0]);
		buffer.putShort(gyroscope[1]);
		buffer.putShort(gyroscope[2]);
		buffer.putFloat(gyroscopeScalingFactor);
	}
	
	public void fromByteBuffer(ByteBuffer buffer) {
		analog[0] = buffer.getFloat();
		analog[1] = buffer.getFloat();
		analog[2] = buffer.getFloat();
		analog[3] = buffer.getFloat();
		barometer[0] = buffer.getInt();
		barometer[1] = buffer.getInt();
		accelerometer[0] = buffer.getShort();
		accelerometer[1] = buffer.getShort();
		accelerometer[2] = buffer.getShort();
		accelerometerScalingFactor = buffer.getFloat();
		gyroscope[0] = buffer.getShort();
		gyroscope[1] = buffer.getShort();
		gyroscope[2] = buffer.getShort();
		gyroscopeScalingFactor = buffer.getFloat();
	}

}
