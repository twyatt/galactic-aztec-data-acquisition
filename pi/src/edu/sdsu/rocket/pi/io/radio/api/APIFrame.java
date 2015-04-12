package edu.sdsu.rocket.pi.io.radio.api;

/*
 * Start Delimiter |   Length  |        Frame Data      | Checksum
 *       0x7E      | MSB | LSB | API Identifier | Data* |
 *      Byte 1     | Bytes 2-3 |     Byte 4     |       |  1 Byte
 *
 * *Data may be up to 2048 bytes.
 */
public class APIFrame {

	// API Identifier (4) + Data (2048)
	public static final int MAXIMUM_FRAME_DATA_LENGTH = 4 + 2048;
	
	// Start Delimiter (1) + Length (2) + Frame Data + Checksum (1)
	public static final int MAXIMUM_FRAME_LENGTH = 1 + 2 + MAXIMUM_FRAME_DATA_LENGTH + 1;

	public static final byte DEFAULT_START_DELIMITER = 0x7E;
	
	private final byte startDelimiter;
	private final byte[] frameData;
	private final byte checksum;
	
	public APIFrame(byte[] data, byte checksum) {
		this(DEFAULT_START_DELIMITER, data, checksum);
	}
	
	public APIFrame(byte startDelimiter, byte[] frameData, byte checksum) {
		if (frameData.length > MAXIMUM_FRAME_DATA_LENGTH) {
			throw new IllegalArgumentException("Frame data length " + frameData.length + " exceed maximum data length of " + MAXIMUM_FRAME_DATA_LENGTH + ".");
		}
		this.startDelimiter = startDelimiter;
		this.frameData = frameData;
		this.checksum = checksum;
	}
	
	public byte getStartDelimiter() {
		return startDelimiter;
	}
	
	public short getLength() {
		return (short) frameData.length;
	}
	
	public byte[] getFrameData() {
		return frameData;
	}
	
	public byte getChecksum() {
		return checksum;
	}

	public static byte checksum(byte[] data) {
		int total = 0;
		for (byte b : data) total += b;
		return (byte) (0xFF - (total & 0xFF));
	}
	
	public static boolean verify(byte[] data, short checksum) {
		int total = 0;
		for (byte b : data) total += b;
		total += checksum;
		return total == 0xFF;
	}
	
}
