package com.openexchange.sessiond;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.ContextNotFoundException;
import com.openexchange.server.ComfireConfig;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import junit.framework.TestCase;

public class SessiondTest extends TestCase {
	
	protected static final String sessiondPropertiesFile = "sessiondPropertiesFile";
	
	protected static String testUser1 = "test01";
	
	protected static String testUser2 = "test02";
	
	protected static String testUser3 = "test03";
	
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
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		Properties prop = Init.getTestProperties();
		
		String propfile = prop.getProperty(sessiondPropertiesFile);
		
		if (propfile == null) {
			throw new Exception("no sessiond propfile given!");
		}
		
		Properties p = new Properties();
		
		p.load(new FileInputStream(propfile));
		
		testUser1 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser1", testUser1);
		testUser2 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser2", testUser2);
		testUser3 = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.testUser3", testUser3);

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
            Init.stopDB();
        }
        super.tearDown();
    }

    public void testAddSession() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		sc.addSession(testUser1, password, "localhost");
	}
	
	public void testRefreshSession() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(testUser1, password, "localhost");
		sc.refreshSession(sessionObj.getSessionID());
	}
	
	public void testDeleteSession() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(testUser1, password, "localhost");
		sc.removeSession(sessionObj.getSessionID());
	}
	
	public void testGetSession() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		SessionObject sessionObj = sc.addSession(testUser1, password, "localhost");
		SessionObject sessionObjectFromSessiond = sc.getSession(sessionObj.getSessionID());
	}
	
	public void testGetSessions() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		sc.addSession(testUser1, password, "localhost");
		sc.addSession(testUser2, password, "localhost");
		sc.addSession(testUser3, password, "localhost");
		
		int counter = 0;
		
		Iterator it = sc.getSessions();
		while (it.hasNext()) {
			it.next();
			counter++;
		}

		assertTrue("min 3 exiting sessions!", counter >= 2);
	}

	public void testInvalidCredentials() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		try {
			sc.addSession(testUser1, invalidPassword, "localhost");
		} catch (InvalidCredentialsException ex) {
			boolean ok = true;
		}
	}

	public void testUserNotFound() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		try {
			sc.addSession(notExistingUser, password, "localhost");
		} catch (UserNotFoundException ex) {
			return ;
		}
		
		fail("UserNotFoundException doesn't work!");
	}
	
	public void testSessionNotFound() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		assertFalse("this sessionid should fail", sc.refreshSession("1"));
	}
	
	public void testUserNotActivated() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		try {
			sc.addSession(notActiveUser, password, "localhost");
		} catch (UserNotActivatedException ex) {
			return;
		}
		fail("UserNotActivatedException doesn't work!");
	}
	
	public void testPasswordExpired() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		try {
			sc.addSession(passwordExpiredUser, password, "localhost");
		} catch (PasswordExpiredException ex) {
			return;
		}
		fail("PasswordExpiredException doesn't work!");
	}
	
	public void testContextNotFound() throws Exception {
		SessiondConnector sc = SessiondConnector.getInstance();
		try {
			sc.addSession(userWithoutContext, password, "localhost");
		} catch (ContextNotFoundException ex) {
			return;
		}
		fail("ContextNotFoundException doesn't work!");
	}
}

