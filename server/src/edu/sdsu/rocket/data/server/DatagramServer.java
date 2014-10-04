package edu.sdsu.rocket.data.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import edu.sdsu.rocket.data.server.models.Request;

public class DatagramServer {
	
	public interface RequestHandler {
		public void onRequest(Request request);
	}
	
	private static final int BUFFER_SIZE = 8; // bytes
	Queue<Request> fifo = new CircularFifoQueue<Request>(16);

	private DatagramSocket socket;
	private Thread thread;
	
	private RequestHandler requestHandler;
	
	private final int port;

	public DatagramServer(int port) {
		this.port = port;
	}
	
	public void setRequestHandler(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public void start() throws SocketException {
		socket = new DatagramSocket(port);
		System.out.println("Listing on port " + port + ".");
		
		thread = new Thread(new DatagramServerRunnable());
		thread.setName("DatagramServer");
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
		}
	}
	
	public void loop() {
		Request request;
		do {
			synchronized (this) {
				request = fifo.poll();
			}
			if (request != null && requestHandler != null) {
				requestHandler.onRequest(request);
			}
		} while (request != null);
	}
	
	private void onReceivedPacket(DatagramPacket packet) {
		byte[] data = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), 0, data, 0, data.length);
		
		Request request = new Request();
		request.address = packet.getSocketAddress();
		request.data = data;
		
		synchronized (this) {
			fifo.add(request);
		}
	}
	
	private class DatagramServerRunnable implements Runnable {

		@Override
		public void run() {
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				while (!Thread.currentThread().isInterrupted()) {
					socket.receive(packet);
					onReceivedPacket(packet);
				}
			} catch (IOException e) {
				try {
					Thread.sleep(250L);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
		
	}

}
