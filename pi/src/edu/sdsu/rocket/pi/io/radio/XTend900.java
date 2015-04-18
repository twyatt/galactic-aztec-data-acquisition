package edu.sdsu.rocket.pi.io.radio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;

import edu.sdsu.rocket.pi.io.radio.api.APIFrame;
import edu.sdsu.rocket.pi.io.radio.api.TXRequest;

public class XTend900 implements SerialDataEventListener {
	
	public interface XTend900Listener {
		public void onRadioTurnedOff();
		public void onRadioTurnedOn();
		public void onDataReceived(byte[] data); // only used when AP = 0 (default)
	}
	final CopyOnWriteArrayList<XTend900Listener> listeners = new CopyOnWriteArrayList<>();
	
	// only used when AP = 1 or 2
	private final APIFrameHandler apiFrameHandler = new APIFrameHandler();
	
	static final int BUFFER_SIZE = APIFrame.MAXIMUM_FRAME_LENGTH;
	private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

	private OutputStream logOutputStream;
	
	private boolean isTXLedEnabled;
	private GpioPinDigitalOutput txLed;
	private GpioPinDigitalOutput shdn;
	
	private final Serial serial;
	private boolean isCommandMode;

	private final XTend900Config config = new XTend900Config().setDefaults();

	public XTend900(Serial serial) {
		this.serial = serial;
		serial.addListener(this);
	}
	
	public synchronized XTend900 addListener(XTend900Listener ... listener) {
		Collections.addAll(listeners, listener);
		return this;
	}
	
	public synchronized XTend900 removeListener(XTend900Listener ... listener) {
		for (XTend900Listener l : listeners) {
			listeners.remove(l);
		}
		return this;
	}
	
	public XTend900 addAPIListener(APIFrameListener ... listener) {
		apiFrameHandler.addListener(listener);
		return this;
	}
	
	public XTend900 removeAPIListener(APIFrameListener ... listener) {
		apiFrameHandler.removeListener(listener);
		return this;
	}
	
	public synchronized XTend900 setLogOutputStream(OutputStream logOutputStream) {
		this.logOutputStream = logOutputStream;
		return this;
	}
	
	public XTend900 setTXLedEnabled(boolean enabled) {
		isTXLedEnabled = enabled;
		return this;
	}
	
	public XTend900 setup() {
		GpioController gpio = GpioFactory.getInstance();
		shdn = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "SHDN", PinState.LOW);
		txLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "TX_LED", PinState.LOW);
		pulseTXLed(1000);
		return this;
	}
	
	/**
	 * Writes the provided configuration settings to the XTend 900.
	 * 
	 * @param config
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void configure(XTend900Config config) throws IllegalStateException, InterruptedException, IOException {
		if (!serial.isOpen()) {
			throw new IllegalStateException("Serial must be open before configuring " + getClass().getSimpleName());
		}
		
		if (!isOn()) {
			turnOn();
		}
		
		// setup serial listener to see command responses
		SerialDataEventListener listener = new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					System.out.println(event.getAsciiString());
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		};
		serial.addListener(listener);
		
		if (!isCommandMode) {
			enterCommandMode();
		}
		
		System.out.println(config + "," + XTend900Config.Command.EXIT_COMMAND_MODE.getText());
		serial.write(config + "," + XTend900Config.Command.EXIT_COMMAND_MODE.getText() + "\r");
		serial.flush();
		
		this.config.set(config);
		
		Thread.sleep(500L);
		isCommandMode = false;
		
		// clean up serial listener
		serial.removeListener(listener);
	}
	
	public XTend900 enterCommandMode() throws IllegalStateException, IOException, InterruptedException {
		if (isCommandMode) return this;
		
		isCommandMode = true;
		serial.flush();
		Thread.sleep(200L);
		
		System.out.println(XTend900Config.Command.ENTER_AT_COMMAND_MODE.getText());
		serial.write(XTend900Config.Command.ENTER_AT_COMMAND_MODE.getText());
		serial.flush();
		Thread.sleep(1200L);
		
		return this;
	}
	
	public XTend900 requestFirmwareVersionShort() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.FIRMWARE_VERSION_SHORT.getText();
		System.out.println(cmd + " [Firmware Version Short]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestFirmwareVersionVerbose() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.FIRMWARE_VERSION_VERBOSE.getText();
		System.out.println(cmd + " [Firmware Version Verbose]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestBoardVoltage() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.BOARD_VOLTAGE.getText();
		System.out.println(cmd + " [Board Voltage]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestReceivedSignalStrength() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.RECEIVED_SIGNAL_STRENGTH.getText();
		System.out.println(cmd + " [Received Signal Strength]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestHardwareVersion() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.HARDWARE_VERSION.getText();
		System.out.println(cmd + " [Hardware Version]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestBoardTemperature() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.BOARD_TEMPERATURE.getText();
		System.out.println(cmd + " [Board Temperature]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 exitCommandMode() throws IllegalStateException, IOException, InterruptedException {
		String cmd = XTend900Config.COMMAND_PREFIX + XTend900Config.Command.EXIT_COMMAND_MODE.getText();
		System.out.println(cmd + " [Exit Command Mode]");
		writeCommand(cmd);
		isCommandMode = false;
		return this;
	}
	
	private void writeCommand(String string) throws IllegalStateException, IOException {
		serial.write(string + "\r");
		serial.flush();
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
	
	public XTend900 turnOn() {
		shdn.high();
		return this;
	}
	
	public XTend900 turnOff() {
		shdn.low();
		return this;
	}
	
	public XTend900 toggle() {
		shdn.toggle();
		return this;
	}
	
	public boolean isOn() {
		return shdn.isHigh();
	}
	
	public boolean isOff() {
		return shdn.isLow();
	}
	
	synchronized public void send(byte[] data) throws IllegalStateException, IOException {
		if (!isOn()) {
			throw new IllegalStateException(getClass().getSimpleName() + " must be on to send.");
		}
		if (isCommandMode) {
			return;
		}
		
		switch (config.getAPIEnable()) {
		case ENABLED_WITHOUT_ESCAPED_CHARACTERS:
			write(new TXRequest(TXRequest.FRAME_ID_DISABLE, TXRequest.BROADCAST_ADDRESS, TXRequest.OPTIONS_DISABLE_ACK, data));
			break;
		case ENABLED_WITH_ESCAPED_CHARACTERS:
			throw new UnsupportedOperationException("API frame with escaped characters not implemented.");
		default: // DISABLED
			write(data);
			break;
		}
	}
	
	private void write(TXRequest txRequest) throws IllegalStateException, IOException {
		byte[] frameData = txRequest.getFrameData();
		byte checksum = txRequest.getChecksum();
		write(new APIFrame(frameData, checksum));
	}
	
	private void write(APIFrame frame) throws IllegalStateException, IOException {
		WRITE_BUFFER.clear();
		WRITE_BUFFER.put(frame.getStartDelimiter());
		WRITE_BUFFER.putShort(frame.getLength());
		WRITE_BUFFER.put(frame.getFrameData());
		WRITE_BUFFER.put(frame.getChecksum());
		
		byte[] data = new byte[WRITE_BUFFER.position()];
		WRITE_BUFFER.rewind();
		WRITE_BUFFER.get(data);
		
		int offset = 0;
		int length = data.length;
		serial.write(data, offset, length);
		
		pulseTXLed(length);
	}
	
	private void write(byte[] data) throws IllegalStateException, IOException {
		serial.write(data);
		pulseTXLed(data.length);
	}
	
	private void pulseTXLed(long duration) {
		if (isTXLedEnabled) {
			txLed.pulse(duration);
		}
	}

	@Override
	public void dataReceived(SerialDataEvent event) {
		try {
			byte[] data = event.getBytes();
			if (logOutputStream != null) {
				logOutputStream.write(data, 0 /* offset */, data.length);
			}
			
			switch (config.getAPIEnable()) {
			case ENABLED_WITHOUT_ESCAPED_CHARACTERS:
				apiFrameHandler.onData(data);
				break;
			case ENABLED_WITH_ESCAPED_CHARACTERS:
				throw new UnsupportedOperationException("API frame with escaped characters not implemented.");
			default: // DISABLED
				Collection<XTend900Listener> listenersCopy = new ArrayList<XTend900Listener>(listeners);
		        for (XTend900Listener listener : listenersCopy) {
	                listener.onDataReceived(data);
		        }
				break;
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
