package org.quickcached.binary;

import org.quickcached.Util;

/**
 *
 * @author akshath
 */
public class Header {
	private String magic;
	private String opcode;
	private int keyLength;

	private int extrasLength;
	private String dataType = "00";

	private int totalBodyLength;
	private String opaque;
	private String cas;

	public String getMagic() {
		return magic;
	}

	public void setMagic(String magic) {
		this.magic = magic;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public int getExtrasLength() {
		return extrasLength;
	}

	public void setExtrasLength(int extrasLength) {
		this.extrasLength = extrasLength;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getTotalBodyLength() {
		return totalBodyLength;
	}

	public void setTotalBodyLength(int totalBodyLength) {
		this.totalBodyLength = totalBodyLength;
	}

	public String getOpaque() {
		return opaque;
	}

	public void setOpaque(String opaque) {
		this.opaque = opaque;
	}

	public String getCas() {
		return cas;
	}

	public void setCas(String cas) {
		this.cas = cas;
		if(cas!=null && cas.length()!=16) throw new IllegalArgumentException("Bad cas value!");
	}
	public void setCas(int cas) {
		this.cas = Util.prefixZerros(cas, 16);//8x2
	}

	public String encodedString() {
		return null;
	}
}
