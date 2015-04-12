package edu.sdsu.rocket.pi.io.radio.api;

import java.nio.ByteBuffer;

public class RXPacket {

	private static final byte ACK_BIT = 0x1;
	private static final byte INDICATE_BROADCAST_BIT = 0x2;

	/**
	 * Source Address
	 * MSB (most significant byte) first, LSB (least significant) last
	 */
	short sourceAddress;
	
	/**
	 * RSSI
	 * Received Signal Strength Indicator - Hexadecimal equivalent of (-dBm)
	 * value. (For example: If RX signal strength = -40 dBm, "0x28" (40 decimal)
	 * is returned)
	 */
	byte signalStrength;
	
	/**
	 * Options
	 * bit 0 = ACK
	 * bit 1 = Indicate broadcast
	 * bits 2-7 [reserved]
	 */
	byte options;
	
	/**
	 * RF Data
	 * Up to 2048 Bytes per packet
	 */
	byte[] rfData;
	
	public RXPacket(byte[] frameData) {
		ByteBuffer buffer = ByteBuffer.wrap(frameData);
		sourceAddress = buffer.getShort();
		signalStrength = buffer.get();
		options = buffer.get();
		rfData = new byte[buffer.remaining()];
		buffer.get(rfData);
	}

	public boolean isACK() {
		return (options & ACK_BIT) != 0;
	}
	
	public boolean isBroadcast() {
		return (options & INDICATE_BROADCAST_BIT) != 0;
	}
	
	public short getSourceAddres() {
		return sourceAddress;
	}
	
	public byte getSignalStrength() {
		return signalStrength;
	}
	
	public byte getOptions() {
		return options;
	}
	
	public byte[] getRFData() {
		return rfData;
	}
	
}
