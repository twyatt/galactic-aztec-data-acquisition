package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class DatagramClient {
	
	private final InetSocketAddress address;
	private DatagramSocket socket;
	
	private Thread thread;
	
	private DatagramPacketRunnable runnable;
	private DatagramPacketListener listener;
	
	public DatagramClient(InetAddress addr, int port) {
		this(new InetSocketAddress(addr, port));
	}
	
	public DatagramClient(InetSocketAddress address) {
		this.address = address;
	}
	
	public void setListener(DatagramPacketListener listener) {
		this.listener = listener;
		if (runnable != null) {
			runnable.setListener(listener);
		}
	}
	
	public void start() throws SocketException {
		socket = new DatagramSocket();
		
		runnable = new DatagramPacketRunnable(socket);
		runnable.setListener(listener);
		
		thread = new InterruptableDatagramThread(socket, runnable);
		thread.setName(getClass().getSimpleName());
		thread.start();
	}
	
	public void stop() {
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			thread = null;
		}
		runnable = null;
	}
	
	public void send(byte[] data, int length) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, length, address);
		socket.send(packet);
	}
	
}
