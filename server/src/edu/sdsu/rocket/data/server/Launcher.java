package edu.sdsu.rocket.data.server;

import java.io.IOException;

import edu.sdsu.rocket.data.models.Sensors;

public class Launcher {
	
	public static void main(String[] args) throws IOException {
		Application app = new Application(new Sensors());
		app.setup();
		while (true) {
			app.loop();
		}
	}

}
