package edu.sdsu.rocket.pi.io.radio.api;

import java.nio.ByteBuffer;

public class TXRequest {

	public static final byte TX_REQUEST_API_IDENTIFIER = 0x01;
	public static final short BROADCAST_ADDRESS = (short) 0xFFFF;
	public static final byte FRAME_ID_DISABLE = 0x0;
	public static final byte OPTIONS_STANDARD    = 0x0;
	public static final byte OPTIONS_DISABLE_ACK = 0x1;
	
	// buffer size is maximum frame data length minus the identifier byte
	private static final int BUFFER_SIZE = APIFrame.MAXIMUM_FRAME_DATA_LENGTH - 1;
	
	private byte[] frameData;
	private byte checksum;
	
	public TXRequest(byte[] data) {
		this(BROADCAST_ADDRESS, data);
	}
	
	public TXRequest(short destinationAddress, byte[] data) {
		this((byte) 0x0, destinationAddress, data);
	}
	
	public TXRequest(byte frameId, short destinationAddress, byte[] data) {
		this(frameId, destinationAddress, OPTIONS_STANDARD, data);
	}
	
	public TXRequest(byte frameId, short destinationAddress, byte options, byte[] data) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.put(TX_REQUEST_API_IDENTIFIER);
		buffer.put(frameId);
		buffer.putShort(destinationAddress);
		buffer.put(options);
		buffer.put(data);
		
		frameData = new byte[buffer.position()];
		buffer.rewind();
		buffer.get(frameData);
		
		checksum = APIFrame.checksum(frameData);
	}

	public byte[] getFrameData() {
		return frameData;
	}
	
	public byte getChecksum() {
		return checksum;
	}

}
