package client.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.data.io.Message;
import edu.sdsu.rocket.data.models.Sensors;

public class DatagramClient {
	
	public interface ClientListener {
		public void onSensorData(Sensors sensors);
	}
	
	private static final int MAX_DATA_LENGTH = 65536;
	
	private Sensors sensors = new Sensors();

	private InetSocketAddress address;
	private DatagramSocket socket;
	
	private Thread listenThread;
	private ListenRunnable listenRunnable;
	private MessageInputStream in;
	
	private Thread requestThread;
	private RequestRunnable requestRunnable;

	private ClientListener listener;
	
	public void setListener(ClientListener listener) {
		this.listener = listener;
	}
	
	public void setRemoteAddress(InetSocketAddress address) {
		this.address = address;
	}
	
	public void setFrequency(float frequency) {
		requestRunnable.setFrequency(frequency);
	}
	
	public void start() throws SocketException {
		socket = new DatagramSocket();
		in = new MessageInputStream(new DatagramInputStream(socket), Message.START_BYTES, MAX_DATA_LENGTH);
		
		requestRunnable = new RequestRunnable();
		requestThread = new Thread(requestRunnable);
		requestThread.setName(requestRunnable.getClass().getSimpleName());
		requestThread.start();
		
		listenRunnable = new ListenRunnable();
		listenThread = new Thread(listenRunnable);
		listenThread.setName(listenRunnable.getClass().getSimpleName());
		listenThread.start();
	}
	
	public void stop() {
		if (requestThread != null) {
			requestThread.interrupt();
			try {
				requestThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		socket.close();
		
		if (listenThread != null) {
			listenThread.interrupt();
			try {
				listenThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void pause() {
		requestRunnable.isRunning = false;
	}
	
	public void resume() {
		requestRunnable.isRunning = true;
	}
	
	public void sendSensorRequest() {
		send(Message.SENSOR_REQUEST);
	}
	
	public void send(byte b) {
		if (address == null)
			return;
		
		byte[] data = new byte[1];
		data[0] = b;
		DatagramPacket packet = new DatagramPacket(data, data.length, address);
		
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onReceivedMessage(Message message) {
		switch (message.id) {
		case Message.SENSOR_RESPONSE:
			// TODO catch buffer underflow
			sensors.fromByteBuffer(ByteBuffer.wrap(message.data));
			
			if (listener != null) {
				listener.onSensorData(sensors);
			}
		}
	}
	
	private class ListenRunnable implements Runnable {
		
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					onReceivedMessage(in.readMessage());
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Thread.sleep(250L);
					} catch (InterruptedException e1) {
						return;
					}
				}
			}
		}
		
	}
	
	private class RequestRunnable implements Runnable {
		
		private volatile boolean isRunning;
		private volatile long sleep;
		
		/**
		 * Sets the running frequency.
		 * 
		 * @param frequency Hz
		 */
		public void setFrequency(float frequency) {
			if (frequency == 0f) {
				throw new IllegalArgumentException("Frequency cannot be zero.");
			}
			setSleep(Math.round(1000f / frequency));
		}
		
		/**
		 * Sets the duration to sleep between thread loops.
		 * 
		 * @param sleep Thread loop sleep duration (milliseconds).
		 */
		public void setSleep(long sleep) {
			this.sleep = sleep;
		}
		
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					if (isRunning) {
						sendSensorRequest();
						if (sleep != 0L) {
							Thread.sleep(sleep);
						}
					} else { // paused
						Thread.sleep(250L);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
	}

}
