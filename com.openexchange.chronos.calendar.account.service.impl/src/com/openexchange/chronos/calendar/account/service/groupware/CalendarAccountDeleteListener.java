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

package com.openexchange.chronos.calendar.account.service.groupware;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.calendar.account.service.impl.CalendarAccountServiceImpl;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarParameters;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarAccountDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAccountDeleteListener implements DeleteListener {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAccountDeleteListener}.
     *
     * @param services A service lookup reference
     */
    public CalendarAccountDeleteListener(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        /*
         * check if applicable
         */
        if (DeleteEvent.TYPE_USER != event.getType() || DeleteEvent.SUBTYPE_ANONYMOUS_GUEST == event.getSubType() || DeleteEvent.SUBTYPE_INVITED_GUEST == event.getSubType()) {
            return;
        }
        /*
         * initialize calendar storage & delete accounts
         */
        SimpleDBProvider dbProvider = new SimpleDBProvider(readCon, writeCon);
        DefaultCalendarParameters parameters = new DefaultCalendarParameters();
        parameters.set(Connection.class.getName(), writeCon);
        CalendarStorage calendarStorage = requireService(CalendarStorageFactory.class, services).create(event.getContext(), -1, null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
        List<CalendarAccount> storedAccounts = calendarStorage.getAccountStorage().loadAccounts(event.getId());
        if (null == storedAccounts || storedAccounts.isEmpty()) {
            return;
        }
        CalendarProviderRegistry providerRegistry = requireService(CalendarProviderRegistry.class, services);
        for (CalendarAccount storedAccount : storedAccounts) {
            /*
             * delete account data from storage, then let provider perform any additional cleanup tasks
             */
            calendarStorage.getAccountStorage().deleteAccount(storedAccount.getUserId(), storedAccount.getAccountId(), CalendarUtils.DISTANT_FUTURE);
            CalendarProvider calendarProvider = providerRegistry.getCalendarProvider(storedAccount.getProviderId());
            if (null == calendarProvider) {
                LoggerFactory.getLogger(CalendarAccountServiceImpl.class).warn("Provider '{}' not available, skipping additional cleanup tasks for deleted account {}.",
                    storedAccount.getProviderId(), storedAccount, CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(storedAccount.getProviderId()));
            } else {
                calendarProvider.onAccountDeleted(event.getContext(), storedAccount, parameters);
            }
        }
    }

}
