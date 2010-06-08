package com.openexchange.mobility.provisioning.json.container;

public class Device {

	private static final String IDENTIFIER_IPHONE = "iphone";
	private static final String IDENTIFIER_WINDOWSMOBILE = "windowsmobile";
	
	public static final int UNKNOWN = 0;
	public static final int IPHONE = 1;
	public static final int WINDOWSMOBILE = 2;
	
	public static int getDeviceId(String identifier) {
		if (isIphone(identifier)) {
			return Device.IPHONE;
		} else if (isWindowsMobile(identifier)) {
			return Device.WINDOWSMOBILE;
		}
		return Device.UNKNOWN;
	}
	
	public static boolean isIphone(String identifier) {
		return identifier.equals(IDENTIFIER_IPHONE);
	}
	
	public static boolean isWindowsMobile(String identifier) {
		return identifier.equals(IDENTIFIER_WINDOWSMOBILE);
	}	
}
