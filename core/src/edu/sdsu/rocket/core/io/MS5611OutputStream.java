package edu.sdsu.rocket.core.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.sdsu.rocket.core.helpers.Stopwatch;

public class MS5611OutputStream extends DataOutputStream {
	
	public static final byte SENSOR_VALUES = 0x0;
	public static final byte FAULT         = 0x1;
	
	private final Stopwatch stopwatch = new Stopwatch();

	public MS5611OutputStream(OutputStream out) {
		super(out);
	}

	public void writeValues(int T, int P) throws IOException {
		write(SENSOR_VALUES);
		writeLong(stopwatch.nanoSecondsElapsed());
		writeInt(T);
		writeInt(P);
	}
	
	public void writeFault(int fault) throws IOException {
		write(FAULT);
		writeLong(stopwatch.nanoSecondsElapsed());
		writeInt(fault);
	}

}
