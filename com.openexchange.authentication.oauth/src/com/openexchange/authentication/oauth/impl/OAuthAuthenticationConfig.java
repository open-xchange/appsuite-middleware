/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
