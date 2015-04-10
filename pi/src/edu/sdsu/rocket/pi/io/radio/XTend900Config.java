package edu.sdsu.rocket.pi.io.radio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class XTend900Config {
	
	public enum Command {
		ENTER_AT_COMMAND_MODE   ("+++"),
		BOARD_VOLTAGE           ("%V"),
		AUTOSET_MY              ("AM"),
		API_ENABLE              ("AP"),
		INTERFACE_DATA_RATE     ("BD"),
		RF_DATA_RATE            ("BR"),
		GPO2_CONFIGURATION      ("CD"),
		NUMBER_BASE             ("CF"),
		EXIT_COMMAND_MODE       ("CN"),
		RECEIVED_SIGNAL_STRENGTH("DB"),
		DESTINATION_ADDRESS     ("DT"),
		HARDWARE_VERSION        ("HV"),
		SOURCE_ADDRESS          ("MY"),
		BOARD_TEMPERATURE       ("TP"),
		TX_POWER_LEVEL          ("PL"),
		RETRIES                 ("RR"),
		TRANSMIT_ONLY           ("TX"),
		;
		final String text;
		Command(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	}
	private static final String COMMAND_PREFIX = "AT";
	private static final String COMMAND_SEPARATOR = ",";
	
	public enum APIEnable {
		DISABLED                          (0), // default
		ENABLED_WITHOUT_ESCAPED_CHARACTERS(1),
		ENABLED_WITH_ESCAPED_CHARACTERS   (2),
		;
		final int parameter;
		APIEnable(int parameter) {
			this.parameter = parameter;
		}
		public String getText() {
			return Command.API_ENABLE.getText() + parameter;
		}
	}
	
	public enum InterfaceDataRate {
		BAUD_1200  (0, 1200),
		BAUD_2400  (1, 2400),
		BAUD_4800  (2, 4800),
		BAUD_9600  (3, 9600), // default
		BAUD_19200 (4, 19200),
		BAUD_38400 (5, 38400),
		BAUD_57600 (6, 57600),
		BAUD_115200(7, 115200),
		BAUD_230400(8, 230400),
		;
		final int parameter;
		final int baud;
		InterfaceDataRate(int parameter, int baud) {
			this.parameter = parameter;
			this.baud = baud;
		}
		public int getBaud() {
			return baud;
		}
		public String getText() {
			return Command.INTERFACE_DATA_RATE.getText() + parameter;
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
		public String getText() {
			return Command.RF_DATA_RATE.getText() + parameter;
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
		public String getText() {
			return Command.GPO2_CONFIGURATION.getText() + parameter;
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
		public String getText() {
			return Command.NUMBER_BASE.getText() + parameter;
		}
	}
	
	public class DestinationAddress {
		final String address;
		public DestinationAddress() {
			this.address = "0"; // default
		}
		public DestinationAddress(String address) {
			this.address = address;
		}
		public String getAddress() {
			return address;
		}
		public String getText() {
			return Command.DESTINATION_ADDRESS.getText() + address;
		}
	}
	
	public class SourceAddress {
		final String address;
		public SourceAddress() {
			this.address = "FFFF"; // default;
		}
		public SourceAddress(String address) {
			this.address = address;
		}
		public String getAddress() {
			return address;
		}
		public String getText() {
			return Command.SOURCE_ADDRESS.getText() + address;
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
		public String getText() {
			return Command.TX_POWER_LEVEL.getText() + parameter;
		}
	}
	
	public class Retries implements Json.Serializable {
		final int parameter;
		public Retries(int parameter) {
			this.parameter = parameter;
		}
		public String getText() {
			return Command.RETRIES.getText() + String.format("%02x", parameter).toUpperCase();
		}
		@Override
		public void write(Json json) {
			json.writeValue("retries", parameter);
			// TODO Auto-generated method stub
		}
		@Override
		public void read(Json json, JsonValue jsonData) {
			jsonData.getInt("retries", 10);
			// TODO Auto-generated method stub
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
		public String getText() {
			return Command.TRANSMIT_ONLY.getText() + parameter;
		}
	}
	
	private boolean autosetMy;
	private APIEnable apiEnable;
	private InterfaceDataRate interfaceDataRate;
	private RFDataRate rfDataRate;
	private GPO2Configuration gpo2Configuration;
	private NumberBase numberBase;
	private DestinationAddress destinationAddress;
	private SourceAddress sourceAddress;
	private TXPowerLevel txPowerLevel;
	private Short retries;
	private TransmitOnly transmitOnly;
	
	public XTend900Config set(XTend900Config config) {
		autosetMy = config.getAutosetMy();
		apiEnable = config.getAPIEnable();
		interfaceDataRate = config.getInterfaceDataRate();
		rfDataRate = config.getRFDataRate();
		gpo2Configuration = config.getGPO2Configuration();
		numberBase = config.getNumberBase();
		destinationAddress = config.getDestinationAddress();
		sourceAddress = config.getSourceAddress();
		txPowerLevel = config.getTXPowerLevel();
		retries = config.getRetries();
		transmitOnly = config.getTransmitOnly();
		return this;
	}
	
	public XTend900Config setAutosetMy(boolean autosetMy) {
		this.autosetMy = autosetMy;
		return this;
	}
	
	public boolean getAutosetMy() {
		return autosetMy;
	}
	
	public XTend900Config setAPIEnable(APIEnable apiEnable) {
		this.apiEnable = apiEnable;
		return this;
	}
	
	public APIEnable getAPIEnable() {
		return apiEnable;
	}
	
	public XTend900Config setInterfaceDataRate(InterfaceDataRate interfaceDataRate) {
		this.interfaceDataRate = interfaceDataRate;
		return this;
	}
	
	public InterfaceDataRate getInterfaceDataRate() {
		return interfaceDataRate;
	}
	
	public XTend900Config setRFDataRate(RFDataRate rfDataRate) {
		this.rfDataRate = rfDataRate;
		return this;
	}
	
	public RFDataRate getRFDataRate() {
		return rfDataRate;
	}
	
	public XTend900Config setGPO2Configuration(GPO2Configuration gpo2Configuration) {
		this.gpo2Configuration = gpo2Configuration;
		return this;
	}
	
	public GPO2Configuration getGPO2Configuration() {
		return gpo2Configuration;
	}
	
	public XTend900Config setNumberBase(NumberBase numberBase) {
		this.numberBase = numberBase;
		return this;
	}
	
	public NumberBase getNumberBase() {
		return numberBase;
	}
	
	public XTend900Config setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress == null ? null : new DestinationAddress(destinationAddress);
		return this;
	}
	
	public DestinationAddress getDestinationAddress() {
		return destinationAddress;
	}
	
	public XTend900Config setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress == null ? null : new SourceAddress(sourceAddress);
		return this;
	}
	
	public SourceAddress getSourceAddress() {
		return sourceAddress;
	}
	
	public XTend900Config setTXPowerLevel(TXPowerLevel txPowerLevel) {
		this.txPowerLevel = txPowerLevel;
		return this;
	}
	
	public TXPowerLevel getTXPowerLevel() {
		return txPowerLevel;
	}
	
	public XTend900Config setRetries(Short retries) {
		this.retries = retries;
		return this;
	}
	
	public Short getRetries() {
		return retries;
	}
	
	public XTend900Config setTransmitOnly(TransmitOnly transmitOnly) {
		this.transmitOnly = transmitOnly;
		return this;
	}
	
	public TransmitOnly getTransmitOnly() {
		return transmitOnly;
	}
	
	public XTend900Config setDefaults() {
		autosetMy = false;
		apiEnable = APIEnable.DISABLED;
		interfaceDataRate = InterfaceDataRate.BAUD_9600;
		rfDataRate = RFDataRate.BAUD_115200;
		gpo2Configuration = GPO2Configuration.DEFAULT_LOW;
		numberBase = NumberBase.UNSIGNED_HEX_WITHOUT_UNITS;
		destinationAddress = new DestinationAddress();
		sourceAddress = new SourceAddress();
		retries = 10;
		transmitOnly = TransmitOnly.TX_RX;
		return this;
	}
	
	public XTend900Config setPointToPoint() {
		setAutosetMy(true);
		setSourceAddress(null);
		setDestinationAddress("FFFF");
		return this;
	}
	
	public XTend900Config setPointToMultipointBase() {
		setAutosetMy(false);
		setSourceAddress("0");
		setDestinationAddress("FFFF");
		return this;
	}
	
	public XTend900Config setPointToMultipointRemote() {
		setAutosetMy(true);
		setSourceAddress(null);
		setDestinationAddress("0");
		return this;
	}
	
	@Override
	public String toString() {
		List<String> commands = new ArrayList<String>();
		if (autosetMy)                  commands.add(Command.AUTOSET_MY.text);
		if (apiEnable != null)          commands.add(apiEnable.getText());
		if (interfaceDataRate != null)  commands.add(interfaceDataRate.getText());
		if (rfDataRate != null)         commands.add(rfDataRate.getText());
		if (gpo2Configuration != null)  commands.add(gpo2Configuration.getText());
		if (numberBase != null)         commands.add(numberBase.getText());
		if (destinationAddress != null) commands.add(destinationAddress.getText());
		if (sourceAddress != null)      commands.add(sourceAddress.getText());
		if (retries != null)            commands.add(Command.RETRIES.getText() + String.format("%x", retries).toUpperCase());
		if (transmitOnly != null)       commands.add(transmitOnly.getText());
		return COMMAND_PREFIX + StringUtils.join(commands.toArray(), COMMAND_SEPARATOR);
	}
	
}
