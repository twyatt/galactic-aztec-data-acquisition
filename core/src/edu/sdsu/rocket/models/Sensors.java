package edu.sdsu.rocket.models;

import java.nio.ByteBuffer;

public class Sensors {
	
	public final Accelerometer accelerometer = new Accelerometer(); // Gs
	public final Gyroscope gyroscope = new Gyroscope(); // deg/sec
	public final Barometer barometer = new Barometer(); // C, mbar
	public final Analog analog = new Analog(); // mV
	public final Pressures pressures = new Pressures(analog); // PSI
	public final GPS gps = new GPS();
	
	public void toByteBuffer(ByteBuffer buffer) {
		buffer.putFloat(analog.getA0());
		buffer.putFloat(analog.getA1());
		buffer.putFloat(analog.getA2());
		buffer.putFloat(analog.getA3());
		
		buffer.putInt(barometer.getRawTemperature());
		buffer.putInt(barometer.getRawPressure());
		
		buffer.putFloat(accelerometer.getScalingFactor());
		buffer.putShort((short) accelerometer.getRawX());
		buffer.putShort((short) accelerometer.getRawY());
		buffer.putShort((short) accelerometer.getRawZ());
		
		buffer.putFloat(gyroscope.getScalingFactor());
		buffer.putShort((short) gyroscope.getRawX());
		buffer.putShort((short) gyroscope.getRawY());
		buffer.putShort((short) gyroscope.getRawZ());
		
		buffer.putDouble(gps.getLatitude());
		buffer.putDouble(gps.getLongitude());
		buffer.putDouble(gps.getAltitude());
	}
	
	public void fromByteBuffer(ByteBuffer buffer) {
		analog.setA0(buffer.getFloat());
		analog.setA1(buffer.getFloat());
		analog.setA2(buffer.getFloat());
		analog.setA3(buffer.getFloat());
		
		barometer.setRawTemperature(buffer.getInt());
		barometer.setRawPressure(buffer.getInt());
		
		accelerometer.setScalingFactor(buffer.getFloat());
		accelerometer.setRawX(buffer.getShort());
		accelerometer.setRawY(buffer.getShort());
		accelerometer.setRawZ(buffer.getShort());
		
		gyroscope.setScalingFactor(buffer.getFloat());
		gyroscope.setRawX(buffer.getShort());
		gyroscope.setRawY(buffer.getShort());
		gyroscope.setRawZ(buffer.getShort());
		
		gps.setLatitude(buffer.getDouble());
		gps.setLongitude(buffer.getDouble());
		gps.setAltitude(buffer.getDouble());
	}

}
