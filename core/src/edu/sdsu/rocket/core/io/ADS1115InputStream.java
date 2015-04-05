package edu.sdsu.rocket.core.io;

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
			case ADS1115OutputStream.SENSOR_VALUE:
				ADS1115Reading reading = new ADS1115Reading();
				reading.timestamp = readLong();
				int channel = readInt();
				for (int i = 0; i < 4; i++) {
					reading.values[i] = channel == i ? readFloat() : Float.NaN;
				}
				return reading;
			default:
				throw new IOException("Unsupported value type: " + type);
			}
		}
		return null;
	}

}
