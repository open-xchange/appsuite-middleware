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

package com.openexchange.chronos.provider.caching.internal.db;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link ConnectionHelper}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ConnectionHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConnectionHelper.class);

    private final DatabaseService databaseService;
    private final Context context;
    private CalendarStorage calendarStorage;
    private SimpleDBProvider dbProvider;
    private boolean committed;

    public ConnectionHelper(DatabaseService databaseService, Context context, int accountId) {
        this.databaseService = databaseService;
        this.context = context;

        try {
            Connection writable = databaseService.getWritable(context);
            writable.setAutoCommit(false);
            Connection readOnly = databaseService.getReadOnly(context);
            this.dbProvider = new SimpleDBProvider(readOnly, writable);
            this.calendarStorage = Services.getService(CalendarStorageFactory.class).create(context, accountId, null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        } catch (OXException | SQLException e) {
            LoggerFactory.getLogger(ConnectionHelper.class).error("Error while creating ConnectionHelper. Won't be able to handle connections!", e);
        }
    }

    public CalendarStorage getCalendarStorage() {
        return calendarStorage;
    }

    public Connection getWriteConnection() {
        return this.dbProvider.getWriteConnection(context);
    }

    public Connection getReadConnection() {
        return this.dbProvider.getReadConnection(context);
    }

    /**
     * Backs all acquired database connections to the pool if needed.
     */
    public void back() {
        backReadOnly();
        backWritable();
    }

    /**
     * Backs an acquired read-only connection to the pool if needed.
     */
    public void backReadOnly() {
        Connection readConnection = getReadConnection();
        if (null != readConnection) {
            databaseService.backReadOnly(context, readConnection);
        }
    }

    /**
     * Backs an acquired writable connection to the pool if needed, rolling back the transaction automatically if not yet committed.
     */
    public void backWritable() {
        Connection writeConnection = getWriteConnection();
        if (null != writeConnection) {
            if (!committed) {
                rollback(writeConnection);
            }
            autocommit(writeConnection);
            databaseService.backWritable(context, writeConnection);
        }
    }

    public void commit() throws OXException {
        try {
            this.dbProvider.getWriteConnection(context).commit();
        } catch (SQLException e) {
            LOG.error("Unable to commit transaction.", e);
            throw CalendarExceptionCodes.DB_ERROR.create(e.getMessage(), e);
        }
        committed = true;
    }
}
