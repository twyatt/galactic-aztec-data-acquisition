package edu.sdsu.rocket.pi.devices;

import java.io.IOException;

import edu.sdsu.rocket.pi.devices.DeviceManager.Device;

public class MS5611Device implements Device {
	
	public interface BarometerListener {
		public void onValues(int T, int P);
		public void onFault(Fault fault);
	}
	
	public enum Fault {
		D1,
		D2,
	}
	
	private long start = 0L;
	private int wait = 0;
	private long adc;
	private long D1 = 0, D2 = 0;
	private int i = 0;
	
	private final MS5611 ms5611;
	private BarometerListener listener;
	
	public MS5611Device(MS5611 ms5611) {
		this.ms5611 = ms5611;
	}
	
	public void setListener(BarometerListener listener) {
		this.listener = listener;
	}
	
	public MS5611 getDevice() {
		return ms5611;
	}

	@Override
	public void loop() throws IOException {
		if (i == 0) {
			i++;
			wait = ms5611.writeConversion(MS5611.MS5611_CONVERT_D2_OSR_256);
			start = System.nanoTime();
		} else if (i == 1 && System.nanoTime() - start > wait) {
			i++;
			if ((adc = ms5611.readADC()) == 0) {
				fault(Fault.D2);
				return;
			} else {
				D2 = adc;
			}
		} else if (i == 2) {
			i++;
			wait = ms5611.writeConversion(MS5611.MS5611_CONVERT_D1_OSR_256);
			start = System.nanoTime();
		} else if (i == 3 && System.nanoTime() - start > wait) {
			i = 0;
			if ((adc = ms5611.readADC()) == 0) {
				fault(Fault.D1);
				return;
			} else {
				D1 = adc;
				int T = ms5611.getTemperature(D2);
				int P = ms5611.getPressure(D1, D2);
				done(T, P);
			}
		}
	}
	
	private void done(int T, int P) {
//		System.out.println("Temperature = " + (T / 100f) + " C,\tPressure = " + (P / 100f) + " mbar");
		if (listener != null) {
			listener.onValues(T, P);
		}
	}

	private void fault(Fault fault) {
		if (listener != null) {
			listener.onFault(fault);
		}
	}
}
