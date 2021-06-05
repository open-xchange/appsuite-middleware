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

package com.openexchange.mail.oauth.google;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link GoogleMailOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleMailOAuthAccountAssociationProvider implements OAuthAccountAssociationProvider {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GoogleMailOAuthAccountAssociationProvider}.
     */
    public GoogleMailOAuthAccountAssociationProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        Collection<OAuthAccountAssociation> associations = null;
        MailAccountStorageService storage = services.getService(MailAccountStorageService.class);
        for (MailAccount mailAccount : storage.getUserMailAccounts(session.getUserId(), session.getContextId())) {
            if (accountId != mailAccount.getMailOAuthId()) {
                continue;
            }
            if (null == associations) {
                associations = new LinkedList<>();
            }
            associations.add(new GoogleMailOAuthAccountAssociation(accountId, session.getUserId(), session.getContextId(), mailAccount));
        }
        return null == associations ? Collections.<OAuthAccountAssociation> emptyList() : associations;
    }
}
