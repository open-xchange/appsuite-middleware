package org.quickcached;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author akshath
 */
public class HexUtil {
	//converts binary data to hex sting
	private static String charset = "ISO-8859-1";//"utf-8";
	
	public static String getCharset() {
		return charset;
	}

	public static void setCharset(String aCharset) {
		charset = aCharset;
	}
	
	public static String encode(String sourceText) throws UnsupportedEncodingException {
		return encode(sourceText.getBytes(getCharset()));
	}
	public static String encode(byte[] rawData) {
		StringBuilder hexText = new StringBuilder();
		String initialHex = null;
		int initHexLength = 0;

		for (int i = 0; i < rawData.length; i++) {
			int positiveValue = rawData[i] & 0x000000FF;
			initialHex = Integer.toHexString(positiveValue);
			initHexLength = initialHex.length();
			while (initHexLength++ < 2) {
				hexText.append("0");
			}
			hexText.append(initialHex);
		}
		return hexText.toString();
	}

	public static String encode(byte rawData) {
		StringBuilder hexText = new StringBuilder();
		String initialHex = null;
		int initHexLength = 0;

		int positiveValue = rawData & 0x000000FF;

		initialHex = Integer.toHexString(positiveValue);
		initHexLength = initialHex.length();
		while (initHexLength++ < 2) {
			hexText.append("0");
		}
		hexText.append(initialHex);

		return hexText.toString();
	}

	//converts hex sting to binary data
	public static String decodeToString(String hexText) throws UnsupportedEncodingException {
		byte[] rawToByte = decodeToByte(hexText);
		return new String(rawToByte, getCharset());
	}
	public static byte[] decodeToByte(String hexText) {
		String chunk = null;
		if (hexText != null && hexText.length() > 0) {
			int numBytes = hexText.length() / 2;

			byte[] rawToByte = new byte[numBytes];
			int offset = 0;
			for (int i = 0; i < numBytes; i++) {
				chunk = hexText.substring(offset, offset + 2);
				offset += 2;
				rawToByte[i] = (byte) (Integer.parseInt(chunk, 16) & 0x000000FF);
			}
			return rawToByte;
		}
		return null;
	}

	
}
