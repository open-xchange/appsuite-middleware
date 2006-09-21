package com.openexchange.webdav.action;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MockWebdavResponse implements WebdavResponse {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	private Map<String,String> headers = new HashMap<String,String>();
	
	public String getResponseBodyAsString() {
		try {
			return new String(out.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public String getHeader(String headerName) {
		return headers.get(headerName.toUpperCase());
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public void setHeader(String header, String value) {
		headers.put(header.toUpperCase(),value);
	}

}
