package edu.sdsu.rocket.core.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HMC5883LInputStream extends DataInputStream {
	
	public class HMC5883LReading {
		public long timestamp;
		public float scalingFactor = 1f;
		public short values[] = new short[3];
	}

	private float scalingFactor = 1f;

	public HMC5883LInputStream(InputStream in) {
		super(in);
	}
	
	public HMC5883LReading readReading() throws IOException {
		int type = 0;
		while ((type = read()) != -1) {
			switch (type) {
			case HMC5883LOutputStream.SENSOR_VALUES:
				HMC5883LReading reading = new HMC5883LReading();
				reading.timestamp = readLong();
				reading.values[0] = readShort();
				reading.values[1] = readShort();
				reading.values[2] = readShort();
				reading.scalingFactor = scalingFactor;
				return reading;
			case HMC5883LOutputStream.SCALING_FACTOR:
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
