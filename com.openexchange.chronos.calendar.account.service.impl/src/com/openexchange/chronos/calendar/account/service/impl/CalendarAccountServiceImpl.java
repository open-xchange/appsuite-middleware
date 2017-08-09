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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAccountServiceImpl}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class CalendarAccountServiceImpl implements CalendarAccountService {

    //Maybe put the default account in a central position
    /** The default <i>internal</i> calendar provider account */
    private static final CalendarAccount DEFAULT_ACCOUNT = new DefaultCalendarAccount("chronos", 0, 0, Collections.<String, Object> emptyMap(), null);

    private final CalendarAccountStorage storage;
    //Maybe use session instead
    private final int userId;

    /**
     * Initializes a new {@link CalendarAccountServiceImpl}.
     * @param calendarAccountStorage The calendar account storage
     * @param userId The userId of the sessions user
     */
    public CalendarAccountServiceImpl(CalendarAccountStorage calendarAccountStorage, int userId) {
        super();
        this.storage = calendarAccountStorage;
        this.userId = userId;
    }

    @Override
    public int insertAccount(String providerId, int userId, Map<String, Object> configuration) throws OXException {
        return storage.insertAccount(providerId, userId, configuration);
    }

    @Override
    public void updateAccount(int id, Map<String, Object> configuration, long timestamp) throws OXException {
        if (DEFAULT_ACCOUNT.getAccountId() == id) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id));
        } else if (accountPermissionCheck(id)) {
            storage.updateAccount(id, configuration, timestamp);
        } else {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id));
        }
    }

    @Override
    public void deleteAccount(int id) throws OXException {
        if (DEFAULT_ACCOUNT.getAccountId() == id) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id));
        } else if (accountPermissionCheck(id)) {
            storage.deleteAccount(id);
        } else {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id));
        }
    }

    @Override
    public CalendarAccount loadAccount(int id) throws OXException {
        if (DEFAULT_ACCOUNT.getAccountId() == id) {
            return DEFAULT_ACCOUNT;
        } else if (accountPermissionCheck(id)) {
            return storage.loadAccount(id);
        } else {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(id));
        }
    }

    @Override
    public List<CalendarAccount> loadAccounts(int userId) throws OXException {
        if (this.userId == userId) {
            List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
            accounts.add(DEFAULT_ACCOUNT);
            accounts.addAll(storage.loadAccounts(userId));
            return accounts;
        } else {
            throw CalendarExceptionCodes.INSUFFICIENT_ACCOUNT_PERMISSIONS.create();
        }
    }

    private boolean accountPermissionCheck(int accId) throws OXException {
        CalendarAccount account = storage.loadAccount(accId);
        if (null == account || userId != account.getUserId()) {
            return false;
        }
        return true;
    }

}
