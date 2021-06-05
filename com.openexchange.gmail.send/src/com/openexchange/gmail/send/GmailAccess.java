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

package com.openexchange.gmail.send;

import com.google.api.services.gmail.Gmail;
import com.openexchange.oauth.OAuthAccount;

/**
 * {@link GmailAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GmailAccess {

    /**
     * Creates a new access for specified arguments.
     *
     * @param gmail The Gmail service API access
     * @param oauthAccount The associated OAuth account
     * @return The newly created Gmail access instance
     */
    public static GmailAccess accessFor(Gmail gmail, OAuthAccount oauthAccount) {
        return new GmailAccess(gmail, oauthAccount);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    /** The Gmail service API access */
    public final Gmail gmail;

    /** The associated OAuth account */
    public final OAuthAccount oauthAccount;

    /**
     * Initializes a new {@link GmailAccess}.
     *
     * @param gmail The Gmail service API access
     * @param oauthAccount The associated OAuth account
     */
    private GmailAccess(Gmail gmail, OAuthAccount oauthAccount) {
        super();
        this.gmail = gmail;
        this.oauthAccount = oauthAccount;
    }

}
