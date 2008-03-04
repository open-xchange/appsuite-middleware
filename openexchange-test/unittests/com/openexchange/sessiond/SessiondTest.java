package com.openexchange.sessiond;

import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.test.TestInit;

public class SessiondTest extends TestCase {
	
	protected static final String sessiondPropertiesFile = "sessiondPropertiesFile";
	
	protected static String testUser1 = "test01";
	
	protected static String testUser2 = "test02";
	
	protected static String testUser3 = "test03";
	
	protected static String defaultContext = "defaultcontext";
	
	protected static String notExistingUser = "notexistinguser";
	
	protected static String notActiveUser = "notactiveuser";

	protected static String passwordExpiredUser = "passwordexpireduser";
	
	protected static String userWithoutContext = "user@withoutcontext.de";
	
	protected static String password = "netline";
	
	protected static String invalidPassword = "qwertz";
	
	private static boolean isInit = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		if (isInit) {
			return ;
		}
		
		Init.startServer();

		Properties prop = TestInit.getTestProperties();
		
		String propfile = prop.getProperty(sessiondPropertiesFile);
		
		if (propfile == null) {
			throw new Exception("no sessiond propfile given!");
		}
		
		Properties p = new Properties();
		
		p.load(new FileInputStream(propfile));
		
		testUser1 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser1", testUser1);
		testUser2 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser2", testUser2);
		testUser3 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser3", testUser3);

		defaultContext = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.defaultContext", defaultContext);
		
		notExistingUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.notExistingUser", notExistingUser);
		notActiveUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.notActiveUser", notActiveUser);
		passwordExpiredUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.passwordExpiredUser", passwordExpiredUser);
		userWithoutContext = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.userWithoutContext", userWithoutContext);
		
		isInit = true;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        if (isInit) {
            isInit = false;
            Init.stopServer();
        }
        super.tearDown();
    }

    public void testDummy() {
    	
    }

    public void testAddSession() throws Exception {
    	final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
    	final Context context = ContextStorage.getInstance().getContext(contextId);
    	final int userId = UserStorage.getInstance().getUserId(testUser1, context);
		//final SessiondConnectorInterface sessiondCon = SessiondService.getInstance().getService();
		//sessiondCon.addSession(userId, testUser1, "secret", context, "localhost");
	}
	
	public void testRefreshSession() throws Exception {
    	final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
    	final Context context = ContextStorage.getInstance().getContext(contextId);
    	final int userId = UserStorage.getInstance().getUserId(testUser1, context);
		//final SessiondConnectorInterface sessiondCon = SessiondService.getInstance().getService();
		//final String sessionId = sessiondCon.addSession(userId, testUser1, "secret", context, "localhost");
		//essiondCon.refreshSession(sessionId);
	}
	
	public void testDeleteSession() throws Exception {
    	final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
    	final Context context = ContextStorage.getInstance().getContext(contextId);
    	final int userId = UserStorage.getInstance().getUserId(testUser1, context);
		//final SessiondConnectorInterface sessiondCon = SessiondService.getInstance().getService();
		//final String sessionId = sessiondCon.addSession(userId, testUser1, "secret", context, "localhost");
		//sessiondCon.removeSession(sessionId);
	}
	
	public void testGetSession() throws Exception {
    	final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
    	final Context context = ContextStorage.getInstance().getContext(contextId);
    	final int userId = UserStorage.getInstance().getUserId(testUser1, context);
		//final SessiondConnectorInterface sessiondCon = SessiondService.getInstance().getService();
		//final String sessionId = sessiondCon.addSession(userId, testUser1, "secret", context, "localhost");
		//sessiondCon.refreshSession(sessionId);
		//final Session session = sessiondCon.getSession(sessionId);
	}
}
