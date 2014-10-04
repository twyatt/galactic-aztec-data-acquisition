package edu.sdsu.rocket.data.helpers;

public class ByteHelper {
	
	final protected static char[] hexArray = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String bytesToHexString(byte[] values) {
		String hex = "";
		for (int j = 0; j < values.length; j++) {
			hex += byteToHexString(values[j]);
			if (j < values.length - 1)
				hex += " ";
		}
		return hex;
	}
	
	public static String byteToHexString(byte value) {
		int unsigned = value & 0xFF;
		return "" + hexArray[unsigned >>> 4] + hexArray[unsigned & 0x0F];
	}
	
}
