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

import static com.openexchange.contact.ContactSessionParameterNames.getParamReadOnlyConnection;
import static com.openexchange.contact.ContactSessionParameterNames.getParamWritableConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.common.DefaultContactsAccount;
import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalContactsDatabasePerformer} - Database performer for all
 * internal ({@link DefaultContactsAccount}) contacts accounts.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public abstract class InternalContactsDatabasePerformer<T> extends AbstractContactsDatabasePerformer<T> {

    private final Session session;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link InternalContactsDatabasePerformer}.
     * 
     * @param session the session
     * @throws OXException in case the {@link DatabaseService} is absent
     */
    public InternalContactsDatabasePerformer(ServiceLookup services, Session session) throws OXException {
        super(services.getServiceSafe(DatabaseService.class), session.getContextId());
        this.services = services;
        this.session = session;
    }

    @Override
    ContactStorages initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        Context context = ServerSessionAdapter.valueOf(session).getContext();
        ContactsStorageFactory storageFactory = services.getServiceSafe(ContactsStorageFactory.class);
        return storageFactory.create(context, dbProvider, txPolicy);
    }

    @Override
    protected void onConnection(Connection connection) {
        try {
            session.setParameter(connection.isReadOnly() ? getParamReadOnlyConnection() : getParamWritableConnection(), connection);
        } catch (SQLException e) {
            LoggerFactory.getLogger(InternalContactsDatabasePerformer.class).debug("Unable to set the 'connection' parameter to session", e);
        }
    }
}
