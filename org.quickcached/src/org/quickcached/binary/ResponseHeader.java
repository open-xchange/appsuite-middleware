/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quickcached.binary;

import org.quickcached.Util;


/**
 *
 * @author akshath
 */
public class ResponseHeader  extends Header {
	public static final String STATUS_NO_ERROR = "0000";
	public static final String KEY_NOT_FOUND  = "0001";
	public static final String KEY_EXISTS = "0002";
	public static final String INVALID_ARGUMENTS = "0004";
	public static final String ITEM_NOT_STORED = "0005";
	public static final String UNKNOWN_COMMAND = "0081";
	public static final String INTERNAL_ERROR = "0084";
	
	
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String encodedString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getMagic());
		sb.append(getOpcode());
		sb.append(Util.prefixZerros(Util.decimal2hex(getKeyLength()),4));

		sb.append(Util.prefixZerros(Util.decimal2hex(getExtrasLength()),2));
		sb.append(getDataType());
		sb.append(getStatus());

		sb.append(Util.prefixZerros(Util.decimal2hex(getTotalBodyLength()),8));
		sb.append(Util.prefixZerros(getOpaque(),8));

		if(getCas()!=null) {
			sb.append(Util.prefixZerros(getCas(),16));
		} else {
			sb.append(Util.prefixZerros(0,16));
		}

		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[RequestHeader {");

		sb.append("Magic:");
		sb.append(getMagic());
		sb.append(", Opcode:");
		sb.append(getOpcode());
		sb.append(", KeyLength:");
		sb.append(getKeyLength());

		sb.append(", ExtrasLength:");
		sb.append(getExtrasLength());
		sb.append(", DataType:");
		sb.append(getDataType());
		sb.append(", Status:");
		sb.append(getStatus());

		sb.append(", TotalBodyLength:");
		sb.append(getTotalBodyLength());
		sb.append(", Opaque:");
		sb.append(getOpaque());
		sb.append(", Cas:");
		if(getCas()!=null) sb.append(getCas());

		sb.append("}]");
		return sb.toString();
	}
}
