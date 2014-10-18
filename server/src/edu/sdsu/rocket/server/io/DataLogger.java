package edu.sdsu.rocket.server.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.sdsu.rocket.helpers.Stopwatch;
import edu.sdsu.rocket.server.Application;
import edu.sdsu.rocket.server.Console;

public class DataLogger {
	
	private static final byte SENSOR_VALUES = 0x0;
	
	private final Stopwatch stopwatch = new Stopwatch();
	private final Map<String, DataOutputStream> streams = new HashMap<String, DataOutputStream>();
	
	private boolean isEnabled = true;
	
	private final File logDir;

	public DataLogger(File logDir) throws IOException {
		if (!logDir.isDirectory() || !logDir.canWrite()) {
			throw new IOException("Log directory does not exist or is not writable: " + logDir);
		}
		this.logDir = logDir;
	}
	
	public Stopwatch getStopwatch() {
		return stopwatch;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void enable() {
		isEnabled = true;
	}
	
	public void disable() {
		isEnabled = false;
	}
	
	public void log(String filename, byte id, float value) throws FileNotFoundException {
		if (isEnabled) {
			log(getOutputStream(filename), id, value);
		}
	}
	
	public void log(String filename, int[] values) throws FileNotFoundException {
		if (isEnabled) {
			log(getOutputStream(filename), values);
		}
	}
	
	public void log(String filename, short[] values) throws FileNotFoundException {
		if (isEnabled) {
			log(getOutputStream(filename), values);
		}
	}
	
	public void log(String filename, float[] values) throws FileNotFoundException {
		if (isEnabled) {
			log(getOutputStream(filename), values);
		}
	}
	
	/**
	 * Writes to the specified output stream:
	 *   id (byte)
	 *   value (float)
	 * 
	 * @param out
	 * @param id
	 * @param value
	 */
	private void log(DataOutputStream out, byte id, float value) {
		try {
			out.write(id);
			out.writeFloat(value);
		} catch (IOException e) {
			error(e);
		}
	}
	
	/**
	 * Writes to the specified output stream:
	 *   0x0 (byte)
	 *   timestamp (long)
	 *   values (int, int, ...)
	 * 
	 * @param out
	 * @param values
	 */
	private void log(DataOutputStream out, int[] values) {
		try {
			out.writeLong(stopwatch.nanoSecondsElapsed());
			out.write(SENSOR_VALUES);
			for (int value : values) {
				out.writeInt(value);
			}
		} catch (IOException e) {
			error(e);
		}
	}
	
	/**
	 * Writes to the specified output stream:
	 *   0x0 (byte)
	 *   timestamp (long)
	 *   values (short, short, ...)
	 * 
	 * @param out
	 * @param values
	 */
	private void log(DataOutputStream out, short[] values) {
		try {
			out.writeLong(stopwatch.nanoSecondsElapsed());
			out.write(SENSOR_VALUES);
			for (short value : values) {
				out.writeShort(value);
			}
		} catch (IOException e) {
			error(e);
		}
	}
	
	/**
	 * Writes to the specified output stream:
	 *   0x0 (byte)
	 *   timestamp (long)
	 *   values (float, float, ...)
	 * 
	 * @param out
	 * @param values
	 */
	private void log(DataOutputStream out, float[] values) {
		try {
			out.writeLong(stopwatch.nanoSecondsElapsed());
			out.write(SENSOR_VALUES);
			for (float value : values) {
				out.writeFloat(value);
			}
		} catch (IOException e) {
			error(e);
		}
	}

	private DataOutputStream getOutputStream(String filename) throws FileNotFoundException {
		DataOutputStream stream = streams.get(filename);
		if (stream == null) {
			String file = logDir + Application.FILE_SEPARATOR + filename;
			stream = new DataOutputStream(new FileOutputStream(file));
			streams.put(filename, stream);
		}
		return stream;
	}
	
	private static void error(Throwable e) {
		Console.error(e.getMessage());
	}

}
