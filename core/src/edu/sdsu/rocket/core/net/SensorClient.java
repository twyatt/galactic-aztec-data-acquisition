package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.helpers.RateLimitedRunnable;
import edu.sdsu.rocket.core.models.Sensors;

public class SensorClient {
	
	public enum Mode {
		LOCAL (DatagramMessage.SENSORS_LOCAL), // default
		REMOTE(DatagramMessage.SENSORS_REMOTE),
		;
		byte value;
		Mode(byte value) {
			this.value = value;
		}
	}
	
	public interface SensorClientListener {
		public void onSensorsUpdated();
	}
	
	private static final int BUFFER_SIZE = 128; // bytes
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramClient client;
	
	private Thread thread;
	private float frequency;
	private RateLimitedRunnable runnable;
	
	private int requestNumber; // message request number
	private int responseNumber; // message response number
	
	private SensorClientListener listener;
	
	private Mode mode = Mode.LOCAL;
	private final Sensors localSensors;
	private final Sensors remoteSensors;
	
	public SensorClient(Sensors localSensors, Sensors remoteSensors) {
		this.localSensors = localSensors;
		this.remoteSensors = remoteSensors;
	}
	
	public void setListener(SensorClientListener listener) {
		this.listener = listener;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
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
		client.setListener(new DatagramMessageHandler() {
			@Override
			public void onMessageReceived(DatagramMessage message) {
				switch (message.id) {
				case DatagramMessage.PING:
					// FIXME implement
					break;
				case DatagramMessage.SENSORS_LOCAL: // fall thru intentional
				case DatagramMessage.SENSORS_REMOTE:
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
					System.err.println(e);
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
				System.err.println(e);
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
		sendMessage(mode.value);
	}
	
	public void sendMessage(byte id) throws IOException {
		sendMessage(id, null);
	}
	
	public void sendMessage(byte id, byte data) throws IOException {
		sendMessage(id, new byte[] { data });
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
	
	protected void onSensorData(DatagramMessage message) {
		if (message.number != 0) {
			if (message.number < responseNumber || message.number > requestNumber) {
				return; // drop packet
			} else {
				responseNumber = message.number;
			}
		}
		
		Sensors sensors = message.id == DatagramMessage.SENSORS_REMOTE ? remoteSensors : localSensors;
		try {
			ByteBuffer buffer = ByteBuffer.wrap(message.data);
			int mask = buffer.get();
			sensors.fromByteBuffer(buffer, mask);
			
			if (listener != null) {
				listener.onSensorsUpdated();
			}
		} catch (BufferUnderflowException e) {
			System.err.println(e);
		}
	}
	
}
