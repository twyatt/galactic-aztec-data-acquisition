package edu.sdsu.rocket.server.io.radio.api;

public class RFModuleStatus {

	private static final byte HARDWARE_RESET_BIT       = 0x0;
	private static final byte WATCHDOG_TIMER_RESET_BIT = 0x1;
	
	private final byte[] frameData;
	
	public RFModuleStatus(byte[] frameData) {
		this.frameData = frameData;
	}
	
	public byte[] getFrameData() {
		return frameData;
	}
	
	public byte getStatus() {
		return getFrameData()[0];
	}
	
	public boolean isHardwareReset() {
		return (getStatus() & HARDWARE_RESET_BIT) != 0;
	}
	
	public boolean isWatchdogTimerReset() {
		return (getStatus() & WATCHDOG_TIMER_RESET_BIT) != 0;
	}
	
}
