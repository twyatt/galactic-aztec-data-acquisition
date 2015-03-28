package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import edu.sdsu.rocket.server.DeviceManager.Device;

public class ITG3205Device implements Device {

	public interface GyroscopeListener {
		public void onValues(short x, short y, short z);
	}
	private GyroscopeListener listener;

	private final short[] values = new short[3]; // X, Y, Z
	
	private final ITG3205 itg3205;

	public ITG3205Device(ITG3205 itg3205) {
		this.itg3205 = itg3205;
	}
	
	public void setListener(GyroscopeListener listener) {
		this.listener = listener;
	}

	@Override
	public void loop() throws IOException {
		itg3205.readRawRotations(values);
		
		if (listener != null) {
			listener.onValues(values[0], values[1], values[2]);
		}
	}

}
