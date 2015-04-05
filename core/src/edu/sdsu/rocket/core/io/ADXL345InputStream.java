package edu.sdsu.rocket.core.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ADXL345InputStream extends DataInputStream {
	
	public class ADXL345Reading {
		public long timestamp;
		public float scalingFactor = 1f;
		public short values[] = new short[3];
	}

	private float scalingFactor = 1f;

	public ADXL345InputStream(InputStream in) {
		super(in);
	}
	
	public ADXL345Reading readReading() throws IOException {
		int type = 0;
		while ((type = read()) != -1) {
			switch (type) {
			case ADXL345OutputStream.SENSOR_VALUES:
				ADXL345Reading reading = new ADXL345Reading();
				reading.timestamp = readLong();
				reading.values[0] = readShort();
				reading.values[1] = readShort();
				reading.values[2] = readShort();
				reading.scalingFactor = scalingFactor;
				return reading;
			case ADXL345OutputStream.SCALING_FACTOR:
				readLong(); // timestamp
				scalingFactor = readFloat();
				break;
			default:
				throw new IOException("Unsupported value type: " + type);
			}
		}
		return null;
	}

}
