package edu.sdsu.rocket.data.server.io;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import edu.sdsu.rocket.data.io.Message;
import edu.sdsu.rocket.data.io.MessageHandler;
import edu.sdsu.rocket.data.io.MessageHandler.MessageListener;
import edu.sdsu.rocket.data.io.PacketRunnable;

public class DatagramServer implements MessageListener {
	
	private DatagramSocket socket;
	private Thread thread;
	
	Queue<Message> fifo = new CircularFifoQueue<Message>(16);
	
	private final int port;

	public DatagramServer(int port) {
		this.port = port;
	}
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public void start() throws SocketException {
		socket = new DatagramSocket(port);
		System.out.println("Listing on port " + port + ".");
		
		thread = new Thread(new PacketRunnable(socket, new MessageHandler(this)));
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
	
	// non-blocking, returns null if no messages available
	public synchronized Message read() {
		return fifo.poll();
	}
	
	@Override
	public synchronized void onMessageReceived(Message message) {
		fifo.add(message);
	}
	
}
