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

package com.openexchange.chronos.provider.composition.impl;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 *
 * {@link CompositingIDBasedCalendarQuotaProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CompositingIDBasedCalendarQuotaProvider extends AbstractCompositingIDBasedCalendarAccess {

    /**
     * Initializes a new {@link CompositingIDBasedCalendarQuotaProvider}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     * @throws OXException If context can not be resolved
     */
    public CompositingIDBasedCalendarQuotaProvider(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super(session, providerRegistry, services);
    }

    public String getModuleID() {
        return "calendar";
    }

    public String getDisplayName() {
        return "Calendar";
    }

    public AccountQuota get(String accountID) throws OXException {
        try {
            // Get account
            int accountId = Integer.parseInt(accountID);
            CalendarAccount account = getAccount(accountId);
            return getAccountQuota(account, getAccess(account, QuotaAware.class).getQuotas());
        } catch (NumberFormatException e) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(e, accountID, getModuleID());
        }
    }

    public List<AccountQuota> get() throws OXException {
        // Get accounts
        List<AccountQuota> accountQuotas = new LinkedList<>();
        for (CalendarAccount account : getAccounts(QuotaAware.class)) {
            accountQuotas.add(getAccountQuota(account, getAccess(account, QuotaAware.class).getQuotas()));
        }
        return accountQuotas;
    }

    /**
     * Get the {@link AccountQuota} for the {@link CalendarAccount}
     *
     * @param account The account to get the quota for
     * @param quotas The account's quotas
     * @return The {@link AccountQuota}
     * @throws OXException In case the access is denied or quota is not available
     */
    private AccountQuota getAccountQuota(CalendarAccount account, Quota[] quotas) {
        DefaultAccountQuota accountQuota = new DefaultAccountQuota(String.valueOf(account.getAccountId()), getProviderName(account));
        if (null != quotas && 0 < quotas.length) {
            for (Quota quota : quotas) {
                accountQuota.addQuota(quota);
            }
        }
        return accountQuota;
    }

}
