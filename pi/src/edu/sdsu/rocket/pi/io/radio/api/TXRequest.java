package edu.sdsu.rocket.pi.io.radio.api;

import java.nio.ByteBuffer;

public class TXRequest {

	private static final byte TX_REQUEST_API_IDENTIFIER = 0x01;
	private static final int BROADCAST_ADDRESS = 0xFFFF;
	
	// buffer size is maximum frame data length minus the identifier byte
	private static final int BUFFER_SIZE = APIFrame.MAXIMUM_FRAME_DATA_LENGTH - 1;
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	public TXRequest(byte[] data) {
		this(BROADCAST_ADDRESS, data);
	}
	
	public TXRequest(int destinationAddress, byte[] data) {
		this((byte) 0x0, destinationAddress, data);
	}
	
	public TXRequest(byte frameId, int destinationAddress, byte[] data) {
		buffer.put(TX_REQUEST_API_IDENTIFIER);
		buffer.put(frameId);
		buffer.putInt(destinationAddress);
		buffer.put(data);
		buffer.put(APIFrame.checksum(data));
	}

	public byte[] getFrameData() {
		byte[] frameData = new byte[buffer.position()];
		buffer.get(frameData);
		return frameData;
	}

}
