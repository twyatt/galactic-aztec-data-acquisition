package edu.sdsu.rocket.core.net;

import java.net.SocketAddress;

public class DatagramMessage {
	
	public static final byte PING            = 0x0;
	public static final byte SENSORS_LOCAL   = 0x1;
	public static final byte SENSORS_REMOTE1 = 0x2;
	public static final byte SENSORS_REMOTE2 = 0x3;
	
	public SocketAddress address;
	public int number;
	public byte id;
	public byte[] data;
	
}
