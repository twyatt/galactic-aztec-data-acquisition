package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockADXL345 extends ADXL345 {

	private float x;

	@Override
	public void setup() throws IOException {}
	
	@Override
	public boolean verifyDeviceID() throws IOException {
		return true;
	}
	
	@Override
	public int readDeviceID() throws IOException {
		return 0;
	}
	
	@Override
	public void writeRange(int range) throws IOException {}
	
	@Override
	public void writeFullResolution(boolean resolution) throws IOException {}

	@Override
	public void writeRate(int rate) throws IOException {}
	
	@Override
	public void loop() throws IOException {
		x += 0.01f;
		float c = MathUtils.cos(x); // -1 to 1
		float s = MathUtils.sin(x); // -1 to 1
		
		if (listener != null) {
			listener.onValues((short) (s * 9.8 * 100), (short) (c * 9.8 * 100), (short) (s * 9.8 * 100));
		}
	}
	
}
