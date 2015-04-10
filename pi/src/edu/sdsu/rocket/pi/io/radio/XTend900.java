package edu.sdsu.rocket.pi.io.radio;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;

public class XTend900 implements SerialDataEventListener {
	
	// only used when AP = 0 (default)
	public interface XTend900Listener {
		public void onDataReceived(byte[] data);
	}
	
	/*
	 * Start Delimiter |   Length  |        Frame Data      | Checksum
	 *       0x7E      | MSB | LSB | API Identifier | Data* |
	 *      Byte 1     | Bytes 2-3 |     Byte 4     |       |  1 Byte
	 *
	 * *Data may be up to 2048 bytes.
	 */
	static class APIFrame {
		
		// API Identifier (4) + Data (2048)
		static final int MAXIMUM_FRAME_DATA_LENGTH = 4 + 2048;
		
		// Start Delimiter (1) + Length (2) + Frame Data + Checksum (1)
		static final int MAXIMUM_FRAME_LENGTH = 1 + 2 + MAXIMUM_FRAME_DATA_LENGTH + 1;
		
		public byte startDelimiter = 0x7E;
		public short length;
		public byte[] data;
		public short checksum;
		
		public APIFrame(byte[] data) {
			if (data.length > MAXIMUM_FRAME_DATA_LENGTH) {
				throw new IllegalArgumentException("Frame data length " + data.length + " exceed maximum data length of " + MAXIMUM_FRAME_DATA_LENGTH + ".");
			}
			length = (short) data.length;
			this.data = data;
			checksum = checksum(data);
		}

		public static short checksum(byte[] data) {
			int total = 0;
			for (byte b : data) total += b;
			return (short) (0xFF - (total & 0xFF));
		}
		
		public static boolean verify(byte[] data, short checksum) {
			int total = 0;
			for (byte b : data) total += b;
			total += checksum;
			return total == 0xFF;
		}
		
	}
	
	static final int BUFFER_SIZE = APIFrame.MAXIMUM_FRAME_LENGTH;
	private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

	private XTend900Listener listener;
	private APIFrameListener apiFrameListener;
	private APIFrameHandler apiFrameHandler;
	
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
	
	public XTend900 setListener(XTend900Listener listener) {
		this.listener = listener;
		return this;
	}
	
	public XTend900 setAPIListener(APIFrameListener listener) {
		apiFrameListener = listener;
		if (apiFrameHandler != null) {
			apiFrameHandler.setListener(apiFrameListener);
		}
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
		if (!isCommandMode) {
			enterCommandMode();
		}
		
		System.out.println(config);
		serial.write(config + "," + XTend900Config.Command.EXIT_COMMAND_MODE + "\r");
		serial.flush();
		
		this.config.set(config);
		
		switch (config.getAPIEnable()) {
		case ENABLED_WITHOUT_ESCAPED_CHARACTERS:
			apiFrameHandler = new APIFrameHandler(apiFrameListener);
			break;
		case ENABLED_WITH_ESCAPED_CHARACTERS:
			throw new UnsupportedOperationException("API frame with escaped characters not implemented.");
		default:
			break;
		}
		
		Thread.sleep(500L);
		isCommandMode = false;
	}
	
	private void enterCommandMode() throws IllegalStateException, IOException, InterruptedException {
		if (isCommandMode) return;
		
		isCommandMode = true;
		serial.flush();
		Thread.sleep(500L);
		
		System.out.println(XTend900Config.Command.ENTER_AT_COMMAND_MODE);
		serial.write(XTend900Config.Command.ENTER_AT_COMMAND_MODE.toString());
		serial.flush();
		Thread.sleep(1000L);
	}
	
	public XTend900 requestBoardVoltage() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.Command.BOARD_VOLTAGE.toString();
		System.out.println(cmd + " [Board Voltage]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestReceivedSignalStrength() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.Command.RECEIVED_SIGNAL_STRENGTH.toString();
		System.out.println(cmd + " [Received Signal Strength]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestHardwareVersion() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.Command.HARDWARE_VERSION.toString();
		System.out.println(cmd + " [Hardware Version]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 requestBoardTemperature() throws IllegalStateException, IOException, InterruptedException {
		enterCommandMode();
		String cmd = XTend900Config.Command.BOARD_TEMPERATURE.toString();
		System.out.println(cmd + " [Board Temperature]");
		writeCommand(cmd);
		return this;
	}
	
	public XTend900 exitCommandMode() throws IllegalStateException, IOException, InterruptedException {
		writeCommand(XTend900Config.Command.EXIT_COMMAND_MODE.toString());
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
			write(new APIFrame(data));
			break;
		case ENABLED_WITH_ESCAPED_CHARACTERS:
			throw new UnsupportedOperationException("API frame with escaped characters not implemented.");
		default: // DISABLED
			write(data);
			break;
		}
	}
	
	private void write(APIFrame frame) throws IllegalStateException, IOException {
		WRITE_BUFFER.clear();
		WRITE_BUFFER.put(frame.startDelimiter);
		WRITE_BUFFER.putShort(frame.length);
		WRITE_BUFFER.put(frame.data);
		WRITE_BUFFER.put((byte) (frame.checksum & 0xFF));
		
		byte[] data = WRITE_BUFFER.array();
		int offset = 0;
		int length = WRITE_BUFFER.position();
		serial.write(data, offset, length);
		
		pulseTXLed(WRITE_BUFFER.position());
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
			
			switch (config.getAPIEnable()) {
			case ENABLED_WITHOUT_ESCAPED_CHARACTERS:
				if (apiFrameHandler != null) {
					apiFrameHandler.onData(data);
				}
				break;
			case ENABLED_WITH_ESCAPED_CHARACTERS:
				throw new UnsupportedOperationException("API frame with escaped characters not implemented.");
			default: // DISABLED
				if (listener != null) {
					listener.onDataReceived(data);
				}
				break;
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
