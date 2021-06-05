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

package com.openexchange.authentication.oauth.impl;

import java.net.URI;
import com.openexchange.authentication.NamePart;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link OAuthAuthenticationConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public interface OAuthAuthenticationConfig {

    /**
     * Gets the authorization server token endpoint URI
     */
    URI getTokenEndpoint();

    /**
     * Gets the OAuth client ID
     */
    String getClientId();

    /**
     * Gets the OAuth client secret
     */
    String getClientSecret();

    /**
     * Gets the scope to be requested or <code>null</code>.
     * Return value can be a comma-separated string of multiple
     * scopes.
     */
    String getScope();

    /**
     * Gets the number of seconds an access token can be refreshed
     * before it actually expires.
     */
    int getEarlyTokenRefreshSeconds();

    /**
     * Lock timeout before giving up trying to refresh an access token for
     * a session. If multiple threads try to check or refresh the access token
     * at the same time, only one gets a lock and blocks the others. In case
     * of a timeout, this is logged as a temporary issue and the request continued
     * as usual.
     */
    long getTokenLockTimeoutSeconds();

    /**
     * Gets the {@link NamePart} to be used for an issued Resource
     * Owner Password Credentials Grant. The part is taken from the
     * user-provided login name.
     */
    NamePart getPasswordGrantUserNamePart();

    /**
     * Gets the {@link LookupSource} used for determining the context
     * of a user for which a token pair has been obtained.
     */
    LookupSource getContextLookupSource();

    /**
     * Gets the name of a response parameter in case {@link #getContextLookupSource()}
     * returns {@link LookupSource#RESPONSE_PARAMETER}.
     *
     * @see #getContextLookupSource()
     */
    String getContextLookupParameter();

    /**
     * Gets the {@link NamePart} used for determining the context
     * of a user for which a token pair has been obtained. The part
     * is taken from the value of the according {@link LookupSource}.
     *
     * @see #getContextLookupSource()
     * @see #getContextLookupParameter()
     */
    NamePart getContextLookupNamePart();

    /**
     * Gets the {@link LookupSource} used for determining the user for
     * which a token pair has been obtained.
     */
    LookupSource getUserLookupSource();

    /**
     * Gets the name of a response parameter in case {@link #getUserLookupSource()}
     * returns {@link LookupSource#RESPONSE_PARAMETER}.
     *
     * @see #getUserLookupSource()
     */
    String getUserLookupParameter();

    /**
     * Gets the {@link NamePart} used for determining the user for
     * which a token pair has been obtained. The part is taken from
     * the value of the according {@link LookupSource}.
     *
     * @see #getUserLookupSource()
     * @see #getUserLookupParameter()
     */
    NamePart getUserLookupNamePart();

    /**
     * Gets whether token refresh should try to recover valid tokens from
     * the session instance that is present in {@link SessionStorageService}.
     * This is only tried as a fall-back, after token refresh failed with an
     * {@code invalid_grant} error.
     */
    boolean tryRecoverStoredTokens();

    /**
     * Whether the user password provided during login shall be stored in the
     * {@link Session} or not.
     */
    boolean keepPasswordInSession();

}
