package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.groupware.AbstractOXException;

public interface HTTPResponse<R> {
	public R getPayload() throws AbstractOXException;
	public void setPayload(R payload);
	
	public Map<String, String> getHeaders();
	public Map<String, String> getCookies();
}
