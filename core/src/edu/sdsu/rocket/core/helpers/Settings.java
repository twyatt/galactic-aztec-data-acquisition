package edu.sdsu.rocket.core.helpers;

public class Settings {
	
	public boolean test;
	public LoggingSettings logging;
	public ServerSettings server;
	public DevicesSettings devices;

	public static class LoggingSettings {
		public String dateFormat;
		public String[] directories;
	}
	
	public static class ServerSettings {
		public int port;
	}
	
	public static class DevicesSettings {
		public XTend900Settings xtend900;
		public ADXL345Settings adxl345;
		public ITG3205Settings itg3205;
		public MS5611Settings ms5611;
		public ADS1115Settings ads1115;
		public GPSSettings gps;
	}
	
	public static class XTend900Settings {
		public boolean enabled;
		public String mode;
		public long sleep; // thread sleep (milliseconds)
		public String device;
		public int baud;
		public int rfDataRate;
		public int txPowerLevel;
		public int transmitOnly;
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
	}
	
}
