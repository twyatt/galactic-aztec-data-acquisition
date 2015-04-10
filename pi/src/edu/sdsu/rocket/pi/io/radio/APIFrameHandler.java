package edu.sdsu.rocket.pi.io.radio;

import java.nio.ByteBuffer;

public class APIFrameHandler {
	
	private static final ByteBuffer READ_BUFFER = ByteBuffer.allocate(XTend900.BUFFER_SIZE);
	
	private APIFrameListener listener;
	
	public APIFrameHandler(APIFrameListener listener) {
		setListener(listener);
	}
	
	public void setListener(APIFrameListener listener) {
		this.listener = listener;
	}
	
	public void onData(byte[] data) {
		// TODO parse data
	}


}
