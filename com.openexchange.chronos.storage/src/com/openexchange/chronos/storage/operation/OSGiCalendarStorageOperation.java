/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
