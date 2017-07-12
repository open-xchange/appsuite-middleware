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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.impl.availability;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.chronos.storage.CalendarAvailabilityStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AbstractStorageOperation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractStorageOperation<T> implements StorageOperation<T> {

    /** The session parameter name where the underlying database connection is held during transactions */
    public static final String PARAM_CONNECTION = Connection.class.getName();

    private final CalendarSession session;
    private final Context context;

    private ServiceLookup services;

    /**
     * Initialises a new {@link StorageOperation}.
     *
     * @param session The server session
     * @param services The {@link ServiceLookup} instance
     */
    public AbstractStorageOperation(ServiceLookup services, CalendarSession session) throws OXException {
        super();
        this.services = services;
        this.session = session;
        this.context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.impl.StorageOperation#executeQuery()
     */
    @Override
    public T executeQuery() throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly(context);
            session.set(PARAM_CONNECTION, readConnection);
            return execute(session, initStorage(new SimpleDBProvider(readConnection, null)));
        } finally {
            session.set(PARAM_CONNECTION, null);
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.availability.impl.StorageOperation#executeUpdate()
     */
    @Override
    public T executeUpdate() throws OXException {
        boolean committed = false;
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection writeConnection = null;
        try {
            writeConnection = dbService.getWritable(context);
            writeConnection.setAutoCommit(false);
            session.set(PARAM_CONNECTION, writeConnection);
            T result = execute(session, initStorage(new SimpleDBProvider(writeConnection, writeConnection)));
            writeConnection.commit();
            committed = true;
            return result;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            session.set(PARAM_CONNECTION, null);
            if (null != writeConnection) {
                if (false == committed) {
                    Databases.rollback(writeConnection);
                    Databases.autocommit(writeConnection);
                    dbService.backWritableAfterReading(context, writeConnection);
                } else {
                    Databases.autocommit(writeConnection);
                    dbService.backWritable(context, writeConnection);
                }
            }
        }
    }

    /**
     * Executes the storage operation.
     *
     * @param session The calendar session
     * @param storage The initialised calendar availability storage to use
     * @return The result
     */
    protected abstract T execute(CalendarSession session, CalendarAvailabilityStorage storage) throws OXException;

    ///////////////////////////////// HELPERS ///////////////////////////////

    /**
     * Initialises the storage
     * 
     * @param dbProvider The database provider
     * @return The storage instance
     * @throws OXException if the storage instance cannot be initialised
     */
    private CalendarAvailabilityStorage initStorage(DBProvider dbProvider) throws OXException {
        return services.getService(CalendarAvailabilityStorageFactory.class).create(context, 0, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }
}
