package edu.sdsu.rocket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.marineapi.provider.event.PositionEvent;
import net.sf.marineapi.provider.event.ProviderListener;

import com.badlogic.gdx.math.Vector3;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import edu.sdsu.rocket.helpers.Console;
import edu.sdsu.rocket.io.ADS1115OutputStream;
import edu.sdsu.rocket.io.ADXL345OutputStream;
import edu.sdsu.rocket.io.ITG3205OutputStream;
import edu.sdsu.rocket.io.MS5611OutputStream;
import edu.sdsu.rocket.io.Message;
import edu.sdsu.rocket.io.MessageHandler;
import edu.sdsu.rocket.models.Analog;
import edu.sdsu.rocket.models.Barometer;
import edu.sdsu.rocket.models.GPS;
import edu.sdsu.rocket.models.Sensors;
import edu.sdsu.rocket.server.devices.ADS1115;
import edu.sdsu.rocket.server.devices.ADS1115Device;
import edu.sdsu.rocket.server.devices.ADS1115Device.Channel;
import edu.sdsu.rocket.server.devices.ADXL345;
import edu.sdsu.rocket.server.devices.ADXL345Device;
import edu.sdsu.rocket.server.devices.AdafruitGPS;
import edu.sdsu.rocket.server.devices.ITG3205;
import edu.sdsu.rocket.server.devices.ITG3205Device;
import edu.sdsu.rocket.server.devices.ITG3205Device.GyroscopeListener;
import edu.sdsu.rocket.server.devices.MS5611;
import edu.sdsu.rocket.server.devices.MS5611Device;
import edu.sdsu.rocket.server.devices.MS5611Device.Fault;
import edu.sdsu.rocket.server.devices.XTend900Device;
import edu.sdsu.rocket.server.devices.XTend900Device.NumberBase;
import edu.sdsu.rocket.server.devices.XTend900Device.RFDataRate;
import edu.sdsu.rocket.server.devices.XTend900Device.TXPowerLevel;
import edu.sdsu.rocket.server.io.DatagramServer;

public class Application {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final long SECONDS_TO_NANOSECONDS = 1000000000L;
	
	private static final int SERVER_PORT = 4444;
	private static final int BUFFER_SIZE = 128; // bytes
	
	protected static final String EVENT_LOG   = "event.log";
	protected static final String ADXL345_LOG = "adxl345.log";
	protected static final String ITG3205_LOG = "itg3205.log";
	protected static final String MS5611_LOG  = "ms5611.log";
	protected static final String ADS1115_LOG = "ads1115.log";
	protected static final String GPS_LOG     = "gps.txt";
	
	protected File logDir;
	protected PrintWriter log;
	protected ADXL345OutputStream adxl345log;
	protected ITG3205OutputStream itg3205log;
	protected MS5611OutputStream ms5611log;
	protected ADS1115OutputStream ads1115log;
	
	private final DeviceManager manager = new DeviceManager();
	private DatagramServer server;
	
	private final Reader input = new InputStreamReader(System.in);
	protected final Sensors sensors;
	
	private XTend900Device radio;
	private AdafruitGPS gps;
	
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final Vector3 tmpVec = new Vector3();
	
	public Application(Sensors sensors) {
		Console.log("Starting application.");
		if (sensors == null) {
			throw new NullPointerException();
		}
		this.sensors = sensors;
	}
	
	public void setup() throws IOException {
		setupLogging();
		setupRadio();
		setupSensors();
		setupServer();
	}

	public void loop() throws IOException {
		switch (input.read()) {
		case '?':
			Console.log();
			Console.log("?: help");
			Console.log("t: cpu temperature");
			Console.log("f: loop frequency");
			Console.log("a: accelerometer");
			Console.log("y: gyroscope");
			Console.log("b: barometer");
			Console.log("c: analog");
			Console.log("g: gps");
			Console.log("r: toggle radio (currently " + (radio != null && radio.isOn() ? "ON" : "OFF") + ")");
			Console.log("q: quit");
			Console.log();
			break;
		case 't':
		case 'T':
			try {
				float tempC = Pi.getCpuTemperature();
				float tempF = tempC * 9f / 5f + 32f;
				Console.log("CPU: " + tempC + " C, " + tempF + " F");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 'f':
		case 'F':
			Console.log(manager.toString());
			break;
		case 'a':
		case 'A':
			sensors.accelerometer.get(tmpVec);
			Console.log(tmpVec.scl(9.8f) + " m/s^2");
			break;
		case 'b':
		case 'B':
			Barometer barometer = sensors.barometer;
			Console.log(barometer.getTemperature() + " C, " + barometer.getPressure() + " mbar");
			break;
		case 'y':
		case 'Y':
			sensors.gyroscope.get(tmpVec);
			Console.log(tmpVec + " deg/s");
			break;
		case 'c':
		case 'C':
			Analog a = sensors.analog;
			Console.log("A0=" + a.getA0() + " mV,\tA1=" + a.getA1() + " mV,\tA2=" + a.getA2() + " mV,\tA3=" + a.getA3() + " mV");
			break;
		case 'g':
		case 'G':
			GPS gps = sensors.gps;
			Console.log("latitude=" + gps.getLatitude() + ", longitude=" + gps.getLongitude() + ", altitude=" + gps.getAltitude() + " m MSL");
			break;
		case 'r':
		case 'R':
			if (radio != null) {
				radio.toggle();
				Console.log("Radio is now " + (radio.isOn() ? "ON" : "OFF") + ".");
			}
			break;
		case 'q':
		case 'Q':
			shutdown();
			break;
		}
	}
	
	protected void setupLogging() throws IOException {
		Console.log("Setup Logging.");
		
		File userDir = new File(System.getProperty("user.dir", "~"));
		if (!userDir.exists()) {
			throw new IOException("Directory does not exist: " + userDir);
		}
		
		DateFormat dirDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = dirDateFormat.format(new Date());
		
		logDir = new File(userDir + FILE_SEPARATOR + "logs" + FILE_SEPARATOR + timestamp);
		Console.log("Log Directory: " + logDir);
		if (!logDir.exists()) {
			Console.log("mkdir " + logDir);
			logDir.mkdirs();
		}
		
		long now = System.currentTimeMillis();
		DateFormat logDateFormat = DateFormat.getDateInstance(DateFormat.LONG);
		log = new PrintWriter(logDir + FILE_SEPARATOR + EVENT_LOG);
		String message = "Application started at " + logDateFormat.format(new Date(now)) + " (" + now + " ms since Unix Epoch).";
		Console.log(message);
		log.println(message);
	}
	
	private void setupRadio() throws IOException {
		Console.log("Setup XTend900.");
		Serial serial = SerialFactory.createInstance();
		SerialConfig config = new SerialConfig();
		config.baud(Baud._9600);
		serial.open(config);
		
		// setup serial listener to see command responses
		SerialDataEventListener listener = new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					Console.log(event.getAsciiString());
				} catch (IOException e) {
					Console.error(e);
				}
			}
		};
		serial.addListener(listener);
		
		radio = new XTend900Device(serial, sensors);
		try {
			radio
				.enterATCommandMode()
				.writeNumberBase(NumberBase.DEFAULT_WITH_UNITS)
				.requestBoardVoltage()
				.requestHardwareVersion()
				.writeRFDataRate(RFDataRate.BAUD_115200)
				.writeTXPowerLevel(TXPowerLevel.TX_1000mW)
				
				// point-to-multipoint: remotes (pg 44)
				.writeAutosetMY()
				.writeDestinationAddress("0") // 0x00
				
				.exitATCommandMode()
				;
		} catch (IllegalStateException e) {
			Console.error(e);
			throw new IOException(e);
		} catch (InterruptedException e) {
			Console.error(e);
			throw new IOException(e);
		}
		
		// clean up serial listener
		serial.removeListener(listener);
		
		manager.add(radio)
			.setSleep(100L /* ms */); // 100 ms = 10 Hz
	}
	
	protected void setupSensors() throws IOException {
		setupADXL345();
		setupITG3205();
		setupMS5611();
		setupADS1115();
//		setupGPS();
	}
	
	private void setupADXL345() throws IOException {
		Console.log("Setup ADXL345.");
		
		String file = logDir + FILE_SEPARATOR + ADXL345_LOG;
		Console.log("Log: " + file);
		adxl345log = new ADXL345OutputStream(new FileOutputStream(file));
		
		ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1);
		adxl345.setup();
		if (!adxl345.verifyDeviceID()) {
			throw new IOException("Failed to verify ADXL345 device ID");
		}
		adxl345.writeRange(ADXL345.ADXL345_RANGE_16G);
		adxl345.writeFullResolution(true);
		adxl345.writeRate(ADXL345.ADXL345_RATE_400);
		float scalingFactor = adxl345.getScalingFactor();
		sensors.accelerometer.setScalingFactor(scalingFactor);
		adxl345log.writeScalingFactor(scalingFactor);
		Console.log("Scaling Factor: " + scalingFactor);
		
		ADXL345Device device = new ADXL345Device(adxl345);
		device.setListener(new ADXL345Device.AccelerometerListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.accelerometer.setRawX(x);
				sensors.accelerometer.setRawY(y);
				sensors.accelerometer.setRawZ(z);
				try {
					adxl345log.writeValues(x, y, z);
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
			}
		});
		manager.add(device)
			.setThrottle(100 /* Hz */);
	}

	private void setupITG3205() throws IOException, FileNotFoundException {
		Console.log("Setup ITG3205.");
		
		String file = logDir + FILE_SEPARATOR + ADXL345_LOG;
		Console.log("Log: " + file);
		itg3205log = new ITG3205OutputStream(new FileOutputStream(file));
		
		ITG3205 itg3205 = new ITG3205(I2CBus.BUS_1);
		itg3205.setup();
		if (!itg3205.verifyDeviceID()) {
			throw new IOException("Failed to verify ITG3205 device ID");
		}
		// F_sample = F_internal / (divider + 1)
		// divider = F_internal / F_sample - 1
		itg3205.writeSampleRateDivider(2); // 2667 Hz
		itg3205.writeDLPFBandwidth(ITG3205.ITG3205_DLPF_BW_256);
		
		sensors.gyroscope.setScalingFactor(1f / ITG3205.ITG3205_SENSITIVITY_SCALE_FACTOR);
		itg3205log.writeScalingFactor(sensors.gyroscope.getScalingFactor());
		
		ITG3205Device device = new ITG3205Device(itg3205);
		device.setListener(new GyroscopeListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.gyroscope.setRawX(x);
				sensors.gyroscope.setRawY(y);
				sensors.gyroscope.setRawZ(z);
				try {
					itg3205log.writeValues(x, y, z);
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
			}
			
		});
		manager.add(device)
			.setThrottle(100 /* Hz */);
	}
	
	private void setupMS5611() throws IOException {
		Console.log("Setup MS5611.");
		
		String file = logDir + FILE_SEPARATOR + ADXL345_LOG;
		Console.log("Log: " + file);
		ms5611log = new MS5611OutputStream(new FileOutputStream(file));
		
		MS5611 ms5611 = new MS5611(I2CBus.BUS_1);
		ms5611.setup();
		
		MS5611Device device = new MS5611Device(ms5611);
		device.setListener(new MS5611Device.BarometerListener() {
			@Override
			public void onValues(int T, int P) {
				sensors.barometer.setRawTemperature(T);
				sensors.barometer.setRawPressure(P);
				try {
					ms5611log.writeValues(T, P);
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
			}

			@Override
			public void onFault(Fault fault) {
				try {
					ms5611log.writeFault(fault.ordinal());
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
				Console.error("MS5611 fault: " + fault);
			}
		});
		
		manager.add(device)
			.setThrottle(100 /* Hz */);
	}

	private void setupADS1115() throws IOException {
		Console.log("Setup ADS1115.");
		
		String file = logDir + FILE_SEPARATOR + ADXL345_LOG;
		Console.log("Log: " + file);
		ads1115log = new ADS1115OutputStream(new FileOutputStream(file));
		
		ADS1115 ads1115 = new ADS1115(I2CBus.BUS_1);
		ads1115.setup()
			.setGain(ADS1115.Gain.PGA_1)
			.setMode(ADS1115.Mode.MODE_SINGLE)
			.setRate(ADS1115.Rate.DR_860SPS)
			.setComparator(ADS1115.Comparator.COMP_MODE_HYSTERESIS)
//			.setPolarity(ADS1115.Polarity.COMP_POL_ACTIVE_HIGH)
//			.setLatching(ADS1115.Latching.COMP_LAT_LATCHING)
//			.setQueue(ADS1115.Queue.COMP_QUE_1_CONVERSION)
			;
		int sps = ads1115.getRate().getSamplesPerSecond();
		
		ADS1115Device device = new ADS1115Device(ads1115);
		long timeout = (1L * SECONDS_TO_NANOSECONDS) / sps * 5L; // 5 X expected sample duration
		device.setTimeout(timeout);
		Console.log("ADS1115 timeout: " + timeout);
		device.setListener(new ADS1115Device.AnalogListener() {
			@Override
			public void onValue(Channel channel, float value) {
				sensors.analog.set(channel.ordinal(), value);
				try {
					ads1115log.writeValue(channel.ordinal(), value);
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
			}
		});
		
		manager.add(device);
	}
	
	private void setupGPS() throws FileNotFoundException {
		Console.log("Setup Adafruit Ultimate GPS.");
		
		String source = "/dev/ttyUSB0"; // USB
//		String source = "/dev/ttyAMA0"; // GPIO
		Console.log("Source: " + source);
		String file = logDir + FILE_SEPARATOR + GPS_LOG;
		Console.log("Log: " + file);
		
		FileInputStream in = new FileInputStream(source);
		gps = new AdafruitGPS(in);
		gps.setOutputStream(new FileOutputStream(file));
		gps.setGPS(sensors.gps);
		gps.getPositionProvider().addListener(new ProviderListener<PositionEvent>() {
			@Override
			public void providerUpdate(PositionEvent event) {
				double latitude  = event.getPosition().getLatitude();
				double longitude = event.getPosition().getLongitude();
				double altitude  = event.getPosition().getAltitude();
				sensors.gps.set(latitude, longitude, altitude);
			}
		});
	}
	
	protected void setupServer() throws IOException {
		Console.log("Setup server.");
		server = new DatagramServer(SERVER_PORT);
		server.start(new MessageHandler.MessageListener() {
			@Override
			public void onMessageReceived(Message message) {
				try {
					switch (message.id) {
					case Message.PING:
						// FIXME implement
						break;
					case Message.SENSOR:
						sendSensorData(message.address, message.number);
						break;
					}
				} catch (IOException e) {
					Console.error(e.getMessage());
				}
			}
		});
	}

	/**
	 * Sends current sensor data to specified address.
	 * 
	 * @param address
	 * @param number 
	 * @throws IOException 
	 */
	public void sendSensorData(SocketAddress address, int number) throws IOException {
		DatagramSocket socket = server.getSocket();
		if (socket != null) {
			buffer.clear();
			buffer.putInt(number);
			buffer.put(Message.SENSOR);
			sensors.toByteBuffer(buffer);
			
			byte[] buf = buffer.array();
			int length = buffer.position();
			DatagramPacket packet = new DatagramPacket(buf, length, address);
			
			socket.send(packet);
		}
	}

	private void shutdown() {
		Console.log("Shutting down.");
		
		Console.log("Stopping server.");
		server.stop();
		
		Console.log("Stopping device manager.");
		manager.clear();
		
		Console.log("Closing log streams.");
		try {
			adxl345log.close();
			itg3205log.close();
			ms5611log.close();
			ads1115log.close();
		} catch (IOException e) {
			Console.error(e.getMessage());
		}
		log.close();
		
		System.exit(0);
	}
	
}
