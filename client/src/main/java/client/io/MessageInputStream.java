package client.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import edu.sdsu.rocket.data.io.Message;

/**
 * Reads messages from an InputStream of the format:
 * 
 *   START_BYTES (byte[]) | MESSAGE_NUMBER (int) | MESSAGE_ID (byte) | LENGTH (int) | DATA (byte[]) | CRC32 (long)
 */
public class MessageInputStream extends DataInputStream {
	
	private static final boolean DEBUG = false;
	
	private final byte[] startBytes;
	private final int maxDataLength;
	
	private int number;
	private int last;
	
	public MessageInputStream(InputStream in, byte[] startBytes, int maxDataLength) {
		super(new CheckedInputStream(in, new CRC32()));
		
		if (startBytes == null || startBytes.length == 0) {
			throw new IllegalArgumentException();
		}
		
		this.startBytes = startBytes;
		this.maxDataLength = maxDataLength;
	}

	public Message readMessage() throws IOException {
		Message message;
		while ((message = readNext()) == null);
		return message;
	}
	
	private Message readNext() throws IOException {
		((CheckedInputStream) in).getChecksum().reset();
		int position = 0;
		
		if (DEBUG) System.out.println("START_BYTES");
		
		int sb;
		do {
			if ((sb = in.read()) == -1) {
				throw new EOFException();
			}
			if ((byte) sb == startBytes[position]) {
				position++;
			} else {
				return null; // invalid start byte
			}
		} while (position < startBytes.length);
		
		if (DEBUG) System.out.println("NUMBER");
		
		number = readInt();
		if (number < last) {
			return null; // out of order
		}
		last = number;
		
		if (DEBUG) System.out.println("ID");
		
		int id = in.read();
		if (id == -1) {
			throw new EOFException();
		}
		
		if (DEBUG) System.out.println("LENGTH");
		
		int length = readInt();
		if (length < 0 || length > maxDataLength) {
			return null; // invalid data length
		}
		
		if (DEBUG) System.out.println("DATA");
		
		byte[] data;
		if (length == 0) {
			data = null;
		} else {
			data = new byte[length];
			readFully(data);
		}
		
//		if (DEBUG)
//			System.out.println("CHECKSUM");
//		
//		long calculatedChecksum = ((CheckedInputStream) in).getChecksum().getValue();
//		long readChecksum = readLong();
//		if (readChecksum != calculatedChecksum) {
//			throw new MessageException("Checksum failed. Read checksum of " + readChecksum + " does not equal calculated checksum of " + calculatedChecksum + ".");
//		}
		
		Message message = new Message();
		message.id = id;
		message.data = data;
		return message;
	}
	
	public class MessageException extends IOException {

		private static final long serialVersionUID = -6032252841958123951L;
		
		public MessageException(String detailMessage) {
			super(detailMessage);
		}
		
	}
	
}
