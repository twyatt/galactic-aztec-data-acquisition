package edu.sdsu.rocket.core.models;

import java.util.concurrent.atomic.AtomicInteger;

public class Radio {

	private final AtomicInteger signalStrength = new AtomicInteger();
	
	public void setSignalStrength(int signalStrength) {
		this.signalStrength.set(signalStrength);
	}
	
	public int getSignalStrength() {
		return signalStrength.get();
	}
	
}
