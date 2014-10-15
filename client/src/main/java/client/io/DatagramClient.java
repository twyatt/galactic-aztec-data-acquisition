package client.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.data.io.Message;
import edu.sdsu.rocket.data.io.MessageHandler;
import edu.sdsu.rocket.data.io.MessageHandler.MessageListener;
import edu.sdsu.rocket.data.io.PacketRunnable;
import edu.sdsu.rocket.data.models.Sensors;

public class DatagramClient implements MessageListener {
	
	public interface ClientListener {
		public void onSensorData(Sensors sensors);
	}
	
	private Sensors sensors = new Sensors();

	private InetSocketAddress address;
	private DatagramSocket socket;
	
	private Thread listenThread;
	private PacketRunnable listenRunnable;
	
	private Thread requestThread;
	private RequestRunnable requestRunnable;
	private int requestNumber; // message request number
	private int responseNumber; // message response number

	private ClientListener listener;
	
	public void setListener(ClientListener listener) {
		this.listener = listener;
	}
	
	public void setFrequency(float frequency) {
		requestRunnable.setFrequency(frequency);
	}
	
	public void start(InetAddress addr, int port) throws SocketException {
		socket = new DatagramSocket();
		address = new InetSocketAddress(addr, port);
		
		requestRunnable = new RequestRunnable();
		requestThread = new Thread(requestRunnable);
		requestThread.setName(requestRunnable.getClass().getSimpleName());
		requestThread.start();
		
		listenRunnable = new PacketRunnable(socket, new MessageHandler(this));
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
		sendMessage(Message.SENSOR);
	}
	
	public void sendMessage(byte id) {
		sendMessage(id, null);
	}
	
	public void sendMessage(byte id, byte[] data) {
		if (address == null)
			return;
		
		int length = data == null ? 0 : data.length;
		byte[] raw = new byte[5 + length]; // NUMBER + ID = 4 + 1 = 5
		ByteBuffer buffer = ByteBuffer.wrap(raw);
		buffer.putInt(++requestNumber);
		buffer.put(id);
		if (data != null) {
			buffer.put(data);
		}
		
		DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position(), address);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if (message.number != 0) {
			if (message.number < responseNumber) {
				return; // drop packet
			} else {
				responseNumber = message.number;
			}
		}
		
		switch (message.id) {
		case Message.SENSOR:
			// TODO catch buffer underflow
			sensors.fromByteBuffer(ByteBuffer.wrap(message.data));
			
			if (listener != null) {
				listener.onSensorData(sensors);
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
