package client.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramInputStream extends InputStream {
	
	private static final int BUFFER_SIZE = 256; // bytes
	private final byte[] buffer = new byte[BUFFER_SIZE];
	
	private final DatagramSocket socket;
	private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	
	private int position; // buffer read position
	private int length; // data length in buffer

	public DatagramInputStream(DatagramSocket socket) {
		this.socket = socket;
	}
	
	private void receive() throws IOException {
		socket.receive(packet);
		position = 0;
		length = packet.getLength();
	}
	
	@Override
	public int available() throws IOException {
		return length - position;
	}

	@Override
	public int read() throws IOException {
		if (position == length) {
			receive();
		}
		return (int) buffer[position++] & 0xFF;
	}
	
	@Override
	public void close() throws IOException {
		socket.close();
	}

}
