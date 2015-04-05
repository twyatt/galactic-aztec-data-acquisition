package edu.sdsu.rocket.pi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Pi {

	// cat /sys/class/thermal/thermal_zone0/temp
	private static final String CPU_TEMPERATURE_DEVICE = "/sys/class/thermal/thermal_zone0/temp";
	
	// degrees celsius
	public static float getCpuTemperature() throws IOException {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(CPU_TEMPERATURE_DEVICE));
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
//			e.printStackTrace();
			return 0f;
		}
		float temperature = (float) Integer.valueOf(in.readLine()) / 1000f;
		in.close();
		return temperature;
	}
	
}
