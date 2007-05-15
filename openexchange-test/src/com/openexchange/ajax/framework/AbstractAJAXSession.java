package com.openexchange.ajax.framework;

import junit.framework.TestCase;

import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.session.LoginRequest;
import com.openexchange.ajax.session.LoginResponse;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.LogoutRequest;
import com.openexchange.configuration.AJAXConfig;

public abstract class AbstractAJAXSession extends TestCase {

    private final AJAXSession session = new AJAXSession();

    protected AbstractAJAXSession(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        final String login = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        final String password = AJAXConfig.getProperty(AJAXConfig.Property
            .PASSWORD);
        final LoginResponse response = LoginTools.login(session,
            new LoginRequest(login, password));
        session.setId(response.getSessionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (null != session.getId()) {
            LoginTools.logout(session, new LogoutRequest(session.getId()));
            session.setId(null);
        }
        session.getConversation().clearContents();
    }
    
    public static class AJAXSession {
        private final WebConversation conversation;
        private String id;
        public AJAXSession() {
            super();
            conversation = new WebConversation();
        }
        /**
         * @return the conversation
         */
        public WebConversation getConversation() {
            return conversation;
        }
        /**
         * @return the sessionId
         */
        public String getId() {
            return id;
        }
        /**
         * @param sessionId the sessionId to set
         */
        public void setId(final String id) {
            this.id = id;
        }
    }

    /**
     * @return the session
     */
    public AJAXSession getSession() {
        return session;
    }
}
