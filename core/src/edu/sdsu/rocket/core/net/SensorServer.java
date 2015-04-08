package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.models.Sensors;

public class SensorServer {

	private static final int BUFFER_SIZE = 1024; // bytes
	private final ByteBuffer PING_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer MESSAGE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramServer server;
	
	private final Sensors local;
	private final Sensors remote1;
	private final Sensors remote2;
	
	public SensorServer(Sensors localSensors, Sensors remote1, Sensors remote2) {
		this.local = localSensors;
		this.remote1 = remote1;
		this.remote2 = remote2;
	}
	
	public void start(int port) throws SocketException {
		if (server != null) {
			throw new SocketException("Server already started.");
		}
		
		server = new DatagramServer();
		server.setListener(new DatagramMessageHandler() {
			@Override
			public void onMessageReceived(DatagramMessage message) {
				try {
					switch (message.id) {
					case DatagramMessage.PING:
						sendPingResponse(message);
						break;
					case DatagramMessage.SENSORS_LOCAL: // fall thru intentional
					case DatagramMessage.SENSORS_REMOTE1: // fall thru intentional
					case DatagramMessage.SENSORS_REMOTE2:
						sendSensorResponse(message);
						break;
					}
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		});
		server.start(port);
	}
	
	public void stop() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}
	
	protected void sendPingResponse(DatagramMessage message) throws IOException {
		if (server == null) return;
		
		DatagramSocket socket = server.getSocket();
		if (socket == null) return;
		
		PING_BUFFER.clear();
		PING_BUFFER.putInt(message.number);
		PING_BUFFER.put(message.id);
		PING_BUFFER.put(message.data);
		
		byte[] buf = PING_BUFFER.array();
		int length = PING_BUFFER.position();
		DatagramPacket packet = new DatagramPacket(buf, length, message.address);
		
		socket.send(packet);
	}
	
	protected void sendSensorResponse(DatagramMessage message) throws IOException {
		if (server == null) return;
		
		DatagramSocket socket = server.getSocket();
		if (socket == null) return;
		
		Sensors sensors;
		switch (message.id) {
		case DatagramMessage.SENSORS_REMOTE1:
			sensors = remote1;
			break;
		case DatagramMessage.SENSORS_REMOTE2:
			sensors = remote2;
			break;
		default:
			sensors = local;
			break;
		}
		
		MESSAGE_BUFFER.clear();
		MESSAGE_BUFFER.putInt(message.number);
		MESSAGE_BUFFER.put(message.id);
		int mask = message.data == null || message.data.length == 0 ? Sensors.ALL_MASK : message.data[0];
		MESSAGE_BUFFER.put((byte) (mask & 0xFF));
		sensors.toByteBuffer(MESSAGE_BUFFER, mask);
		
		byte[] buf = MESSAGE_BUFFER.array();
		int length = MESSAGE_BUFFER.position();
		DatagramPacket packet = new DatagramPacket(buf, length, message.address);
		
		socket.send(packet);
	}

}
