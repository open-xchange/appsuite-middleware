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
    
}
