package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.types.Response;
import com.openexchange.groupware.Init;

import junit.framework.TestCase;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXTest extends TestCase {

    private static final String HOSTNAME = "hostname";

    public static final String PROTOCOL = "http://";

    private static String sessionId = null;

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
            sessionId = LoginTest.getLogin(getWebConversation(), getHostName(),
                getLogin(), getPassword());
            assertNotNull("Can't get session id.", sessionId);
        }
        return sessionId;
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
		PutMethodWebRequest m = new PutMethodWebRequest(url, new ByteArrayInputStream(body.getBytes()), "text/javascript");
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
		return ResponseParser.parse(gS(url));
	}
	
	protected Response pT(String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		return ResponseParser.parse(pS(url,data));
	}
	
	protected Response putT(String url, String data) throws MalformedURLException, JSONException, IOException, SAXException  {
		return ResponseParser.parse(putS(url,data));
	}
	
	public static void assertNoError(Response res) {
		assertFalse(res.getErrorMessage(),res.hasError());
	}
    
}
