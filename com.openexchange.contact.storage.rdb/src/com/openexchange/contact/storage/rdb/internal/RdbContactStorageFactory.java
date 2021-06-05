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

import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbContactStorageFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class RdbContactStorageFactory implements ContactsStorageFactory {

    private final DBProvider dbProvider;
    private final RdbContactStorage delegate;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link RdbContactStorageFactory}.
     * 
     * @param services A service lookup reference
     * @param dbProvider The db provider\
     * @param delegate The {@link RdbContactStorage} delegate
     */
    public RdbContactStorageFactory(ServiceLookup services, DBProvider dbProvider, RdbContactStorage delegate) {
        super();
        this.services = services;
        this.dbProvider = dbProvider;
        this.delegate = delegate;
    }

    @Override
    public ContactStorages create(Context context) throws OXException {
        return create(context, this.dbProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
    }

    @Override
    public ContactStorages create(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        return new RdbContactsStorage(services, context, dbProvider, txPolicy, delegate);
    }
}
