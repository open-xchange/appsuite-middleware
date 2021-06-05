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

package com.openexchange.oauth.yahoo.access;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.yahoo.osgi.Services;
import com.openexchange.session.Session;

/**
 * {@link YahooOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooOAuthAccess extends AbstractOAuthAccess {

    private final int accountId;

    /**
     * Initialises a new {@link YahooOAuthAccess}.
     */
    public YahooOAuthAccess(Session session, int accountId) {
        super(session);
        this.accountId = accountId;
    }

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            OAuthService oauthService = Services.getService(OAuthService.class);
            OAuthAccount oauthAccount = oauthService.getAccount(getSession(), accountId);
            verifyAccount(oauthAccount, oauthService);
            setOAuthAccount(oauthAccount);
            setOAuthClient(new OAuthClient<YahooClient>(new YahooClient(oauthAccount, getSession()), oauthAccount.getToken()));
        }
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        return this;
    }

    @Override
    public boolean ping() throws OXException {
        YahooClient yc = (YahooClient) getOAuthClient().getClient();
        return yc.ping();
    }

    @Override
    public int getAccountId() throws OXException {
        return accountId;
    }
}
