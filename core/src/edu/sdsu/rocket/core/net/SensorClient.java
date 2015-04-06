package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.helpers.Console;
import edu.sdsu.rocket.core.helpers.RateLimitedRunnable;
import edu.sdsu.rocket.core.models.Sensors;

public class SensorClient {
	
	public interface SensorClientListener {
		public void onSensorsUpdated(Sensors sensors);
	}
	
	private static final int BUFFER_SIZE = 128; // bytes
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramClient client;
	
	private Thread thread;
	private float frequency;
	private RateLimitedRunnable runnable;
	
	private int requestNumber; // message request number
	private int responseNumber; // message response number
	
	private final Sensors sensors;
	private SensorClientListener listener;
	
	public SensorClient(Sensors sensors) {
		this.sensors = sensors;
	}
	
	public void setListener(SensorClientListener listener) {
		this.listener = listener;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
		if (runnable != null) {
			if (frequency == 0) {
				runnable.pause();
			} else {
				runnable.setFrequency(frequency);
				runnable.resume();
			}
		}
	}
	
	public void start(InetAddress addr, int port) throws SocketException {
		start(new InetSocketAddress(addr, port));
	}
	
	public void start(InetSocketAddress address) throws SocketException {
		if (client != null) {
			throw new SocketException("Client already started.");
		}
		
		client = new DatagramClient(address);
		client.setListener(new MessageHandler() {
			@Override
			public void onMessageReceived(Message message) {
				switch (message.id) {
				case Message.PING:
					// FIXME implement
					break;
				case Message.SENSOR:
					onSensorData(message);
					break;
				}
			}
		});
		client.start();
		
		runnable = new RateLimitedRunnable() {
			@Override
			public void loop() throws InterruptedException {
				try {
					sendSensorRequest();
				} catch (IOException e) {
					Console.error(e);
				}
			}
		};
		setFrequency(frequency);
		
		thread = new Thread(runnable);
		thread.setName(getClass().getSimpleName());
		thread.start();
	}
	
	public void stop() {
		if (client != null) {
			client.stop();
			client = null;
		}
		
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				Console.error(e);
			}
			thread = null;
		}
		runnable = null;
	}
	
	public void pause() {
		runnable.pause();
	}
	
	public void resume() {
		runnable.resume();
	}
	
	public void sendSensorRequest() throws IOException {
		sendMessage(Message.SENSOR);
	}
	
	public void sendMessage(byte id) throws IOException {
		sendMessage(id, null);
	}
	
	public void sendMessage(byte id, byte[] data) throws IOException {
		buffer.clear();
		buffer.putInt(++requestNumber);
		buffer.put(id);
		if (data != null) {
			buffer.put(data);
		}
		
		client.send(buffer.array(), buffer.position());
	}
	
	protected void onSensorData(Message message) {
		if (message.number != 0) {
			if (message.number < responseNumber || message.number > requestNumber) {
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
				listener.onSensorsUpdated(sensors);
			}
		}
	}
	
}
