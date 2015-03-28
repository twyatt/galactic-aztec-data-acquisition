package edu.sdsu.rocket.models;

import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;

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
	public final Analog analog = new Analog(); // mV
	public final GPS gps = new GPS();
	
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
//		float volts = analog[MOTOR_INDEX] / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) MOTOR_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #1 on Nov 13, 2014
		return 0.210439f * analog.get(MOTOR_INDEX) - 150.502f;
	}
	
	public float getLoxPressure() {
//		float volts = analog[LOX_INDEX] / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) LOX_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #2 on Nov 11, 2014
		return 0.20688f * analog.get(LOX_INDEX) - 143.273f;
	}
	
	public float getKerosenePressure() {
//		float volts = analog[KEROSENE_INDEX] / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) KEROSENE_MAX_PRESSURE);
		
		// P51-500-A-A-I36-5V-000-000
		// calibrated transducer #3 on Nov 11, 2014
		return 0.212968f * analog.get(KEROSENE_INDEX) - 147.109f;
	}
	
	public float getHeliumPressure() {
//		float volts = analog[HELIUM_INDEX] / 1000f;
//		return MathHelper.translate(volts, 0f, 3.3f, 0f, (float) HELIUM_MAX_PRESSURE);
		
		// MSP-300-2K5-P-4-N-1
		// calibrated transducer #4 on Nov 13, 2014
		return 1.060797f * analog.get(HELIUM_INDEX) - 653.691f;
	}
	
	public void toByteBuffer(ByteBuffer buffer) {
		buffer.putFloat(analog.get(MOTOR_INDEX));
		buffer.putFloat(analog.get(LOX_INDEX));
		buffer.putFloat(analog.get(KEROSENE_INDEX));
		buffer.putFloat(analog.get(HELIUM_INDEX));
		
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
		
		buffer.putDouble(gps.getLatitude());
		buffer.putDouble(gps.getLongitude());
		buffer.putDouble(gps.getAltitude());
	}
	
	public void fromByteBuffer(ByteBuffer buffer) {
		analog.set(MOTOR_INDEX,    buffer.getFloat());
		analog.set(LOX_INDEX,      buffer.getFloat());
		analog.set(KEROSENE_INDEX, buffer.getFloat());
		analog.set(HELIUM_INDEX,   buffer.getFloat());
		
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
		
		gps.set(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
	}

}
