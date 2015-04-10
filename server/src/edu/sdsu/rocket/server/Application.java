package edu.sdsu.rocket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.provider.PositionProvider;
import net.sf.marineapi.provider.SatelliteInfoProvider;
import net.sf.marineapi.provider.event.PositionEvent;
import net.sf.marineapi.provider.event.ProviderListener;
import net.sf.marineapi.provider.event.SatelliteInfoEvent;
import net.sf.marineapi.provider.event.SatelliteInfoListener;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import edu.sdsu.rocket.core.io.ADS1115OutputStream;
import edu.sdsu.rocket.core.io.ADXL345OutputStream;
import edu.sdsu.rocket.core.io.ITG3205OutputStream;
import edu.sdsu.rocket.core.io.MS5611OutputStream;
import edu.sdsu.rocket.core.io.OutputStreamMultiplexer;
import edu.sdsu.rocket.core.models.Sensors;
import edu.sdsu.rocket.core.net.SensorServer;
import edu.sdsu.rocket.pi.Logging;
import edu.sdsu.rocket.pi.Pi;
import edu.sdsu.rocket.pi.Settings;
import edu.sdsu.rocket.pi.devices.ADS1115;
import edu.sdsu.rocket.pi.devices.ADXL345;
import edu.sdsu.rocket.pi.devices.DeviceManager;
import edu.sdsu.rocket.pi.devices.ITG3205;
import edu.sdsu.rocket.pi.devices.ITG3205.GyroscopeListener;
import edu.sdsu.rocket.pi.devices.MS5611;
import edu.sdsu.rocket.pi.devices.MockADS1115;
import edu.sdsu.rocket.pi.devices.MockADXL345;
import edu.sdsu.rocket.pi.devices.MockITG3205;
import edu.sdsu.rocket.pi.devices.MockMS5611;
import edu.sdsu.rocket.pi.io.radio.SensorsTransmitter;
import edu.sdsu.rocket.pi.io.radio.XTend900;
import edu.sdsu.rocket.pi.io.radio.XTend900Config;

public class Application {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final long NANOSECONDS_PER_SECOND = 1000000000L;
	
	private Settings settings;
	private Logging log;
	
	private final DeviceManager manager = new DeviceManager();
	private final Reader input = new InputStreamReader(System.in);
	
	/**
	 * Provides storage of local sensor values. By local, we are referring to
	 * the sensors physically attached to the system this application is running
	 * on.
	 */
	private final Sensors local = new Sensors();
	
	/**
	 * Provides storage of the remote sensor values. Whereas remote refers to
	 * the sensors that are located remotely from the system this application is
	 * running on. This is generally only used for receiving sensor data from
	 * the rocket over radio so this will not be used when this application is
	 * running on a system located on the rocket.
	 */
	private final Sensors remote = new Sensors();
	
	private final SensorServer server = new SensorServer(local, remote);
	
	private XTend900 radio;
	
	private final Vector3 tmpVec = new Vector3();
	
	public Application() {
		System.out.println("Starting application.");
	}
	
	public void setup() throws IOException {
		loadSettings();
		setupLogging();
		setupDevices();
		setupServer();
	}

	private void loadSettings() throws FileNotFoundException {
		File file = new File("settings.json");
		System.out.println("Loading Settings: " + file);
		
		Json json = new Json();
		settings = json.fromJson(Settings.class, new FileInputStream(file));
		
		if (settings.test) {
			try {
				System.out.println("!!! TESTING MODE !!! TESTING MODE !!! TESTING MODE !!!");
				Thread.sleep(1000L);
				System.out.println("!!! TESTING MODE !!! TESTING MODE !!! TESTING MODE !!!");
				Thread.sleep(1000L);
				System.out.println("!!! TESTING MODE !!! TESTING MODE !!! TESTING MODE !!!");
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
	}

	protected void setupLogging() throws IOException {
		System.out.println("Setup Logging.");
		log = new Logging(settings);
		
		OutputStreamMultiplexer out = new OutputStreamMultiplexer(System.out);
		OutputStreamMultiplexer err = new OutputStreamMultiplexer(System.err);
		Array<File> dirs = log.getDirectories();
		for (File d : dirs) {
			FileOutputStream f = new FileOutputStream(d + Logging.FILE_SEPARATOR + "log.txt");
			out.add(f);
			err.add(f);
		}
		
		// http://stackoverflow.com/a/18669284/196486
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
		
		System.out.println("Logging started at " + System.nanoTime() + ".");
	}
	
	protected void setupDevices() throws IOException {
		setupAccelerometer();
		setupGyroscope();
		setupBarometer();
		setupADC();
		setupGPS();
	}
	
	private void setupAccelerometer() throws IOException {
		if (!settings.devices.adxl345.enabled) return;
		System.out.println("Setup Accelerometer [ADXL345].");
		final ADXL345OutputStream adxl345log = log.getADXL345OutputStream();
		
		ADXL345 adxl345 = settings.test ? new MockADXL345() : new ADXL345();
		adxl345.setup();
		if (!adxl345.verifyDeviceID()) {
			throw new IOException("Failed to verify ADXL345 device ID");
		}
		adxl345.writeRange(ADXL345.ADXL345_RANGE_16G);
		adxl345.writeFullResolution(true);
		adxl345.writeRate(ADXL345.ADXL345_RATE_400);
		float scalingFactor = adxl345.getScalingFactor();
		local.accelerometer.setScalingFactor(scalingFactor);
		adxl345log.writeScalingFactor(scalingFactor);
		System.out.println("Scaling Factor: " + scalingFactor);
		
		adxl345.setListener(new ADXL345.AccelerometerListener() {
			@Override
			public void onValues(short x, short y, short z) {
				local.accelerometer.setRawX(x);
				local.accelerometer.setRawY(y);
				local.accelerometer.setRawZ(z);
				try {
					adxl345log.writeValues(x, y, z);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		});
		manager.add(adxl345).setThrottle(settings.devices.adxl345.throttle);
	}

	private void setupGyroscope() throws IOException, FileNotFoundException {
		if (!settings.devices.itg3205.enabled) return;
		System.out.println("Setup Gyroscope [ITG3205].");
		final ITG3205OutputStream itg3205log = log.getITG3205OutputStream();
		
		ITG3205 itg3205 = settings.test ? new MockITG3205() : new ITG3205();
		itg3205.setup();
		if (!itg3205.verifyDeviceID()) {
			throw new IOException("Failed to verify ITG3205 device ID");
		}
		// F_sample = F_internal / (divider + 1)
		// divider = F_internal / F_sample - 1
		itg3205.writeSampleRateDivider(2); // 2667 Hz
		itg3205.writeDLPFBandwidth(ITG3205.ITG3205_DLPF_BW_256);
		
		local.gyroscope.setScalingFactor(1f / ITG3205.ITG3205_SENSITIVITY_SCALE_FACTOR);
		itg3205log.writeScalingFactor(local.gyroscope.getScalingFactor());
		
		itg3205.setListener(new GyroscopeListener() {
			@Override
			public void onValues(short x, short y, short z) {
				local.gyroscope.setRawX(x);
				local.gyroscope.setRawY(y);
				local.gyroscope.setRawZ(z);
				try {
					itg3205log.writeValues(x, y, z);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
			
		});
		manager.add(itg3205).setThrottle(settings.devices.itg3205.throttle);
	}
	
	private void setupBarometer() throws IOException {
		if (!settings.devices.ms5611.enabled) return;
		System.out.println("Setup Barometer [MS5611].");
		final MS5611OutputStream ms5611log = log.getMS5611OutputStream();
		
		MS5611 ms5611 = settings.test ? new MockMS5611() : new MS5611();
		ms5611.setup();
		
		ms5611.setListener(new MS5611.BarometerListener() {
			@Override
			public void onValues(int T, int P) {
				local.barometer.setRawTemperature(T);
				local.barometer.setRawPressure(P);
				try {
					ms5611log.writeValues(T, P);
				} catch (IOException e) {
					System.err.println(e);
				}
			}

			@Override
			public void onFault(MS5611.Fault fault) {
				try {
					ms5611log.writeFault(fault.ordinal());
				} catch (IOException e) {
					System.err.println(e);
				}
				System.err.println("MS5611 fault: " + fault);
			}
		});
		
		manager.add(ms5611).setThrottle(settings.devices.ms5611.throttle);
	}

	private void setupADC() throws IOException {
		if (!settings.devices.ads1115.enabled) return;
		System.out.println("Setup ADC [ADS1115].");
		final ADS1115OutputStream ads1115log = log.getADS1115OutputStream();
		
		ADS1115 ads1115 = settings.test ? new MockADS1115() : new ADS1115();
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
		
		long timeout = (1L * NANOSECONDS_PER_SECOND) / sps * 5L; // 5 X expected sample duration
		ads1115.setTimeout(timeout);
		System.out.println("ADS1115 timeout: " + timeout);
		
		if (settings.devices.ads1115.sequence != null) {
			ads1115.setSequence(settings.devices.ads1115.sequence);
		}
		
		ads1115.setListener(new ADS1115.AnalogListener() {
			@Override
			public void onValue(ADS1115.Channel channel, float value) {
				local.analog.set(channel.ordinal(), value);
				try {
					ads1115log.writeValue(channel.ordinal(), value);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		});
		
		manager.add(ads1115);
	}
	
	private void setupGPS() throws FileNotFoundException {
		if (!settings.devices.gps.enabled) return;
		System.out.println("Setup GPS [Adafruit Ultimate GPS].");
		
		if (settings.test) {
			return;
		}
		
		FileInputStream in = new FileInputStream(settings.devices.gps.device);
		OutputStream out = log.getGPSOutputStream();
		final PrintWriter writer = new PrintWriter(out);
		
		SentenceReader reader = new SentenceReader(in);
		reader.addSentenceListener(new SentenceListener() {
			@Override
			public void readingStarted() {
				System.out.println("GPS reading started.");
			}
			
			@Override
			public void readingStopped() {
				System.out.println("GPS reading stopped.");
			}
			
			@Override
			public void readingPaused() {
				System.out.println("GPS reading paused.");
			}
			
			@Override
			public void sentenceRead(SentenceEvent event) {
				String sentence = event.getSentence().toString();
				writer.println(sentence);
			}
		});
		
		PositionProvider position = new PositionProvider(reader);
		position.addListener(new ProviderListener<PositionEvent>() {
			@Override
			public void providerUpdate(PositionEvent event) {
				double latitude  = event.getPosition().getLatitude();
				double longitude = event.getPosition().getLongitude();
				double altitude  = event.getPosition().getAltitude();
				local.gps.set(latitude, longitude, altitude);
			}
		});
		
		SatelliteInfoProvider satelliteInfo = new SatelliteInfoProvider(reader);
		satelliteInfo.addListener(new SatelliteInfoListener() {
			@Override
			public void providerUpdate(SatelliteInfoEvent event) {
				int fixStatus  = event.getGpsFixStatus().toInt();
				int satellites = event.getSatelliteIds().length;
				local.gps.setFixStatus(fixStatus);
				local.gps.setSatellites(satellites);
			}
		});
		
		reader.start();
	}
	
	protected void setupServer() throws IOException {
		System.out.println("Setup server.");
		server.start(settings.server.port);
	}
	
	protected void setupRadio() throws IOException, IllegalStateException, InterruptedException {
		if (!settings.devices.xtend900.enabled) return;
		System.out.println("Setup Radio [XTend900].");
		
		XTend900Config config = settings.devices.xtend900.config;
		System.out.println("Config: " + config);
		
		Serial serial = SerialFactory.createInstance();
		
		// setup serial listener to see command responses
		SerialDataEventListener listener = new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					System.out.println(event.getAsciiString());
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		};
		serial.addListener(listener);
		
		String device = settings.devices.xtend900.device;
		int baud = config.getInterfaceDataRate().getBaud();
		serial.open(device, baud);
		
		radio = new XTend900(serial);
		radio.setup().configure(config);
		
		// clean up serial listener
		serial.removeListener(listener);
		
		if (settings.devices.xtend900.sendSensorData) {
			manager.add(new SensorsTransmitter(radio, local));
		}
	}
	
	public void loop() throws IOException {
		switch (input.read()) {
		case '?':
			System.out.println();
			System.out.println("?: help");
			System.out.println("t: cpu temperature");
			System.out.println("f: loop frequency");
			System.out.println("a: accelerometer (local)");
			System.out.println("A: accelerometer (remote)");
			System.out.println("y: gyroscope (local)");
			System.out.println("Y: gyroscope (remote)");
			System.out.println("b: barometer (local)");
			System.out.println("B: barometer (remote)");
			System.out.println("c: analog (local)");
			System.out.println("C: analog (remote)");
			System.out.println("g: gps (local)");
			System.out.println("G: gps (remote)");
			System.out.println("s: start radio (currently " + (radio == null ? "NOT " : "") + "started)");
			System.out.println("r: toggle radio (currently " + (radio != null && radio.isOn() ? "ON" : "OFF") + ")");
			System.out.println("q: quit");
			System.out.println();
			break;
		case 't':
			try {
				float tempC = Pi.getCpuTemperature();
				float tempF = tempC * 9f / 5f + 32f;
				System.out.println("CPU: " + tempC + " C, " + tempF + " F");
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
			break;
		case 'f':
			System.out.println(manager.toString());
			break;
		case 'a':
			local.accelerometer.get(tmpVec);
			System.out.println(tmpVec.scl(9.8f) + " m/s^2");
			break;
		case 'A':
			remote.accelerometer.get(tmpVec);
			System.out.println(tmpVec.scl(9.8f) + " m/s^2");
			break;
		case 'b':
			System.out.println(local.barometer.getTemperature() + " C, " + local.barometer.getPressure() + " mbar");
			break;
		case 'B':
			System.out.println(remote.barometer.getTemperature() + " C, " + remote.barometer.getPressure() + " mbar");
			break;
		case 'y':
			local.gyroscope.get(tmpVec);
			System.out.println(tmpVec + " deg/s");
			break;
		case 'Y':
			remote.gyroscope.get(tmpVec);
			System.out.println(tmpVec + " deg/s");
			break;
		case 'c':
			System.out.println(
					"A0=" + local.analog.getA0() + " mV,\t" +
					"A1=" + local.analog.getA1() + " mV,\t" +
					"A2=" + local.analog.getA2() + " mV,\t" +
					"A3=" + local.analog.getA3() + " mV"
					);
			break;
		case 'C':
			System.out.println(
					"A0=" + remote.analog.getA0() + " mV,\t" +
					"A1=" + remote.analog.getA1() + " mV,\t" +
					"A2=" + remote.analog.getA2() + " mV,\t" +
					"A3=" + remote.analog.getA3() + " mV"
					);
			break;
		case 'g':
			System.out.println(
					"latitude="  + local.gps.getLatitude() + ",\t" +
					"longitude=" + local.gps.getLongitude() + ",\t" +
					"altitude="  + local.gps.getAltitude() + " m MSL"
					);
			break;
		case 'G':
			System.out.println(
					"latitude="  + remote.gps.getLatitude() + ",\t" +
					"longitude=" + remote.gps.getLongitude() + ",\t" +
					"altitude="  + remote.gps.getAltitude() + " m MSL"
					);
			break;
		case 's':
			if (radio == null) {
				try {
					setupRadio();
				} catch (IllegalStateException e) {
					System.err.println(e);
				} catch (InterruptedException e) {
					System.err.println(e);
				}
			} else {
				System.out.println("Radio already started.");
			}
			break;
		case 'r':
			if (radio != null) {
				radio.toggle();
				System.out.println("Radio is now " + (radio.isOn() ? "ON" : "OFF") + ".");
			}
			break;
		case 'q':
			shutdown();
			break;
		}
	}

	private void shutdown() {
		System.out.println("Shutting down.");
		
		System.out.println("Stopping server.");
		server.stop();
		
		System.out.println("Stopping device manager.");
		manager.clear();
		
		System.out.println("Closing log streams.");
		log.close();
		
		System.exit(0);
	}
	
}
