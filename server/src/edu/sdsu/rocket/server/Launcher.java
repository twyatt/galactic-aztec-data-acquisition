package edu.sdsu.rocket.server;

import java.io.IOException;

public class Launcher {
	
	private static String[] args;
	
	public static String[] getArgs() {
		return args;
	}

	public static void main(String[] args) throws IOException {
		Launcher.args = args;
		
		Application app = new Application();
		app.setup();
		while (true) {
			app.loop();
		}
	}

}
