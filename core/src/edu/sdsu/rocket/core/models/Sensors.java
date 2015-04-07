package edu.sdsu.rocket.core.models;

import java.nio.ByteBuffer;

public class Sensors {
	
	public final Accelerometer accelerometer = new Accelerometer(); // Gs
	public final Gyroscope gyroscope = new Gyroscope(); // deg/sec
	public final Barometer barometer = new Barometer(); // C, mbar
	public final Analog analog = new Analog(); // mV
	public final Pressures pressures = new Pressures(analog); // PSI
	public final GPS gps = new GPS();
	
	public static final int ANALOG_MASK        = 0x1;
	public static final int BAROMETER_MASK     = 0x2;
	public static final int ACCELEROMETER_MASK = 0x4;
	public static final int GYROSCOPE_MASK     = 0x8;
	public static final int GPS_MASK           = 0x10;
	public static final int ALL_MASK           = 0xF;
	
	public void toByteBuffer(ByteBuffer buffer) {
		toByteBuffer(buffer, ALL_MASK);
	}
	
	public void toByteBuffer(ByteBuffer buffer, int mask) {
		if (mask == 0) mask = ALL_MASK;
		
		if ((mask & ANALOG_MASK) != 0) {
			buffer.putFloat(analog.getA0());
			buffer.putFloat(analog.getA1());
			buffer.putFloat(analog.getA2());
			buffer.putFloat(analog.getA3());
		}
		
		if ((mask & BAROMETER_MASK) != 0) {
			buffer.putInt(barometer.getRawTemperature());
			buffer.putInt(barometer.getRawPressure());
		}
		
		if ((mask & ACCELEROMETER_MASK) != 0) {
			buffer.putFloat(accelerometer.getScalingFactor());
			buffer.putShort((short) accelerometer.getRawX());
			buffer.putShort((short) accelerometer.getRawY());
			buffer.putShort((short) accelerometer.getRawZ());
		}
		
		if ((mask & GYROSCOPE_MASK) != 0) {
			buffer.putFloat(gyroscope.getScalingFactor());
			buffer.putShort((short) gyroscope.getRawX());
			buffer.putShort((short) gyroscope.getRawY());
			buffer.putShort((short) gyroscope.getRawZ());
		}
		
		if ((mask & GPS_MASK) != 0) {
			buffer.putDouble(gps.getLatitude());
			buffer.putDouble(gps.getLongitude());
			buffer.putDouble(gps.getAltitude());
		}
	}
	
	public void fromByteBuffer(ByteBuffer buffer) {
		fromByteBuffer(buffer, ALL_MASK);
	}
	
	public void fromByteBuffer(ByteBuffer buffer, int mask) {
		if (mask == 0) mask = ALL_MASK;
		
		if ((mask & ANALOG_MASK) != 0) {
			analog.setA0(buffer.getFloat());
			analog.setA1(buffer.getFloat());
			analog.setA2(buffer.getFloat());
			analog.setA3(buffer.getFloat());
		}
		
		if ((mask & BAROMETER_MASK) != 0) {
			barometer.setRawTemperature(buffer.getInt());
			barometer.setRawPressure(buffer.getInt());
		}
		
		if ((mask & ACCELEROMETER_MASK) != 0) {
			accelerometer.setScalingFactor(buffer.getFloat());
			accelerometer.setRawX(buffer.getShort());
			accelerometer.setRawY(buffer.getShort());
			accelerometer.setRawZ(buffer.getShort());
		}
		
		if ((mask & GYROSCOPE_MASK) != 0) {
			gyroscope.setScalingFactor(buffer.getFloat());
			gyroscope.setRawX(buffer.getShort());
			gyroscope.setRawY(buffer.getShort());
			gyroscope.setRawZ(buffer.getShort());
		}
		
		if ((mask & GPS_MASK) != 0) {
			gps.setLatitude(buffer.getDouble());
			gps.setLongitude(buffer.getDouble());
			gps.setAltitude(buffer.getDouble());
		}
	}

}
