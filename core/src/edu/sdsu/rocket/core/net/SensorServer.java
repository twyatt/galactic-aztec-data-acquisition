package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.helpers.Console;
import edu.sdsu.rocket.core.models.Sensors;

public class SensorServer {

	private static final int BUFFER_SIZE = 128; // bytes
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramServer server;
	
	private final Sensors sensors;
	
	public SensorServer(Sensors sensors) {
		this.sensors = sensors;
	}
	
	public void start(int port) throws SocketException {
		if (server != null) {
			throw new SocketException("Server already started.");
		}
		
		server = new DatagramServer();
		server.setListener(new MessageHandler() {
			@Override
			public void onMessageReceived(Message message) {
				try {
					switch (message.id) {
					case Message.PING:
						// FIXME implement
						break;
					case Message.SENSOR:
						sendSensorResponse(message);
						break;
					}
				} catch (IOException e) {
					Console.error(e.getMessage());
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
	
	private void sendSensorResponse(Message message) throws IOException {
		if (server == null) return;
		
		DatagramSocket socket = server.getSocket();
		if (socket == null) return;
		
		buffer.clear();
		buffer.putInt(message.number);
		buffer.put(Message.SENSOR);
		sensors.toByteBuffer(buffer);
		
		byte[] buf = buffer.array();
		int length = buffer.position();
		DatagramPacket packet = new DatagramPacket(buf, length, message.address);
		
		socket.send(packet);
	}

}
