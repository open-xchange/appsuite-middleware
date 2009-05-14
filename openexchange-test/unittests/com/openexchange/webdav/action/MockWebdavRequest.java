package com.openexchange.webdav.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.action.ifheader.IfHeaderParser;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public class MockWebdavRequest implements WebdavRequest {

	private WebdavPath url;
	private String uriPrefix = null;
	private final WebdavFactory factory;
	private String content;
	private final Map<String,String> headers = new HashMap<String,String>();
	private WebdavResource res = null;
	private WebdavResource dest;
	
	private Map<String, Object> userInfo = new HashMap<String,Object>();
	
	public MockWebdavRequest(final WebdavFactory factory, final String prefix) {
		this.factory = factory;
		this.uriPrefix = prefix;
	}
	

    public void setUrl(final WebdavPath url) {
        this.url = url;
    }

    public WebdavResource getResource() throws WebdavProtocolException {
		if(res != null) {
			return res;
		}
		return res = factory.resolveResource(url);
	}
	
	public WebdavResource getDestination() throws WebdavProtocolException {
		if(null == getHeader("destination")) {
			return null;
		}
		if(dest != null) {
			return dest;
		}
		return dest = factory.resolveResource(getHeader("destination"));
	}
	
	public WebdavCollection getCollection() throws WebdavProtocolException {
		if(res != null) {
			return (WebdavCollection) res;
		}
		return (WebdavCollection) (res = factory.resolveCollection(url));
	}
	
	public WebdavPath getDestinationUrl(){
		return new WebdavPath(getHeader("destination"));
	}

	public WebdavPath getUrl() {
		return url;
	}

	public void setBodyAsString(final String content) {
		this.content = content;
	}
	
	public InputStream getBody(){
		try {
			return new ByteArrayInputStream((content == null) ? new byte[0] : content.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setHeader(final String header, final String value) {
		headers .put(header.toLowerCase(), value);
	}

	public String getHeader(final String header) {
		return headers.get(header.toLowerCase());
	}

	public List<String> getHeaderNames() {
		return new LinkedList<String>(headers.keySet());
	}

	public Document getBodyAsDocument() throws JDOMException, IOException {
		return new SAXBuilder().build(getBody());
	}

	public String getURLPrefix() {
		return uriPrefix;
	}

	public IfHeader getIfHeader() throws IfHeaderParseException {
		final String ifHeader = getHeader("If");
		if(ifHeader == null) {
			return null;
		}
		return new IfHeaderParser().parse(getHeader("If"));
	}

	public int getDepth(final int def) {
		final String depth = getHeader("depth");
		if(null == depth) {
			return def;
		}
		return "Infinity".equalsIgnoreCase(depth) ? WebdavCollection.INFINITY : new Integer(depth);
	}

	public WebdavFactory getFactory() throws WebdavProtocolException {
		return factory;
	}

	
	public String getCharset() {
		return "UTF-8";
	}


    public boolean hasBody() {
        return content != null;
    }

    public Map<String, Object> getUserInfo() {
        return userInfo;
    }
}
