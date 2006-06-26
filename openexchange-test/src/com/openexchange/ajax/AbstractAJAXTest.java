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

    public static final String PROTOCOL = "http://";

    protected String sessionId = null;
	
	protected String hostName = null;

    protected WebConversation webConversation = null;

    private Properties ajaxProps = null;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ajaxProps = Init.getAJAXProperties();
        webConversation = new WebConversation();
		hostName = ajaxProps.getProperty("hostname");
        sessionId = LoginTest.getLogin(webConversation, hostName
            , ajaxProps.getProperty("login"), ajaxProps
            .getProperty("password"));
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
}
