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

package com.openexchange.mail.oauth;

import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.session.Session;

/**
 * {@link MailOAuthProvider} - Mail/transport OAuth access for a certain OAuth provider..
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface MailOAuthProvider {

    /**
     * Gets the identifier of the provider
     *
     * @return The provider identifier
     */
    String getProviderId();

    /**
     * Gets the auto-configuration for specified OAuth account and associated session.
     *
     * @param oauthAccount The OAuth account
     * @param session The session
     * @return The resolved auto-configuration or <code>null</code>
     * @throws OXException If appropriate auto-configuration cannot be returned
     */
    Autoconfig getAutoconfigFor(OAuthAccount oauthAccount, Session session) throws OXException;

    /**
     * Gets the applicable token for specified OAuth account and associated session.
     *
     * @param oauthAccount The OAuth account
     * @param session The session
     * @return The applicable token
     * @throws OXException If applicable token cannot be returned
     */
    TokenInfo getTokenFor(OAuthAccount oauthAccount, Session session) throws OXException;

}
