package edu.sdsu.rocket.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.helpers.ByteHelper;
import edu.sdsu.rocket.core.models.Sensors;

public class SensorServer {

	private static final int BUFFER_SIZE = 256; // bytes
	private final ByteBuffer PING_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer MESSAGE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
	
	private DatagramServer server;
	
	private final Sensors local;
	private final Sensors remote;
	
	private boolean debug;
	
	public SensorServer(Sensors local, Sensors remote) {
		this.local = local;
		this.remote = remote;
	}
	
	public void setDebug(boolean enabled) {
		debug = enabled;
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
						if (debug) System.out.println("Received ping request.");
						sendPingResponse(message);
						break;
					case DatagramMessage.SENSORS_LOCAL:  // fall thru intentional
					case DatagramMessage.SENSORS_REMOTE: // fall thru intentional
					case DatagramMessage.SENSORS_BOTH:
						if (debug) System.out.println("Received sensors request: " + ByteHelper.byteToHexString(message.id));
						sendSensorResponse(message);
						break;
					default:
						if (debug) System.out.println("Unknown request: " + ByteHelper.byteToHexString(message.id));
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
		
		Sensors sensors1;
		Sensors sensors2 = null;
		
		switch (message.id) {
		case DatagramMessage.SENSORS_REMOTE:
			sensors1 = remote;
			break;
		case DatagramMessage.SENSORS_BOTH:
			sensors2 = remote;
			// fall-thru intentional
		default: // SENSORS_LOCAL
			sensors1 = local;
			break;
		}
		
		MESSAGE_BUFFER.clear();
		MESSAGE_BUFFER.putInt(message.number);
		MESSAGE_BUFFER.put(message.id);
		
		int mask1 = message.data == null || message.data.length == 0 ? Sensors.ALL_MASK : message.data[0];
		int mask2 = message.data == null || message.data.length  < 2 ? Sensors.ALL_MASK : message.data[1];
		MESSAGE_BUFFER.put((byte) (mask1 & 0xFF));
		if (sensors2 != null) {
			MESSAGE_BUFFER.put((byte) (mask2 & 0xFF));
		}
		
		sensors1.toByteBuffer(MESSAGE_BUFFER, mask1);
		if (sensors2 != null) {
			sensors2.toByteBuffer(MESSAGE_BUFFER, mask2);
		}
		
		byte[] buf = MESSAGE_BUFFER.array();
		int length = MESSAGE_BUFFER.position();
		DatagramPacket packet = new DatagramPacket(buf, length, message.address);
		
		socket.send(packet);
	}
	
}
