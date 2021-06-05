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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.Utils.optConnection;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import static com.openexchange.java.Autoboxing.b;
import java.sql.Connection;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.operation.CalendarStorageOperation;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalCalendarStorageOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class InternalCalendarStorageOperation<T> extends CalendarStorageOperation<T> {

    private final CalendarSession session;

    /**
     * Initializes a new {@link InternalCalendarStorageOperation}.
     *
     * @param session The calendar session
     */
    public InternalCalendarStorageOperation(CalendarSession session) {
        super(Services.getService(DatabaseService.class), session.getSession().getContextId(), DEFAULT_RETRIES, optConnection(session));
        this.session = session;
    }

    /**
     * Executes the storage operation.
     *
     * @param session The calendar session
     * @param storage The calendar storage to use
     * @return The result
     */
    protected abstract T execute(CalendarSession session, CalendarStorage storage) throws OXException;

    @Override
    protected T call(CalendarStorage storage) throws OXException {
        T result = execute(session, storage);
        Utils.addWarnings(session, collectWarnings(storage));
        return result;
    }

    @Override
    protected void onConnection(Connection connection) {
        session.set(PARAMETER_CONNECTION(), connection);
    }

    @Override
    protected CalendarStorage initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        Context context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        CalendarStorage storage = storageFactory.create(context, Utils.ACCOUNT_ID, session.getEntityResolver(), dbProvider, txPolicy);
        if (b(session.get(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.class, Boolean.FALSE))) {
            storage = storageFactory.makeResilient(storage);
        }
        return storage;
    }

}
