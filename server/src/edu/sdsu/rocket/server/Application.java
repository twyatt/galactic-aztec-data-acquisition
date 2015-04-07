package edu.sdsu.rocket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.provider.PositionProvider;
import net.sf.marineapi.provider.event.PositionEvent;
import net.sf.marineapi.provider.event.ProviderListener;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import edu.sdsu.rocket.core.helpers.Logging;
import edu.sdsu.rocket.core.helpers.Settings;
import edu.sdsu.rocket.core.io.ADS1115OutputStream;
import edu.sdsu.rocket.core.io.ADXL345OutputStream;
import edu.sdsu.rocket.core.io.ITG3205OutputStream;
import edu.sdsu.rocket.core.io.MS5611OutputStream;
import edu.sdsu.rocket.core.io.OutputStreamMultiplexer;
import edu.sdsu.rocket.core.models.Analog;
import edu.sdsu.rocket.core.models.Barometer;
import edu.sdsu.rocket.core.models.GPS;
import edu.sdsu.rocket.core.models.Sensors;
import edu.sdsu.rocket.core.net.SensorServer;
import edu.sdsu.rocket.pi.Pi;
import edu.sdsu.rocket.pi.devices.ADS1115;
import edu.sdsu.rocket.pi.devices.ADXL345;
import edu.sdsu.rocket.pi.devices.AdafruitGPS;
import edu.sdsu.rocket.pi.devices.DeviceManager;
import edu.sdsu.rocket.pi.devices.ITG3205;
import edu.sdsu.rocket.pi.devices.ITG3205.GyroscopeListener;
import edu.sdsu.rocket.pi.devices.MS5611;
import edu.sdsu.rocket.pi.devices.XTend900;
import edu.sdsu.rocket.pi.devices.XTend900.NumberBase;
import edu.sdsu.rocket.pi.devices.XTend900.RFDataRate;
import edu.sdsu.rocket.pi.devices.XTend900.TXPowerLevel;

public class Application {
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final long NANOSECONDS_PER_SECOND = 1000000000L;
	
	private Settings settings;
	private Logging log;
	
	protected ADXL345OutputStream adxl345log;
	protected ITG3205OutputStream itg3205log;
	protected MS5611OutputStream ms5611log;
	protected ADS1115OutputStream ads1115log;
	
	private final DeviceManager manager = new DeviceManager();
	private final Reader input = new InputStreamReader(System.in);
	
	protected final Sensors sensors;
	private final SensorServer server;
	
	private XTend900 radio;
	
	private final Vector3 tmpVec = new Vector3();
	
	public Application(Sensors sensors) {
		System.out.println("Starting application.");
		if (sensors == null) {
			throw new NullPointerException();
		}
		this.sensors = sensors;
		this.server = new SensorServer(sensors);
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
		setupAcceleromter();
		setupGyroscope();
		setupBarometer();
		setupADC();
		setupGPS();
	}
	
	private void setupAcceleromter() throws IOException {
		if (!settings.devices.adxl345.enabled) return;
		System.out.println("Setup Accelerometer [ADXL345].");
		adxl345log = log.openADXL345OutputStream();
		
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
		System.out.println("Scaling Factor: " + scalingFactor);
		
		adxl345.setListener(new ADXL345.AccelerometerListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.accelerometer.setRawX(x);
				sensors.accelerometer.setRawY(y);
				sensors.accelerometer.setRawZ(z);
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
		itg3205log = log.openITG3205OutputStream();
		
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
		
		itg3205.setListener(new GyroscopeListener() {
			@Override
			public void onValues(short x, short y, short z) {
				sensors.gyroscope.setRawX(x);
				sensors.gyroscope.setRawY(y);
				sensors.gyroscope.setRawZ(z);
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
		ms5611log = log.openMS5611OutputStream();
		
		MS5611 ms5611 = new MS5611(I2CBus.BUS_1);
		ms5611.setup();
		
		ms5611.setListener(new MS5611.BarometerListener() {
			@Override
			public void onValues(int T, int P) {
				sensors.barometer.setRawTemperature(T);
				sensors.barometer.setRawPressure(P);
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
		ads1115log = log.openADS1115OutputStream();
		
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
		
		long timeout = (1L * NANOSECONDS_PER_SECOND) / sps * 5L; // 5 X expected sample duration
		ads1115.setTimeout(timeout);
		System.out.println("ADS1115 timeout: " + timeout);
		
		if (settings.devices.ads1115.sequence != null) {
			ads1115.setSequence(settings.devices.ads1115.sequence);
		}
		
		ads1115.setListener(new ADS1115.AnalogListener() {
			@Override
			public void onValue(ADS1115.Channel channel, float value) {
				sensors.analog.set(channel.ordinal(), value);
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
		
		FileInputStream in = new FileInputStream(settings.devices.gps.device);
		OutputStreamMultiplexer out = log.openGPSOutputStream();
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
		
		PositionProvider provider = new PositionProvider(reader);
		provider.addListener(new ProviderListener<PositionEvent>() {
			@Override
			public void providerUpdate(PositionEvent event) {
				double latitude  = event.getPosition().getLatitude();
				double longitude = event.getPosition().getLongitude();
				double altitude  = event.getPosition().getAltitude();
				sensors.gps.set(latitude, longitude, altitude);
			}
		});
		reader.start();
		
		AdafruitGPS gps = new AdafruitGPS(in);
		gps.setOutputStream(out);
	}
	
	protected void setupServer() throws IOException {
		System.out.println("Setup server.");
		server.start(settings.server.port);
	}
	
	private void setupRadio() throws IOException {
		if (!settings.devices.xtend900.enabled) return;
		System.out.println("Setup Radio [XTend900].");
		
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
		serial.open(settings.devices.xtend900.device, settings.devices.xtend900.baud);
		
		radio = new XTend900(serial, sensors);
		try {
			radio.turnOn().enterATCommandMode();
			radio
				.writeNumberBase(NumberBase.DEFAULT_WITH_UNITS)
				.requestBoardVoltage()
				.requestHardwareVersion()
				.writeRFDataRate(RFDataRate.valueOf(settings.devices.xtend900.rfDataRate))
				.writeTXPowerLevel(TXPowerLevel.valueOf(settings.devices.xtend900.txPowerLevel));
			
			if (settings.devices.xtend900.autosetMY) {
				radio.writeAutosetMY();
			}
			if (settings.devices.xtend900.sourceAddress != null) {
				radio.writeSourceAddress(settings.devices.xtend900.sourceAddress);
			}
			if (settings.devices.xtend900.destinationAddress != null) {
				radio.writeDestinationAddress(settings.devices.xtend900.destinationAddress);
			}
			radio.exitATCommandMode().turnOff();
		} catch (IllegalStateException e) {
			System.err.println(e);
			throw new IOException(e);
		} catch (InterruptedException e) {
			System.err.println(e);
			throw new IOException(e);
		}
		
		// clean up serial listener
		serial.removeListener(listener);
		
		manager.add(radio)
			.setSleep(settings.devices.xtend900.sleep); // 100 ms = 10 Hz
	}
	
	public void loop() throws IOException {
		switch (input.read()) {
		case '?':
			System.out.println();
			System.out.println("?: help");
			System.out.println("t: cpu temperature");
			System.out.println("f: loop frequency");
			System.out.println("a: accelerometer");
			System.out.println("y: gyroscope");
			System.out.println("b: barometer");
			System.out.println("c: analog");
			System.out.println("g: gps");
			System.out.println("s: start radio (currently " + (radio == null ? "NOT " : "") + "started)");
			System.out.println("r: toggle radio (currently " + (radio != null && radio.isOn() ? "ON" : "OFF") + ")");
			System.out.println("q: quit");
			System.out.println();
			break;
		case 't':
		case 'T':
			try {
				float tempC = Pi.getCpuTemperature();
				float tempF = tempC * 9f / 5f + 32f;
				System.out.println("CPU: " + tempC + " C, " + tempF + " F");
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
			break;
		case 'f':
		case 'F':
			System.out.println(manager.toString());
			break;
		case 'a':
		case 'A':
			sensors.accelerometer.get(tmpVec);
			System.out.println(tmpVec.scl(9.8f) + " m/s^2");
			break;
		case 'b':
		case 'B':
			Barometer barometer = sensors.barometer;
			System.out.println(barometer.getTemperature() + " C, " + barometer.getPressure() + " mbar");
			break;
		case 'y':
		case 'Y':
			sensors.gyroscope.get(tmpVec);
			System.out.println(tmpVec + " deg/s");
			break;
		case 'c':
		case 'C':
			Analog a = sensors.analog;
			System.out.println("A0=" + a.getA0() + " mV,\tA1=" + a.getA1() + " mV,\tA2=" + a.getA2() + " mV,\tA3=" + a.getA3() + " mV");
			break;
		case 'g':
		case 'G':
			GPS gps = sensors.gps;
			System.out.println("latitude=" + gps.getLatitude() + ", longitude=" + gps.getLongitude() + ", altitude=" + gps.getAltitude() + " m MSL");
			break;
		case 's':
		case 'S':
			if (radio == null) {
				setupRadio();
			} else {
				System.out.println("Radio already started.");
			}
			break;
		case 'r':
		case 'R':
			if (radio != null) {
				radio.toggle();
				System.out.println("Radio is now " + (radio.isOn() ? "ON" : "OFF") + ".");
			}
			break;
		case 'q':
		case 'Q':
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
		try {
			if (adxl345log != null) adxl345log.close();
			if (itg3205log != null) itg3205log.close();
			if (ms5611log != null) ms5611log.close();
			if (ads1115log != null) ads1115log.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		
		System.exit(0);
	}
	
}
