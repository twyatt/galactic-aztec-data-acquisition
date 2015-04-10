package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

public class GPS {

	private final AtomicInteger fixStatus  = new AtomicInteger(1);
	private final AtomicInteger satellites = new AtomicInteger();
	private final AtomicDouble latitude    = new AtomicDouble();
	private final AtomicDouble longitude   = new AtomicDouble();
	private final AtomicDouble altitude    = new AtomicDouble();
	
	public void setFixStatus(int fixStatus) {
		this.fixStatus.set(fixStatus);
	}
	
	public int getFixStatus() {
		return fixStatus.get();
	}
	
	public void setSatellites(int satellites) {
		this.satellites.set(satellites);
	}
	
	public int getSatellites() {
		return satellites.get();
	}
	
	public void setLatitude(double latitude) {
		this.latitude.set(latitude);
	}
	
	public double getLatitude() {
		return latitude.get();
	}

	public void setLongitude(double longitude) {
		this.longitude.set(longitude);
	}
	
	public double getLongitude() {
		return longitude.get();
	}

	public void setAltitude(double altitude) {
		this.altitude.set(altitude);
	}
	
	public double getAltitude() {
		return altitude.get();
	}

	public void set(double latitude, double longitude, double altitude) {
		setLatitude(latitude);
		setLongitude(longitude);
		setAltitude(altitude);
	}
	
}
