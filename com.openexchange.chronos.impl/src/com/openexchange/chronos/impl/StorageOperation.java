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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.LegacyCalendarStorageFactory;
import com.openexchange.chronos.storage.ReplayingCalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link StorageOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class StorageOperation<T> {

    /** The session parameter name where the underlying database connection is held during transactions */
    public static final String PARAM_CONNECTION = Connection.class.getName();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StorageOperation.class);

    private final CalendarSession session;
    private final Context context;

    /**
     * Initializes a new {@link StorageOperation}.
     *
     * @param session The server session
     */
    public StorageOperation(CalendarSession session) throws OXException {
        super();
        this.session = session;
        this.context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
    }

    /**
     * Executes the read/write storage operation in a transaction.
     *
     * @return The result
     */
    public T executeUpdate() throws OXException {
        boolean committed = false;
        DatabaseService dbService = Services.getService(DatabaseService.class);
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
                    rollback(writeConnection);
                    autocommit(writeConnection);
                    dbService.backWritableAfterReading(context, writeConnection);
                } else {
                    autocommit(writeConnection);
                    dbService.backWritable(context, writeConnection);
                }
            }
        }
    }

    /**
     * Executes the read-only storage operation.
     *
     * @return The result
     */
    public T executeQuery() throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
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

    private CalendarStorage initStorage(DBProvider dbProvider) throws OXException {
        if (session.getConfig().isReplayToLegacyStorage()) {
            return Services.getService(ReplayingCalendarStorageFactory.class).create(context, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        } else if (session.getConfig().isUseLegacyStorage()) {
            return Services.getService(LegacyCalendarStorageFactory.class).create(context, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        } else {
            return Services.getService(CalendarStorageFactory.class).create(context, 0, session.getEntityResolver(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        }
    }

    /**
     * Executes the storage operation.
     *
     * @param session The calendar session
     * @param storage The initialized calendar storage to use
     * @return The result
     */
    protected abstract T execute(CalendarSession session, CalendarStorage storage) throws OXException;

    private static void rollback(Connection connection) {
        if (null != connection) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

    private static void autocommit(Connection connection) {
        if (null != connection) {
            try {
                if (false == connection.isClosed()) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

}
