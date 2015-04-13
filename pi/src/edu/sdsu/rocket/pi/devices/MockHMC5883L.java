package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockHMC5883L extends HMC5883L {

	private float x;

	@Override
	public HMC5883L setup() throws IOException {
		return this;
	}
	
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
