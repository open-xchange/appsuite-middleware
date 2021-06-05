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

package com.openexchange.chronos.provider.google.access;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.exception.OXException;

/**
 * {@link OAuthAccountDeleteListener}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class OAuthAccountDeleteListener implements com.openexchange.oauth.OAuthAccountDeleteListener {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccountDeleteListener.class);

    /**
     * Initialises a new {@link OAuthAccountDeleteListener}.
     */
    public OAuthAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) {
        // nothing to do
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        AdministrativeCalendarAccountService administrativeCalendarAccountService = requireService(AdministrativeCalendarAccountService.class, Services.getServiceLookup());
        List<CalendarAccount> allAccounts = administrativeCalendarAccountService.getAccounts(cid, user, GoogleCalendarProvider.PROVIDER_ID);

        List<CalendarAccount> accountsToDelete = new ArrayList<>(allAccounts.size());
        for (CalendarAccount acc : allAccounts) {
            try {
                if (id == acc.getUserConfiguration().getInt(GoogleCalendarConfigField.OAUTH_ID)) {
                    accountsToDelete.add(acc);
                }
            } catch (JSONException e) {
                LOG.warn("Unable to check google calendar account with id %s for user %s in context %s: %s", I(acc.getAccountId()), I(user), I(cid), e.getMessage());
            }
        }

        administrativeCalendarAccountService.deleteAccounts(cid, user, accountsToDelete);

    }
}
