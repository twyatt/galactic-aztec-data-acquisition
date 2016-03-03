package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import edu.sdsu.rocket.server.devices.DeviceManager.Device;

public class HMC5883L implements Device {
	
	public interface MagnetometerListener {
		public void onValues(short x, short y, short z);
	}
	protected MagnetometerListener listener;
	public void setListener(MagnetometerListener listener) {
		this.listener = listener;
	}
	
	public static final int HMC5883L_DEFAULT_ADDRESS = 0x1E;
	private static final int DEFAULT_IDENTIFICATION = 0x483433;
	
	public enum Register {
		CONFIGURATION_REGISTER_A  ( 0), // R/W
		CONFIGURATION_REGISTER_B  ( 1), // R/W
		MODE_REGISTER             ( 2), // R/W
		DATA_OUTPUT_X_MSB_REGISTER( 3), // R
		DATA_OUTPUT_X_LSB_REGISTER( 4), // R
		DATA_OUTPUT_Z_MSB_REGISTER( 5), // R
		DATA_OUTPUT_Z_LSB_REGISTER( 6), // R
		DATA_OUTPUT_Y_MSB_REGISTER( 7), // R
		DATA_OUTPUT_Y_LSB_REGISTER( 8), // R
		STATUS_REGISTER           ( 9), // R
		IDENTIFICATION_REGISTER_A (10), // R
		IDENTIFICATION_REGISTER_B (11), // R
		IDENTIFICATION_REGISTER_C (12), // R
		;
		final int address;
		Register(int address) {
			this.address = address;
		}
	}
	
	public static final int CONFIG_DEFAULT = 0x10; // 0b0001_0000
	
	// Configuration Register A
	public enum SamplesAveraged {
		SAMPLES_1(0x00), // default
		SAMPLES_2(0x20),
		SAMPLES_4(0x40),
		SAMPLES_8(0x60),
		;
		final int config;
		private SamplesAveraged(int config) {
			this.config = config;
		}
	}
	
	// Configuration Register A
	public enum DataOutputRate {
		RATE_0_75(0x00, 0.75f), // 0.75 Hz
		RATE_1_5 (0x04, 1.5f),  // 1.5 Hz
		RATE_3   (0x08, 3f),    // 3 Hz
		RATE_7_5 (0x0C, 7.5f),  // 7.5 Hz
		RATE_15  (0x10, 15f),   // 15 Hz, default
		RATE_30  (0x14, 30f),   // 30 Hz
		RATE_75  (0x18, 75f),   // 75 Hz
		;
		final int config;
		final float frequency;
		final long delay;
		private DataOutputRate(int config, float frequency) {
			this.config = config;
			this.frequency = frequency;
			this.delay = (long) Math.ceil(1f / frequency * 1000f);
		}
		public long getDelay() {
			return delay;
		}
	}
	
	// Configuration Register A
	public enum MeasurementMode {
		NORMAL       (0x0), // default
		POSITIVE_BIAS(0x1),
		NEGATIVE_BIAS(0x2),
		;
		final int config;
		private MeasurementMode(int config) {
			this.config = config;
		}
	}
	
	public enum Gain {
		GAIN_1370(0x00, 0.73f),
		GAIN_1090(0x20, 0.92f), // default
		GAIN_820 (0x40, 1.22f),
		GAIN_660 (0x60, 1.52f),
		GAIN_440 (0x80, 2.27f),
		GAIN_390 (0xA0, 2.56f),
		GAIN_330 (0xC0, 3.03f),
		GAIN_230 (0xE0, 4.35f),
		;
		final int config;
		final float resolution;
		private Gain(int config, float resolution) {
			this.config = config;
			this.resolution = resolution;
		}
		public float getResolution() {
			return resolution;
		}
	}
	
	public enum OperatingMode {
		CONTINUOUS(0x0),
		SINGLE    (0x1),
		IDLE      (0x2),
		;
		final int config;
		private OperatingMode(int config) {
			this.config = config;
		}
	}
	
	public enum Status {
		LOCK (0x2),
		READY(0x1),
		;
		final int value;
		private Status(int value) {
			this.value = value;
		}
	}
	
	private SamplesAveraged samplesAveraged = SamplesAveraged.SAMPLES_1;
	private DataOutputRate dataOutputRate = DataOutputRate.RATE_15;
	private MeasurementMode measurementMode = MeasurementMode.NORMAL;
	private Gain gain = Gain.GAIN_1090;
	private OperatingMode operatingMode = OperatingMode.IDLE;

	/**
	 * I2C bus number to use to access device.
	 */
	private final int i2cBus;
	
	/**
	 * Address of I2C device.
	 */
	private final int devAddr;
	
	/**
	 * Abstraction of I2C device.
	 */
	private I2CDevice i2c;
	
	/**
	 * Read/write buffer.
	 */
    private final byte[] BUFFER = new byte[6];

	private short x;
	private short y;
	private short z;
	
	public HMC5883L() {
    	this(I2CBus.BUS_1);
    }
    
    public HMC5883L(int bus) {
    	this(bus, HMC5883L_DEFAULT_ADDRESS);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param address I2C address
     */
    public HMC5883L(int bus, int address) {
    	i2cBus = bus;
    	devAddr = address;
    }
    
    public HMC5883L setSamplesAveraged(SamplesAveraged samplesAveraged) {
    	this.samplesAveraged = samplesAveraged;
    	return this;
    }
    
    public SamplesAveraged getSamplesAveraged() {
    	return samplesAveraged;
    }
    
    public HMC5883L setDataOutputRate(DataOutputRate dataOutputRate) {
    	this.dataOutputRate = dataOutputRate;
    	return this;
    }
    
    public DataOutputRate getDataOutputRate() {
    	return dataOutputRate;
    }
    
    public HMC5883L setMeasurementMode(MeasurementMode measurementMode) {
    	this.measurementMode = measurementMode;
    	return this;
    }
    
    public MeasurementMode getMeasurementMode() {
    	return measurementMode;
    }
    
    public HMC5883L setGain(Gain gain) {
    	this.gain = gain;
    	return this;
    }
    
    public Gain getGain() {
    	return gain;
    }
    
    public HMC5883L setOperatingMode(OperatingMode operatingMode) {
    	this.operatingMode = operatingMode;
    	return this;
    }
    
    public OperatingMode getOperatingMode() {
    	return operatingMode;
    }
    
    /**
     * Setup the sensor for general usage.
     * @return 
     * 
     * @throws IOException 
     */
	public HMC5883L setup() throws IOException {
		// http://pi4j.com/example/control.html
		i2c = I2CFactory.getInstance(i2cBus).getDevice(devAddr);
		writeConfiguration();
		return this;
	}
	
	public int getIdentification() throws IOException {
		int address = Register.IDENTIFICATION_REGISTER_A.address;
		int offset = 0;
		int size = 3;
		i2c.read(address, BUFFER, offset, size);
		
		int identification = (BUFFER[0] << 16) | (BUFFER[1] << 8) | (BUFFER[2] & 0xFF);
		return identification;
	}
	
	public boolean verifyIdentification() throws IOException {
		return getIdentification() == DEFAULT_IDENTIFICATION;
	}
    
    public int getConfiguration() {
    	int configRegisterA = samplesAveraged.config
    			| dataOutputRate.config
    			| measurementMode.config;
    	int configRegisterB = gain.config;
    	int modeRegister = operatingMode.config;
    	
    	return (configRegisterA << 16) | (configRegisterB << 8) | (modeRegister & 0xFF);
	}
    
    public void writeConfiguration() throws IOException {
    	int address = Register.CONFIGURATION_REGISTER_A.address;
    	int config = getConfiguration();
    	BUFFER[0] = (byte) ((config >> 16) & 0xFF);
    	BUFFER[1] = (byte) ((config >> 8)  & 0xFF);
    	BUFFER[2] = (byte) (config         & 0xFF);
		int offset = 0;
		int size = 3;
		i2c.write(address, BUFFER, offset, size);
    }
    
    public boolean isReady() throws IOException {
    	int address = Register.STATUS_REGISTER.address;
    	int offset = 0;
		int size = 1;
		i2c.read(address, BUFFER, offset, size);
		return (BUFFER[0] & Status.READY.value) == Status.READY.value;
    }

	public void readMagnetometer() throws IOException {
		int address = Register.DATA_OUTPUT_X_MSB_REGISTER.address;
		int offset = 0;
		int size = 6;
		i2c.read(address, BUFFER, offset, size);

		x = (short) ((BUFFER[0] << 8) | (BUFFER[1] & 0xFF));
		z = (short) ((BUFFER[2] << 8) | (BUFFER[3] & 0xFF));
		y = (short) ((BUFFER[4] << 8) | (BUFFER[5] & 0xFF));
	}

	@Override
	public void loop() throws IOException, InterruptedException {
		readMagnetometer();
		
		if (listener != null) {
			listener.onValues(x, y, z);
		}
	}

}
