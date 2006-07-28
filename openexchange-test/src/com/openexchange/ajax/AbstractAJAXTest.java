package com.openexchange.ajax;

import java.util.Properties;

import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.Init;

import junit.framework.TestCase;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXTest extends TestCase {

    private static final String HOSTNAME = "hostname";

    public static final String PROTOCOL = "http://";

    protected String sessionId = null;

	protected String hostName = null;
	
	protected String login = null;
	
	protected String seconduser = null;
	
	protected String password = null;
	
	protected int userId = -1;

    protected WebConversation webConversation = null;

    protected Properties ajaxProps = null;
	
	protected String jsonTagData = "data";

	protected String jsonTagTimestamp = "timestamp";
	
	protected String jsonTagError = "error";

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ajaxProps = Init.getAJAXProperties();
        webConversation = new WebConversation();
		hostName = ajaxProps.getProperty(HOSTNAME);
		login = ajaxProps.getProperty("login");
		seconduser = ajaxProps.getProperty("seconduser");
		password = ajaxProps.getProperty("password");
        sessionId = LoginTest.getLogin(webConversation, hostName, login, password);
		assertNotNull("Can't get session id.", sessionId);
    }

    protected String getAJAXProperty(final String key) {
        return ajaxProps.getProperty(key);
    }

    /**
     * @return Returns the sessionId.
     */
    protected String getSessionId() {
        return sessionId;
    }

    /**
     * @return Returns the webConversation.
     */
    protected WebConversation getWebConversation() {
        return webConversation;
    }

    /**
     * 
     * @return Returns the hostname.
     */
	public String getHostName() {
		return hostName;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getSeconduser() {
		return seconduser;
	}
    
}
