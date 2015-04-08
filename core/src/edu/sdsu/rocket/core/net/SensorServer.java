package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.models.Sensors;

public class SensorServer {

	private static final int BUFFER_SIZE = 128; // bytes
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramServer server;
	
	private final Sensors localSensors;
	private final Sensors remoteSensors;
	
	public SensorServer(Sensors localSensors, Sensors remoteSensors) {
		this.localSensors = localSensors;
		this.remoteSensors = remoteSensors;
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
						// FIXME implement
						break;
					case DatagramMessage.SENSORS_LOCAL: // fall thru intentional
					case DatagramMessage.SENSORS_REMOTE:
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
	
	private void sendSensorResponse(DatagramMessage message) throws IOException {
		if (server == null) return;
		
		DatagramSocket socket = server.getSocket();
		if (socket == null) return;
		
		Sensors sensors = message.id == DatagramMessage.SENSORS_REMOTE ? remoteSensors : localSensors;
		
		buffer.clear();
		buffer.putInt(message.number);
		buffer.put(message.id);
		int mask = message.data == null || message.data.length == 0 ? Sensors.ALL_MASK : message.data[0];
		buffer.put((byte) (mask & 0xFF));
		sensors.toByteBuffer(buffer, mask);
		
		byte[] buf = buffer.array();
		int length = buffer.position();
		DatagramPacket packet = new DatagramPacket(buf, length, message.address);
		
		socket.send(packet);
	}

}
