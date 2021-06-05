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

package com.openexchange.subscribe.microsoft.graph.oauth;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.microsoft.graph.MicrosoftGraphOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.oauth.AbstractSubscribeOAuthAccountAssociation;

/**
 * {@link MicrosoftContactsOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftContactsOAuthAccountAssociation extends AbstractSubscribeOAuthAccountAssociation {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link MicrosoftContactsOAuthAccountAssociation}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public MicrosoftContactsOAuthAccountAssociation(int accountId, int userId, int contextId, String displayName, Subscription subscription, ServiceLookup services) {
        super(accountId, userId, contextId, displayName, subscription);
        this.services = services;
    }

    @Override
    public String getModule() {
        return Module.CONTACTS.getModuleName();
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(MicrosoftGraphOAuthScope.contacts_ro);
    }

    @SuppressWarnings("unused")
    @Override
    public Status getStatus(Session session) throws OXException {
        String accessToken = null;
        String accessSecret = null;
        try {
            OAuthService service = services.getService(OAuthService.class);
            OAuthAccount account = service.getAccount(session, getOAuthAccountId());
            accessToken = account.getToken();
            accessSecret = account.getSecret();
        } catch (OXException e) {
            return Status.RECREATION_NEEDED;
        }
        if (Strings.isEmpty(accessToken) && Strings.isEmpty(accessSecret)) {
            return Status.INVALID_GRANT;
        }
        OAuthServiceMetaDataRegistry registry = services.getService(OAuthServiceMetaDataRegistry.class);
        OAuthServiceMetaData metadata = registry.getService(KnownApi.MICROSOFT_GRAPH.getServiceId(), session.getUserId(), session.getContextId());
        String userIdentity = metadata.getUserIdentity(session, getOAuthAccountId(), accessToken, accessSecret);
        if (Strings.isEmpty(userIdentity)) {
            return Status.INVALID_GRANT;
        }
        return Status.OK;
    }

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        // nope
        throw new UnsupportedOperationException("There is no OAuthAccess for Microsoft Graph Contacts.");
    }
}
