package edu.sdsu.rocket.server;

import java.io.IOException;

import edu.sdsu.rocket.core.models.Sensors;

public class TestLauncher {
	
	private static String[] args;
	
	public static String[] getArgs() {
		return args;
	}

	public static void main(String[] args) throws IOException {
		TestLauncher.args = args;
		
		Application app = new TestApplication(new Sensors());
		app.setup();
		while (true) {
			app.loop();
		}
	}

}
