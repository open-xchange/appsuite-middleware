package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.Init;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXTest extends TestCase {
	
	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(AbstractAJAXTest.class);
	
	public static final String PROTOCOL = "http://";
	
	private static final String HOSTNAME = "hostname";
	
	protected static final String jsonTagData = "data";
	
	protected static final String jsonTagTimestamp = "timestamp";
	
	protected static final String jsonTagError = "error";
	
	private static Pattern CALLBACK_ARG_PATTERN = Pattern.compile("callback\\s*\\((.*?)\\);");
	
	private static final Map<String, String> testArgsMap = new HashMap<String, String>();
	
	private String hostName = null;
	
	private WebConversation webConversation = null;
	
	private WebConversation webConversation2 = null;
	
	private String sessionId = null;
	
	private String sessionId2 = null;
	
	private String login = null;
	
	private String seconduser = null;
	
	private String password = null;
	
	private Properties ajaxProps = null;
	
	public AbstractAJAXTest(String name) {
		super(name);

		try {
			AJAXConfig.init();
		} catch (ConfigurationException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
			protected void tearDown() throws Exception {
		logout();
		super.tearDown();
	}
	
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
	 * @return Returns the hostname.
	 */
	public String getHostName() {
		if (null == hostName) {
			hostName = getAJAXProperty(HOSTNAME);
		}
		return hostName;
	}
	
	/**
	 * @return Returns the webConversation.
	 */
	protected WebConversation getWebConversation() {
		if (null == webConversation) {
			webConversation = newWebConversation();
		}
		return webConversation;
	}
	
	/**
	 * @return Returns the webConversation.
	 */
	protected WebConversation getSecondWebConversation() {
		if (null == webConversation2) {
			webConversation2 = newWebConversation();
		}
		return webConversation2;
	}
	
	/**
	 * Setup the web conversation here so tests are able to create additional if
	 * several users are needed for tests.
	 * @return a new web conversation.
	 */
	protected WebConversation newWebConversation() {
		HttpUnitOptions.setDefaultCharacterSet("UTF-8");
		return new WebConversation();
	}
	
	/**
	 * @return Returns the sessionId.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
	 */
	protected String getSessionId() throws IOException, SAXException,
        JSONException {
		if (null == sessionId) {
			sessionId = LoginTest.getSessionId(getWebConversation(),
					getHostName(), getLogin(), getPassword());
			assertNotNull("Can't get session id.", sessionId);
		}
		return sessionId;
	}
	
	protected String getSecondSessionId() throws Exception {
		if(null == sessionId2) {
			sessionId2 = LoginTest.getSessionId(getSecondWebConversation(),
					getHostName(), getSeconduser(), getPassword());
			assertNotNull("Can't get session id for second user.",sessionId2);
		}
		return sessionId2;
	}
	
	/**
	 * Terminates the session on the server.
	 * @throws Exception if an error occurs.
	 */
	protected void logout() throws Exception {
		if (null != sessionId) {
			LoginTest.logout(getWebConversation(), getHostName(),
					getSessionId());
			sessionId = null;
			webConversation = null;
		}
		webConversation = null;
		if (null != sessionId2) {
			LoginTest.logout(getSecondWebConversation(), getHostName(),
					getSecondSessionId());
			sessionId2 = null;
		}
		webConversation2 = null;
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
	
	protected String putS(WebConversation webConv, String url, String body) throws MalformedURLException, IOException, SAXException {
		PutMethodWebRequest m = new PutMethodWebRequest(url, new ByteArrayInputStream(body.getBytes("UTF-8")), "text/javascript; charset=UTF-8");
		WebResponse resp = webConv.getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject put(WebConversation webConv, String url, String body) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(putS(webConv,url, body));
		return o;
	}
	
	protected void putN(WebConversation webConv, String url, String body) throws MalformedURLException, IOException, SAXException  {
		putS(webConv,url, body);
	}
	
	protected JSONArray putA(WebConversation webConv, String url, String body) throws MalformedURLException, JSONException, IOException, SAXException  {
		JSONArray a = new JSONArray(putS(webConv,url, body));
		return a;
	}
	
	protected String gS(WebConversation webConv, String url) throws MalformedURLException, IOException, SAXException {
		GetMethodWebRequest m = new GetMethodWebRequest(url);
		WebResponse resp = webConv.getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject g(WebConversation webConv, String url) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(gS(webConv, url));
		return o;
	}
	
	protected JSONArray gA(WebConversation webConv, String url) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONArray a = new JSONArray(gS(webConv, url));
		return a;
	}
	
	protected String pS(WebConversation webConv, String url, Map<String,String> data) throws MalformedURLException, IOException, SAXException {
		PostMethodWebRequest m = new PostMethodWebRequest(url);
		
		for(String key : data.keySet()) {
			m.setParameter(key,data.get(key));
		}
		WebResponse resp = webConv.getResponse(m);
		return resp.getText();
	}
	
	protected JSONObject p(WebConversation webConv, String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		JSONObject o = new JSONObject(pS(webConv,url, data));
		return o;
	}
	
	protected JSONArray pA(WebConversation webConv, String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		return new JSONArray(pS(webConv,url, data));
	}
	
	protected Response gT(WebConversation webConv, String url) throws MalformedURLException, JSONException, IOException, SAXException {
		String res = gS(webConv, url);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
		
	}
	
	protected Response pT(WebConversation webConv, String url, Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
		String res = pS(webConv,url, data);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
		
	}
	
	protected Response putT(WebConversation webConv, String url, String data) throws MalformedURLException, JSONException, IOException, SAXException  {
		String res = putS(webConv,url, data);
		if("".equals(res.trim()))
			return null;
		return Response.parse(res);
	}
	
	public static void assertNoError(Response res) {
		assertFalse(res.getErrorMessage()+" : "+res.getErrorParams(),res.hasError());
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
	
	public static String appendPrefix(String host) {
		if (host.startsWith("http://")) {
			return host;
		}
		return "http://" + host;
	}
	
	public static final boolean containsTestArg(final String arg) {
		return testArgsMap.containsKey(arg);
	}
	
	public static final String getTestArg(final String arg) {
		return testArgsMap.get(arg);
	}
	
	public static final void putTestArg(final String arg, final String value) {
		testArgsMap.put(arg, value);
	}
}
