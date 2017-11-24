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

package com.openexchange.chronos.alarm;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.AbstractUserServiceInterceptor;

/**
 * {@link AlarmTriggerServiceInterceptor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerServiceInterceptor extends AbstractUserServiceInterceptor {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmTriggerServiceInterceptor.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AlarmTriggerServiceInterceptor}.
     *
     * @param services The {@link ServiceLookup}
     */
    public AlarmTriggerServiceInterceptor(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        if (context == null || user == null) {
            // ignore
            return;
        }
        AdministrativeCalendarAccountService accountService = requireService(AdministrativeCalendarAccountService.class, services);
        List<CalendarAccount> accounts = accountService.getAccounts(context.getContextId(), user.getId());

        CalendarStorageFactory factory = requireService(CalendarStorageFactory.class, services);
        DBProvider dbProvider = requireService(DBProvider.class, services);

        Connection writeConnection = dbProvider.getWriteConnection(context);
        boolean committed = false;
        int updated = 0;
        try {
            writeConnection.setAutoCommit(false);
            SimpleDBProvider simpleDBProvider = new SimpleDBProvider(null, writeConnection);

            for (CalendarAccount acc : accounts) {
                CalendarStorage storage = factory.create(context, acc.getAccountId(), optEntityResolver(context.getContextId()), simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
                storage.getAlarmTriggerStorage().recalculateFloatingAlarmTriggers(user.getId());
            }
            CalendarStorage storage = factory.create(context, 0, optEntityResolver(context.getContextId()), simpleDBProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            storage.getAlarmTriggerStorage().recalculateFloatingAlarmTriggers(user.getId());
            writeConnection.commit();
            committed = true;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            if (null != writeConnection) {
                if (false == committed) {
                    Databases.rollback(writeConnection);
                    Databases.autocommit(writeConnection);
                    dbProvider.releaseWriteConnectionAfterReading(context, writeConnection);
                } else {
                    Databases.autocommit(writeConnection);
                    if (updated != 0) {
                        dbProvider.releaseWriteConnection(context, writeConnection);
                    } else {
                        dbProvider.releaseWriteConnectionAfterReading(context, writeConnection);
                    }
                }
            }
        }
    }

    protected EntityResolver optEntityResolver(int contextId) throws OXException {
        CalendarUtilities calendarUtilities = services.getService(CalendarUtilities.class);
        return null != calendarUtilities ? calendarUtilities.getEntityResolver(contextId) : null;
    }

}
