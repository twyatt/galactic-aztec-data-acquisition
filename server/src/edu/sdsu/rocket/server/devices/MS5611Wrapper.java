package edu.sdsu.rocket.server.devices;

import java.io.IOException;

public class MS5611Wrapper {
	
	public static final int STALE    = -1;
	public static final int FRESH    =  0;
	public static final int D1_FAULT =  1;
	public static final int D2_FAULT =  2;
	
	private long start = 0L;
	private int wait = 0;
	private long adc;
	private long D1 = 0, D2 = 0;
	private int i = 0;
	
	private final MS5611 ms5611;
	
	public MS5611Wrapper(MS5611 ms5611) {
		this.ms5611 = ms5611;
	}
	
	public MS5611 getDevice() {
		return ms5611;
	}
	
	public int read(int[] raw) throws IOException {
		if (i == 0) {
			i++;
			wait = ms5611.writeConversion(MS5611.MS5611_CONVERT_D2_OSR_256);
			start = System.nanoTime();
		} else if (i == 1 && System.nanoTime() - start > wait) {
			i++;
			if ((adc = ms5611.readADC()) == 0)
				return D2_FAULT;
			else
				D2 = adc;
		} else if (i == 2) {
			i++;
			wait = ms5611.writeConversion(MS5611.MS5611_CONVERT_D1_OSR_256);
			start = System.nanoTime();
		} else if (i == 3 && System.nanoTime() - start > wait) {
			i = 0;
			if ((adc = ms5611.readADC()) == 0) {
				return D1_FAULT;
			} else {
				D1 = adc;
				raw[0] = ms5611.getTemperature(D2);
				raw[1] = ms5611.getPressure(D1, D2);
				return FRESH;
//				System.out.println("Temperature = " + (T / 100f) + " C,\tPressure = " + (P / 100f) + " mbar");
			}
		}
		
		return STALE;
	}
}
