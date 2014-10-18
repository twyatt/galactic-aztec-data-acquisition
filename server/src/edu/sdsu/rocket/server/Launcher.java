package edu.sdsu.rocket.server;

import java.io.IOException;

import edu.sdsu.rocket.models.Sensors;

public class Launcher {
	
	private static String[] args;
	
	public static String[] getArgs() {
		return args;
	}

	public static void main(String[] args) throws IOException {
		Launcher.args = args;
		
		Application app = new Application(new Sensors());
		app.setup();
		while (true) {
			app.loop();
		}
	}

}
