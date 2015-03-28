package edu.sdsu.rocket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.math.Vector3;
import com.pi4j.io.i2c.I2CBus;

import edu.sdsu.rocket.io.Message;
import edu.sdsu.rocket.models.Analog;
import edu.sdsu.rocket.models.Barometer;
import edu.sdsu.rocket.models.GPS;
import edu.sdsu.rocket.models.Sensors;
import edu.sdsu.rocket.server.devices.ADS1115;
import edu.sdsu.rocket.server.devices.ADS1115Device;
import edu.sdsu.rocket.server.devices.ADXL345;
import edu.sdsu.rocket.server.devices.ADXL345Device;
import edu.sdsu.rocket.server.devices.AdafruitGPS;
import edu.sdsu.rocket.server.devices.ITG3205;
import edu.sdsu.rocket.server.devices.ITG3205Device;
import edu.sdsu.rocket.server.devices.ITG3205Device.GyroscopeListener;
import edu.sdsu.rocket.server.devices.MS5611;
import edu.sdsu.rocket.server.devices.MS5611Device;
import edu.sdsu.rocket.server.devices.MS5611Device.Fault;
import edu.sdsu.rocket.server.io.DataLogger;
import edu.sdsu.rocket.server.io.DatagramServer;
import edu.sdsu.rocket.server.io.TextLogger;

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
	private static final byte SCALING_FACTOR = 0x1; // log identifier
	
	private File logDir;
	
	protected TextLogger log;
	protected DataLogger logger;
	private final DeviceManager manager = new DeviceManager();
	private DatagramServer server;
	
	private final Reader input = new InputStreamReader(System.in);
	protected final Sensors sensors;
	
	private AdafruitGPS gps;
	
	private long start = System.nanoTime();
	private long loops;
	private long frequency; // Hz
	
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
		setupLogger();
		setupSensors();
		setupServer();
	}
	
	public void loop() throws IOException {
		loopServer();
		loopInput();
		
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		loops++;
		long time = System.nanoTime();
		if (time - start > 1000000000) {
			frequency = loops;
			loops = 0;
			start = time;
		}
	}
	
	protected void setupLogger() throws IOException {
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
		log = new TextLogger(logDir + FILE_SEPARATOR + EVENT_LOG);
		String message = "Logging started at " + logDateFormat.format(new Date(now)) + " (" + now + " ms since Unix Epoch).";
		Console.log(message);
		log.message(message);
		
		logger = new DataLogger(logDir);
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
		ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1);
		adxl345.setup();
		if (!adxl345.verifyDeviceID()) {
			throw new IOException("Failed to verify ADXL345 device ID");
		}
		adxl345.writeRange(ADXL345.ADXL345_RANGE_16G);
		adxl345.writeFullResolution(true);
		adxl345.writeRate(ADXL345.ADXL345_RATE_400);
		sensors.accelerometer.setScalingFactor(adxl345.getScalingFactor());
		logger.log(ADXL345_LOG, SCALING_FACTOR, sensors.accelerometer.getScalingFactor());
		
		ADXL345Device device = new ADXL345Device(adxl345);
		device.setListener(new ADXL345Device.AccelerometerListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.accelerometer.setRawX(x);
				sensors.accelerometer.setRawY(y);
				sensors.accelerometer.setRawZ(z);
				try {
					logger.log(ADXL345_LOG, x, y, z);
				} catch (FileNotFoundException e) {
					Console.error(e.getMessage());
				}
			}
		});
		manager.add(device, 100 /* Hz */);
	}

	private void setupITG3205() throws IOException, FileNotFoundException {
		Console.log("Setup ITG3205.");
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
		logger.log(ITG3205_LOG, SCALING_FACTOR, sensors.gyroscope.getScalingFactor());
		
		ITG3205Device device = new ITG3205Device(itg3205);
		device.setListener(new GyroscopeListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.gyroscope.setRawX(x);
				sensors.gyroscope.setRawY(y);
				sensors.gyroscope.setRawZ(z);
				try {
					logger.log(ITG3205_LOG, x, y, z);
				} catch (FileNotFoundException e) {
					Console.error(e.getMessage());
				}
			}
			
		});
		manager.add(device, 100 /* Hz */);
	}
	
	private void setupMS5611() throws IOException {
		Console.log("Setup MS5611.");
		MS5611 ms5611 = new MS5611(I2CBus.BUS_1);
		ms5611.setup();
		
		MS5611Device device = new MS5611Device(ms5611);
		device.setListener(new MS5611Device.BarometerListener() {
			@Override
			public void onValues(int T, int P) {
				sensors.barometer.setRawTemperature(T);
				sensors.barometer.setRawPressure(P);
				try {
					logger.log(MS5611_LOG, T, P);
				} catch (FileNotFoundException e) {
					Console.error(e.getMessage());
				}
			}

			@Override
			public void onFault(Fault fault) {
				Console.error("MS5611 error: " + fault);
			}
		});
		manager.add(device, 100 /* Hz */);
	}

	private void setupADS1115() throws IOException {
		Console.log("Setup ADS1115.");
		
		ADS1115 ads1115 = new ADS1115(I2CBus.BUS_1);
		ads1115.setup()
			.setGain(ADS1115.Gain.PGA_1)
			.setMode(ADS1115.Mode.MODE_SINGLE)
			.setRate(ADS1115.Rate.DR_860SPS)
			.setComparator(ADS1115.Comparator.COMP_MODE_HYSTERESIS)
			.setPolarity(ADS1115.Polarity.COMP_POL_ACTIVE_HIGH)
			.setLatching(ADS1115.Latching.COMP_LAT_LATCHING)
			.setQueue(ADS1115.Queue.COMP_QUE_1_CONVERSION)
			;
		int sps = ads1115.getRate().getSamplesPerSecond();
		
		ADS1115Device device = new ADS1115Device(ads1115);
		long timeout = (1L * SECONDS_TO_NANOSECONDS) / sps * 5L; // 5 X expected sample duration
		device.setTimeout(timeout);
		Console.log("ADS1115 timeout: " + timeout);
		device.setListener(new ADS1115Device.AnalogListener() {
			@Override
			public void onValues(float a0, float a1, float a2, float a3) {
				sensors.analog.setA0(a0);
				sensors.analog.setA1(a1);
				sensors.analog.setA2(a2);
				sensors.analog.setA3(a3);
				try {
					logger.log(ADS1115_LOG, a0, a1, a2, a3);
				} catch (FileNotFoundException e) {
					Console.error(e.getMessage());
				}
			}
		});
		
		manager.add(device);
	}
	
	private void setupGPS() throws FileNotFoundException {
		Console.log("Setup Adafruit Ultimate GPS.");
//		FileInputStream in = new FileInputStream("/dev/ttyUSB0"); // USB
		FileInputStream in = new FileInputStream("/dev/ttyAMA0"); // GPIO
		gps = new AdafruitGPS(in);
		gps.setOutputStream(new FileOutputStream(logDir + FILE_SEPARATOR + GPS_LOG));
		gps.setGPS(sensors.gps);
	}
	
	protected void setupServer() throws IOException {
		Console.log("Setup server.");
		server = new DatagramServer(SERVER_PORT);
		server.start();
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
	
	protected void loopServer() {
		Message message = server.read();
		if (message != null) {
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
	}

	protected void loopInput() throws IOException {
		if (input.ready()) {
			int c = input.read();
			switch (c) {
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
				Console.log("Main: " + frequency + " Hz, " + manager);
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
			case 'q':
			case 'Q':
				Console.log("Quitting.");
				manager.clear();
				System.exit(0);
			}
		}
	}
	
}
