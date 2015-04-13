package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicInteger;

public class Radio {

	private final AtomicInteger signalStrength = new AtomicInteger();
	
	public void setSignalStrength(byte signalStrength) {
		this.signalStrength.set(signalStrength);
	}
	
	public byte getSignalStrength() {
		return (byte) (signalStrength.get() & 0xFF);
	}
	
}
