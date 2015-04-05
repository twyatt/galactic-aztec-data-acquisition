package edu.sdsu.rocket.core.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.sdsu.rocket.core.helpers.Stopwatch;

public class ADS1115OutputStream extends DataOutputStream {
	
	public static final byte SENSOR_VALUE = 0x0;
	
	private final Stopwatch stopwatch = new Stopwatch();

	public ADS1115OutputStream(OutputStream out) {
		super(out);
	}

	public void writeValue(int channel, float value) throws IOException {
		write(SENSOR_VALUE);
		writeLong(stopwatch.nanoSecondsElapsed());
		writeInt(channel);
		writeFloat(value);
	}

}
