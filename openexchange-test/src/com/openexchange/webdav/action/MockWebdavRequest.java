package com.openexchange.webdav.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavResource;

public class MockWebdavRequest implements WebdavRequest {

	private String url;
	private WebdavFactory factory;
	private String content;
	private Map<String,String> headers = new HashMap<String,String>();
	private WebdavResource res = null;
	
	public MockWebdavRequest(WebdavFactory factory) {
		this.factory = factory;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public WebdavResource getResource() throws WebdavException {
		if(res != null)
			return res;
		return res = factory.resolveResource(url);
	}

	public String getUrl() {
		return url;
	}

	public void setBodyAsString(String content) {
		this.content = content;
	}
	
	public InputStream getBody(){
		try {
			return new ByteArrayInputStream(content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setHeader(String header, String value) {
		headers .put(header.toLowerCase(), value);
	}

	public String getHeader(String header) {
		return headers.get(header.toLowerCase());
	}

	public List<String> getHeaderNames() {
		return new LinkedList<String>(headers.keySet());
	}

}
