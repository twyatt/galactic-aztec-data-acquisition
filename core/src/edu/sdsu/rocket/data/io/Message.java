package edu.sdsu.rocket.data.io;

import java.net.SocketAddress;

public class Message {
	
	public static final byte PING   = 0x0;
	public static final byte SENSOR = 0x1;
	
	public SocketAddress address;
	public int number;
	public int id;
	public byte[] data;
	
}
