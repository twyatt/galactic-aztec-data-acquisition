package edu.sdsu.rocket.pi.io.radio;

import java.nio.ByteBuffer;

import edu.sdsu.rocket.pi.io.radio.XTend900.APIFrame;

public class APIFrameHandler {
	
	private static final byte START_DELIMITER = 0x7E;

	private static final byte RX_PACKET_IDENTIFIER = (byte) 0x81;
	
	private static final int LENGTH_SIZE = 2;
	private static final ByteBuffer LENGTH_BUFFER = ByteBuffer.allocate(LENGTH_SIZE);
	private static final ByteBuffer FRAME_DATA_BUFFER = ByteBuffer.allocate(XTend900.APIFrame.MAXIMUM_FRAME_DATA_LENGTH);
	
	enum Mode {
		START_DELIMITER,
		LENGTH,
		FRAME_DATA,
		CHECKSUM,
	}
	private Mode mode = Mode.START_DELIMITER;
	
	private APIFrameListener listener;

	public APIFrameHandler(APIFrameListener listener) {
		setListener(listener);
	}
	
	public void setListener(APIFrameListener listener) {
		this.listener = listener;
	}
	
	public void onData(byte[] data) {
		int i = 0;
		byte b;
		while (i < data.length) {
			b = data[i];
			switch (mode) {
			case START_DELIMITER:
				if (b == START_DELIMITER) mode = Mode.LENGTH;
				break;
			case LENGTH:
				if (LENGTH_BUFFER.hasRemaining()) {
					LENGTH_BUFFER.put(b);
				} else {
					int length = LENGTH_BUFFER.getShort(0);
					if (length <= 0 || length > FRAME_DATA_BUFFER.capacity()) {
						System.err.println("Invalid API frame length: " + length);
						mode = Mode.START_DELIMITER;
						break;
					}
					FRAME_DATA_BUFFER.clear();
					FRAME_DATA_BUFFER.limit(length);
					mode = Mode.FRAME_DATA;
				}
				break;
			case FRAME_DATA:
				if (FRAME_DATA_BUFFER.hasRemaining()) {
					FRAME_DATA_BUFFER.put(b);
				} else {
					mode = Mode.CHECKSUM;
				}
				break;
			case CHECKSUM:
				byte[] d = new byte[FRAME_DATA_BUFFER.limit()];
				FRAME_DATA_BUFFER.get(d);
				byte checksum = b;
				if (APIFrame.verify(d, checksum)) {
					process(d);
				} else {
					System.err.println("API frame checksum failed.");
				}
				mode = Mode.START_DELIMITER;
				break;
			}
		}
	}

	private void process(byte[] data) {
		if (listener != null) {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			byte identifier = buffer.get();
			
			if (identifier == RX_PACKET_IDENTIFIER) {
				RXPacket packet = new RXPacket();
				packet.sourceAddress = buffer.getShort();
				packet.signalStrength = buffer.get();
				packet.options = buffer.get();
				packet.data = new byte[buffer.remaining()];
				buffer.get(packet.data);
				
				listener.onRXPacket(packet);
			}
		}
	}

}
