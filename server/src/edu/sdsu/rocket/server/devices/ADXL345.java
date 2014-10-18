package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import com.i2cdevlib.I2Cdev;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class ADXL345 {
	
	/**
	 * Address of the ADXL345 with the ALT pin tied to GND (low).
	 */
	public static final byte ADXL345_ADDRESS_ALT_LOW  = 0x53;
	
	/**
	 * Address of the ADXL345 with the ALT pin tied to VCC (high).
	 */
	public static final byte ADXL345_ADDRESS_ALT_HIGH = 0x1D;
	
	public static final byte ADXL345_DEFAULT_ADDRESS  = ADXL345_ADDRESS_ALT_LOW;

	public static final byte ADXL345_REGISTER_DEVID          = 0x00;
	public static final byte ADXL345_REGISTER_RESERVED1      = 0x01;
	public static final byte ADXL345_REGISTER_THRESH_TAP     = 0x1D;
	public static final byte ADXL345_REGISTER_OFSX           = 0x1E;
	public static final byte ADXL345_REGISTER_OFSY           = 0x1F;
	public static final byte ADXL345_REGISTER_OFSZ           = 0x20;
	public static final byte ADXL345_REGISTER_DUR            = 0x21;
	public static final byte ADXL345_REGISTER_LATENT         = 0x22;
	public static final byte ADXL345_REGISTER_WINDOW         = 0x23;
	public static final byte ADXL345_REGISTER_THRESH_ACT     = 0x24;
	public static final byte ADXL345_REGISTER_THRESH_INACT   = 0x25;
	public static final byte ADXL345_REGISTER_TIME_INACT     = 0x26;
	public static final byte ADXL345_REGISTER_ACT_INACT_CTL  = 0x27;
	public static final byte ADXL345_REGISTER_THRESH_FF      = 0x28;
	public static final byte ADXL345_REGISTER_TIME_FF        = 0x29;
	public static final byte ADXL345_REGISTER_TAP_AXES       = 0x2A;
	public static final byte ADXL345_REGISTER_ACT_TAP_STATUS = 0x2B;
	public static final byte ADXL345_REGISTER_BW_RATE        = 0x2C;
	public static final byte ADXL345_REGISTER_POWER_CTL      = 0x2D;
	public static final byte ADXL345_REGISTER_INT_ENABLE     = 0x2E;
	public static final byte ADXL345_REGISTER_INT_MAP        = 0x2F;
	public static final byte ADXL345_REGISTER_INT_SOURCE     = 0x30;
	public static final byte ADXL345_REGISTER_DATA_FORMAT    = 0x31;
	public static final byte ADXL345_REGISTER_DATAX0         = 0x32;
	public static final byte ADXL345_REGISTER_DATAX1         = 0x33;
	public static final byte ADXL345_REGISTER_DATAY0         = 0x34;
	public static final byte ADXL345_REGISTER_DATAY1         = 0x35;
	public static final byte ADXL345_REGISTER_DATAZ0         = 0x36;
	public static final byte ADXL345_REGISTER_DATAZ1         = 0x37;
	public static final byte ADXL345_REGISTER_FIFO_CTL       = 0x38;
	public static final byte ADXL345_REGISTER_FIFO_STATUS    = 0x39;

	public static final byte ADXL345_AIC_ACT_AC_BIT          = 7;
	public static final byte ADXL345_AIC_ACT_X_BIT           = 6;
	public static final byte ADXL345_AIC_ACT_Y_BIT           = 5;
	public static final byte ADXL345_AIC_ACT_Z_BIT           = 4;
	public static final byte ADXL345_AIC_INACT_AC_BIT        = 3;
	public static final byte ADXL345_AIC_INACT_X_BIT         = 2;
	public static final byte ADXL345_AIC_INACT_Y_BIT         = 1;
	public static final byte ADXL345_AIC_INACT_Z_BIT         = 0;

	public static final byte ADXL345_TAPAXIS_SUP_BIT         = 3;
	public static final byte ADXL345_TAPAXIS_X_BIT           = 2;
	public static final byte ADXL345_TAPAXIS_Y_BIT           = 1;
	public static final byte ADXL345_TAPAXIS_Z_BIT           = 0;

	public static final byte ADXL345_TAPSTAT_ACTX_BIT        = 6;
	public static final byte ADXL345_TAPSTAT_ACTY_BIT        = 5;
	public static final byte ADXL345_TAPSTAT_ACTZ_BIT        = 4;
	public static final byte ADXL345_TAPSTAT_ASLEEP_BIT      = 3;
	public static final byte ADXL345_TAPSTAT_TAPX_BIT        = 2;
	public static final byte ADXL345_TAPSTAT_TAPY_BIT        = 1;
	public static final byte ADXL345_TAPSTAT_TAPZ_BIT        = 0;

	public static final byte ADXL345_BW_LOWPOWER_BIT         = 4;
	public static final byte ADXL345_BW_RATE_BIT             = 3;
	public static final byte ADXL345_BW_RATE_LENGTH          = 4;

	public static final byte ADXL345_RATE_3200               = 0b1111;
	public static final byte ADXL345_RATE_1600               = 0b1110;
	public static final byte ADXL345_RATE_800                = 0b1101;
	public static final byte ADXL345_RATE_400                = 0b1100;
	public static final byte ADXL345_RATE_200                = 0b1011;
	public static final byte ADXL345_RATE_100                = 0b1010;
	public static final byte ADXL345_RATE_50                 = 0b1001;
	public static final byte ADXL345_RATE_25                 = 0b1000;
	public static final byte ADXL345_RATE_12P5               = 0b0111;
	public static final byte ADXL345_RATE_6P25               = 0b0110;
	public static final byte ADXL345_RATE_3P13               = 0b0101;
	public static final byte ADXL345_RATE_1P56               = 0b0100;
	public static final byte ADXL345_RATE_0P78               = 0b0011;
	public static final byte ADXL345_RATE_0P39               = 0b0010;
	public static final byte ADXL345_RATE_0P20               = 0b0001;
	public static final byte ADXL345_RATE_0P10               = 0b0000;

	public static final byte ADXL345_PCTL_LINK_BIT           = 5;
	public static final byte ADXL345_PCTL_AUTOSLEEP_BIT      = 4;
	public static final byte ADXL345_PCTL_MEASURE_BIT        = 3;
	public static final byte ADXL345_PCTL_SLEEP_BIT          = 2;
	public static final byte ADXL345_PCTL_WAKEUP_BIT         = 1;
	public static final byte ADXL345_PCTL_WAKEUP_LENGTH      = 2;

	public static final byte ADXL345_WAKEUP_8HZ              = 0b00;
	public static final byte ADXL345_WAKEUP_4HZ              = 0b01;
	public static final byte ADXL345_WAKEUP_2HZ              = 0b10;
	public static final byte ADXL345_WAKEUP_1HZ              = 0b11;

	public static final byte ADXL345_INT_DATA_READY_BIT      = 7;
	public static final byte ADXL345_INT_SINGLE_TAP_BIT      = 6;
	public static final byte ADXL345_INT_DOUBLE_TAP_BIT      = 5;
	public static final byte ADXL345_INT_ACTIVITY_BIT        = 4;
	public static final byte ADXL345_INT_INACTIVITY_BIT      = 3;
	public static final byte ADXL345_INT_FREE_FALL_BIT       = 2;
	public static final byte ADXL345_INT_WATERMARK_BIT       = 1;
	public static final byte ADXL345_INT_OVERRUN_BIT         = 0;

	public static final byte ADXL345_FORMAT_SELFTEST_BIT     = 7;
	public static final byte ADXL345_FORMAT_SPIMODE_BIT      = 6;
	public static final byte ADXL345_FORMAT_INTMODE_BIT      = 5;
	public static final byte ADXL345_FORMAT_FULL_RES_BIT     = 3;
	public static final byte ADXL345_FORMAT_JUSTIFY_BIT      = 2;
	public static final byte ADXL345_FORMAT_RANGE_BIT        = 1;
	public static final byte ADXL345_FORMAT_RANGE_LENGTH     = 2;

	public static final byte ADXL345_RANGE_2G                = 0b00;
	public static final byte ADXL345_RANGE_4G                = 0b01;
	public static final byte ADXL345_RANGE_8G                = 0b10;
	public static final byte ADXL345_RANGE_16G               = 0b11;

	public static final byte ADXL345_FIFO_MODE_BIT           = 7;
	public static final byte ADXL345_FIFO_MODE_LENGTH        = 2;
	public static final byte ADXL345_FIFO_TRIGGER_BIT        = 5;
	public static final byte ADXL345_FIFO_SAMPLES_BIT        = 4;
	public static final byte ADXL345_FIFO_SAMPLES_LENGTH     = 5;

	public static final byte ADXL345_FIFO_MODE_BYPASS        = 0b00;
	public static final byte ADXL345_FIFO_MODE_FIFO          = 0b01;
	public static final byte ADXL345_FIFO_MODE_STREAM        = 0b10;
	public static final byte ADXL345_FIFO_MODE_TRIGGER       = 0b11;

	public static final byte ADXL345_FIFOSTAT_TRIGGER_BIT    = 7;
	public static final byte ADXL345_FIFOSTAT_LENGTH_BIT     = 5;
	public static final byte ADXL345_FIFOSTAT_LENGTH_LENGTH  = 6;
	
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
    
    /**
     * The G range that the device is operating in.
     * 
     * 0x0 = +/- 2G
     * 0x1 = +/- 4G
     * 0x2 = +/- 6G
     * 0x3 = +/- 8G
     * 
     * This value will be out of sync with the device until a read or write of
     * the range occurs.
     */
    private int range;
    
    /**
     * Full resolution mode toggle.
     * 
     * This value will be out of sync with the device until a read or write of
     * the full resolution bit occurs.
     */
    private boolean resolution;
    
    public ADXL345(int bus) {
    	this(bus, ADXL345_DEFAULT_ADDRESS);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param address I2C address
     */
    public ADXL345(int bus, int address) {
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
		
		i2c.write(ADXL345_REGISTER_POWER_CTL, (byte) 0); // reset power settings
		readFullResolution();
		readRange();
	    writeMeasureEnabled(true);
	}

	/**
	 * Verify the device ID.
	 * 
	 * @return True if device ID is valid, false otherwise
	 * @throws IOException 
	 */
	public boolean verifyDeviceID() throws IOException {
	    return readDeviceID() == 0xE5;
	}

	/**
	 * Reads the device ID.
	 * 
	 * Register 0x00 -- DEVID (Read Only)
	 * 
	 * The DEVID register holds a fixed device ID code of 0xE5 (345 octal).
	 * 
	 * @return Device ID
	 * @throws IOException 
	 */
	public int readDeviceID() throws IOException {
		return i2c.read(ADXL345_REGISTER_DEVID);
	}

	/**
	 * Read axis offsets.
	 * 
	 * Register 0x1E, 0x1F, 0x20 -- OFSX, OFSY, OFSZ (Read/Write)
	 * 
	 * The OFSX, OFSY, and OFSZ registers are each eight bits and offer user-set
	 * offset adjustments in twos complement format with a scale factor of 15.6
	 * mg/LSB (that is, 0x7F = 2 g). The value stored in the offset registers is
	 * automatically added to the acceleration data, and the resulting value is
	 * stored in the output data registers. For additional information regarding
	 * offset calibration and the use of the offset registers, refer to the
	 * Offset Calibration section.
	 * 
	 * @param offsets Array of axis offsets whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void readOffsets(short[] offsets) throws IOException {
		if (offsets == null)
			throw new NullPointerException("Offsets array cannot be null");
		if (offsets.length != 3)
			throw new IllegalArgumentException("Offset array must have a length of 3");
		
		int offset = 0;
		int size = 3;
		i2c.read(ADXL345_REGISTER_OFSX, BUFFER, offset, size);
		offsets[0] = (short) (BUFFER[0] & 0xFF);
		offsets[1] = (short) (BUFFER[1] & 0xFF);
		offsets[2] = (short) (BUFFER[2] & 0xFF);
	}
	
	/**
	 * Write axis offsets.
	 * 
	 * Register 0x1E, 0x1F, 0x20 -- OFSX, OFSY, OFSZ (Read/Write)
	 * 
	 * The OFSX, OFSY, and OFSZ registers are each eight bits and offer user-set
	 * offset adjustments in twos complement format with a scale factor of 15.6
	 * mg/LSB (that is, 0x7F = 2 g). The value stored in the offset registers is
	 * automatically added to the acceleration data, and the resulting value is
	 * stored in the output data registers. For additional information regarding
	 * offset calibration and the use of the offset registers, refer to the
	 * Offset Calibration section.
	 * 
	 * @param offsets Array of axis offsets whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void writeOffsets(short[] offsets) throws IOException {
		if (offsets == null)
			throw new NullPointerException("Offsets array cannot be null");
		if (offsets.length != 3)
			throw new IllegalArgumentException("Offset array must have a length of 3");
		
		i2c.write(ADXL345_REGISTER_OFSX, (byte) (offsets[0] & 0xFF));
		i2c.write(ADXL345_REGISTER_OFSY, (byte) (offsets[1] & 0xFF));
		i2c.write(ADXL345_REGISTER_OFSZ, (byte) (offsets[2] & 0xFF));
	}
	
	/**
	 * Read the device bandwidth rate.
	 * 
	 * Register 0x2C -- BW_RATE (Read/Write)
	 * 
	 * These bits select the device bandwidth and output data rate (see Table 7
	 * and Table 8 for details). The default value is 0x0A, which translates to
	 * a 100 Hz output data rate. An output data rate should be selected that is
	 * appropriate for the communication protocol and frequency selected.
	 * Selecting too high of an output data rate with a low communication speed
	 * results in samples being discarded.
	 * 
	 * <pre>
	 * Table 7. Typical Current Consumption vs. Data Rate
	 * Output Data Rate (Hz) | Bandwidth (Hz) | Rate Code |   Value   | Idd (uA)
	 *          3200         |      1600      |    1111   | 0xF or 15 |   140
	 *          1600         |       800      |    1110   | 0xE or 14 |    90
	 *           800         |       400      |    1101   | 0xD or 13 |   140
	 *           400         |       200      |    1100   | 0xC or 12 |   140
	 *           200         |       100      |    1011   | 0xB or 11 |   140
	 *           100         |        50      |    1010   | 0xA or 10 |   140
	 *            50         |        25      |    1001   | 0x9 or 9  |    90
	 *            25         |      12.5      |    1000   | 0x8 or 8  |    60
	 *          12.5         |      6.25      |    0111   | 0x7 or 7  |    50
	 *          6.25         |      3.13      |    0110   | 0x6 or 6  |    45
	 *          3.13         |      1.56      |    0101   | 0x5 or 5  |    40
	 *          1.56         |      0.78      |    0100   | 0x4 or 4  |    34
	 *          0.78         |      0.39      |    0011   | 0x3 or 3  |    23
	 *          0.39         |      0.20      |    0010   | 0x2 or 2  |    23
	 *          0.20         |      0.10      |    0001   | 0x1 or 1  |    23
	 *          0.10         |      0.05      |    0000   | 0x0 or 0  |    23
	 * </pre>
	 * 
	 * @return Data rate (0 to 15)
	 * @throws IOException
	 */
	public int readRate() throws IOException {
		return I2Cdev.readBits(i2c, ADXL345_REGISTER_BW_RATE, ADXL345_BW_RATE_BIT, ADXL345_BW_RATE_LENGTH);
	}
	
	/**
	 * Write the device bandwidth rate.
	 * 
	 * Register 0x2C -- BW_RATE (Read/Write)
	 * 
	 * These bits select the device bandwidth and output data rate (see Table 7
	 * and Table 8 for details). The default value is 0x0A, which translates to
	 * a 100 Hz output data rate. An output data rate should be selected that is
	 * appropriate for the communication protocol and frequency selected.
	 * Selecting too high of an output data rate with a low communication speed
	 * results in samples being discarded.
	 * 
	 * <pre>
	 * Table 7. Typical Current Consumption vs. Data Rate
	 * Output Data Rate (Hz) | Bandwidth (Hz) | Rate Code |   Value   | Idd (uA)
	 *          3200         |      1600      |    1111   | 0xF or 15 |   140
	 *          1600         |       800      |    1110   | 0xE or 14 |    90
	 *           800         |       400      |    1101   | 0xD or 13 |   140
	 *           400         |       200      |    1100   | 0xC or 12 |   140
	 *           200         |       100      |    1011   | 0xB or 11 |   140
	 *           100         |        50      |    1010   | 0xA or 10 |   140
	 *            50         |        25      |    1001   | 0x9 or 9  |    90
	 *            25         |      12.5      |    1000   | 0x8 or 8  |    60
	 *          12.5         |      6.25      |    0111   | 0x7 or 7  |    50
	 *          6.25         |      3.13      |    0110   | 0x6 or 6  |    45
	 *          3.13         |      1.56      |    0101   | 0x5 or 5  |    40
	 *          1.56         |      0.78      |    0100   | 0x4 or 4  |    34
	 *          0.78         |      0.39      |    0011   | 0x3 or 3  |    23
	 *          0.39         |      0.20      |    0010   | 0x2 or 2  |    23
	 *          0.20         |      0.10      |    0001   | 0x1 or 1  |    23
	 *          0.10         |      0.05      |    0000   | 0x0 or 0  |    23
	 * </pre>
	 * 
	 * @param rate Data rate (0 to 15)
	 * @throws IOException
	 */
	public void writeRate(int rate) throws IOException {
		I2Cdev.writeBits(i2c, ADXL345_REGISTER_BW_RATE, ADXL345_BW_RATE_BIT, ADXL345_BW_RATE_LENGTH, rate);
	}
	
	/**
	 * Read measurement enabled status.
	 * 
	 * Register 0x2D -- POWER_CTL (Read/Write)
	 * 
	 * A setting of 0 in the measure bit places the part into standby mode, and
	 * a setting of 1 places the part into measurement mode. The ADXL345 powers
	 * up in standby mode with minimum power consumption.
	 * 
	 * @return Measurement enabled status
	 * @throws IOException
	 */
	public boolean readMeasureEnabled() throws IOException {
		return I2Cdev.readBit(i2c, ADXL345_REGISTER_POWER_CTL, ADXL345_PCTL_MEASURE_BIT);
	}
	
	/**
	 * Write measurement enabled status.
	 * 
	 * Register 0x2D -- POWER_CTL (Read/Write)
	 * 
	 * A setting of 0 in the measure bit places the part into standby mode, and
	 * a setting of 1 places the part into measurement mode. The ADXL345 powers
	 * up in standby mode with minimum power consumption.
	 * 
	 * @param enabled Measurement enabled status
	 * @throws IOException
	 */
	public void writeMeasureEnabled(boolean enabled) throws IOException {
		I2Cdev.writeBit(i2c, ADXL345_REGISTER_POWER_CTL, ADXL345_PCTL_MEASURE_BIT, enabled);
	}
	
	/**
	 * Read full resolution mode setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * When this bit is set to a value of 1, the device is in full resolution
	 * mode, where the output resolution increases with the g range set by the
	 * range bits to maintain a 4 mg/LSB scale factor. When the FULL_RES bit is
	 * set to 0, the device is in 10-bit mode, and the range bits determine the
	 * maximum g range and scale factor.
	 * 
	 * @return Full resolution enabled setting
	 * @throws IOException
	 */
	public boolean readFullResolution() throws IOException {
		resolution = I2Cdev.readBit(i2c, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_FULL_RES_BIT);
		return resolution;
	}
	
	/**
	 * Write full resolution mode setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * When this bit is set to a value of 1, the device is in full resolution
	 * mode, where the output resolution increases with the g range set by the
	 * range bits to maintain a 4 mg/LSB scale factor. When the FULL_RES bit is
	 * set to 0, the device is in 10-bit mode, and the range bits determine the
	 * maximum g range and scale factor.
	 * 
	 * @param resolution Full resolution enabled setting
	 * @throws IOException
	 */
	public void writeFullResolution(boolean resolution) throws IOException {
		this.resolution = resolution;
		I2Cdev.writeBit(i2c, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_FULL_RES_BIT, resolution);
	}
	
	/**
	 * Read data range setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * These bits set the g range as described in Table 21.
	 * 
	 * <pre>
	 * Table 21. g Range Setting
	 * | Setting |  g Range | Value |
	 * |    00   | +/-  2 g |   0   |
	 * |    01   | +/-  4 g |   1   |
	 * |    10   | +/-  8 g |   2   |
	 * |    11   | +/- 16 g |   3   |
	 * </pre>
	 * 
	 * @return Range value (0 to 3)
	 * @throws IOException 
	 */
	public int readRange() throws IOException {
		range = I2Cdev.readBits(i2c, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_RANGE_BIT, ADXL345_FORMAT_RANGE_LENGTH);
		return range;
	}
	
	/**
	 * Write data range setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * These bits set the g range as described in Table 21.
	 * 
	 * <pre>
	 * Table 21. g Range Setting
	 * | Setting |  g Range | Value |
	 * |    00   | +/-  2 g |   0   |
	 * |    01   | +/-  4 g |   1   |
	 * |    10   | +/-  8 g |   2   |
	 * |    11   | +/- 16 g |   3   |
	 * </pre>
	 * 
	 * @param range Range value (0 to 3)
	 * @throws IOException 
	 */
	public void writeRange(int range) throws IOException {
		this.range = range;
		I2Cdev.writeBits(i2c, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_RANGE_BIT, ADXL345_FORMAT_RANGE_LENGTH, range);
	}

	/**
	 * Read raw acceleration values.
	 * 
	 * Register 0x32 to Register 0x37 -- DATAX0, DATAX1, DATAY0, DATAY1, DATAZ0,
	 * DATAZ1 (Read Only)
	 * 
	 * These six bytes (Register 0x32 to Register 0x37) are eight bits each and
	 * hold the output data for each axis. Register 0x32 and Register 0x33 hold
	 * the output data for the x-axis, Register 0x34 and Register 0x35 hold the
	 * output data for the y-axis, and Register 0x36 and Register 0x37 hold the
	 * output data for the z-axis. The output data is twos complement, with
	 * DATAx0 as the least significant byte and DATAx1 as the most significant
	 * byte, where x represent X, Y, or Z. The DATA_FORMAT register (Address
	 * 0x31) controls the format of the data. It is recommended that a
	 * multiple-byte read of all registers be performed to prevent a change in
	 * data between reads of sequential registers.
	 * 
	 * @param raw Array of raw values whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void readRawAcceleration(short[] raw) throws IOException {
		if (raw.length != 3)
			throw new IllegalArgumentException("Acceleration array must have a length of 3");
		
		int offset = 0;
		int size = 6;
		i2c.read(ADXL345_REGISTER_DATAX0, BUFFER, offset, size);
		
		raw[0] = (short) ((BUFFER[1] << 8) | (BUFFER[0] & 0xFF)); // x
		raw[1] = (short) ((BUFFER[3] << 8) | (BUFFER[2] & 0xFF)); // y
		raw[2] = (short) ((BUFFER[5] << 8) | (BUFFER[4] & 0xFF)); // z
	}
	
	/**
     * Determines the scaling factor of raw values to obtain Gs.
     * 
     * The scale factor changes dependent on other device settings, so be sure
     * to get the scaling factor after writing desired settings.
     * 
     * https://www.sparkfun.com/tutorials/240
     * 
     * @return Raw value scaling factor
     */
    public float getScalingFactor() {
    	int bits = resolution ? 10 + range : 10;
    	float gRange = 4f * (float) Math.pow(2, range);
    	float bitRange = (float) Math.pow(2, bits);
    	return gRange / bitRange;
	}
	
}
