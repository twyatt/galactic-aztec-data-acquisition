package edu.sdsu.rocket.server.devices;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.io.IOException;

public class MockADXL345 extends ADXL345 {

	static final float TIME_MULTIPLIER = 0.001f;
	static final float VALUE_MULTIPLIER = 2f;
	
	long t = System.currentTimeMillis();
	
	Vector3 i = new Vector3(0, MathUtils.PI / 2f, MathUtils.PI);
	Vector3 v = new Vector3();

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
		long dt = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		i.x += (float) dt * TIME_MULTIPLIER;
		i.y += (float) dt * TIME_MULTIPLIER;
		i.z += (float) dt * TIME_MULTIPLIER;
		
		v.set(MathUtils.cos(i.x), MathUtils.cos(i.y), MathUtils.cos(i.z)); // -1 to 1
		v.x *= 9.8f * VALUE_MULTIPLIER;
		v.y *= 9.8f * VALUE_MULTIPLIER;
		v.z *= 9.8f * VALUE_MULTIPLIER;
		
		if (listener != null) {
			listener.onValues((short) v.x, (short) v.y, (short) v.z);
		}
	}
	
}
