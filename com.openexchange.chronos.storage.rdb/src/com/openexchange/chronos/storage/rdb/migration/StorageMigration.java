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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;

/**
 * {@link StorageMigration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class StorageMigration {

    private static final int DEFAULT_BATCH_SIZE = 500;
    private static final int DEFAULT_MAX_TOMBSTONE_AGE_IN_MONTHS = 12;

    private final Context context;
    private final ServiceLookup services;

    public StorageMigration(ServiceLookup services, int contextId) throws OXException {
        super();
        this.services = services;
        this.context = services.getService(ContextService.class).getContext(contextId);
    }

    public MigrationResult run() throws OXException {
        try {
            return run(DEFAULT_BATCH_SIZE, getDefaultMinTombstoneLastModified());
        } catch (Exception e) {
            throw new OXException(e);
        }
    }

    public boolean checkRead() throws OXException {

        try {
            return checkRead(DEFAULT_BATCH_SIZE, getDefaultMinTombstoneLastModified());
        } catch (Exception e) {
            throw new OXException(e);
        }
    }

    public MigrationResult run(int batchSize, Date minTombstoneLastModified) throws Exception {
        EntityResolver entityResolver = optEntityResolver(services, context.getContextId());
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection writeConnection = null;
        boolean committed = false;
        try {
            writeConnection = dbService.getWritable(context);
            writeConnection.setAutoCommit(false);
            SimpleDBProvider dbProvider = new SimpleDBProvider(writeConnection, writeConnection);
            CalendarStorage sourceStorage = new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            CalendarStorage destinationStorage = new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, 0, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            MigrationResult result = run(sourceStorage, destinationStorage, batchSize, minTombstoneLastModified);
            if (null == result.getErrors() || result.getErrors().isEmpty()) {
                writeConnection.commit();
                committed = true;
            }
            return result;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
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

    public boolean checkRead(int batchSize, Date minTombstoneLastModified) throws Exception {
        EntityResolver entityResolver = optEntityResolver(services, context.getContextId());
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly(context);
            SimpleDBProvider dbProvider = new SimpleDBProvider(readConnection, null);
            CalendarStorage sourceStorage = new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            return runCheckRead(sourceStorage, batchSize, minTombstoneLastModified);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    private MigrationResult run(CalendarStorage sourceStorage, CalendarStorage destinationStorage, int batchSize, Date minTombstoneLastModified) throws Exception {
        MigrationResult result = new MigrationResult(context.getContextId());
        result.setStart(new Date());
        StorageCopyTask copyTask = new StorageCopyTask(sourceStorage, destinationStorage, batchSize, minTombstoneLastModified);
        copyTask.call();
        result.addErrors(sourceStorage.getAndFlushWarnings());
        result.addErrors(destinationStorage.getAndFlushWarnings());
        result.setEnd(new Date());
        return result;
    }

    private boolean runCheckRead(CalendarStorage sourceStorage, int batchSize, Date minTombstoneLastModified) throws Exception {
        MigrationResult result = new MigrationResult(context.getContextId());
        result.setStart(new Date());
        StorageReadTask copyTask = new StorageReadTask(sourceStorage, batchSize, minTombstoneLastModified);
        copyTask.call();
        result.addErrors(sourceStorage.getAndFlushWarnings());
        result.setEnd(new Date());
        return true;
    }

    private static EntityResolver optEntityResolver(ServiceLookup services, int contextId) {
        CalendarUtilities calendarUtilities = Services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities) {
            try {
                return calendarUtilities.getEntityResolver(contextId);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(StorageMigration.class).warn("Error getting entity resolver for context {}: {}", I(contextId), e.getMessage(), e);
            }
        }
        return null;
    }

    private static final Date getDefaultMinTombstoneLastModified() {
        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, null);
        calendar.add(Calendar.MONTH, -1 * DEFAULT_MAX_TOMBSTONE_AGE_IN_MONTHS);
        return calendar.getTime();
    }

}

