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

package com.openexchange.webdav.client;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 *
 * {@link BearerAuthScheme}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.10.5
 */
public class BearerAuthScheme implements AuthScheme {

    public static final String NAME = "Bearer";

    public BearerAuthScheme() {
        // All auth schemes must have a no arg constructor.
    }

    @Override
    public String getParameter(String name) {
        // this scheme does not use parameters, see RFC2617Scheme for an example
        return null;
    }

    @Override
    public String getRealm() {
        // this scheme does not use realms
        return null;
    }

    @Override
    public String getSchemeName() {
        return NAME;
    }

    @Override
    public boolean isConnectionBased() {
        return false;
    }

    @Override
    public boolean isComplete() {
        // again we're not a challenge based scheme so this is always true
        return true;
    }

    @Override
    public void processChallenge(Header header) throws MalformedChallengeException {
        // Nothing to do here, this is not a challenge based
        // auth scheme.  See NTLMScheme for a good example.
    }

    @Override
    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        Args.notNull(credentials, "Credentials");
        Args.notNull(request, "HTTP request");
        final CharArrayBuffer buffer = new CharArrayBuffer(32);
        buffer.append(AUTH.WWW_AUTH_RESP);
        buffer.append(": Bearer ");
        // we use the user.getName to store the bearer token
        buffer.append(credentials.getUserPrincipal().getName());
        return new BufferedHeader(buffer);
    }

}
