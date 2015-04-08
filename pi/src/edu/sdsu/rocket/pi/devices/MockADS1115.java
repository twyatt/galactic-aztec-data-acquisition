package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockADS1115 extends ADS1115 {

	private float x;

	@Override
	public ADS1115 setup() throws IOException {
		return this;
	}
	
	@Override
	public void begin() throws IOException {}
	
	@Override
	public boolean isPerformingConversion() throws IOException {
		return false;
	}
	
	@Override
	public float readMillivolts() throws IOException {
		x += 0.0001f;
		float s = MathUtils.sin(x); // -1 to 1
		float sp = (s / 2f) + 0.5f; // 0 to 1
		return sp * 3300;
	}
	
}
