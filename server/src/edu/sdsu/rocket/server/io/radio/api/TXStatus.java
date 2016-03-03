package edu.sdsu.rocket.server.io.radio.api;

public class TXStatus {

//	private static final int API_IDENTIFIER_INDEX = 0;
	private static final int FRAME_ID_INDEX       = 1;
	private static final int STATUS_INDEX         = 2;
	
	private final byte[] frameData;

	public TXStatus(byte[] frameData) {
		this.frameData = frameData;
	}
	
	public byte getFrameID() {
		return frameData[FRAME_ID_INDEX];
	}
	
	public byte getStatus() {
		return frameData[STATUS_INDEX];
	}
	
	public boolean isSuccess() {
		return getStatus() == 0;
	}
	
	public boolean isNoACKReceived() {
		return getStatus() == 1;
	}

}
