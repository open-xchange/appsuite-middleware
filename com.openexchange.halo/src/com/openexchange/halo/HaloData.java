package com.openexchange.halo;

public class HaloData {
	
	private String format;
	private String type;
	private Object payload;

	/**
	 * 
	 * @param format - The format of the data, used for conversion
	 * @param payload - the data
	 * @param type - more detailed type information that get's passed on to frontend renderers
	 */
	public HaloData(String format, Object payload, String type) {
		super();
		this.format = format;
		this.payload = payload;
	}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String type) {
		this.format = type;
	}
	
	public Object getPayload() {
		return payload;
	}
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}

