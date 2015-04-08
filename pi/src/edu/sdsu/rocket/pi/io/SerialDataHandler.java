package edu.sdsu.rocket.pi.io;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;

public class SerialDataHandler implements SerialDataEventListener {

	private final SerialMessageHandler handler;

	public SerialDataHandler(SerialMessageHandler handler) {
		this.handler = handler;
	}

	@Override
	public void dataReceived(SerialDataEvent event) {
		// TODO Auto-generated method stub

	}

}
