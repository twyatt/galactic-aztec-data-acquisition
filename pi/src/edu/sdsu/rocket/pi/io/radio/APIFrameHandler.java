package edu.sdsu.rocket.pi.io.radio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.sdsu.rocket.core.helpers.ByteHelper;
import edu.sdsu.rocket.pi.io.radio.api.APIFrame;
import edu.sdsu.rocket.pi.io.radio.api.RFModuleStatus;
import edu.sdsu.rocket.pi.io.radio.api.RXPacket;
import edu.sdsu.rocket.pi.io.radio.api.TXStatus;

public class APIFrameHandler {
	
	private static final byte START_DELIMITER = 0x7E;

	private static final byte RX_PACKET_IDENTIFIER       = (byte) 0x81;
	private static final byte RF_MODULE_STAUS_IDENTIFIER = (byte) 0x8A;
	private static final byte TX_STATUS_IDENTIFIER       = (byte) 0x89;
	
	final CopyOnWriteArrayList<APIFrameListener> listeners = new CopyOnWriteArrayList<>();
	
	private static final int LENGTH_SIZE = 2;
	private static final ByteBuffer LENGTH_BUFFER = ByteBuffer.allocate(LENGTH_SIZE);
	private static final ByteBuffer FRAME_DATA_BUFFER = ByteBuffer.allocate(APIFrame.MAXIMUM_FRAME_DATA_LENGTH);
	
	enum Mode {
		START_DELIMITER,
		LENGTH,
		FRAME_DATA,
		CHECKSUM,
	}
	private Mode mode = Mode.START_DELIMITER;
	
	public synchronized void addListener(APIFrameListener ... listener) {
		Collections.addAll(listeners, listener);
	}
	
	public synchronized void removeListener(APIFrameListener ... listener) {
		for (APIFrameListener l : listener) {
			listeners.remove(l);
		}
	}
	
	public void onData(byte[] data) {
//		System.out.println(ByteHelper.bytesToHexString(data));
		int i = 0;
		byte b;
		
		while (i < data.length) {
			b = data[i++];
			
			switch (mode) {
			case START_DELIMITER:
				if (b == START_DELIMITER) mode = Mode.LENGTH;
				break;
			case LENGTH:
				if (LENGTH_BUFFER.hasRemaining()) {
					LENGTH_BUFFER.put(b);
				}
				if (!LENGTH_BUFFER.hasRemaining()) {
					int length = LENGTH_BUFFER.getShort(0);
					if (length <= 0 || length > FRAME_DATA_BUFFER.capacity()) {
						System.err.println("Invalid API frame length: " + length);
						reset();
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
				}
				if (!FRAME_DATA_BUFFER.hasRemaining()) {
					mode = Mode.CHECKSUM;
				}
				break;
			case CHECKSUM:
				byte checksum = b;
				byte[] d = new byte[FRAME_DATA_BUFFER.limit()];
				FRAME_DATA_BUFFER.rewind();
				FRAME_DATA_BUFFER.get(d);
				if (APIFrame.verify(d, checksum)) {
					process(d);
				} else {
					System.err.println("API frame checksum failed: " + ByteHelper.byteToHexString(checksum) + " (received) vs. " + ByteHelper.byteToHexString(APIFrame.checksum(d)) + " (calculated).");
				}
				reset();
				break;
			}
		}
	}

	private void reset() {
		mode = Mode.START_DELIMITER;
		LENGTH_BUFFER.clear();
		FRAME_DATA_BUFFER.clear();
	}

	private void process(byte[] data) {
		if (data == null || data.length == 0) {
			System.err.println("Invalid frame data length: " + (data == null ? "null" : data.length));
		}
		
		Collection<APIFrameListener> listenersCopy = new ArrayList<APIFrameListener>(listeners);
		
		byte identifier = data[0];
		for (APIFrameListener listener : listenersCopy) {
			if (identifier == RX_PACKET_IDENTIFIER) {
				listener.onRXPacket(new RXPacket(data));
			} else if (identifier == RF_MODULE_STAUS_IDENTIFIER) {
				listener.onRFModuleStatus(new RFModuleStatus(data));
			} else if (identifier == TX_STATUS_IDENTIFIER) {
				listener.onTXStatus(new TXStatus(data));
			} else {
				System.err.println("Unhandled API frame: " + ByteHelper.byteToHexString(identifier));
			}
		}
	}

}
