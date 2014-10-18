package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import com.i2cdevlib.I2Cdev;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class ADS1115 {
	
	public static final byte ADS1115_ADDRESS_ADDR_GND    = 0x48; // address pin low (GND)
	public static final byte ADS1115_ADDRESS_ADDR_VDD    = 0x49; // address pin high (VCC)
	public static final byte ADS1115_ADDRESS_ADDR_SDA    = 0x4A; // address pin tied to SDA pin
	public static final byte ADS1115_ADDRESS_ADDR_SCL    = 0x4B; // address pin tied to SCL pin
	public static final byte ADS1115_DEFAULT_ADDRESS     = ADS1115_ADDRESS_ADDR_GND;

	public static final byte ADS1115_RA_CONVERSION       = 0x00;
	public static final byte ADS1115_RA_CONFIG           = 0x01;
	public static final byte ADS1115_RA_LO_THRESH        = 0x02;
	public static final byte ADS1115_RA_HI_THRESH        = 0x03;

	private static final byte ADS1115_CFG_OS_BIT          = 15;
	private static final byte ADS1115_CFG_MUX_BIT         = 14;
	private static final byte ADS1115_CFG_MUX_LENGTH      = 3;
	private static final byte ADS1115_CFG_PGA_BIT         = 11;
	private static final byte ADS1115_CFG_PGA_LENGTH      = 3;
	private static final byte ADS1115_CFG_MODE_BIT        = 8;
	private static final byte ADS1115_CFG_DR_BIT          = 7;
	private static final byte ADS1115_CFG_DR_LENGTH       = 3;
	private static final byte ADS1115_CFG_COMP_MODE_BIT   = 4;
	private static final byte ADS1115_CFG_COMP_POL_BIT    = 3;
	private static final byte ADS1115_CFG_COMP_LAT_BIT    = 2;
	private static final byte ADS1115_CFG_COMP_QUE_BIT    = 1;
	private static final byte ADS1115_CFG_COMP_QUE_LENGTH = 2;

	public static final byte ADS1115_OS_ACTIVE           = 0x00;
	public static final byte ADS1115_OS_INACTIVE         = 0x01;
	public static final byte ADS1115_OS_BEGIN            = 0x01;

	public static final byte ADS1115_MUX_P0_N1           = 0x00; // default
	public static final byte ADS1115_MUX_P0_N3           = 0x01;
	public static final byte ADS1115_MUX_P1_N3           = 0x02;
	public static final byte ADS1115_MUX_P2_N3           = 0x03;
	public static final byte ADS1115_MUX_P0_NG           = 0x04;
	public static final byte ADS1115_MUX_P1_NG           = 0x05;
	public static final byte ADS1115_MUX_P2_NG           = 0x06;
	public static final byte ADS1115_MUX_P3_NG           = 0x07;

	public static final byte ADS1115_PGA_6P144           = 0x00;
	public static final byte ADS1115_PGA_4P096           = 0x01;
	public static final byte ADS1115_PGA_2P048           = 0x02; // default
	public static final byte ADS1115_PGA_1P024           = 0x03;
	public static final byte ADS1115_PGA_0P512           = 0x04;
	public static final byte ADS1115_PGA_0P256           = 0x05;
	public static final byte ADS1115_PGA_0P256B          = 0x06;
	public static final byte ADS1115_PGA_0P256C          = 0x07;

	public static final float ADS1115_MV_6P144           = 0.187500f;
	public static final float ADS1115_MV_4P096           = 0.125000f;
	public static final float ADS1115_MV_2P048           = 0.062500f; // default
	public static final float ADS1115_MV_1P024           = 0.031250f;
	public static final float ADS1115_MV_0P512           = 0.015625f;
	public static final float ADS1115_MV_0P256           = 0.007813f;
	public static final float ADS1115_MV_0P256B          = 0.007813f; 
	public static final float ADS1115_MV_0P256C          = 0.007813f;

	public static final byte ADS1115_MODE_CONTINUOUS     = 0x00;
	public static final byte ADS1115_MODE_SINGLESHOT     = 0x01; // default

	public static final byte ADS1115_RATE_8              = 0x00;
	public static final byte ADS1115_RATE_16             = 0x01;
	public static final byte ADS1115_RATE_32             = 0x02;
	public static final byte ADS1115_RATE_64             = 0x03;
	public static final byte ADS1115_RATE_128            = 0x04; // default
	public static final byte ADS1115_RATE_250            = 0x05;
	public static final byte ADS1115_RATE_475            = 0x06;
	public static final byte ADS1115_RATE_860            = 0x07;

	public static final byte ADS1115_COMP_MODE_HYSTERESIS  = 0x00; // default
	public static final byte ADS1115_COMP_MODE_WINDOW      = 0x01;

	public static final byte ADS1115_COMP_POL_ACTIVE_LOW   = 0x00; // default
	public static final byte ADS1115_COMP_POL_ACTIVE_HIGH  = 0x01;

	public static final boolean ADS1115_COMP_LAT_NON_LATCHING = false; // default
	public static final boolean ADS1115_COMP_LAT_LATCHING     = true;

	public static final byte ADS1115_COMP_QUE_ASSERT1    = 0x00;
	public static final byte ADS1115_COMP_QUE_ASSERT2    = 0x01;
	public static final byte ADS1115_COMP_QUE_ASSERT4    = 0x02;
	public static final byte ADS1115_COMP_QUE_DISABLE    = 0x03; // default
	
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
	
	private byte pgaMode = ADS1115_PGA_2P048;
	private byte sampleRate = ADS1115_RATE_128;
	
	public static int getSamplesPerSecond(byte rate) {
		switch (rate) {
		case ADS1115.ADS1115_RATE_8: return 8;
		case ADS1115.ADS1115_RATE_16: return 16;
		case ADS1115.ADS1115_RATE_32: return 32;
		case ADS1115.ADS1115_RATE_64: return 64;
		case ADS1115.ADS1115_RATE_128: return 128;
		case ADS1115.ADS1115_RATE_250: return 250;
		case ADS1115.ADS1115_RATE_475: return 475;
		case ADS1115.ADS1115_RATE_860: return 860;
		default: return 128;
		}
	}

    public ADS1115(int bus) {
    	this(bus, ADS1115_DEFAULT_ADDRESS);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param bus
     * @param address I2C address
     */
    public ADS1115(int bus, int address) {
    	i2cBus = bus;
    	devAddr = address;
    }
    
    /**
     * Setup the sensor for general usage.
     * 
     * @throws IOException 
     */
	public void setup() throws IOException {
		// http://pi4j.com/example/control.html
		i2c = I2CFactory.getInstance(i2cBus).getDevice(devAddr);
	}
	
	/**
	 * Write the multiplexer connection.
	 * 
	 * Continuous mode may fill the conversion register with data before the MUX
	 * setting has taken effect. A stop/start of the conversion is done to reset
	 * the values.
	 * 
	 * @param mux
	 * @throws IOException 
	 */
	public void writeMultiplexer(byte mux) throws IOException {
		I2Cdev.writeBitsW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_MUX_BIT, ADS1115_CFG_MUX_LENGTH, mux);
	}
	
	/**
	 * Write programmable gain amplifier level.
	 * 
	 * Continuous mode may fill the conversion register with data before the
	 * gain setting has taken effect. A stop/start of the conversion is done to
	 * reset the values.
	 * 
	 * @param gain
	 * @throws IOException 
	 */
	public void writeGain(byte gain) throws IOException {
		I2Cdev.writeBitsW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_PGA_BIT, ADS1115_CFG_PGA_LENGTH, gain);
		pgaMode = gain;
	}
	
	/**
	 * Write the device mode.
	 * 
	 * @param mode
	 * @throws IOException 
	 */
	public void writeMode(byte mode) throws IOException {
		if (mode != ADS1115_MODE_SINGLESHOT) {
			throw new UnsupportedOperationException("Continuous mode not implemented.");
		}
		I2Cdev.writeBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_MODE_BIT, mode);
	}
	
	/**
	 * Read data rate.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte readRate() throws IOException {
		sampleRate = (byte) I2Cdev.readBitsW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_DR_BIT, ADS1115_CFG_DR_LENGTH);
		return sampleRate;
	}
	
	/**
	 * Write data rate.
	 * 
	 * @param rate
	 * @throws IOException
	 */
	public void writeRate(byte rate) throws IOException {
	    I2Cdev.writeBitsW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_DR_BIT, ADS1115_CFG_DR_LENGTH, rate);
	    sampleRate = rate;
	}
	
	/**
	 * Write comparator mode.
	 * 
	 * @param mode
	 * @throws IOException
	 */
	public void writeComparatorMode(byte mode) throws IOException {
		I2Cdev.writeBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_COMP_MODE_BIT, mode);
	}
	
	/**
	 * Write comparator polarity.
	 * 
	 * @param ads1115CompPolActiveLow
	 * @throws IOException 
	 */
	public void writeComparatorPolarity(byte polarity) throws IOException {
		I2Cdev.writeBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_COMP_POL_BIT, polarity);
	}
	
	/**
	 * Write comparator latch enabled value.
	 * 
	 * @param enabled
	 * @throws IOException
	 */
	public void writeComparatorLatchEnabled(boolean enabled) throws IOException {
		I2Cdev.writeBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_COMP_LAT_BIT, enabled);
	}
	
	/**
	 * Set comparator queue mode.
	 * 
	 * @param mode
	 * @throws IOException 
	 */
	public void writeComparatorQueueMode(byte mode) throws IOException {
		I2Cdev.writeBitsW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_COMP_QUE_BIT, ADS1115_CFG_COMP_QUE_LENGTH, mode);
	}
	
	/**
	 * Read differential value based on current MUX configuration.
	 * 
	 * The default MUX setting sets the device to get the differential between
	 * the AIN0 and AIN1 pins. There are 8 possible MUX settings, but if you are
	 * using all four input pins as single-end voltage sensors, then the default
	 * option is not what you want; instead you will need to set the MUX to
	 * compare the desired AIN* pin with GND. There are shortcut methods
	 * (getConversion*) to do this conveniently, but you can also do it manually
	 * with setMultiplexer() followed by this method.
	 * 
	 * In single-shot mode, this register may not have fresh data. You need to
	 * write a 1 bit to the MSB of the CONFIG register to trigger a single
	 * read/conversion before this will be populated with fresh data. This
	 * technique is not as effortless, but it has enormous potential to save
	 * power by only running the comparison circuitry when needed.
	 * 
	 * @return 16-bit signed differential value
	 * @throws IOException 
	 */
	public short readConversion() throws IOException {
		return I2Cdev.readWord(i2c, ADS1115_RA_CONVERSION);
	}
	
	/**
	 * Read the current voltage reading.
	 * 
	 * Read the current differential and return it multiplied
	 * by the constant for the current gain.  mV is returned to
	 * increase the precision of the voltage
	 * @throws IOException 
	 */
	public float readMillivolts() throws IOException {
		switch (pgaMode) {
		case ADS1115_PGA_6P144:
			return readConversion() * ADS1115_MV_6P144;
		case ADS1115_PGA_4P096:
			return readConversion() * ADS1115_MV_4P096;
		case ADS1115_PGA_2P048:
			return readConversion() * ADS1115_MV_2P048;
		case ADS1115_PGA_1P024:
			return readConversion() * ADS1115_MV_1P024;
		case ADS1115_PGA_0P512:
			return readConversion() * ADS1115_MV_0P512;
		case ADS1115_PGA_0P256:
		case ADS1115_PGA_0P256B:
		case ADS1115_PGA_0P256C:
			return readConversion() * ADS1115_MV_0P256;
		default:
			return 0;
		}
	}

	/**
	 * Write operational status.
	 * 
	 * This bit can only be written while in power-down mode (no conversions
	 * active).
	 * 
	 * @param status New operational status (0 does nothing, 1 to trigger conversion)
	 * @throws IOException 
	 */
	public void writeOpStatus(byte status) throws IOException {
		I2Cdev.writeBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_OS_BIT, status);
	}

	/**
	 * Read operational status.
	 * 
	 * @return Current operational status (0 for active conversion, 1 for inactive)
	 * @throws IOException 
	 */
	public byte readOpStatus() throws IOException {
		if (I2Cdev.readBitW(i2c, ADS1115_RA_CONFIG, ADS1115_CFG_OS_BIT)) {
			return ADS1115_OS_INACTIVE;
		} else {
			return ADS1115_OS_ACTIVE;
		}
	}

}
