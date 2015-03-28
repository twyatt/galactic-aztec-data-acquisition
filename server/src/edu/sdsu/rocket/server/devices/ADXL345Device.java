package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import edu.sdsu.rocket.server.DeviceManager.Device;

public class ADXL345Device implements Device {
	
	public interface AccelerometerListener {
		public void onValues(short x, short y, short z);
	}
	private AccelerometerListener listener;

	private final short[] values = new short[3]; // X, Y, Z
	
	private final ADXL345 adxl345;

	public ADXL345Device(ADXL345 adxl345) {
		this.adxl345 = adxl345;
	}
	
	public void setListener(AccelerometerListener listener) {
		this.listener = listener;
	}

	@Override
	public void loop() throws IOException {
		adxl345.readRawAcceleration(values);
		
		if (listener != null) {
			listener.onValues(values[0], values[1], values[2]);
		}
	}

}
