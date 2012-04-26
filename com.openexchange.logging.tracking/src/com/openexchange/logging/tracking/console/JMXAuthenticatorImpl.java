package com.openexchange.logging.tracking.console;

import java.util.Collections;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

public final class JMXAuthenticatorImpl implements JMXAuthenticator {

    private final String login;
    private final String password;

    public JMXAuthenticatorImpl(final String login, final String password) {
        super();
        this.login = login;
        this.password = password;
    }

  
    public Subject authenticate(final Object credentials) {
        if (!(credentials instanceof String[])) {
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }
        final String[] creds = (String[]) credentials;
        if (creds.length != 2) {
            throw new SecurityException("Credentials should have 2 elements");
        }
        /*
         * Perform authentication
         */
        final String username = creds[0];
        final String testPassword = creds[1];
        if (login.equals(username) && password.equals(testPassword)) {
            return new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET, Collections.EMPTY_SET);
        }
        throw new SecurityException("Invalid credentials");

    }
}
