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

package com.openexchange.contact.storage.rdb.internal;

import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.contact.storage.ContactTombstoneStorage;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contact.storage.ContactsAccountStorage;
import com.openexchange.contact.storage.rdb.internal.account.RdbContactsAccountStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbContactsStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class RdbContactsStorage implements ContactStorages {

    private final ContactsAccountStorage contactsAccountStorage;
    private final RdbContactStorage delegate;

    /**
     * Initializes a new {@link RdbContactsStorage}.
     * 
     * @param services A service lookup reference
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     * @param delegaate The delegate {@link RdbContactsStorage}
     */
    public RdbContactsStorage(ServiceLookup services, Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy, RdbContactStorage delegate) {
        super();
        this.delegate = delegate;
        this.contactsAccountStorage = RdbContactsAccountStorage.init(services, context, dbProvider, txPolicy);
    }

    @Override
    public ContactsAccountStorage getContactsAccountsStorage() {
        return contactsAccountStorage;
    }

    @Override
    public ContactStorage getContactStorage() {
        return delegate;
    }

    @Override
    public ContactUserStorage getContactUserStorage() {
        return delegate;
    }

    @Override
    public ContactTombstoneStorage getContactTombStorage() {
        return delegate;
    }
}
