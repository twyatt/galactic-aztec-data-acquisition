package edu.sdsu.rocket.core.net;

import java.net.DatagramSocket;

// http://stackoverflow.com/a/4671080/196486
public class InterruptableDatagramThread extends Thread {
	
	private final DatagramSocket socket;

	public InterruptableDatagramThread(DatagramSocket socket, Runnable runnable) {
		super(runnable);
		this.socket = socket;
	}

	@Override
	public void interrupt() {
		super.interrupt();
		socket.close();
	}
	
}
