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

package com.openexchange.chronos.calendar.account.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarAccountStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CalendarAccountServiceImpl}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class CalendarAccountServiceImpl implements CalendarAccountService {

    private final ServiceLookup serviceLookup;
    /**
     * Initializes a new {@link CalendarAccountServiceImpl}.
     */
    public CalendarAccountServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public CalendarAccount createAccount(Session session, String providerId, Map<String, Object> configuration) throws OXException {
        return loadCalendarAccount(getCalendarAccountStorage(session).createAccount(providerId, session.getUserId(), configuration), session);
    }

    @Override
    public CalendarAccount updateAccount(Session session, int id, Map<String, Object> configuration, long timestamp) throws OXException {
        verifyAccountAction(session, loadCalendarAccount(id, session), timestamp, false);
        getCalendarAccountStorage(session).updateAccount(id, configuration, timestamp);
        return loadCalendarAccount(id, session);
    }

    @Override
    public void deleteAccount(Session session, int id, long timestamp) throws OXException {
        verifyAccountAction(session, loadCalendarAccount(id, session), timestamp, false);
        getCalendarAccountStorage(session).deleteAccount(id);
    }

    @Override
    public CalendarAccount getAccount(Session session, int id) throws OXException {
        if (CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == id) {
            return CalendarAccount.DEFAULT_ACCOUNT;
        }
        return verifyAccountAction(session, loadCalendarAccount(id, session), true);
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        accounts.addAll(getCalendarAccountStorage(session).getAccounts(session.getUserId()));
        accounts.add(CalendarAccount.DEFAULT_ACCOUNT);

        {
            //            createAccount(session, "birthdays", null);
        }

        return accounts;
    }

    @Override
    public List<CalendarAccount> getAccounts(Session session, String providerId) throws OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        if (CalendarAccount.DEFAULT_ACCOUNT.getProviderId().equals(providerId)) {
            accounts.add(CalendarAccount.DEFAULT_ACCOUNT);
        } else {
            accounts.addAll(getCalendarAccountStorage(session).getAccounts(session.getUserId(), providerId));
        }
        return accounts;
    }

    private CalendarAccount verifyAccountAction(Session session, CalendarAccount account, boolean hasDefaultAccountRights) throws OXException {
        return verifyAccountAction(session, account, null, hasDefaultAccountRights);
    }

    private CalendarAccount verifyAccountAction(Session session, CalendarAccount account, Long timestamp, boolean hasDefaultAccountRights) throws OXException {
        if (null == account || session.getUserId() != account.getUserId()) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(account.getAccountId()));
        } else if (CalendarAccount.DEFAULT_ACCOUNT.getAccountId() == account.getAccountId() && !hasDefaultAccountRights) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } else if (null != account.getLastModified() && null != timestamp && account.getLastModified().getTime() > timestamp) {
            throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(account.getAccountId()), timestamp, account.getLastModified().getTime());
        } else {
            return account;
        }
    }

    private CalendarAccount  loadCalendarAccount(int id, Session session) throws OXException {
        return getCalendarAccountStorage(session).getAccount(id);
    }

    private CalendarAccountStorage getCalendarAccountStorage(Session session) throws OXException {
        CalendarAccountStorageFactory storageFactory = serviceLookup.getOptionalService(CalendarAccountStorageFactory.class);
        if (null == storageFactory) {
            throw ServiceExceptionCode.absentService(CalendarAccountStorageFactory.class);
        }
        return storageFactory.create(getContext(session.getContextId()));
    }

    private Context getContext(int contextId) throws OXException {
        return serviceLookup.getService(ContextService.class).getContext(contextId);
    }

}
