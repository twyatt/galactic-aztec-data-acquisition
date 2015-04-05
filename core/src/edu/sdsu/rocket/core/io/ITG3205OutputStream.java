package edu.sdsu.rocket.core.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.sdsu.rocket.core.helpers.Stopwatch;

public class ITG3205OutputStream extends DataOutputStream {

	public static final byte SENSOR_VALUES  = 0x0;
	public static final byte SCALING_FACTOR = 0x1;
	
	private final Stopwatch stopwatch = new Stopwatch();
	
	public ITG3205OutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeScalingFactor(float scalingFactor) throws IOException {
		write(SCALING_FACTOR);
		writeLong(stopwatch.nanoSecondsElapsed());
		writeFloat(scalingFactor);
	}
	
	public void writeValues(short x, short y, short z) throws IOException {
		write(SENSOR_VALUES);
		writeLong(stopwatch.nanoSecondsElapsed());
		writeShort(x);
		writeShort(y);
		writeShort(z);
	}

}
