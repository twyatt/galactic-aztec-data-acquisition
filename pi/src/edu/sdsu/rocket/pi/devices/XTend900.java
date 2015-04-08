package edu.sdsu.rocket.pi.devices;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;

import edu.sdsu.rocket.core.models.Sensors;
import edu.sdsu.rocket.pi.devices.DeviceManager.Device;

public class XTend900 implements Device {
	
	public enum Mode {
		POINT_TO_POINT, // default
		POINT_TO_MULTIPOINT_BASE,
		POINT_TO_MULTIPOINT_REMOTE,
	}
	
	public enum Command {
		ENTER_AT_COMMAND_MODE   ("+++"),
		BOARD_VOLTAGE           ("AT%V"),
		INTERFACE_DATA_RATE     ("ATBD"),
		RF_DATA_RATE            ("ATBR"),
		GPO2_CONFIGURATION      ("ATCD"),
		NUMBER_BASE             ("ATCF"),
		EXIT_AT_COMMAND_MODE    ("ATCN"),
		RECEIVED_SIGNAL_STRENGTH("ATDB"),
		HARDWARE_VERSION        ("ATHV"),
		AUTOSET_MY              ("ATAM"),
		DESTINATION_ADDRESS     ("ATDT"),
		SOURCE_ADDRESS          ("ATMY"),
		TX_POWER_LEVEL          ("ATPL"),
		BOARD_TEMPERATURE       ("ATTP"),
		TRANSMIT_ONLY           ("ATTX"),
		;
		final String text;
		Command(String text) {
			this.text = text;
		}
	}
	
	public enum NumberBase {
		DEFAULT_WITH_UNITS        (0),
		UNSIGNED_HEX_WITHOUT_UNITS(1), // default
		DEFAULT_WITHOUT_UNITS     (2),
		;
		final int parameter;
		NumberBase(int parameter) {
			this.parameter = parameter;
		}
	}
	
	public enum InterfaceDataRate {
		BAUD_1200  (0),
		BAUD_2400  (1),
		BAUD_4800  (2),
		BAUD_9600  (3), // default
		BAUD_19200 (4),
		BAUD_38400 (5),
		BAUD_57600 (6),
		BAUD_115200(7),
		BAUD_230400(8),
		;
		final int paramter;
		InterfaceDataRate(int parameter) {
			paramter = parameter;
		}
	}
	
	public enum RFDataRate {
		BAUD_9600  (0),
		BAUD_115200(1), // default
		;
		final int parameter;
		RFDataRate(int parameter) {
			this.parameter = parameter;
		}
		public static RFDataRate valueOf(int value) {
			for (RFDataRate v : values()) {
				if (value == v.parameter) return v;
			}
			return BAUD_115200;
		}
	}
	
	public enum GPO2Configuration {
		RX_LED                   (0),
		DEFAULT_HIGH             (1),
		DEFAULT_LOW              (2), // default
		RX_LED_VALID_ADDRESS_ONLY(4),
		;
		final int parameter;
		GPO2Configuration(int parameter) {
			this.parameter = parameter;
		}
	}
	
	public enum TXPowerLevel {
		TX_1mW   (0),
		TX_10mW  (1),
		TX_100mW (2),
		TX_500mW (3),
		TX_1000mW(4), // default
		;
		final int parameter;
		TXPowerLevel(int parameter) {
			this.parameter = parameter;
		}
		public static TXPowerLevel valueOf(int value) {
			for (TXPowerLevel v : values()) {
				if (value == v.parameter) return v;
			}
			return TX_1000mW;
		}
	}
	
	public enum TransmitOnly {
		TX_RX  (0), // default
		TX_ONLY(1),
		;
		final int parameter;
		TransmitOnly(int parameter) {
			this.parameter = parameter;
		}
		public static TransmitOnly valueOf(int value) {
			for (TransmitOnly v : values()) {
				if (value == v.parameter) return v;
			}
			return TX_RX;
		}
	}
	
	private static final byte[] START_BYTES = { (byte) 0xF0, (byte) 0x0D };
	
	private static final int BUFFER_SIZE = 1024;
	private static final ByteBuffer BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

	private final Serial serial;
	private final Sensors sensors;

	private final GpioPinDigitalOutput txLed;
	private final GpioPinDigitalOutput shdn;

	private boolean isCommandMode;
	
	private int id;

	public XTend900(Serial serial, Sensors sensors) {
		this.serial = serial;
		this.sensors = sensors;
		
		GpioController gpio = GpioFactory.getInstance();
		
		shdn = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "SHDN", PinState.LOW);
		txLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "TX_LED", PinState.LOW);
		
		txLed.pulse(1000L);
	}
	
	public XTend900 setId(int id) {
		this.id = id;
		return this;
	}
	
	public XTend900 enterATCommandMode() throws InterruptedException, IllegalStateException, IOException {
		isCommandMode = true;
		serial.flush();
		Thread.sleep(500L);
		String cmd = Command.ENTER_AT_COMMAND_MODE.text;
		System.out.println(cmd + " [Enter AT Command Mode]");
		serial.write(cmd);
		serial.flush();
		Thread.sleep(1000L);
		return this;
	}
	
	public XTend900 exitATCommandMode() throws IllegalStateException, IOException {
		String cmd = Command.EXIT_AT_COMMAND_MODE.text;
		System.out.println(cmd + " [Exit AT Command Mode]");
		writeln(cmd);
		isCommandMode = false;
		return this;
	}
	
	public XTend900 requestBoardVoltage() throws IllegalStateException, IOException {
		String cmd = Command.BOARD_VOLTAGE.text;
		System.out.println(cmd + " [Board Voltage]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 requestReceivedSignalStrength() throws IllegalStateException, IOException {
		String cmd = Command.RECEIVED_SIGNAL_STRENGTH.text;
		System.out.println(cmd + " [Received Signal Strength]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 requestHardwareVersion() throws IllegalStateException, IOException {
		String cmd = Command.HARDWARE_VERSION.text;
		System.out.println(cmd + " [Hardware Version]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 requestBoardTemperature() throws IllegalStateException, IOException {
		String cmd = Command.BOARD_TEMPERATURE.text;
		System.out.println(cmd + " [Board Temperature]");
		writeln(cmd);
		return this;
	}

	public XTend900 writeNumberBase(NumberBase base) throws IllegalStateException, IOException {
		String cmd = Command.NUMBER_BASE.text + base.parameter;
		System.out.println(cmd + " [Number Base: " + base + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeInterfaceDataRate(InterfaceDataRate rate) throws IllegalStateException, IOException {
		String cmd = Command.INTERFACE_DATA_RATE.text + rate.paramter;
		System.out.println(cmd + " [Interface Data Rate: " + rate + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeRFDataRate(RFDataRate rate) throws IllegalStateException, IOException {
		String cmd = Command.RF_DATA_RATE.text + rate.parameter;
		System.out.println(cmd + " [RF Data Rate: " + rate + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeGPO2Configuration(GPO2Configuration config) throws IllegalStateException, IOException {
		String cmd = Command.GPO2_CONFIGURATION.text + config.parameter;
		System.out.println(cmd + " [GPO2 Configuration: " + config + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeTXPowerLevel(TXPowerLevel level) throws IllegalStateException, IOException {
		String cmd = Command.TX_POWER_LEVEL.text + level.parameter;
		System.out.println(cmd + " [TX Power Level: " + level + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeAutosetMY() throws IllegalStateException, IOException {
		String cmd = Command.AUTOSET_MY.text;
		System.out.println(cmd + " [Auto-set MY]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeSourceAddress(String address) throws IllegalStateException, IOException {
		String cmd = Command.SOURCE_ADDRESS.text + address;
		System.out.println(cmd + " [Source Address: " + address + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeDestinationAddress(String address) throws IllegalStateException, IOException {
		String cmd = Command.DESTINATION_ADDRESS.text + address;
		System.out.println(cmd + " [Destination Address: " + address + "]");
		writeln(cmd);
		return this;
	}
	
	public XTend900 writeTransmitOnly(TransmitOnly txOnly) throws IllegalStateException, IOException {
		String cmd = Command.TRANSMIT_ONLY.text + txOnly.parameter;
		System.out.println(cmd + " [Transmit Only: " + txOnly + "]");
		writeln(cmd);
		return this;
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
	
	private void writeln(String string) throws IllegalStateException, IOException {
		serial.write(string + "\r");
		serial.flush();
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	@Override
	public void loop() throws IOException, InterruptedException {
		if (isOn() && !isCommandMode) {
			sendSensorData();
		} else {
			Thread.sleep(250L);
		}
	}

	private void sendSensorData() throws IllegalStateException, IOException {
		BUFFER.clear();
		
		BUFFER.put(START_BYTES);
		int lengthPosition = BUFFER.position();
		BUFFER.putInt(0); // length placeholder
		
		int start = BUFFER.position();
		BUFFER.putInt(id);
		sensors.toByteBuffer(BUFFER);
		int end = BUFFER.position();
		int length = end - start;
		
		BUFFER.putInt(lengthPosition, length);
		BUFFER.flip();
		
//		txLed.pulse(100L);
		serial.write(BUFFER);
	}

}
