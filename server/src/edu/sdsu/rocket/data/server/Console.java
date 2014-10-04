package edu.sdsu.rocket.data.server;

public class Console {
	
	public static void log() {
		System.out.println();
	}
	
	public static void log(String string) {
		System.out.println(string);
	}
	
	public static void error(String message) {
		System.err.println(message);
	}
	
}
