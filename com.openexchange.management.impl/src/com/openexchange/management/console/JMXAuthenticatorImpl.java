/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.management.console;

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

    @Override
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
