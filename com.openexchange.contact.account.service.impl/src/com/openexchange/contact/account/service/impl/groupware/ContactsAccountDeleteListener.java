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

package com.openexchange.contact.account.service.impl.groupware;

import static com.openexchange.contact.common.ContactsParameters.PARAMETER_CONNECTION;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.DefaultContactsParameters;
import com.openexchange.contact.account.service.impl.ContactsAccountServiceImpl;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.provider.ContactsProvider;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.ContactsProviderRegistry;
import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ContactsAccountDeleteListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsAccountDeleteListener implements DeleteListener {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactsAccountDeleteListener}.
     * 
     * @param services A service lookup reference
     */
    public ContactsAccountDeleteListener(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER != event.getType() || DeleteEvent.SUBTYPE_ANONYMOUS_GUEST == event.getSubType() || DeleteEvent.SUBTYPE_INVITED_GUEST == event.getSubType()) {
            // Note that the DeleteEvent.TYPE_CONTEXT is already handled by 
            // com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage.deleteTablesData(String, Integer, Connection, boolean)
            return;
        }

        SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);
        ContactStorages contactsStorage = requireService(ContactsStorageFactory.class, services).create(event.getContext(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        List<ContactsAccount> storedAccounts = contactsStorage.getContactsAccountsStorage().loadAccounts(event.getId());
        if (null == storedAccounts || storedAccounts.isEmpty()) {
            return;
        }

        ContactsProviderRegistry providerRegistry = requireService(ContactsProviderRegistry.class, services);
        for (ContactsAccount storedAccount : storedAccounts) {
            contactsStorage.getContactsAccountsStorage().deleteAccount(storedAccount.getUserId(), storedAccount.getAccountId(), Long.MAX_VALUE);
            Optional<ContactsProvider> contactsProvider = providerRegistry.getContactProvider(storedAccount.getProviderId());
            if (false == contactsProvider.isPresent()) {
                LoggerFactory.getLogger(ContactsAccountServiceImpl.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.", storedAccount.getProviderId(), storedAccount, ContactsProviderExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
                continue;
            }
            DefaultContactsParameters parameters = new DefaultContactsParameters();
            parameters.set(PARAMETER_CONNECTION(), writeCon);
            contactsProvider.get().onAccountDeleted(event.getContext(), storedAccount, parameters);
        }
    }
}
