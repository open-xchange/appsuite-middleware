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

package com.openexchange.subscribe.google.oauth;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.google.GoogleContactsSubscribeService;
import com.openexchange.subscribe.oauth.AbstractSubscribeOAuthAccountAssociation;

/**
 * {@link GoogleContactsOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleContactsOAuthAccountAssociation extends AbstractSubscribeOAuthAccountAssociation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleContactsOAuthAccountAssociation.class);

    private final GoogleContactsSubscribeService service;

    /**
     * Initialises a new {@link GoogleContactsOAuthAccountAssociation}.
     */
    public GoogleContactsOAuthAccountAssociation(int accountId, int userId, int contextId, String displayName, Subscription subscription, GoogleContactsSubscribeService service) {
        super(accountId, userId, contextId, displayName, subscription);
        this.service = service;
    }

    @Override
    public String getModule() {
        return Module.CONTACTS.getModuleName();
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.contacts_ro);
    }

    @Override
    public Status getStatus(Session session) throws OXException {
        try {
            service.ping(session, GoogleApiClients.getGoogleAccount(getOAuthAccountId(), session));
            return Status.OK;
        } catch (OXException e) {
            LOGGER.debug("", e);
            return Status.RECREATION_NEEDED;
        }
    }

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        // nope
        throw new UnsupportedOperationException("There is no OAuthAccess for Google Contacts.");
    }
}
