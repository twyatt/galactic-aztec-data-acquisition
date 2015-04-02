package edu.sdsu.rocket.server.devices;

import java.io.IOException;

import edu.sdsu.rocket.server.DeviceManager.Device;

public class ADS1115Device implements Device {
	
	public enum Channel {
		A0, A1, A2, A3,
	}
	
	public interface AnalogListener {
		public void onValue(Channel channel, float value);
	}
	
	private static final int CHANNELS = 4;
	
	private static final boolean DEBUG = true;
	
	private AnalogListener listener;
	private long timeout;
	
	private final ADS1115 ads1115;

	public ADS1115Device(ADS1115 ads1115) {
		this.ads1115 = ads1115;
	}
	
	public ADS1115Device setListener(AnalogListener listener) {
		this.listener = listener;
		return this;
	}
	
	public ADS1115 getDevice() {
		return ads1115;
	}
	
	public ADS1115Device setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public void loop() throws IOException {
		for (int i = 0; i < CHANNELS; i++) {
			long start = System.nanoTime();
			ads1115.setSingleEnded(i).begin();
			
			boolean ready = false, waiting = true;
			while (!ready) {
				if (ads1115.isPerformingConversion()) {
					ready = true;
				} else if (System.nanoTime() - start > timeout) {
					/*
					 * Timed out waiting for assert, so we'll assume we
					 * missed the assert and the conversion is already
					 * complete.
					 */
					ready = true;
					waiting = false;
					if (DEBUG) System.out.print("?");
				}
			}
			
			// wait for conversion
			while (waiting) {
				if (!ads1115.isPerformingConversion()) {
					waiting = false;
				} else if (System.nanoTime() - start > timeout) {
					if (DEBUG) System.out.print("!");
					waiting = false;
				}
			}
			
			float value = ads1115.readMillivolts();
			
			if (listener != null) {
				Channel channel;
				switch (i) {
				case 1:
					channel = Channel.A1;
					break;
				case 2:
					channel = Channel.A2;
					break;
				case 3:
					channel = Channel.A3;
					break;
				default:
					channel = Channel.A0;
					break;
				}
				listener.onValue(channel, value);
			}
		}
		
	}

}
