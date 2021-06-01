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
