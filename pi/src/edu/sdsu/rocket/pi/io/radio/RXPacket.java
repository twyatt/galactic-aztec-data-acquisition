package edu.sdsu.rocket.pi.io.radio;

public class RXPacket {

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
	byte[] data;
	
	public boolean isACK() {
		return (options & 0x1) != 0;
	}
	
	public boolean isBroadcast() {
		return (options & 0x2) != 0;
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
	
	public byte[] getData() {
		return data;
	}
	
}
