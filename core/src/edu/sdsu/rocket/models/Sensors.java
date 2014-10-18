package edu.sdsu.rocket.models;

import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;

import edu.sdsu.rocket.helpers.MathHelper;

public class Sensors {
	
	public static final int BAROMETER_TEMPERATURE_INDEX = 0;
	public static final int BAROMETER_PRESSURE_INDEX    = 1;
	
	public static final int X_INDEX = 0;
	public static final int Y_INDEX = 1;
	public static final int Z_INDEX = 2;
	
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
	
	public final short[] accelerometer = new short[3]; // G * scale
	public final short[] gyroscope = new short[3]; // deg/sec * scale
	public final int[] barometer = new int[2]; // C * 100, mbar * 100
	public final float[] analog = new float[4]; // mV
	
	public void setAccelerometerScalingFactor(float scale) {
		accelerometerScalingFactor = scale;
	}
	
	public float getAccelerometerScalingFactor() {
		return accelerometerScalingFactor;
	}
	
	public void setGyroscopeScalingFactor(float scale) {
		gyroscopeScalingFactor = scale;
	}
	
	public float getGyroscopeScalingFactor() {
		return gyroscopeScalingFactor;
	}
	
	/**
	 * Sets the latest accelerometer values in the provided Vector3 object.
	 * 
	 * Units are in G's (1 G = 9.8 m/s^2).
	 * 
	 * @param v
	 */
	public void getAccelerometer(Vector3 v) {
		v.set(accelerometer[X_INDEX], accelerometer[Y_INDEX], accelerometer[Z_INDEX])
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
		v.set(gyroscope[X_INDEX], gyroscope[Y_INDEX], gyroscope[Z_INDEX])
			.scl(gyroscopeScalingFactor);
	}
	
	/**
	 * Returns the latest barometer temperature reading.
	 * 
	 * @return Barometer temperature (C)
	 */
	public float getBarometerTemperature() {
		return (float) barometer[BAROMETER_TEMPERATURE_INDEX] / 100f;
	}
	
	/**
	 * Returns the latest barometer pressure.
	 * 
	 * @return Barometer pressure (mbar)
	 */
	public float getBarometerPressure() {
		return (float) barometer[BAROMETER_PRESSURE_INDEX] / 100f;
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
		buffer.putFloat(analog[MOTOR_INDEX]);
		buffer.putFloat(analog[LOX_INDEX]);
		buffer.putFloat(analog[KEROSENE_INDEX]);
		buffer.putFloat(analog[HELIUM_INDEX]);
		
		buffer.putInt(barometer[BAROMETER_TEMPERATURE_INDEX]);
		buffer.putInt(barometer[BAROMETER_PRESSURE_INDEX]);
		
		buffer.putFloat(accelerometerScalingFactor);
		buffer.putShort(accelerometer[X_INDEX]);
		buffer.putShort(accelerometer[Y_INDEX]);
		buffer.putShort(accelerometer[Z_INDEX]);
		
		buffer.putFloat(gyroscopeScalingFactor);
		buffer.putShort(gyroscope[X_INDEX]);
		buffer.putShort(gyroscope[Y_INDEX]);
		buffer.putShort(gyroscope[Z_INDEX]);
	}
	
	public void fromByteBuffer(ByteBuffer buffer) {
		analog[MOTOR_INDEX]    = buffer.getFloat();
		analog[LOX_INDEX]      = buffer.getFloat();
		analog[KEROSENE_INDEX] = buffer.getFloat();
		analog[HELIUM_INDEX]   = buffer.getFloat();
		
		barometer[BAROMETER_TEMPERATURE_INDEX] = buffer.getInt();
		barometer[BAROMETER_PRESSURE_INDEX]    = buffer.getInt();
		
		accelerometerScalingFactor = buffer.getFloat();
		accelerometer[X_INDEX] = buffer.getShort();
		accelerometer[Y_INDEX] = buffer.getShort();
		accelerometer[Z_INDEX] = buffer.getShort();
		
		gyroscopeScalingFactor = buffer.getFloat();
		gyroscope[X_INDEX] = buffer.getShort();
		gyroscope[Y_INDEX] = buffer.getShort();
		gyroscope[Z_INDEX] = buffer.getShort();
	}

}
