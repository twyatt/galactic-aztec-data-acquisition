package edu.sdsu.rocket.log2csv;

import java.io.File;

public class Launcher {
	
	private static final String NAME = System.getProperty("program.name");

	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
			System.exit(1);
		}
		
		File folder = new File(args[0]);
		if (!folder.exists()) {
			System.err.println("Location not found: " + folder);
			System.exit(1);
		}
		if (!folder.isDirectory()) {
			usage();
			System.exit(1);
		}
		
		Converter converter = new Converter(folder);
		converter.convert();
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("  " + NAME + " FOLDER");
		System.out.println();
	}
	
}
