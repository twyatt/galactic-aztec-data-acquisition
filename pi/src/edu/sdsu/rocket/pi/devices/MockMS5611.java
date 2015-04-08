package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockMS5611 extends MS5611 {
	
	private float x;
	
	@Override
	public void setup() throws IOException {}
	
	@Override
	public void loop() throws IOException {
		x += 0.01f;
		float c = MathUtils.cos(x); // -1 to 1
		float s = MathUtils.sin(x); // -1 to 1
		float cp = (c / 2f) + 0.5f; // 0 to 1
		float sp = (s / 2f) + 0.5f; // 0 to 1
		
		if (listener != null) {
			listener.onValues((int) (sp * 100 * 100), (int) (cp * 1000 * 100));
		}
	}

}
