package edu.sdsu.rocket.log2csv;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ADS1115InputStream extends DataInputStream {
	
	public class ADS1115Reading {
		public long timestamp;
		public float values[] = new float[4];
	}

	public ADS1115InputStream(InputStream in) {
		super(in);
	}
	
	public ADS1115Reading readReading() throws IOException {
		int type = 0;
		while ((type = read()) != -1) {
			switch (type) {
			case 0: // sensor values
				ADS1115Reading reading = new ADS1115Reading();
				reading.timestamp = readLong();
				reading.values[0] = readFloat();
				reading.values[1] = readFloat();
				reading.values[2] = readFloat();
				reading.values[3] = readFloat();
				return reading;
			default:
				throw new IOException("Unsupported value type: " + type);
			}
		}
		return null;
	}

}
