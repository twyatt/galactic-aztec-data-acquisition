package edu.sdsu.rocket.data.models;

import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;

import edu.sdsu.rocket.data.helpers.MathHelper;

public class Sensors {
	
	public static final int MOTOR_INDEX    = 0;
	public static final int LOX_INDEX      = 1;
	public static final int KEROSENE_INDEX = 2;
	public static final int HELIUM_INDEX   = 3;
	
	public static final double MOTOR_MAX_PRESSURE    = 100;
	public static final double LOX_MAX_PRESSURE      = 600;
	public static final double KEROSENE_MAX_PRESSURE = 600;
	public static final double HELIUM_MAX_PRESSURE   = 2500;

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
	public void getGyroscope(Vector3 v) {
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
	
	public float getMotorPressure() {
		float volts = analog[MOTOR_INDEX] / 1000f;
		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) MOTOR_MAX_PRESSURE);
	}
	
	public float getLoxPressure() {
		float volts = analog[LOX_INDEX] / 1000f;
		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) LOX_MAX_PRESSURE);
	}
	
	public float getKerosenePressure() {
		float volts = analog[KEROSENE_INDEX] / 1000f;
		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) KEROSENE_MAX_PRESSURE);
	}
	
	public float getHeliumPressure() {
		float volts = analog[HELIUM_INDEX] / 1000f;
		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) HELIUM_MAX_PRESSURE);
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
