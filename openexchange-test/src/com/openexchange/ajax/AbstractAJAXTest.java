package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.Init;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXTest extends TestCase {

    private static final String HOSTNAME = "hostname";

    public static final String PROTOCOL = "http://";

    protected static String sessionId = null;
    
    protected static String sessionId2 = null;

	private static String hostName = null;
	
	private static String login = null;
	
	private String seconduser = null;
	
	private static String password = null;

	protected static int userId = -1;

    private WebConversation webConversation = null;

    private Properties ajaxProps = null;
	
	protected static final String jsonTagData = "data";

	protected static final String jsonTagTimestamp = "timestamp";
	
	protected static final String jsonTagError = "error";
	
	private static Pattern CALLBACK_ARG_PATTERN = Pattern.compile("callback\\s*\\((.*?)\\);");

    protected String getAJAXProperty(final String key) {
        return getAJAXProperties().getProperty(key);
    }

    protected Properties getAJAXProperties() {
        if (null == ajaxProps) {
            ajaxProps = Init.getAJAXProperties();
        }
        return ajaxProps;
    }
    
    /**
     * @return Returns the sessionId.
     * @throws Exception if an error occurs while authenticating.
     */
    protected String getSessionId() throws Exception {
        if (null == sessionId) {
            sessionId = LoginTest.getSessionId(getWebConversation(), getHostName(),
                getLogin(), getPassword());
            assertNotNull("Can't get session id.", sessionId);
        }
        return sessionId;
    }
    
    protected String getSecondSessionId() throws Exception {
    	if(null == sessionId2) {
    		sessionId2 = LoginTest.getSessionId(getWebConversation(), getHostName(),
    				getSeconduser(), getPassword());
    		assertNotNull("Can't get session id for second user.",sessionId2);
    	}
    	return sessionId2;
    }

    /**
     * @return Returns the webConversation.
     */
    protected WebConversation getWebConversation() {
    	HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        if (null == webConversation) {
            webConversation = new WebConversation();
        }
        return webConversation;
    }

    /**
     * @return Returns the hostname.
     */
	public String getHostName() {
        if (null == hostName) {
            hostName = getAJAXProperty(HOSTNAME);
        }
		return hostName;
	}

	public String getLogin() {
        if (null == login) {
            login = getAJAXProperty("login");
        }
		return login;
	}

	public String getPassword() {
        if (null == password) {
            password = getAJAXProperty("password");
        }
		return password;
	}

	public String getSeconduser() {
        if (null == seconduser) {
            seconduser = getAJAXProperty("seconduser");
        }
		return seconduser;
	}
	
	// Query methods
	
	protected String putS(String url, String body) throws MalformedURLException, IOException, SAXException {
		PutMethodWebRequest m = new PutMethodWebRequest(url, new ByteArrayInputStream(body.getBytes("UTF-8")), "text/javascript");
		WebResponse resp = getWebConversation().getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject put(String url, String body) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(putS(url,body));
		return o;
	}
	
	protected void putN(String url, String body) throws MalformedURLException, IOException, SAXException  {
		putS(url,body);
	}
	
	protected JSONArray putA(String url, String body) throws MalformedURLException, JSONException, IOException, SAXException  {
		JSONArray a = new JSONArray(putS(url,body));
		return a;
	}
	
	protected String gS(String url) throws MalformedURLException, IOException, SAXException {
		GetMethodWebRequest m = new GetMethodWebRequest(url);
		WebResponse resp = getWebConversation().getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject g(String url) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(gS(url));
		return o;
	}
	
	protected JSONArray gA(String url) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONArray a = new JSONArray(gS(url));
		return a;
	}
	
	protected String pS(String url, Map<String,String> data) throws MalformedURLException, IOException, SAXException {
		PostMethodWebRequest m = new PostMethodWebRequest(url);
		for(String key : data.keySet()) {
			m.setParameter(key,data.get(key));
		}
		WebResponse resp = getWebConversation().getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject p(String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(pS(url,data));
		return o;
	}
	
	protected JSONArray pA(String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		return new JSONArray(pS(url,data));
	}
	
	protected Response gT(String url) throws MalformedURLException, JSONException, IOException, SAXException {
		String res = gS(url);
		//System.out.println("* "+res);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
		
	}
	
	protected Response pT(String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		String res = pS(url,data);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
			
	}
	
	protected Response putT(String url, String data) throws MalformedURLException, JSONException, IOException, SAXException  {
		String res = putS(url,data);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
	}
	
	public static void assertNoError(Response res) {
		assertFalse(res.getErrorMessage(),res.hasError());
	}
	
	public static void assertEqualsAndNotNull(String message, Date expect, Date value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message, expect.getTime(), value.getTime());
		} 
	} 
	
	public static void assertEqualsAndNotNull(String message, byte[] expect, byte[] value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message + " byte array size is not equals", expect.length, value.length);
			for (int a = 0; a < expect.length; a++) {
				assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a], value[a]);
			}
		} 
	} 
	
	public static void assertEqualsAndNotNull(String message, Object expect, Object value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message, expect, value);
		} 
	} 
	
	public static void assertSameContent(InputStream is1, InputStream is2) throws IOException {
		int i = 0;
		while((i = is1.read()) != -1){
			assertEquals(i, is2.read());
		}
		assertEquals(-1,is2.read());
	}
	
	public static JSONObject extractFromCallback(String html) throws JSONException {
		Matcher matcher = CALLBACK_ARG_PATTERN.matcher(html);
		if(matcher.find()){
			return new JSONObject(matcher.group(1));
		}
		return null;
	}
	
	// A poor mans hash literal
	protected Map<String,String> m(String ...pairs){
		if(pairs.length % 2 != 0)
			throw new IllegalArgumentException("Must contain matching pairs");
		
		Map<String,String> m = new HashMap<String,String>();
		
		for(int i = 0; i < pairs.length; i++) {
			m.put(pairs[i], pairs[++i]);
		}
		
		return m;
		
	}
}
