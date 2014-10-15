package edu.sdsu.rocket.data.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;
import com.pi4j.io.i2c.I2CBus;

import edu.sdsu.rocket.data.helpers.Stopwatch;
import edu.sdsu.rocket.data.io.Message;
import edu.sdsu.rocket.data.models.Sensors;
import edu.sdsu.rocket.data.server.devices.ADS1115;
import edu.sdsu.rocket.data.server.devices.ADS1115Wrapper;
import edu.sdsu.rocket.data.server.devices.ADXL345;
import edu.sdsu.rocket.data.server.devices.ITG3205;
import edu.sdsu.rocket.data.server.devices.MS5611;
import edu.sdsu.rocket.data.server.devices.MS5611Wrapper;
import edu.sdsu.rocket.data.server.io.DataLogger;
import edu.sdsu.rocket.data.server.io.DatagramServer;

public class Application {
	
	private static final int SERVER_PORT = 4444;
	private static final int BUFFER_SIZE = 64; // bytes
	
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	private static final String ADS1115_LOG = "ads1115.log";
	
	// http://pi.gadgetoid.com/pinout
	ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1);
	ITG3205 itg3205 = new ITG3205(I2CBus.BUS_1);
	MS5611Wrapper ms5611 = new MS5611Wrapper(new MS5611(I2CBus.BUS_1));
	ADS1115Wrapper ads1115 = new ADS1115Wrapper(new ADS1115(I2CBus.BUS_1));

	private DataLogger logger;
	private DatagramServer server;
	private int messageNumber;
	
	private final Reader input = new InputStreamReader(System.in);
	private final Sensors sensors;
	
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer dataBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final Vector3 tmpVec = new Vector3();
	
	public Application(Sensors sensors) {
		if (sensors == null) {
			throw new NullPointerException();
		}
		this.sensors = sensors;
	}
	
	public void setup() throws IOException {
		sensorSetup();
		loggerSetup();
		serverSetup();
		Stopwatch.reset();
	}
	
	public void loop() throws IOException {
		sensorLoop();
		serverLoop();
		inputLoop();
	}
	
	private void sensorSetup() throws IOException {
		adxl345.setup();
		if (!adxl345.verifyDeviceID()) {
			throw new IOException("Failed to verify ADXL345 device ID");
		}
		
		itg3205.setup();
		if (!itg3205.verifyDeviceID()) {
			throw new IOException("Failed to verify ITG3205 device ID");
		}
		
		adxl345.writeRange(ADXL345.ADXL345_RANGE_16G);
		adxl345.writeFullResolution(true);
		adxl345.writeRate(ADXL345.ADXL345_RATE_400);
		sensors.setAccelerometerScalingFactor(adxl345.getScalingFactor());
		
		// F_sample = F_internal / (divider + 1)
		// divider = F_internal / F_sample - 1
		itg3205.writeSampleRateDivider(2); // 2667 Hz
		itg3205.writeDLPFBandwidth(ITG3205.ITG3205_DLPF_BW_256);
		sensors.setGryroscopeScalingFactor(1f / ITG3205.ITG3205_SENSITIVITY_SCALE_FACTOR);
		
		ms5611.getDevice().setup();
		
		ads1115.getDevice().setup();
		ads1115.getDevice().writeGain(ADS1115.ADS1115_PGA_4P096); // +/- 4.096V
//		ads1115.getDevice().writeRate(ADS1115.ADS1115_RATE_64); // 64 samples/second => ~15 Hz for 4 single-ended
		ads1115.getDevice().writeRate(ADS1115.ADS1115_RATE_475); // 475 samples/second => ~83 Hz for 4 single-ended
//		ads1115.getDevice().writeRate(ADS1115.ADS1115_RATE_860); // 860 samples/second => ~119 Hz for 4 single-ended
		int sps = ADS1115.getSamplesPerSecond(ads1115.getDevice().readRate());
		long timeout = 1000000000L / sps * 5L; // 5 X expected sample duration
		ads1115.setTimeout(timeout);
	}
	
	private void loggerSetup() throws IOException {
		File userDir = new File(System.getProperty("user.dir", "~"));
		if (!userDir.exists()) {
			throw new IOException("Directory does not exist: " + userDir);
		}
		File logDir = new File(userDir + FILE_SEPARATOR + "logs");
		logger = new DataLogger(logDir);
	}
	
	private void serverSetup() throws IOException {
		server = new DatagramServer(SERVER_PORT);
		server.start();
	}

	/**
	 * Sends current sensor data to specified address.
	 * 
	 * @param address
	 * @throws IOException 
	 */
	public void sendSensorData(SocketAddress address) throws IOException {
		DatagramSocket socket = server.getSocket();
		if (socket != null) {
			dataBuffer.clear();
			sensors.toByteBuffer(dataBuffer);
			dataBuffer.flip();
			
			buffer.clear();
			buffer.putInt(++messageNumber);
			buffer.put(Message.SENSOR);
			buffer.put(dataBuffer);
			buffer.flip();
			
			byte[] temp = new byte[buffer.limit()];
			System.arraycopy(buffer.array(), 0, temp, 0, temp.length);
			
			DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit(), address);
			socket.send(packet);
		}
	}

	private void sensorLoop() throws IOException {
		adxl345.readRawAcceleration(sensors.accelerometer);
		
		itg3205.readRawRotations(sensors.gyroscope);
		
		int status;
		status = ms5611.read(sensors.barometer);
		if (status == 0) {
			// TODO log barometer values
		} else if (status > 0) { // fault occurred
//			Console.error("MS5611 D" + status + " fault.");
			// TODO log barometer fault
		}
		
		status = ads1115.read(sensors.analog);
		if (status == 0) {
//			logger.log(ADS1115_LOG, sensors.analog);
		} else if (status > 0) { // error
//			Console.error("ADS1115 error code " + status + ".");
			// TODO log adc error
		}
	}
	
	private void serverLoop() {
		Message message = server.read();
		if (message != null) {
			try {
				switch (message.id) {
				case Message.SENSOR:
					sendSensorData(message.address);
					break;
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private void inputLoop() throws IOException {
		if (input.ready()) {
			int c = input.read();
			switch (c) {
			case '?':
				Console.log();
				Console.log("?: help");
				Console.log("t: cpu temperature");
				Console.log("q: quit");
				Console.log("a: accelerometer");
				Console.log("b: barometer");
				Console.log("g: gyroscope");
				Console.log("c: analog");
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
			case 'a':
			case 'A':
				sensors.getAccelerometer(tmpVec);
				Console.log(tmpVec.scl(9.8f) + " m/s^2");
				break;
			case 'b':
			case 'B':
				float temperature = sensors.getBarometerTemperature();
				float pressure = sensors.getBarometerPressure();
				Console.log(temperature + " C, " + pressure + " mbar");
				break;
			case 'g':
			case 'G':
				sensors.getGyroscope(tmpVec);
				Console.log(tmpVec + " deg/s");
				break;
			case 'c':
			case 'C':
				float[] a = sensors.analog;
				Console.log("A0=" + a[0] + " mV,\tA1=" + a[1] + " mV,\tA2=" + a[2] + " mV,\tA3=" + a[3] + " mV");
				break;
			case 'q':
			case 'Q':
				Console.log("Quitting.");
				System.exit(0);
			}
		}
	}
	
}
