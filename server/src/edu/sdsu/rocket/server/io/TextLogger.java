package edu.sdsu.rocket.server.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TextLogger {

	private final PrintWriter out;

	public TextLogger(String path) throws FileNotFoundException {
		out = new PrintWriter(path);
	}

	public void message(String message) {
		out.println(message);
	}

}
