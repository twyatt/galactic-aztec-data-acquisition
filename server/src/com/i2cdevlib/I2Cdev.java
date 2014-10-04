package com.i2cdevlib;

import java.io.IOException;

import com.pi4j.io.i2c.I2CDevice;

/**
 * Partial adaptation from:
 * https://github.com/jrowberg/i2cdevlib/blob/master/MSP430/I2Cdev/I2Cdev.cpp
 */
public class I2Cdev {
	
	private static final byte[] WORD_BUFFER = new byte[2];
	
	/** Read a single bit from an 8-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to read from
	 * @param bitNum Bit position to read (0-7)
	 * @return Single bit value
	 * @throws IOException 
	 */
	public static boolean readBit(I2CDevice i2c, int regAddr, int bitNum) throws IOException {
		int b = i2c.read(regAddr);
		
		// http://stackoverflow.com/a/14145767/196486
	    return ((b >> bitNum) & 1) == 1;
	}

	/** Write a single bit in an 8-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to write to
	 * @param bitNum Bit position to write (0-7)
	 * @param value New bit value to write
	 * @return 
	 * @throws IOException 
	 */
	public static void writeBit(I2CDevice i2c, int regAddr, int bitNum, boolean value) throws IOException {
	    int b = i2c.read(regAddr);
	    b = value ? (b | (1 << bitNum)) : (b & ~(1 << bitNum));
	    i2c.write(regAddr, (byte) (b & 0xFF));
	}
	
	/** Read multiple bits from an 8-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to read from
	 * @param bitStart First bit position to read (0-7)
	 * @param length Number of bits to read (not more than 8)
	 * @return right-aligned value (i.e. '101' read from any bitStart position will equal 0x05)
	 * @throws IOException 
	 */
	public static int readBits(I2CDevice i2c, int regAddr, int bitStart, int length) throws IOException {
	    // 01101001 read byte
	    // 76543210 bit numbers
	    //    xxx   args: bitStart=4, length=3
	    //    010   masked
	    //   -> 010 shifted
		int b = i2c.read(regAddr);
		int mask = ((1 << length) - 1) << (bitStart - length + 1);
        b &= mask;
        b >>= (bitStart - length + 1);
	    return b;
	}
	
	/** Write multiple bits in an 8-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to write to
	 * @param bitStart First bit position to write (0-7)
	 * @param length Number of bits to write (not more than 8)
	 * @param data Right-aligned value to write
	 * @return 
	 * @throws IOException 
	 */
	public static void writeBits(I2CDevice i2c, int regAddr, int bitStart, int length, int data) throws IOException {
	    //      010 value to write
	    // 76543210 bit numbers
	    //    xxx   args: bitStart=4, length=3
	    // 00011100 mask byte
	    // 10101111 original value (sample)
	    // 10100011 original & ~mask
	    // 10101011 masked | value
		int b = i2c.read(regAddr);
        int mask = ((1 << length) - 1) << (bitStart - length + 1);
        data <<= (bitStart - length + 1); // shift data into correct position
        data &= mask; // zero all non-important bits in data
        b &= ~(mask); // zero all important bits in existing byte
        b |= data; // combine data with existing byte
        i2c.write(regAddr, (byte) (b & 0xFF));
	}
	
	/** write a single bit in a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to write to
	 * @param bitNum Bit position to write (0-15)
	 * @param value New bit value to write
	 * @throws IOException 
	 */
	public static void writeBitW(I2CDevice i2c, int regAddr, int bitNum, int value) throws IOException {
		writeBitW(i2c, regAddr, bitNum, value != 0);
	}
	
	/** write a single bit in a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to write to
	 * @param bitNum Bit position to write (0-15)
	 * @param value New bit value to write
	 * @throws IOException 
	 */
	public static void writeBitW(I2CDevice i2c, int regAddr, int bitNum, boolean value) throws IOException {
		int w = readWord(i2c, regAddr);
		w = value ? (w | (1 << bitNum)) : (w & ~(1 << bitNum));
		writeWord(i2c, regAddr, w);
	}
	
	/** Write multiple bits in a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to write to
	 * @param bitStart First bit position to write (0-15)
	 * @param length Number of bits to write (not more than 16)
	 * @param data Right-aligned value to write
	 * @throws IOException 
	 */
	public static void writeBitsW(I2CDevice i2c, int regAddr, int bitStart, int length, int data) throws IOException {
		// 010 value to write
		// fedcba9876543210 bit numbers
		// xxx args: bitStart=12, length=3
		// 0001110000000000 mask byte
		// 1010111110010110 original value (sample)
		// 1010001110010110 original & ~mask
		// 1010101110010110 masked | value
		int w = readWord(i2c, regAddr);
		int mask = ((1 << length) - 1) << (bitStart - length + 1);
		data <<= (bitStart - length + 1); // shift data into correct position
		data &= mask; // zero all non-important bits in data
		w &= ~(mask); // zero all important bits in existing word
		w |= data; // combine data with existing word
		writeWord(i2c, regAddr, w);
	}
	
	/** Read single word from a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to read from
	 * @throws IOException 
	 */
	public static short readWord(I2CDevice i2c, int regAddr) throws IOException {
		i2c.read(regAddr, WORD_BUFFER, 0 /* offset */, 2 /* size */);
		return (short) ((WORD_BUFFER[0] << 8) | (WORD_BUFFER[1] & 0xFF));
	}
	
	/** Write single word to a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register address to write to
	 * @param data New word value to write
	 * @throws IOException 
	 */
	public static void writeWord(I2CDevice i2c, int regAddr, int data) throws IOException {
		WORD_BUFFER[0] = (byte) ((data >> 8) & 0xFF);
		WORD_BUFFER[1] = (byte) (data & 0xFF);
		i2c.write(regAddr, WORD_BUFFER, 0 /* offset */, 2 /* size */);
	}

	/** Read a single bit from a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to read from
	 * @param bitNum Bit position to read (0-15)
	 * @return Value (bit value of 1 = true, bit value of 0 = false)
	 * @throws IOException 
	 */
	public static boolean readBitW(I2CDevice i2c, int regAddr, int bitNum) throws IOException {
	    int b = readWord(i2c, regAddr);
	    return ((b >> bitNum) & 0x01) == 0x01;
	}

	/** Read multiple bits from a 16-bit device register.
	 * @param i2c I2C slave device
	 * @param regAddr Register regAddr to read from
	 * @param bitStart First bit position to read (0-15)
	 * @param length Number of bits to read (not more than 16)
	 * @return Right-aligned value (i.e. '101' read from any bitStart position will equal 0x05)
	 * @throws IOException 
	 */
	public static short readBitsW(I2CDevice i2c, int regAddr, int bitStart, byte length) throws IOException {
		// 1101011001101001 read byte
	    // fedcba9876543210 bit numbers
	    //    xxx           args: bitStart=12, length=3
	    //    010           masked
	    //           -> 010 shifted
	    short w = readWord(i2c, regAddr);
        int mask = ((1 << length) - 1) << (bitStart - length + 1);
        w &= mask;
        w >>= (bitStart - length + 1);
        return w;
	}
	
}
