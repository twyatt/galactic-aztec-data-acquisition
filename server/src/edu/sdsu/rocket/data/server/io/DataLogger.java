package edu.sdsu.rocket.data.server.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import edu.sdsu.rocket.data.helpers.Stopwatch;

public class DataLogger {

	private final File logDir;

	public DataLogger(File logDir) throws IOException {
		if (!logDir.isDirectory() || !logDir.canWrite()) {
			throw new IOException("Log directory does not exist or is not writable: " + logDir);
		}
		this.logDir = logDir;
	}

	public void log(String filename, float[] values) {
		log(getOutputStream(filename), values);
	}
	
	private void log(DataOutputStream out, float[] values) {
		try {
			out.writeLong(Stopwatch.nanoSecondsElapsed());
			for (float value : values) {
				out.writeFloat(value);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private DataOutputStream getOutputStream(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

}
