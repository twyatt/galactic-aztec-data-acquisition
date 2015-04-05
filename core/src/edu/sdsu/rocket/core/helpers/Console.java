package edu.sdsu.rocket.core.helpers;

public class Console {
	
	public static void log() {
		System.out.println();
	}
	
	public static void log(String string) {
		System.out.println(string);
	}
	
	public static void error(Throwable t) {
		error(t.getMessage());
	}
	
	public static void error(String message) {
		System.err.println(message);
	}
	
}
