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

package com.openexchange.contact.account.service.impl;

import static com.openexchange.contact.common.ContactsParameters.PARAMETER_CONNECTION;
import java.sql.Connection;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GroupwareContactsDatabasePerformer} - Database performer for all contacts accounts.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public abstract class GroupwareContactsDatabasePerformer<T> extends AbstractContactsDatabasePerformer<T> {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GroupwareContactsDatabasePerformer}.
     * <p/>
     * The passed service lookup reference should yield the {@link ContextService}, the {@link DatabaseService} and the
     * {@link ContactsStorageFactory} service.
     *
     * @param services A service lookup reference providing access for the needed services
     * @param contextId The context identifier
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     */
    public GroupwareContactsDatabasePerformer(ServiceLookup services, int contextId, ContactsParameters parameters) {
        super(services.getService(DatabaseService.class), contextId, optConnection(parameters));
        this.services = services;
    }

    @Override
    ContactStorages initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        Context context = services.getServiceSafe(ContextService.class).getContext(contextId);
        ContactsStorageFactory storageFactory = services.getServiceSafe(ContactsStorageFactory.class);
        return storageFactory.create(context, dbProvider, txPolicy);
    }

    /**
     * Optionally gets a database connection set in contacts parameters.
     *
     * @param parameters The contacts parameters to get the connection from
     * @return The connection, or <code>null</code> if not defined
     */
    private static Connection optConnection(ContactsParameters parameters) {
        return null != parameters ? parameters.get(PARAMETER_CONNECTION(), Connection.class, null) : null;
    }
}
