package edu.sdsu.rocket.server;

import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class Launcher {
	
	private static String[] args;
	
	public static String[] getArgs() {
		return args;
	}

	public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		Launcher.args = args;
		
		Application app = new Application();
		app.setup();
		while (true) {
			app.loop();
		}
	}

}
