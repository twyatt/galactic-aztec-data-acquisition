package edu.sdsu.rocket.pi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Pi {

	// cat /sys/class/thermal/thermal_zone0/temp
	private static final String CPU_TEMPERATURE_DEVICE = "/sys/class/thermal/thermal_zone0/temp";
	
	// degrees celsius
	public static float getCpuTemperatureC() throws IOException {
		return (float) getRawCpuTemperature() / 1000f;
	}
	
	public static int getRawCpuTemperature() throws IOException {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(CPU_TEMPERATURE_DEVICE));
		} catch (FileNotFoundException e) {
			System.err.println(e);
			return 0;
		}
		int temperature = Integer.valueOf(in.readLine());
		in.close();
		return temperature;
	}
	
}
