package edu.sdsu.rocket.server.devices;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.serial.Serial;

import edu.sdsu.rocket.models.Sensors;
import edu.sdsu.rocket.server.DeviceManager.Device;

public class XTend900Device implements Device {
	
	private static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	
	private static final int BUFFER_SIZE = 256;
	private static final ByteBuffer BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

	private final Serial serial;
	private final Sensors sensors;

	public XTend900Device(Serial serial, Sensors sensors) {
		this.serial = serial;
		this.sensors = sensors;
	}

	@Override
	public void loop() throws IOException {
		BUFFER.clear();
		
		BUFFER.put(START_BYTES);
		int lengthPosition = BUFFER.position();
		BUFFER.putInt(0); // length placeholder
		
		int start = BUFFER.position();
		sensors.toByteBuffer(BUFFER);
		int end = BUFFER.position();
		int length = end - start;
		
		BUFFER.putInt(lengthPosition, length);
		BUFFER.flip();
		serial.write(BUFFER);
	}

}
