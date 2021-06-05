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

package com.openexchange.chronos.storage.operation;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import static com.openexchange.java.Autoboxing.b;
import java.sql.Connection;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OSGiCalendarStorageOperation}
 *
 * @param <T> The return type of the operation
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class OSGiCalendarStorageOperation<T> extends CalendarStorageOperation<T> {

    private final ServiceLookup services;
    private final int accountId;
    private final CalendarParameters parameters;

    /**
     * Initializes a new {@link OSGiCalendarStorageOperation}.
     * <p/>
     * The passed service lookup reference should yield the {@link ContextService}, the {@link DatabaseService} and the
     * {@link CalendarStorageFactory}, and optionally the {@link CalendarUtilities} service.
     *
     * @param services A service lookup reference providing access for the needed services
     * @param contextId The context identifier
     * @param accountId The account identifier
     */
    protected OSGiCalendarStorageOperation(ServiceLookup services, int contextId, int accountId) {
        this(services, contextId, accountId, null);
    }

    /**
     * Initializes a new {@link OSGiCalendarStorageOperation}.
     * <p/>
     * The passed service lookup reference should yield the {@link ContextService}, the {@link DatabaseService} and the
     * {@link CalendarStorageFactory}, and optionally the {@link CalendarUtilities} service.
     * <p/>
     * An existing, <i>external</i> database connection may be supplied as <code>java.sql.Connection</code> parameter.<br/>
     * Additionally, {@link CalendarParameters#PARAMETER_IGNORE_STORAGE_WARNINGS} is respected when defined.
     *
     * @param services A service lookup reference providing access for the needed services
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @param parameters Optional additional calendar parameters
     */
    protected OSGiCalendarStorageOperation(ServiceLookup services, int contextId, int accountId, CalendarParameters parameters) {
        super(services.getService(DatabaseService.class), contextId, DEFAULT_RETRIES, optConnection(parameters));
        this.parameters = parameters;
        this.services = services;
        this.accountId = accountId;
    }

    @Override
    protected CalendarStorage initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextId);
        CalendarStorageFactory storageFactory = services.getService(CalendarStorageFactory.class);
        CalendarStorage storage = storageFactory.create(context, accountId, optEntityResolver(), dbProvider, txPolicy);
        if (null != parameters && b(parameters.get(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.class, Boolean.FALSE))) {
            storage = storageFactory.makeResilient(storage);
        }
        return storage;
    }

    /**
     * Optionally gets an entity resolver for the context.
     *
     * @return The entity resolver, or <code>null</code> if not available
     */
    protected EntityResolver optEntityResolver() throws OXException {
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        return null != calendarUtilities ? calendarUtilities.getEntityResolver(contextId) : null;
    }

    /**
     * Optionally gets a database connection set in calendar parameters.
     *
     * @param parameters The calendar parameters to get the connection from
     * @return The connection, or <code>null</code> if not defined
     */
    private static Connection optConnection(CalendarParameters parameters) {
        return null != parameters ? parameters.get(PARAMETER_CONNECTION(), Connection.class, null) : null;
    }

}
