package edu.sdsu.rocket.pi.io.radio;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.sdsu.rocket.core.models.Sensors;
import edu.sdsu.rocket.pi.devices.DeviceManager.Device;

public class SensorsTransmitter implements Device {
	
	static final int BUFFER_SIZE = XTend900.BUFFER_SIZE;
	private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
	
	private final XTend900 radio;
	private final Sensors sensors;

	public SensorsTransmitter(XTend900 radio, Sensors sensors) {
		this.radio = radio;
		this.sensors = sensors;
	}

	@Override
	public void loop() throws IOException, InterruptedException {
		if (radio.isOn()) {
			WRITE_BUFFER.clear();
			sensors.toByteBuffer(WRITE_BUFFER);
//			WRITE_BUFFER.put((byte) 0xF0); // for debugging
			byte[] data = new byte[WRITE_BUFFER.position()];
			WRITE_BUFFER.rewind();
			WRITE_BUFFER.get(data);
			radio.send(data);
		} else {
			Thread.sleep(500L);
		}
	}

}
