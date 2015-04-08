package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.i2cdevlib.I2Cdev;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import edu.sdsu.rocket.pi.devices.DeviceManager.Device;

public class ITG3205 implements Device {
	
	public interface GyroscopeListener {
		public void onValues(short x, short y, short z);
	}
	protected GyroscopeListener listener;
	public void setListener(GyroscopeListener listener) {
		this.listener = listener;
	}

	private final short[] values = new short[3]; // X, Y, Z
	
	/**
	 * Address of the ITG3205 with the AL0 pin tied to GND (low).
	 * 
	 * Default for SparkFun IMU Digital Combo board.
	 */
	public static final byte ITG3205_ADDRESS_AD0_LOW = 0x68;
	
	/**
	 * Address of the ITG3205 with the AL0 pin tied to VCC (high).
	 * 
	 * Default for SparkFun ITG-3200 Breakout board.
	 */
	public static final byte ITG3205_ADDRESS_AD0_HIGH = 0x69;
	
	public static final byte ITG3205_DEFAULT_ADDRESS = ITG3205_ADDRESS_AD0_HIGH;

	public static final byte ITG3205_REGISTER_WHO_AM_I           = 0x00;
	public static final byte ITG3205_REGISTER_SMPLRT_DIV         = 0x15;
	public static final byte ITG3205_REGISTER_DLPF_FS            = 0x16;
	public static final byte ITG3205_REGISTER_INT_CFG            = 0x17;
	public static final byte ITG3205_REGISTER_INT_STATUS         = 0x1A;
	public static final byte ITG3205_REGISTER_TEMP_OUT_H         = 0x1B;
	public static final byte ITG3205_REGISTER_TEMP_OUT_L         = 0x1C;
	public static final byte ITG3205_REGISTER_GYRO_XOUT_H        = 0x1D;
	public static final byte ITG3205_REGISTER_GYRO_XOUT_L        = 0x1E;
	public static final byte ITG3205_REGISTER_GYRO_YOUT_H        = 0x1F;
	public static final byte ITG3205_REGISTER_GYRO_YOUT_L        = 0x20;
	public static final byte ITG3205_REGISTER_GYRO_ZOUT_H        = 0x21;
	public static final byte ITG3205_REGISTER_GYRO_ZOUT_L        = 0x22;
	public static final byte ITG3205_REGISTER_PWR_MGM            = 0x3E;

	public static final byte ITG3205_DEVID_BIT                   = 6;
	public static final byte ITG3205_DEVID_LENGTH                = 6;

	public static final byte ITG3205_DF_FS_SEL_BIT               = 4;
	public static final byte ITG3205_DF_FS_SEL_LENGTH            = 2;
	public static final byte ITG3205_DF_DLPF_CFG_BIT             = 2;
	public static final byte ITG3205_DF_DLPF_CFG_LENGTH          = 3;

	public static final byte ITG3205_FULLSCALE_2000              = 0x03;

	public static final byte ITG3205_DLPF_BW_256                 = 0x00;
	public static final byte ITG3205_DLPF_BW_188                 = 0x01;
	public static final byte ITG3205_DLPF_BW_98                  = 0x02;
	public static final byte ITG3205_DLPF_BW_42                  = 0x03;
	public static final byte ITG3205_DLPF_BW_20                  = 0x04;
	public static final byte ITG3205_DLPF_BW_10                  = 0x05;
	public static final byte ITG3205_DLPF_BW_5                   = 0x06;

	public static final byte ITG3205_INTCFG_ACTL_BIT             = 7;
	public static final byte ITG3205_INTCFG_OPEN_BIT             = 6;
	public static final byte ITG3205_INTCFG_LATCH_INT_EN_BIT     = 5;
	public static final byte ITG3205_INTCFG_INT_ANYRD_2CLEAR_BIT = 4;
	public static final byte ITG3205_INTCFG_ITG_RDY_EN_BIT       = 2;
	public static final byte ITG3205_INTCFG_RAW_RDY_EN_BIT       = 0;

	public static final byte ITG3205_INTMODE_ACTIVEHIGH          = 0x00;
	public static final byte ITG3205_INTMODE_ACTIVELOW           = 0x01;

	public static final byte ITG3205_INTDRV_PUSHPULL             = 0x00;
	public static final byte ITG3205_INTDRV_OPENDRAIN            = 0x01;

	public static final byte ITG3205_INTLATCH_50USPULSE          = 0x00;
	public static final byte ITG3205_INTLATCH_WAITCLEAR          = 0x01;

	public static final byte ITG3205_INTCLEAR_STATUSREAD         = 0x00;
	public static final byte ITG3205_INTCLEAR_ANYREAD            = 0x01;

	public static final byte ITG3205_INTSTAT_ITG_RDY_BIT         = 2;
	public static final byte ITG3205_INTSTAT_RAW_DATA_READY_BIT  = 0;

	public static final byte ITG3205_PWR_H_RESET_BIT             = 7;
	public static final byte ITG3205_PWR_SLEEP_BIT               = 6;
	public static final byte ITG3205_PWR_STBY_XG_BIT             = 5;
	public static final byte ITG3205_PWR_STBY_YG_BIT             = 4;
	public static final byte ITG3205_PWR_STBY_ZG_BIT             = 3;
	public static final byte ITG3205_PWR_CLK_SEL_BIT             = 2;
	public static final byte ITG3205_PWR_CLK_SEL_LENGTH          = 3;

	public static final byte ITG3205_CLOCK_INTERNAL              = 0x00;
	public static final byte ITG3205_CLOCK_PLL_XGYRO             = 0x01;
	public static final byte ITG3205_CLOCK_PLL_YGYRO             = 0x02;
	public static final byte ITG3205_CLOCK_PLL_ZGYRO             = 0x03;
	public static final byte ITG3205_CLOCK_PLL_EXT32K            = 0x04;
	public static final byte ITG3205_CLOCK_PLL_EXT19M            = 0x05;
	
	public static final float ITG3205_SENSITIVITY_SCALE_FACTOR = 14.375f;
	
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

    public ITG3205() {
    	this(I2CBus.BUS_1);
    }
    
    public ITG3205(int bus) {
    	this(bus, ITG3205_DEFAULT_ADDRESS);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param bus
     * @param address I2C address
     */
    public ITG3205(int bus, int address) {
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
		
		writeFullScaleRange(ITG3205_FULLSCALE_2000);
	    writeClockSource(ITG3205_CLOCK_PLL_XGYRO);
	}

	/**
	 * Verify the device ID.
	 * 
	 * @return True if device ID is valid, false otherwise
	 * @throws IOException 
	 */
	public boolean verifyDeviceID() throws IOException {
	    return readDeviceID() == 0b110100;
	}
	
	/**
	 * Reads the device ID.
	 * 
	 * Register 0 - Who Am I
	 * 
	 * This register is used to verify the identity of the device.
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readDeviceID() throws IOException {
		return I2Cdev.readBits(i2c, ITG3205_REGISTER_WHO_AM_I, ITG3205_DEVID_BIT, ITG3205_DEVID_LENGTH);
	}
	
	/**
	 * Read sample rate divider.
	 * 
	 * Register 21 - Sample Rate Divider
	 * 
	 * This register determines the sample rate of the ITG-3205 gyros. The gyros
	 * outputs are sampled internally at either 1kHz or 8kHz, determined by the
	 * DLPF_CFG setting (see register 22). This sampling is then filtered
	 * digitally and delivered into the sensor registers after the number of
	 * cycles determined by this register. The sample rate is given by the
	 * following formula:
	 * 
	 * F_sample = F_internal / (divider+1), where F_internal is either 1kHz or 8kHz
	 * 
	 * As an example, if the internal sampling is at 1kHz, then setting this
	 * register to 7 would give the following:
	 * 
	 * F_sample = 1kHz / (7 + 1) = 125Hz, or 8ms per sample
	 *
	 * @return Current sample rate
	 * @throws IOException 
	 */
	public int readSampleRateDivider() throws IOException {
		return i2c.read(ITG3205_REGISTER_SMPLRT_DIV);
	}
	
	/**
	 * Write sample rate divider.
	 * 
	 * Register 21 - Sample Rate Divider
	 * 
	 * This register determines the sample rate of the ITG-3205 gyros. The gyros
	 * outputs are sampled internally at either 1kHz or 8kHz, determined by the
	 * DLPF_CFG setting (see register 22). This sampling is then filtered
	 * digitally and delivered into the sensor registers after the number of
	 * cycles determined by this register. The sample rate is given by the
	 * following formula:
	 * 
	 * F_sample = F_internal / (divider+1), where F_internal is either 1kHz or 8kHz
	 * 
	 * As an example, if the internal sampling is at 1kHz, then setting this
	 * register to 7 would give the following:
	 * 
	 * F_sample = 1kHz / (7 + 1) = 125Hz, or 8ms per sample
	 *
	 * @param rate Sample rate divider
	 * @throws IOException 
	 */
	public void writeSampleRateDivider(int rate) throws IOException {
		i2c.write(ITG3205_REGISTER_SMPLRT_DIV, (byte) (rate & 0xFF));
	}

	/**
	 * Read full-scale range.
	 * 
	 * Register 22 - DLPF, Full Scale
	 * 
	 * The FS_SEL parameter allows setting the full-scale range of the gyro
	 * sensors, as described in the table below. The power-on-reset value of
	 * FS_SEL is 00h. Set to 03h for proper operation.
	 * 
	 * <pre>
	 * FS_SEL | Gyro Full-Scale Range
	 *    0   |        Reserved 
	 *    1   |        Reserved 
	 *    2   |        Reserved
	 *    3   |    +/- 2000 deg/sec
	 * </pre>
	 *
	 * @return Current full-scale range setting
	 * @throws IOException 
	 */
	public int readFullScaleRange() throws IOException {
		return I2Cdev.readBits(i2c, ITG3205_REGISTER_DLPF_FS, ITG3205_DF_FS_SEL_BIT, ITG3205_DF_FS_SEL_LENGTH);
	}
	
	/**
	 * Write full-scale range.
	 * 
	 * Register 22 - DLPF, Full Scale
	 * 
	 * The FS_SEL parameter allows setting the full-scale range of the gyro
	 * sensors, as described in the table below. The power-on-reset value of
	 * FS_SEL is 00h. Set to 03h for proper operation.
	 * 
	 * <pre>
	 * FS_SEL | Gyro Full-Scale Range
	 *    0   |        Reserved 
	 *    1   |        Reserved 
	 *    2   |        Reserved
	 *    3   |    +/- 2000 deg/sec
	 * </pre>
	 *
	 * @return Current full-scale range setting
	 * @throws IOException 
	 */
	public void writeFullScaleRange(int range) throws IOException {
		I2Cdev.writeBits(i2c, ITG3205_REGISTER_DLPF_FS, ITG3205_DF_FS_SEL_BIT, ITG3205_DF_FS_SEL_LENGTH, range);
	}
	
	/**
	 * Read digital low-pass filter bandwidth.
	 * 
	 * Register 22 - DLPF, Full Scale
	 * 
	 * The DLPF_CFG parameter sets the digital low pass filter configuration. It
	 * also determines the internal sampling rate used by the device as shown in
	 * the table below.
	 *
	 * <pre>
	 * DLPF_CFG | Low Pass Filter Bandwidth | Internal Sample Rate
	 *     0    |            256Hz          |         8kHz
	 *     1    |            188Hz          |         1kHz
	 *     2    |             98Hz          |         1kHz
	 *     3    |             42Hz          |         1kHz
	 *     4    |             20Hz          |         1kHz
	 *     5    |             10Hz          |         1kHz
	 *     6    |              5Hz          |         1kHz
	 *     7    |          Reserved         |       Reserved
	 * </pre>
	 *
	 * @return DLFP bandwidth setting
	 * @throws IOException 
	 */
	public int readDLPFBandwidth() throws IOException {
		return I2Cdev.readBits(i2c, ITG3205_REGISTER_DLPF_FS, ITG3205_DF_DLPF_CFG_BIT, ITG3205_DF_DLPF_CFG_LENGTH);
	}
	
	/**
	 * Write digital low-pass filter bandwidth.
	 * 
	 * Register 22 - DLPF, Full Scale
	 * 
	 * The DLPF_CFG parameter sets the digital low pass filter configuration. It
	 * also determines the internal sampling rate used by the device as shown in
	 * the table below.
	 *
	 * <pre>
	 * DLPF_CFG | Low Pass Filter Bandwidth | Internal Sample Rate
	 *     0    |            256Hz          |         8kHz
	 *     1    |            188Hz          |         1kHz
	 *     2    |             98Hz          |         1kHz
	 *     3    |             42Hz          |         1kHz
	 *     4    |             20Hz          |         1kHz
	 *     5    |             10Hz          |         1kHz
	 *     6    |              5Hz          |         1kHz
	 *     7    |          Reserved         |       Reserved
	 * </pre>
	 *
	 * @param bandwidth DLFP bandwidth setting
	 * @throws IOException 
	 */
	public void writeDLPFBandwidth(int bandwidth) throws IOException {
		I2Cdev.writeBits(i2c, ITG3205_REGISTER_DLPF_FS, ITG3205_DF_DLPF_CFG_BIT, ITG3205_DF_DLPF_CFG_LENGTH, bandwidth);
	}
	
	/**
	 * Read clock source setting.
	 * 
	 * Register 62 - Power Management
	 *
	 * The CLK_SEL setting determines the device clock source as follows:
	 *
	 * <pre>
	 * CLK_SEL |             Clock Source
	 *    0    | Internal oscillator
	 *    1    | PLL with X Gyro reference
	 *    2    | PLL with Y Gyro reference
	 *    3    | PLL with Z Gyro reference
	 *    4    | PLL with external 32.768kHz reference
	 *    5    | PLL with external 19.2MHz reference
	 *    6    | Reserved
	 *    7    | Reserved
	 * </pre>
	 *
	 * On power up, the ITG-3205 defaults to the internal oscillator. It is
	 * highly recommended that the device is configured to use one of the gyros
	 * (or an external clock) as the clock reference, due to the improved
	 * stability.
	 * 
	 * @return Clock source setting
	 * @throws IOException 
	 */
	public int readClockSource() throws IOException {
		return I2Cdev.readBits(i2c, ITG3205_REGISTER_PWR_MGM, ITG3205_PWR_CLK_SEL_BIT, ITG3205_PWR_CLK_SEL_LENGTH);
	}
	
	/**
	 * Write clock source setting.
	 * 
	 * Register 62 - Power Management
	 *
	 * The CLK_SEL setting determines the device clock source as follows:
	 *
	 * <pre>
	 * CLK_SEL |             Clock Source
	 *    0    | Internal oscillator
	 *    1    | PLL with X Gyro reference
	 *    2    | PLL with Y Gyro reference
	 *    3    | PLL with Z Gyro reference
	 *    4    | PLL with external 32.768kHz reference
	 *    5    | PLL with external 19.2MHz reference
	 *    6    | Reserved
	 *    7    | Reserved
	 * </pre>
	 *
	 * On power up, the ITG-3205 defaults to the internal oscillator. It is
	 * highly recommended that the device is configured to use one of the gyros
	 * (or an external clock) as the clock reference, due to the improved
	 * stability.
	 * 
	 * @param source Clock source setting
	 * @throws IOException 
	 */
	public void writeClockSource(int source) throws IOException {
		I2Cdev.writeBits(i2c, ITG3205_REGISTER_PWR_MGM, ITG3205_PWR_CLK_SEL_BIT, ITG3205_PWR_CLK_SEL_LENGTH, source);
	}
	
	/**
	 * Issue a device reset.
	 * 
	 * Reset device and internal registers to the power-up-default settings.
	 * @throws IOException 
	 */
	public void reset() throws IOException {
		I2Cdev.writeBit(i2c, ITG3205_REGISTER_PWR_MGM, ITG3205_PWR_H_RESET_BIT, true);
	}
	
	/**
	 * Read raw temperature data.
	 * 
	 * Registers 27 to 28 - Sensor Registers
	 * 
	 * This register contains the temperature sensor data for the ITG-3205
	 * parts. At any time, this value ￼can be read from the device; however it is
	 * best to use the interrupt function to determine when new data is
	 * available.
	 * 
	 * @return 16-bit temperature data
	 * @throws IOException
	 */
	public short readRawTemperature() throws IOException {
		int offset = 0;
		int size = 2; // 16 bits
		i2c.read(ITG3205_REGISTER_TEMP_OUT_H, BUFFER, offset, size);
		
		return (short) ((BUFFER[0] << 8) | (BUFFER[1] & 0xFF));
	}
	
	/**
	 * Read raw gyro data.
	 * 
	 * Registers 29 to 34 - Sensor Registers
	 * 
	 * These registers contain the gyro sensor data for the ITG-3205 parts. At
	 * any time, these values ￼can be read from the device; however it is best to
	 * use the interrupt function to determine when new data is available.
	 * 
	 * @param raw Array of raw values whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void readRawRotations(short[] raw) throws IOException {
		if (raw.length != 3)
			throw new IllegalArgumentException("Raw rotation array must have a length of 3");
		
		int offset = 0;
		int size = 6;
		i2c.read(ITG3205_REGISTER_GYRO_XOUT_H, BUFFER, offset, size);
		
		raw[0] = (short) ((BUFFER[0] << 8) | (BUFFER[1] & 0xFF)); // x
		raw[1] = (short) ((BUFFER[2] << 8) | (BUFFER[3] & 0xFF)); // y
		raw[2] = (short) ((BUFFER[4] << 8) | (BUFFER[5] & 0xFF)); // z
	}
	
	@Override
	public void loop() throws IOException {
		readRawRotations(values);
		
		if (listener != null) {
			listener.onValues(values[0], values[1], values[2]);
		}
	}
	
}
