package edu.sdsu.rocket.data.io;

public class Message {
	
	public static final byte[] START_BYTES = new byte[] { (byte) 0xF0, 0x0D };

	public static final byte SENSOR_REQUEST = 0x1;
	public static final byte SENSOR_RESPONSE = 0x1;
	
	public int id;
	public byte[] data;
	
}
