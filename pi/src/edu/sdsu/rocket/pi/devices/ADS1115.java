package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import edu.sdsu.rocket.pi.devices.DeviceManager.Device;

public class ADS1115 implements Device {
	
	public enum Channel {
		A0, A1, A2, A3,
		;
		public static Channel valueOf(int value) {
			switch (value) {
			case 1:  return Channel.A1;
			case 2:  return Channel.A2;
			case 3:  return Channel.A3;
			default: return Channel.A0;
			}
		}
	}
	
	public interface AnalogListener {
		public void onValue(Channel channel, float value);

		public void onConversionTimeout();
	}
	protected AnalogListener listener;
	public ADS1115 setListener(AnalogListener listener) {
		this.listener = listener;
		return this;
	}
	
	protected long timeout;
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	protected int[] sequence = new int[] { 0, 1, 2, 3 };
	public void setSequence(int[] sequence) {
		if (sequence == null || sequence.length == 0) {
			sequence = new int[] { 0, 1, 2, 3 };
		}
		this.sequence = sequence;
	}
	
	/**
	 * Table 5. ADDR Pin Connection and Corresponding Slave Address, pg 17
	 * 
	 * ADDR PIN | SLAVE ADDRESS
	 *  Ground  |    1001000
	 *    VDD   |    1001001
	 *    SDA   |    1001010
	 *    SCL   |    1001011
	 */
	public enum Address {
		ADDR_GND(0x48),
		ADDR_VDD(0x49),
		ADDR_SDA(0x4A),
		ADDR_SCL(0x4B),
		;
		int config;
		Address(int config) {
			this.config = config;
		}
	}
	private static final Address ADDR_DEFAULT = Address.ADDR_GND;

	/**
	 * Table 6. Register Address, pg 18
	 * 
	 *  BIT 1 |  BIT 0 | REGISTER
	 *    0   |    0   | Conversion register
	 *    0   |    1   | Config register
	 *    1   |    0   | Lo_thresh register
	 *    1   |    1   | Hi_thresh register
	 * 
	 * Table 7. Pointer Register Byte (Write-Only), pg 18
	 * BIT 7 | BIT 6 | BIT 5 | BIT 4 | BIT 3 | BIT 2 |  BIT 1  |  BIT 0  |
	 *    0  |   0   |   0   |   0   |   0   |   0   |  Register address |
	 */
	public enum Register {
		CONVERSION(0x0), // 0b00
		CONFIG    (0x1), // 0b01
		LO_THRESH (0x2), // 0b10
		HI_THRESH (0x3), // 0b11
		;
		int address;
		Register(int address) {
			this.address = address;
		}
	}
	
    /*
	 * Table 9. Config Register (Read/Write), pg 18
	 * 
	 *  BIT | 15 |  14  |  13  |  12  |  11  |  10  |   9  |   8  |
	 * NAME | OS | MUX2 | MUX1 | MUX0 | PGA2 | PGA1 | PGA0 | MODE |
	 * 
	 *  BIT |  7  |  6  |  5  |     4     |     3    |     2    |      1    |      0    |
	 * NAME | DR2 | DR1 | DR0 | COMP_MODE | COMP_POL | COMP_LAT | COMP_QUE1 | COMP_QUE0 |
	 * 
	 * Default = 8583h = 0b1000_0101_1000_0011
	 */
	public static final int CONFIG_DEFAULT = 0x8583; // 0b1000_0101_1000_0011
	
	/**
	 * Bit [15] OS: Operational status/single-shot conversion start
	 * 
	 * This bit determines the operational status of the device. This bit can
	 * only be written when in power-down mode.
	 */
	public enum Status {
		// For a write status:
		OS_BEGIN   (0x8000), // Begin a single conversion (when in power-down mode)
		// For a read status:
		OS_ACTIVE  (0x0000), // Device is currently performing a conversion
		OS_INACTIVE(0x8000), // Device is not currently performing a conversion
		;
		int config;
		Status(int config) {
			this.config = config;
		}
	}
	
	/**
	 * Bits [14:12] MUX[2:0]: Input multiplexer configuration (ADS1115 only)
	 * 
	 * These bits configure the input multiplexer. They serve no function on the
	 * ADS1113/4.
	 */
	public enum Multiplexer {
		MUX_DIFF_0_1(0x0000), // differential P = AIN0, N = AIN1 (default)
		MUX_DIFF_0_3(0x1000), // differential P = AIN0, N = AIN3
		MUX_DIFF_1_3(0x2000), // differential P = AIN1, N = AIN3
		MUX_DIFF_2_3(0x3000), // differential P = AIN2, N = AIN3
		MUX_SINGLE_0(0x4000), // single-ended P = AIN0, N = GND
		MUX_SINGLE_1(0x5000), // single-ended P = AIN1, N = GND
		MUX_SINGLE_2(0x6000), // single-ended P = AIN2, N = GND
		MUX_SINGLE_3(0x7000), // single-ended P = AIN3, N = GND
		;
		int config;
		Multiplexer(int config) {
			this.config = config;
		}
	}
	
	/**
	 * Bits [11:9] PGA[2:0]: Programmable gain amplifier configuration (ADS1114
	 * and ADS1115 only)
	 * 
	 * These bits configure the programmable gain amplifier. They serve no
	 * function on the ADS1113.
	 */
    public enum Gain {
    	PGA_2_3(0x0000, 0.187500f), // 2/3 FS = +/-6.144V range
    	PGA_1  (0x0200, 0.125000f), //   1 FS = +/-4.096V range
    	PGA_2  (0x0400, 0.062500f), //   2 FS = +/-2.048V range, default
    	PGA_4  (0x0600, 0.031250f), //   4 FS = +/-1.024V range
    	PGA_8  (0x0800, 0.015625f), //   8 FS = +/-0.512V range
    	PGA_16 (0x0A00, 0.007813f), //  16 FS = +/-0.256V range
    	;
    	int config;
    	float resolution;
    	Gain(int config, float resolution) {
    		this.config = config;
    		this.resolution = resolution;
    	}
    }

    /**
     * Bit [8] MODE: Device operating mode
     * 
     * This bit controls the current operational mode of the ADS1113/4/5.
     */
    public enum Mode {
    	MODE_CONTINUOUS(0x0000), // Continuous conversion mode
    	MODE_SINGLE    (0x0100), // Power-down single-shot mode (default)
    	;
    	int config;
    	Mode(int config) {
    		this.config = config;
    	}
    }

    /**
     * Bits [7:5] DR[2:0]: Data rate
     * 
     * These bits control the data rate setting.
     */
    public enum Rate {
    	DR_8SPS  (0x0000, 8),   // 8SPS
    	DR_16SPS (0x0020, 16),  // 16SPS
    	DR_32SPS (0x0040, 32),  // 32SPS
    	DR_64SPS (0x0060, 64),  // 64SPS
    	DR_128SPS(0x0080, 128), // 128SPS (default)
    	DR_250SPS(0x00A0, 250), // 250SPS
    	DR_475SPS(0x00C0, 475), // 475SPS
    	DR_860SPS(0x00E0, 860), // 860SPS
    	;
    	int config;
    	int sps;
    	public int getSamplesPerSecond() { return sps; }
    	Rate(int config, int samplesPerSeconds) {
    		this.config = config;
    		this.sps = samplesPerSeconds;
    	}
    }

    /**
	 * Bit [4] COMP_MODE: Comparator mode (ADS1114 and ADS1115 only)
	 * 
	 * This bit controls the comparator mode of operation. It changes whether
	 * the comparator is implemented as a traditional comparator (COMP_MODE =
	 * '0') or as a window comparator (COMP_MODE = '1'). It serves no function
	 * on the ADS1113.
	 */
    public enum Comparator {
    	COMP_MODE_HYSTERESIS(0x0000), // Traditional comparator with hysteresis (default)
    	COMP_MODE_WINDOW    (0x0010),
    	;
    	int config;
    	Comparator(int config) {
    		this.config = config;
    	}
    }

    /**
	 * Bit [3] COMP_POL: Comparator polarity (ADS1114 and ADS1115 only)
	 * 
	 * This bit controls the polarity of the ALERT/RDY pin. When COMP_POL = '0'
	 * the comparator output is active low. When COMP_POL='1' the ALERT/RDY pin
	 * is active high. It serves no function on the ADS1113.
	 */
    public enum Polarity {
    	COMP_POL_ACTIVE_LOW (0x0000), // ALERT/RDY pin is active low (default)
    	COMP_POL_ACTIVE_HIGH(0x0008), // ALERT/RDY pin is active high
    	;
    	int config;
    	Polarity(int config) {
    		this.config = config;
    	}
    }
    
    /**
     * Bit [2] COMP_LAT: Latching comparator (ADS1114 and ADS1115 only)
     * 
	 * This bit controls whether the ALERT/RDY pin latches once asserted or
	 * clears once conversions are within the margin of the upper and lower
	 * threshold values. When COMP_LAT = '0', the ALERT/RDY pin does not latch
	 * when asserted. When COMP_LAT = '1', the asserted ALERT/RDY pin remains
	 * latched until conversion data are read by the master or an appropriate
	 * SMBus alert response is sent by the master, the device responds with its
	 * address, and it is the lowest address currently asserting the ALERT/RDY
	 * bus line. This bit serves no function on the ADS1113.
	 */
    public enum Latching {
    	COMP_LAT_NON_LATCHING(0x0000), // Non-latching comparator (default)
    	COMP_LAT_LATCHING    (0x0004), // Latching comparator
    	;
    	int config;
    	Latching(int config) {
    		this.config = config;
    	}
    }

    /**
     * Bits [1:0] COMP_QUE: Comparator queue and disable (ADS1114 and ADS1115
     * only)
     * 
	 * These bits perform two functions. When set to '11', they disable the
	 * comparator function and put the ALERT/RDY pin into a high state. When set
	 * to any other value, they control the number of successive conversions
	 * exceeding the upper or lower thresholds required before asserting the
	 * ALERT/RDY pin. They serve no function on the ADS1113.
	 */
    public enum Queue {
    	COMP_QUE_1_CONVERSION(0x0000), // Assert after one conversion
    	COMP_QUE_2_CONVERSION(0x0001), // Assert after two conversions
    	COMP_QUE_4_CONVERSION(0x0002), // Assert after four conversions
    	COMP_QUE_DISABLE     (0x0003), // Disable comparator (default)
    	;
    	int config;
    	Queue(int config) {
    		this.config = config;
    	}
    }
    
    public static final int HI_THRESH_RDY = 0xFFFF;
    public static final int LO_THRESH_RDY = 0x0000;
	
	/**
	 * Address of I2C device.
	 */
	private final Address address;
	
	private Multiplexer multiplexer = Multiplexer.MUX_DIFF_0_1;
	private Gain gain = Gain.PGA_2;
	private Mode mode = Mode.MODE_SINGLE;
	private Rate rate = Rate.DR_128SPS;
	private Comparator comparator = Comparator.COMP_MODE_HYSTERESIS;
	private Polarity polarity = Polarity.COMP_POL_ACTIVE_LOW;
	private Latching latching = Latching.COMP_LAT_NON_LATCHING;
	private Queue queue = Queue.COMP_QUE_DISABLE;
	
	/**
	 * I2C bus number to use to access device.
	 */
	private final int i2cBus;
	
	/**
	 * Abstraction of I2C device.
	 */
	private I2CDevice i2c;
	
	/**
	 * Read/write buffer.
	 */
	private final byte[] BUFFER = new byte[2];

	public ADS1115() {
		this(I2CBus.BUS_1);
	}
	
    public ADS1115(int bus) {
    	this(bus, ADDR_DEFAULT);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param bus
     * @param address I2C address
     */
    public ADS1115(int bus, Address address) {
    	this.i2cBus = bus;
    	this.address = address;
    }
    
    /**
     * Setup the sensor for general usage.
     * 
     * @throws IOException 
     */
	public ADS1115 setup() throws IOException {
		// http://pi4j.com/example/control.html
		i2c = I2CFactory.getInstance(i2cBus).getDevice(address.config);
		return this;
	}
	
	public ADS1115 setMultiplexer(Multiplexer multiplexer) {
		this.multiplexer = multiplexer;
		return this;
	}
	
	public Multiplexer getMultiplexer() {
		return multiplexer;
	}
	
	public ADS1115 setGain(Gain gain) {
		this.gain = gain;
		return this;
	}
	
	public Gain getGain() {
		return gain;
	}
	
	public ADS1115 setMode(Mode mode) {
		this.mode = mode;
		return this;
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public ADS1115 setRate(Rate rate) {
		this.rate = rate;
		return this;
	}
	
	public Rate getRate() {
		return rate;
	}
	
	public ADS1115 setComparator(Comparator comparator) {
		this.comparator = comparator;
		return this;
	}
	
	public Comparator getComparator() {
		return comparator;
	}

	public ADS1115 setPolarity(Polarity polarity) {
		this.polarity = polarity;
		return this;
	}
	
	public Polarity getPolarity() {
		return polarity;
	}

	public ADS1115 setLatching(Latching latching) {
		this.latching = latching;
		return this;
	}
	
	public Latching getLatching() {
		return latching;
	}

	public ADS1115 setQueue(Queue queue) {
		this.queue = queue;
		return this;
	}
	
	public Queue getQueue() {
		return queue;
	}
	
	public ADS1115 setSingleEnded(int channel) {
		switch (channel) {
		case 1:  return setMultiplexer(Multiplexer.MUX_SINGLE_1);
		case 2:  return setMultiplexer(Multiplexer.MUX_SINGLE_2);
		case 3:  return setMultiplexer(Multiplexer.MUX_SINGLE_3);
		default: return setMultiplexer(Multiplexer.MUX_SINGLE_0);
		}
	}
	
	public int getConfig() {
		return multiplexer.config
				| gain.config
				| mode.config
				| rate.config
				| comparator.config
				| polarity.config
				| latching.config
				| queue.config;
	}
	
	public ADS1115 writeConfig() throws IOException {
		writeRegister(Register.CONFIG, getConfig());
		return this;
	}
	
	public ADS1115 writeLoThresh(int value) throws IOException {
		writeRegister(Register.LO_THRESH, value);
		return this;
	}
	
	public int readLoThresh() throws IOException {
		return readRegister(Register.LO_THRESH);
	}
	
	public ADS1115 writeHiThresh(int value) throws IOException {
		writeRegister(Register.HI_THRESH, value);
		return this;
	}
	
	public int readHiThresh() throws IOException {
		return readRegister(Register.HI_THRESH);
	}
	
	public int readConfig() throws IOException {
		return readRegister(Register.CONFIG);
	}
	
	public boolean isPerformingConversion() throws IOException {
		return (readRegister(Register.CONFIG) & Status.OS_INACTIVE.config) == 0;
	}
	
	public Status readStatus() throws IOException {
		if ((readConfig() & Status.OS_INACTIVE.config) != 0) {
			return Status.OS_INACTIVE;
		} else {
			return Status.OS_ACTIVE;
		}
	}

	public void begin() throws IOException {
		int config = Status.OS_BEGIN.config | getConfig();
		writeRegister(Register.CONFIG, config);
	}
	
	public short readConversion() throws IOException {
		return (short) readRegister(Register.CONVERSION);
	}
	
	/**
	 * Read the current millivolt reading.
	 * 
	 * Read the current differential and return it multiplied by the constant
	 * for the current gain.
	 * 
	 * @throws IOException
	 */
	public float readMillivolts() throws IOException {
		return readConversion() * gain.resolution;
	}

	private void writeRegister(Register register, int value) throws IOException {
		int address = register.address;
		BUFFER[0] = (byte) (value >> 8);
		BUFFER[1] = (byte) (value & 0xFF);
		int offset = 0;
		int size = 2;
		i2c.write(address, BUFFER, offset, size);
	}
	
	private int readRegister(Register register) throws IOException {
		int address = register.address;
		int offset = 0;
		int size = 2;
		i2c.read(address, BUFFER, offset, size);
		return (BUFFER[0] << 8) | (BUFFER[1] & 0xFF);
	}
	
	@Override
	public void loop() throws IOException, InterruptedException {
		for (int channel : sequence) {
			long start = System.nanoTime();
			setSingleEnded(channel).begin();
			
			// wait for conversion
			while (isPerformingConversion()) {
				if (System.nanoTime() - start > timeout) {
					if (listener != null) {
						listener.onConversionTimeout();
					}
					break;
				}
			}
			
			float value = readMillivolts();
			
			if (listener != null) {
				listener.onValue(Channel.valueOf(channel), value);
			}
		}
	}

}
