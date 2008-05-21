/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * Tests the login.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginTest extends AbstractAJAXSession {

    /**
     * Default constructor.
     * @param name
     */
    public LoginTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        // Nothing to do.
    }

    /**
     * Tests the login.
     * @throws Throwable if an error occurs.
     */
    public void testLogin() throws Throwable {
        super.setUp();
        final String sessionId = getSession().getId();
        assertNotNull("Got no sessionId", sessionId);
        assertTrue("Length of session identifier is zero.",
            sessionId.length() > 0);
        super.tearDown();
    }
}
