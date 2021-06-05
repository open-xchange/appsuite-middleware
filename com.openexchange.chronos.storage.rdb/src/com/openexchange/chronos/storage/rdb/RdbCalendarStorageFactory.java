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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbCalendarStorageFactory implements CalendarStorageFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbCalendarStorageFactory.class);
    private static final Cache<Integer, Long> WARNINGS_LOGGED_PER_CONTEXT = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private final ServiceLookup services;
    private final DBProvider defaultDbProvider;

    /**
     * Initializes a new {@link RdbCalendarStorageFactory}.
     *
     * @param services A service lookup reference
     * @param defaultDbProvider The default database provider to use
     */
    public RdbCalendarStorageFactory(ServiceLookup services, DBProvider defaultDbProvider) {
        super();
        this.services = services;
        this.defaultDbProvider = defaultDbProvider;
    }

    @Override
    public CalendarStorage create(Context context, int accountId, EntityResolver entityResolver) throws OXException {
        return create(context, accountId, entityResolver, defaultDbProvider, DBTransactionPolicy.NORMAL_TRANSACTIONS);
    }

    @Override
    public CalendarStorage create(Context context, int accountId, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        if (CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == accountId) {
            /*
             * fall back to legacy storage in read-only mode if not yet migrated
             */
            UpdateStatus updateStatus = Updater.getInstance().getStatus(context.getContextId());
            if (false == updateStatus.isExecutedSuccessfully("com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask")) {
                logLegacyStorageWarning(context.getContextId(), "ChronosStorageMigrationTask not executed successfully, falling back to read-only 'legacy' calendar storage for account '0'.");
                return new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, txPolicy);
            }
        }
        return new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, accountId, entityResolver, dbProvider, txPolicy);
    }

    @Override
    public CalendarStorage makeResilient(CalendarStorage storage) {
        return new com.openexchange.chronos.storage.rdb.resilient.RdbCalendarStorage(services, storage, true, true, ProblemSeverity.MAJOR);
    }

    /**
     * Logs a warning related to the usage of the legacy storage (at level WARN from time to time), ensuring that not too many log events
     * are generated for the same context.
     * 
     * @param contextId The identifier of the context for which the legacy storage is initialized for
     * @param message The message to log
     */
    private static void logLegacyStorageWarning(int contextId, String message) {
        long now = System.currentTimeMillis();
        Long lastLogged = WARNINGS_LOGGED_PER_CONTEXT.getIfPresent(I(contextId));
        if (null == lastLogged || now - l(lastLogged) > TimeUnit.HOURS.toMillis(1L)) {
            LOG.warn("{}{}{}  This mode is deprecated and will be removed in a future release. Please perform the calendar migration for context {} now!{}", 
                message, Strings.getLineSeparator(), Strings.getLineSeparator(), I(contextId), Strings.getLineSeparator());
            WARNINGS_LOGGED_PER_CONTEXT.put(I(contextId), L(now));
        } else {
            LOG.debug(message);
        }
    }

}
