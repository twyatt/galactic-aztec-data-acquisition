package edu.sdsu.rocket.data.server.devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * Barometer
 * 
 * High resolution module, 10 cm
 * Supply voltage 1.8 to 3.6 V
 * Operating range: 10 to 1200 mbar, -40 to +85 C
 * 
 * The MS5611-01BA has only five basic commands:
 *   1. Reset
 *   2. Read PROM (128 bit of calibration words)
 *   3. D1 conversion
 *   4. D2 conversion
 *   5. Read ADC result (24 bit pressure / temperature)
 * 
 * http://www.embeddedadventures.com/barometric_pressure_sensor_module_mod-1009.html
 */
public class MS5611 {
	
	/**
	 * Address of the MS5611 with the CSB pin tied to GND (low).
	 */
	public static final byte MS5611_ADDRESS_CSB_LOW = 0x76;
	
	/**
	 * Address of the MS5611 with the CSB pin tied to VCC (high).
	 */
	public static final byte MS5611_ADDRESS_CSB_HIGH = 0x77;
	
	public static final byte MS5611_DEFAULT_ADDRESS = MS5611_ADDRESS_CSB_LOW;
	
	/**
	 * The reset can be sent at any time. In the event that there is not a
	 * successful power on reset this may be caused by the SDA being blocked by
	 * the module in the acknowledge state. The only way to get the MS5611-01BA
	 * to function is to send several SCLKs followed by a reset sequence or to
	 * repeat power on reset.
	 */
	public static final byte MS5611_RESET = 0x1E;
	
	/**
	 * Duration to sleep thread after a reset command, per AN520 example C-code.
	 */
	public static final long MS5611_RESET_DELAY = 3L; // ms
	
	public static final byte MS5611_CONVERT_D1_OSR_256  = 0x40;
	public static final byte MS5611_CONVERT_D1_OSR_512  = 0x42;
	public static final byte MS5611_CONVERT_D1_OSR_1024 = 0x44;
	public static final byte MS5611_CONVERT_D1_OSR_2048 = 0x46;
	public static final byte MS5611_CONVERT_D1_OSR_4096 = 0x48;
	
	public static final byte MS5611_CONVERT_D2_OSR_256  = 0x50;
	public static final byte MS5611_CONVERT_D2_OSR_512  = 0x52;
	public static final byte MS5611_CONVERT_D2_OSR_1024 = 0x54;
	public static final byte MS5611_CONVERT_D2_OSR_2048 = 0x56;
	public static final byte MS5611_CONVERT_D2_OSR_4096 = 0x58;
	
	public static final byte MS5611_ADC_READ = 0x00;
	
	public static final byte MS5611_PROM_READ = (byte) 0xA0; // 1010[Ad2][Ad1][Ad0]0

	/**
	 * CRC4 algorithm ported from AN520 example C-code.
	 * 
	 * @param prom
	 * @return
	 */
	public static byte crc4(int prom[]) {
		int cnt;      // simple counter
		int n_rem;    // crc reminder
		int crc_read; // original value of the crc
		byte n_bit;
		
		n_rem = 0x00;
		crc_read = prom[7]; // save read CRC
		prom[7] &= 0xFF00;  // CRC byte is replaced by 0
		
		for (cnt = 0; cnt < 16; cnt++) { // operation is performed on bytes
			// choose LSB or MSB
			if (cnt % 2 == 1) {
				n_rem ^= prom[cnt >> 1] & 0x00FF;
			} else {
				n_rem ^= prom[cnt >> 1] >> 8;
			}
			
			for (n_bit = 8; n_bit > 0; n_bit--) {
				if ((n_rem & 0x8000) == 0x8000) {
					n_rem = (n_rem << 1) ^ 0x3000;
				} else {
					n_rem <<= 1;
				}
			}
		}
		
		n_rem = 0x000F & (n_rem >> 12); // final 4-bit reminder is CRC code
		prom[7] = crc_read; // restore the crc_read to its original place
		return (byte) (n_rem ^ 0x00);
	}
	
	/**
	 * Method that verifies that the CRC function has been implemented properly.
	 * 
	 * @return True if CRC function works as expected, false otherwise.
	 */
	public static boolean testCRC() {
		int prom[] = { 0x3132, 0x3334, 0x3536, 0x3738, 0x3940, 0x4142, 0x4344, 0x4500 };
		return (crc4(prom) == 0x0B);
	}
	
	private long SENS_T1;
	private long OFF_T1;
	private float TCS;
	private float TCO;
	private long T_REF;
	private float TEMPSENS;
	
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
    private final byte[] BUFFER = new byte[3];

    public MS5611(int bus) {
    	this(bus, MS5611_DEFAULT_ADDRESS);
	}
    
    /**
     * Specific address constructor.
     * 
     * @param bus
     * @param address I2C address
     */
    public MS5611(int bus, int address) {
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
		
		reset();
		try {
			Thread.sleep(MS5611_RESET_DELAY);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readPROM();
	}

	/**
	 * Resets sensor.
	 * 
	 * The Reset sequence shall be sent once after power-on to make sure that
	 * the calibration PROM gets loaded into the internal register. It can be
	 * also used to reset the device ROM from an unknown condition.
	 * 
	 * @throws IOException
	 */
	public void reset() throws IOException {
		i2c.write(MS5611_RESET);
	}

	/**
	 * Read PROM.
	 * 
	 * The PROM Read command consists of two parts. First command sets up the
	 * system into PROM read mode. The second part gets the data from the
	 * system.
	 * 
	 * The read command for PROM shall be executed once after reset by the user
	 * to read the content of the calibration PROM and to calculate the
	 * calibration coefficients. There are in total 8 addresses resulting in a
	 * total memory of 128 bit. Address 0 contains factory data and the setup,
	 * addresses 1-6 calibration coefficients and address 7 contains the serial
	 * code and CRC. The command sequence is 8 bits long with a 16 bit result
	 * which is clocked with the MSB first.
	 * 
	 * <pre>
	 * Prom is returned as an int array of length 8:
	 * 
	 * prom[0] = 16 bit reserved for manufacturer
	 * prom[1] = Coefficient 1 (16 bit unsigned)
	 * prom[2] = Coefficient 2 (16 bit unsigned)
	 * prom[3] = Coefficient 3 (16 bit unsigned)
	 * prom[4] = Coefficient 4 (16 bit unsigned)
	 * prom[5] = Coefficient 5 (16 bit unsigned)
	 * prom[6] = Coefficient 6 (16 bit unsigned)
	 * prom[7] = 4 bit CRC
	 * </pre>
	 * 
	 * @throws IOException 
	 * @return PROM
	 */
	public int[] readPROM() throws IOException {
		int[] prom = new int[8];
		for (int i = 0; i < 8; i++) {
			i2c.write((byte) (MS5611_PROM_READ + i * 2));
			i2c.read(BUFFER, 0 /* offset */, 2 /* size */);
			prom[i] = ((BUFFER[0] & 0xFF) << 8) | (BUFFER[1] & 0xFF);
		}
		
		int C1 = prom[1];
		int C2 = prom[2];
		int C3 = prom[3];
		int C4 = prom[4];
		int C5 = prom[5];
		int C6 = prom[6];
		byte CRC = (byte) (prom[7] & 0x0F);
		
		byte crc4 = crc4(prom);
		if (crc4 != CRC) {
			throw new IOException("PROM CRC mismatch, " + CRC + " (read) != " + crc4 + " (calculated)");
		}
		
		SENS_T1  = C1 * 32768L /* 2^15 */;
		OFF_T1   = C2 * 65536L /* 2^16 */;
		TCS      = C3 / 256f /* 2^8 */;
		TCO      = C4 / 128f /* 2^7 */;
		T_REF    = C5 * 256L /* 2^8 */;
		TEMPSENS = C6 / 8388608f /* 2^23 */;
		
		return prom;
	}
	
	/**
	 * Writes conversion command.
	 * 
	 * A conversion can be started by sending the command to MS5611-01BA. When
	 * command is sent to the system it stays busy until conversion is done.
	 * When conversion is finished the data can be accessed by sending a Read
	 * command, when an acknowledge appears from the MS5611-01BA, 24 SCLK cycles
	 * may be sent to receive all result bits. Every 8 bit the system waits for
	 * an acknowledge signal.
	 * 
	 * @param conversion
	 * @return Typical conversion time (ns)
	 * @throws IOException 
	 */
	public int writeConversion(byte conversion) throws IOException {
		i2c.write(conversion);
		
		switch (conversion) {
		case MS5611_CONVERT_D1_OSR_4096:
		case MS5611_CONVERT_D2_OSR_4096:
			return 9040000;
		case MS5611_CONVERT_D1_OSR_2048:
		case MS5611_CONVERT_D2_OSR_2048:
			return 4540000;
		case MS5611_CONVERT_D1_OSR_1024:
		case MS5611_CONVERT_D2_OSR_1024:
			return 2280000;
		case MS5611_CONVERT_D1_OSR_512:
		case MS5611_CONVERT_D2_OSR_512:
			return 1170000;
		case MS5611_CONVERT_D1_OSR_256:
		case MS5611_CONVERT_D2_OSR_256:
			return 600000;
		default:
			return 0;
		}
	}
	
	/**
	 * Read ADC.
	 * 
	 * @return Raw ADC value
	 * @throws IOException
	 */
	public long readADC() throws IOException {
		i2c.write(MS5611_ADC_READ);
		i2c.read(BUFFER, 0 /* offset */, 3 /* size */);
		return ((BUFFER[0] & 0xFF) << 16) | ((BUFFER[1] & 0xFF) << 8) | (BUFFER[2] & 0xFF);
	}
	
	/**
	 * Converts digital temperature value to 100 times actual temperature.
	 * 
	 * To get correct decimal temperature, divide return of this method by 100f.
	 * 
	 * @param D2 Digital temperature value
	 * @return Actual temperature (C) x 100
	 */
	public int getTemperature(long D2) {
		long dT = D2 - T_REF;
		return (int) (2000 + dT * TEMPSENS);
	}
	
	/**
	 * Converts digital pressure value to 100 times actual pressure.
	 * 
	 * To get correct decimal pressure, divide return of this method by 100f.
	 * 
	 * @param D1 Digital pressure value
	 * @param D2 Digital temperature value
	 * @return Actual pressure (mbar) x 100
	 */
	public int getPressure(long D1, long D2) {
		int dT    = (int) (D2 - T_REF);
		long OFF  = (long) (OFF_T1  + dT * TCO);
		long SENS = (long) (SENS_T1 + dT * TCS);
		return (int) ((D1 * SENS / 2097152f /* 2^21 */ - OFF) / 32768f /* 2^15 */);
	}
	
}
