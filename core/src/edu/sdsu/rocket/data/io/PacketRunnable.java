package edu.sdsu.rocket.data.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PacketRunnable implements Runnable {
	
	// http://en.wikipedia.org/wiki/User_Datagram_Protocol#Packet_structure
	private static final int DEFAULT_PACKET_SIZE = 65527;
	
	private final DatagramSocket socket;
	private final int packetSize;
	private final PacketListener listener;

	public interface PacketListener {
		public void onPacketReceived(DatagramPacket packet);
	}
	
	public PacketRunnable(DatagramSocket socket, PacketListener listener) {
		this(socket, DEFAULT_PACKET_SIZE, listener);
	}
	
	public PacketRunnable(DatagramSocket socket, int packetSize, PacketListener listener) {
		this.socket = socket;
		this.packetSize = packetSize;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[packetSize];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				socket.receive(packet);
				listener.onPacketReceived(packet);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				try {
					Thread.sleep(250L);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}

}
