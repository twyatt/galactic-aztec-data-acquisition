package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockITG3205 extends ITG3205 {
	
	private float x;
	
	@Override
	public void setup() throws IOException {}
	
	@Override
	public boolean verifyDeviceID() throws IOException {
		return true;
	}
	
	@Override
	public void writeSampleRateDivider(int rate) throws IOException {}
	
	@Override
	public void writeDLPFBandwidth(int bandwidth) throws IOException {}
	
	@Override
	public void loop() throws IOException {
		x += 0.01f;
		float c = MathUtils.cos(x); // -1 to 1
		float s = MathUtils.sin(x); // -1 to 1
		
		if (listener != null) {
			listener.onValues((short) (s * 360), (short) (c * 360), (short) (s * 360));
		}
	}

}
