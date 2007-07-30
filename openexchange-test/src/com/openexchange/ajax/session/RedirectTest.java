/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.configuration.AJAXConfig;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectTest extends AbstractAJAXSession {

    private String login;

    private String password;

    /**
     * @param name
     */
    public RedirectTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        password = AJAXConfig.getProperty(AJAXConfig.Property.PASSWORD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        login = null;
        password = null;
        super.tearDown();
    }

    public void testRedirect() throws Throwable {
        final AJAXSession session = new AJAXSession();
        // Create session.
        final LoginResponse lResponse = LoginTools.login(session,
            new LoginRequest(login, password));
        // To get logout with tearDown() working.
        session.setId(lResponse.getSessionId());

        // Remove cookies and that stuff.
        session.getConversation().clearContents();
        session.getConversation().getClientProperties().setAutoRedirect(false);
        // Test redirect
        final RedirectResponse rResponse = LoginTools.redirect(session,
            new RedirectRequest(lResponse.getJvmRoute(), lResponse.getRandom()));
        assertNotNull("Redirect location is missing.", rResponse.getLocation());
        session.getConversation().getClientProperties().setAutoRedirect(true);
    }
}
