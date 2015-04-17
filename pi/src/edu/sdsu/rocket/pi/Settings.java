package edu.sdsu.rocket.pi;

import edu.sdsu.rocket.pi.io.radio.XTend900Config;

public class Settings {
	
	public boolean test;
	public boolean debug;
	public LoggingSettings logging;
	public ServerSettings server;
	public StatusSettings status;
	public DevicesSettings devices;

	public static class LoggingSettings {
		public String dateFormat;
		public String[] directories;
	}
	
	public static class ServerSettings {
		public int port;
	}
	
	public static class StatusSettings {
		public boolean enabled;
	}
	
	public static class DevicesSettings {
		public XTend900Settings xtend900;
		public ADXL345Settings adxl345;
		public ITG3205Settings itg3205;
		public HMC5883LSettings hmc5883l;
		public MS5611Settings ms5611;
		public ADS1115Settings ads1115;
		public GPSSettings gps;
	}
	
	public static class XTend900Settings {
		public boolean enabled;
		public String logFile;
		public boolean txLedEnabled;
		public boolean sendSensorData;
		public String device;
		public XTend900Config config;
	}
	
	public static class ADXL345Settings {
		public boolean enabled;
		public String logFile;
		public long throttle;
	}
	
	public static class ITG3205Settings {
		public boolean enabled;
		public String logFile;
		public long throttle;
	}
	
	public static class HMC5883LSettings {
		public boolean enabled;
		public String logFile;
	}
	
	public static class MS5611Settings {
		public boolean enabled;
		public String logFile;
		public long throttle;
	}
	
	public static class ADS1115Settings {
		public boolean enabled;
		public String logFile;
		public int[] sequence;
	}
	
	public static class GPSSettings {
		public boolean enabled;
		public String logFile;
		public String device;
		public GPSPosition local;
		public GPSPosition remote;
	}
	
	public static class GPSPosition {
		public double latitude;
		public double longitude;
		public double altitude;
	}
	
}
