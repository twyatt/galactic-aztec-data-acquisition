package edu.sdsu.rocket.core.net;

import java.net.DatagramPacket;

public interface DatagramPacketListener {

	public void onPacketReceived(DatagramPacket packet);
	
}
