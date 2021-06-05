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

package com.openexchange.oauth.json.osgi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.HostInfo;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link OSGiOAuthService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiOAuthService extends AbstractOSGiDelegateService<OAuthService> implements OAuthService {

    /**
     * Initializes a new {@link OSGiOAuthService}.
     */
    public OSGiOAuthService() {
        super(OAuthService.class);
    }

    private OAuthService getService0() throws OXException {
        return super.getService();
    }

    @Override
    public OAuthAccount createAccount(Session session, final String serviceMetaData, Set<OAuthScope> scopes, final Map<String, Object> arguments) throws OXException {
        return getService0().createAccount(session, serviceMetaData, scopes, arguments);
    }

    @Override
    public OAuthAccount createAccount(Session session, final String serviceMetaData, Set<OAuthScope> scopes, final OAuthInteractionType type, final Map<String, Object> arguments) throws OXException {
        return getService0().createAccount(session, serviceMetaData, scopes, type, arguments);
    }

    @Override
    public void deleteAccount(Session session, final int accountId) throws OXException {
        getService0().deleteAccount(session, accountId);
    }

    @Override
    public OAuthAccount getAccount(final Session session, final int accountId) throws OXException {
        return getService0().getAccount(session, accountId);
    }

    @Override
    public List<OAuthAccount> getAccounts(final Session session) throws OXException {
        return getService0().getAccounts(session);
    }

    @Override
    public List<OAuthAccount> getAccounts(Session session, String serviceMetaData) throws OXException {
        return getService0().getAccounts(session, serviceMetaData);
    }

    @Override
    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        final OAuthService delegatee = optService();
        if (null == delegatee) {
            throw new IllegalStateException("OAuthService is absent.");
        }
        return delegatee.getMetaDataRegistry();
    }

    @Override
    public OAuthInteraction initOAuth(final Session session, final String serviceMetaData, final String callbackUrl, final HostInfo host, Set<OAuthScope> scopes) throws OXException {
        return getService0().initOAuth(session, serviceMetaData, callbackUrl, host, scopes);
    }

    @Override
    public void updateAccount(Session session, final int accountId, final Map<String, Object> arguments) throws OXException {
        getService0().updateAccount(session, accountId, arguments);
    }

    @Override
    public OAuthAccount updateAccount(Session session, final int accountId, final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, Set<OAuthScope> scopes) throws OXException {
        return getService0().updateAccount(session, accountId, serviceMetaData, type, arguments, scopes);
    }

    @Override
    public OAuthAccount getDefaultAccount(API api, Session session) throws OXException {
        return getService0().getDefaultAccount(api, session);
    }

    @Override
    public OAuthAccount upsertAccount(Session session, String serviceMetaData, int accountId, OAuthInteractionType type, Map<String, Object> arguments, Set<OAuthScope> scopes) throws OXException {
        return getService0().upsertAccount(session, serviceMetaData, accountId, type, arguments, scopes);
    }
}
