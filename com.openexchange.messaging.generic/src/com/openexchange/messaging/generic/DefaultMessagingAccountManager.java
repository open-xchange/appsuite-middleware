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

package com.openexchange.messaging.generic;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.internal.CachingMessagingAccountStorage;
import com.openexchange.messaging.generic.internal.Modifier;
import com.openexchange.session.Session;

/**
 * {@link DefaultMessagingAccountManager} - The default messaging account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class DefaultMessagingAccountManager implements MessagingAccountManager {

    private static final class DefaultModifier implements Modifier {

        private final DefaultMessagingAccountManager manager;

        public DefaultModifier(final DefaultMessagingAccountManager manager) {
            super();
            this.manager = manager;
        }

        @Override
        public MessagingAccount modifyIncoming(final MessagingAccount account) throws OXException {
            return manager.modifyIncoming(account);
        }

        @Override
        public MessagingAccount modifyOutgoing(final MessagingAccount account) throws OXException {
            return manager.modifyOutgoing(account);
        }

    }

    /**
     * The messaging account storage cache.
     */
    private static final CachingMessagingAccountStorage CACHE = CachingMessagingAccountStorage.getInstance();

    /**
     * The identifier of associated messaging service.
     */
    private final String serviceId;

    private final MessagingService service;

    private final Modifier modifier;

    /**
     * Initializes a new {@link DefaultMessagingAccountManager}.
     *
     * @param service The messaging service
     */
    public DefaultMessagingAccountManager(final MessagingService service) {
        super();
        serviceId = service.getId();
        this.service = service;
        modifier = new DefaultModifier(this);
    }

    @SuppressWarnings("unused")
    protected MessagingAccount modifyIncoming(final MessagingAccount account) throws OXException {
        return account;
    }

    @SuppressWarnings("unused")
    protected MessagingAccount modifyOutgoing(final MessagingAccount account) throws OXException {
        return account;
    }

    @Override
    public MessagingAccount getAccount(final int id, final Session session) throws OXException {
        return CACHE.getAccount(serviceId, id, session, modifier);
    }

    @Override
    public List<MessagingAccount> getAccounts(final Session session) throws OXException {
        return CACHE.getAccounts(serviceId, session, modifier);
    }

    @Override
    public int addAccount(final MessagingAccount account, final Session session) throws OXException {
        return CACHE.addAccount(serviceId, account, session, modifier);
    }

    @Override
    public void deleteAccount(final MessagingAccount account, final Session session) throws OXException {
        CACHE.deleteAccount(serviceId, account, session, modifier);
    }

    @Override
    public void updateAccount(final MessagingAccount account, final Session session) throws OXException {
        CACHE.updateAccount(serviceId, account, session, modifier);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        CACHE.migrateToNewSecret(service, oldSecret, newSecret, session);
    }

    @Override
    public boolean hasAccount(final Session session) throws OXException {
        return CACHE.hasAccount(service, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        CACHE.cleanUp(service, secret, session);
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        CACHE.removeUnrecoverableItems(service, secret, session);        
    }
}
