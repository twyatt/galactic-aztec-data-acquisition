package edu.sdsu.rocket.server.io.radio;

import edu.sdsu.rocket.server.io.radio.api.RFModuleStatus;
import edu.sdsu.rocket.server.io.radio.api.RXPacket;
import edu.sdsu.rocket.server.io.radio.api.TXStatus;

public interface APIFrameListener {

	public void onRXPacket(RXPacket rxPacket);
	public void onRFModuleStatus(RFModuleStatus rfModuleStatus);
	public void onTXStatus(TXStatus txStatus);
	
}
