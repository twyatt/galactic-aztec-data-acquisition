package edu.sdsu.rocket.core.net;

import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramServer {
	
	private DatagramSocket socket;
	
	private Thread thread;
	
	private DatagramPacketRunnable runnable;
	private DatagramPacketListener listener;
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public DatagramServer setListener(DatagramPacketListener listener) {
		this.listener = listener;
		if (runnable != null) {
			runnable.setListener(listener);
		}
		return this;
	}
	
	public void start(int port) throws SocketException {
		if (socket != null) {
			throw new SocketException("Server already started.");
		}
		socket = new DatagramSocket(port);
		System.out.println("Listing on port " + port + ".");
		
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
	
}
