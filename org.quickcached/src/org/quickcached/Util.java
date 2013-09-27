package org.quickcached;

/**
 *
 * @author akshath
 */
public class Util {

	public static String prefixZerros(int value, int len) {
		return prefixZerros(value+"", len);
	}

	public static String prefixZerros(String value, int len) {
		StringBuilder sb = new StringBuilder(value);
		while(sb.length()<len) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}

	private static final String digits = "0123456789ABCDEF";
	public static String decimal2hex(int d) {        
        if (d == 0) return "0";
        StringBuilder hex = new StringBuilder();
        while (d > 0) {
            int digit = d % 16;
			hex.insert(0, digits.charAt(digit));
            d = d / 16;
        }
        return hex.toString();
    }

	public static int hex2decimal(String s) {
        return Integer.parseInt(s, 16);
    }
}
