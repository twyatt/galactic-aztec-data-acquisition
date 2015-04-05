package edu.sdsu.rocket.core.io;

import java.net.DatagramSocket;
import java.net.SocketException;

import edu.sdsu.rocket.core.helpers.Console;
import edu.sdsu.rocket.core.io.MessageHandler.MessageListener;

public class DatagramServer {
	
	private DatagramSocket socket;
	private Thread thread;
	
	private final int port;

	public DatagramServer(int port) {
		this.port = port;
	}
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public void start(MessageListener listener) throws SocketException {
		socket = new DatagramSocket(port);
		Console.log("Listing on port " + port + ".");
		
		thread = new InterruptableUDPThread(new PacketRunnable(socket, new MessageHandler(listener)));
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
	
	// http://stackoverflow.com/a/4671080/196486
	public class InterruptableUDPThread extends Thread {
		
		public InterruptableUDPThread(Runnable runnable) {
			super(runnable);
		}

		@Override
		public void interrupt() {
			super.interrupt();
			socket.close();
		}
		
	}
	
}
