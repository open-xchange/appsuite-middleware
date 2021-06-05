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

package com.openexchange.mailfilter.properties;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;

/**
 * {@link CredentialSource}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CredentialSource {
    /**
     * Full login (incl. context part) name and password are used from the current session
     */
    SESSION_FULL_LOGIN("session-full-login"),
    /**
     * lLgin name and password are used from the current session
     */
    SESSION("session"),
    /**
     * The login name is taken from the field imapLogin of the current
     * user the password is taken from the current session
     */
    IMAP_LOGIN("imapLogin"),
    /**
     * Use the primary mail address of the user and the password from the session
     */
    MAIL("mail");

    private static final Map<String, CredentialSource> MAP;
    static {
        ImmutableMap.Builder<String, CredentialSource> b = ImmutableMap.builder();
        for (CredentialSource credentialSource : CredentialSource.values()) {
            b.put(credentialSource.name, credentialSource);
        }
        MAP = b.build();
    }

    public final String name;

    /**
     * Initialises a new {@link CredentialSource}.
     * 
     * @param name
     */
    private CredentialSource(final String name) {
        this.name = name;
    }

    /**
     * The name of the {@link CredentialSource}
     * 
     * @param name The name of the {@link CredentialSource} as string
     * @return The {@link CredentialSource}
     * @throws OXException if an invalid {@link CredentialSource} is requested
     */
    public static CredentialSource credentialSourceFor(String name) throws OXException {
        CredentialSource credentialSource = MAP.get(name);
        if (credentialSource == null) {
            throw MailFilterExceptionCode.NO_VALID_CREDSRC.create();
        }
        return credentialSource;
    }
}
