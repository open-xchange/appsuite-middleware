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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.osgi.Tools.requireService;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AdministrativeCalendarStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbCalendarStorageFactory implements CalendarStorageFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbCalendarStorageFactory.class);

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
             * choose target storage for default account based update status and configuration overrides
             */
            ConfigurationService configService = requireService(ConfigurationService.class, services);
            if (configService.getBoolProperty("com.openexchange.calendar.useLegacyStorage", false)) {
                LOG.debug("Using 'legacy' calendar storage for default account '0' (overridden via configuration).");
                return new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, txPolicy);
            }
            UpdateStatus updateStatus = Updater.getInstance().getStatus(context.getContextId());
            if (false == updateStatus.isExecutedSuccessfully("com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask")) {
                LOG.debug("ChronosStorageMigrationTask not executed successfully, falling back to 'legacy' calendar storage for account '0'.");
                return new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, txPolicy);
            }
            if (updateStatus.isExecutedSuccessfully("com.openexchange.chronos.storage.rdb.migration.ChronosStoragePurgeLegacyDataTask")) {
                LOG.debug("ChronosStoragePurgeLegacyDataTask executed successfully, using default calendar storage for account '0'.");
                return new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, accountId, entityResolver, dbProvider, txPolicy);
            }
            if (configService.getBoolProperty("com.openexchange.calendar.replayToLegacyStorage", true)) {
                LOG.debug("Using 'replaying' calendar storage for default account '0'.");
                CalendarStorage legacyStorage = new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, txPolicy);
                CalendarStorage storage = new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, accountId, entityResolver, dbProvider, txPolicy);
                return new com.openexchange.chronos.storage.rdb.replaying.RdbCalendarStorage(storage, makeResilient(legacyStorage));
            }
        }
        return new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, accountId, entityResolver, dbProvider, txPolicy);
    }

    @Override
    public CalendarStorage makeResilient(CalendarStorage storage) {
        return new com.openexchange.chronos.storage.rdb.resilient.RdbCalendarStorage(services, storage, true, true, ProblemSeverity.MAJOR);
    }

    @Override
    public AdministrativeCalendarStorage createAdministrative() throws OXException {
        return new AdministrativeRdbCalendarStorage();
    }

}
