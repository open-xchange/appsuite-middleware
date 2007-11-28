package com.openexchange.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;

import com.meterware.httpunit.Base64;
import com.meterware.httpunit.WebRequest;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.test.WebdavInit;

import junit.framework.TestCase;

public class WebdavClientTest extends TestCase {
	protected Properties webdavProps;
	protected String login;
	protected String password;
	protected String hostname;
	
	protected List<String> clean = new ArrayList<String>();

	public void setUp() throws Exception {
		// Copied from AbstractWebdavTest
		webdavProps = WebdavInit.getWebdavProperties();
		login = AbstractConfigWrapper.parseProperty(webdavProps, "login", "");
		password = AbstractConfigWrapper.parseProperty(webdavProps, "password", "");
		hostname = AbstractConfigWrapper.parseProperty(webdavProps, "hostname", "localhost");
		
	}
	
	public void tearDown() throws Exception {
		for(String url : clean) {
			getResource(url).deleteMethod();
		}
	}
	
	protected WebdavResource getResource(String url, String login, String password) throws HttpException, IOException{
		HttpURL httpUrl = new HttpURL("http://"+hostname+"/servlet/webdav.infostore/"+url);
		httpUrl.setUserinfo(login, password);
		WebdavResource res = new WebdavResource(httpUrl, 0, WebdavResource.NOACTION);
		return res;
	}
	
	protected WebdavResource getResource(String url) throws HttpException, IOException {
		return getResource(url, login, password);
	}
	
	public void mkdir(String path) throws HttpException, IOException {
		getResource(path).mkcolMethod();
	}
	
	public void save(String path, String data) throws HttpException, IOException {
		getResource(path).putMethod(data);
	}
	
	public void cp(String from, String to) throws HttpException, IOException {
		getResource(from).copyMethod("http://"+hostname+"/servlet/webdav.infostore"+to);
	}
	
	public void mv(String from, String to) throws HttpException, IOException {
		getResource(from).moveMethod("http://"+hostname+"/servlet/webdav.infostore"+to);
	}
	
	public void assertContent(String path, String...names) throws HttpException, IOException {
		WebdavResource res = getResource(path);
		WebdavResources resources = res.getChildResources();
		Set<String> expected = new HashSet<String>(Arrays.asList(names));
		
		Enumeration enumeration = resources.getResourceNames();
		while(enumeration.hasMoreElements()){
			String name = (String) enumeration.nextElement();
			assertTrue(name+" not expected", expected.remove(name));
		}
		assertTrue(expected.toString(), expected.isEmpty());
	}
	
	public void assertBody(String path, InputStream body) throws HttpException, IOException {
		InputStream is = getResource(path).getMethodData();
		assertEqualContent(is, body);
	}
	
	public void assertEqualContent(InputStream is, InputStream body) throws IOException {
		int i = 0;
		int j = 0;
		while((i = is.read()) != -1) {
			j = body.read();
			assertEquals(j, i);
		}
		assertEquals(-1, body.read());
	}

	public void assertBody(String path, String content) throws HttpException, IOException {
		assertEquals(content, getResource(path).getMethodDataAsString());
	}
	
	// Many thanks to offspring for this snippet
	public void setAuth(WebRequest req) {
		if (password == null) {
			password = "";
		}
		
		String authData =  new String(Base64.encode(login + ":" + password)); 
		req.setHeaderField("authorization", "Basic " + authData);
	}
	
	public void setAuth(HttpMethodBase method) {
		if (password == null) {
			password = "";
		}
		
		String authData =  new String(Base64.encode(login + ":" + password)); 
		
		method.setDoAuthentication(true);
		method.setRequestHeader("authorization", "Basic " + authData);
	}
}
