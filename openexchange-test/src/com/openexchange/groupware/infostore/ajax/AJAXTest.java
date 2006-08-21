package com.openexchange.groupware.infostore.ajax;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.groupware.infostore.InfostoreTest;

public abstract class AJAXTest extends InfostoreTest{
	
	private static final String HTTP_HOST = "http.host";

	protected String host;

	public AJAXTest(){
		super();
	}
	
	public AJAXTest(String filename) {
		super(filename);
	}

	@Override
	public void initProperties() {
		super.initProperties();
		host = fixtures.getProperty(HTTP_HOST);
	}
	
	private String session;
	
	protected HttpClient client = new HttpClient();
	
	protected String getSession(String host, String username, String password) throws HttpException, JSONException, IOException, AJAXException{
		if(session != null)
			return session;
		JSONObject sess = g("http://"+host+"/ajax/login?name="+username+"&password="+password);
		session = sess.getString("session");
		return session;
	}
	
	protected JSONObject put(String url, String body) throws JSONException, HttpException, IOException {
		PutMethod m = new PutMethod(url);
		m.setRequestBody(body);
		client.executeMethod(m);
		JSONObject o = new JSONObject(m.getResponseBodyAsString());
		return o;
	}
	
	protected void putN(String url, String body) throws JSONException, HttpException, IOException {
		PutMethod m = new PutMethod(url);
		m.setRequestBody(body);
		client.executeMethod(m);
		m.getResponseBodyAsString();
	}
	
	protected JSONArray putA(String url, String body) throws JSONException, HttpException, IOException {
		PutMethod m = new PutMethod(url);
		m.setRequestBody(body);
		client.executeMethod(m);
		JSONArray a = new JSONArray(m.getResponseBodyAsString());
		return a;
	}
	
	protected JSONObject g(String url) throws JSONException, HttpException, IOException{
		GetMethod m = new GetMethod(url);
		client.executeMethod(m);
		JSONObject o = new JSONObject(m.getResponseBodyAsString());
		return o;
	}
	
	protected JSONArray gA(String url) throws HttpException, IOException, JSONException{
		GetMethod m = new GetMethod(url);
		client.executeMethod(m);
		String res = m.getResponseBodyAsString();
		return new JSONArray(res);
	}
	
	protected JSONObject p(String url, Map<String,String> data) throws JSONException, HttpException, IOException, AJAXException{
		PostMethod m = new PostMethod(url);
		for(String key : data.keySet()) {
			m.addParameter(new NameValuePair(key,data.get(key)));
		}
		client.executeMethod(m);
		String res =  m.getResponseBodyAsString();
		System.out.println(res);
		JSONObject o = new JSONObject(res);
		String error = o.optString("error",null);
		if(error != null)
			throw new AJAXException(error);
		return o;
	}
	
	protected JSONArray pA(String url, Map<String,String> data) throws JSONException, HttpException, IOException, AJAXException{
		PostMethod m = new PostMethod(url);
		for(String key : data.keySet()) {
			m.addParameter(new NameValuePair(key,data.get(key)));
		}
		client.executeMethod(m);
		
		String res = m.getResponseBodyAsString();
		System.out.println(res);
		try {
			JSONObject o = new JSONObject(res);
			String error = o.optString("error",null);
			if(error != null)
				throw new AJAXException(error);
		} catch (JSONException x){}
		
		return new JSONArray(res);
		
	}
	
	protected JSONArray pA(String url, Map<String,String> data, String body) throws JSONException, HttpException, IOException, AJAXException{
		PostMethod m = new PostMethod(url);
		for(String key : data.keySet()) {
			m.addParameter(new NameValuePair(key,data.get(key)));
		}
		
		m.setRequestBody(body);
		
		client.executeMethod(m);
		
		String res = m.getResponseBodyAsString();
		
		try {
			JSONObject o = new JSONObject(res);
			String error = o.optString("error",null);
			if(error != null)
				throw new AJAXException(error);
		} catch (JSONException x){}
		
		return new JSONArray(res);
		
	}
	
	protected ResponseWithTimestamp gT(String url) throws HttpException, JSONException, IOException, AJAXException {
		return new ResponseWithTimestamp(g(url));
	}
	
	protected ResponseWithTimestamp pT(String url, Map<String,String> data) throws HttpException, JSONException, IOException, AJAXException {
		return new ResponseWithTimestamp(p(url,data));
	}
	
	protected ResponseWithTimestamp putT(String url, String data) throws HttpException, JSONException, IOException {
		return new ResponseWithTimestamp(put(url,data));
	}
	
	public static void assertNoError(ResponseWithTimestamp rwt) {
		assertEquals("", rwt.getError());
	}
}
