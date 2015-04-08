package edu.sdsu.rocket.pi.io;

public class SerialMessage {
	
	public static final byte SENSORS = 0x1;
	public static final byte IMAGE   = 0x3;
	
	public int number;
	public byte id;
	public byte[] data;

}
