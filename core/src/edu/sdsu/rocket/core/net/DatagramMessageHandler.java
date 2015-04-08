package edu.sdsu.rocket.core.net;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public abstract class DatagramMessageHandler implements DatagramPacketListener {
	
	/**
	 * Minimum packet size as per Communications Protocol document:
	 * https://docs.google.com/document/d/19HCnihBslgRuMjG8qUwMaPgZf7NI5QqR_Laf--r5vM0
	 * 
	 * NUMBER + ID = int + byte = 4 + 1 = 5
	 */
	private static final int MINIMUM_PACKET_SIZE = 5; // bytes
	
	public abstract void onMessageReceived(DatagramMessage message);

	@Override
	public void onPacketReceived(DatagramPacket packet) {
		if (packet.getLength() < MINIMUM_PACKET_SIZE) {
			return; // drop packet
		}
		
		DatagramMessage message = new DatagramMessage();
		message.address = packet.getSocketAddress();
		
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
		message.number = buffer.getInt();
		message.id = buffer.get();
		if (buffer.hasRemaining()) {
			message.data = new byte[buffer.remaining()];
			System.arraycopy(buffer.array(), buffer.position(), message.data, 0, buffer.remaining());
		}
		
		onMessageReceived(message);
	}

}
