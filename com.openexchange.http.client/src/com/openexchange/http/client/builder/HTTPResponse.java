package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.exception.OXException;

public interface HTTPResponse<R> {
	public R getPayload() throws OXException;
	public void setPayload(R payload);
	
	public Map<String, String> getHeaders();
	public Map<String, String> getCookies();
}
