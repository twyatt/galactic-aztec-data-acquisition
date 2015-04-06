package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import edu.sdsu.rocket.core.helpers.Console;

public class DatagramPacketRunnable implements Runnable {
	
	// http://en.wikipedia.org/wiki/User_Datagram_Protocol#Packet_structure
	private static final int DEFAULT_PACKET_SIZE = 65527;
	
	private final DatagramSocket socket;
	private final int packetSize;

	private DatagramPacketListener listener;
	
	public DatagramPacketRunnable(DatagramSocket socket) {
		this(socket, DEFAULT_PACKET_SIZE);
	}
	
	public DatagramPacketRunnable(DatagramSocket socket, int packetSize) {
		this.socket = socket;
		this.packetSize = packetSize;
	}
	
	public void setListener(DatagramPacketListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[packetSize];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				socket.receive(packet);
				if (listener != null) {
					listener.onPacketReceived(packet);
				}
			} catch (IOException e) {
				Console.error(e);
				
				try {
					Thread.sleep(250L);
				} catch (InterruptedException e1) {
					Console.error(e1);
					return;
				}
			}
		}
	}

}
