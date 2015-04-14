package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;

public class MockADS1115 extends ADS1115 {

	private static final int RANDOM_MIN = 0;
	private static final int RANDOM_MAX = 10;
	private float[] x;
	private int channel;

	@Override
	public ADS1115 setup() throws IOException {
		x = new float[] {
			(int)(Math.random() * (RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN),
			(int)(Math.random() * (RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN),
			(int)(Math.random() * (RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN),
			(int)(Math.random() * (RANDOM_MAX - RANDOM_MIN) + RANDOM_MIN),
		};
		return this;
	}
	
	@Override
	public void begin() throws IOException {}
	
	@Override
	public boolean isPerformingConversion() throws IOException {
		return false;
	}
	
	@Override
	public ADS1115 setSingleEnded(int channel) {
		this.channel = channel;
		if (x[channel] > 1000f) x[channel] = 0;
		x[channel] += 0.01f;
		return this;
	}
	
	@Override
	public float readMillivolts() throws IOException {
		try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		float s = MathUtils.sin(x[channel]); // -1 to 1
		float sp = (s / 2f) + 0.5f; // 0 to 1
		return sp * 3300;
	}
	
}
