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

package com.openexchange.chronos.provider.google.oauth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleCalendarOAuthAccountAssociationProvider implements OAuthAccountAssociationProvider {

    /**
     * Initialises a new {@link GoogleCalendarOAuthAccountAssociationProvider}.
     */
    public GoogleCalendarOAuthAccountAssociationProvider() {
        super();
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        CalendarAccountService accountStorage = Services.getService(CalendarAccountService.class);
        Collection<OAuthAccountAssociation> associations = null;
        for (CalendarAccount calendarAccount : accountStorage.getAccounts(session, null)) {
            int oauthAccountId = getAccountId(calendarAccount.getInternalConfiguration());
            if (oauthAccountId != accountId) {
                continue;
            }
            if (null == associations) {
                associations = new LinkedList<>();
            }
            associations.add(new GoogleCalendarOAuthAccountAssociation(accountId, session.getUserId(), session.getContextId(), calendarAccount));
        }
        return null == associations ? Collections.<OAuthAccountAssociation> emptyList() : associations;
    }

    /**
     * Returns the OAuth account identifier from associated account's internal configuration
     *
     * @param internalConfig The internal configuration
     * @return The account identifier or <code>-1</code> if account identifier cannot be determined
     * @throws IllegalArgumentException if the account identifier is present but cannot be parsed to integer
     */
    private int getAccountId(JSONObject internalConfig) {
        if (internalConfig == null || internalConfig.isEmpty()) {
            return -1;
        }
        Object oauthId = internalConfig.opt("oauthId");
        if (oauthId == null) {
            return -1;
        }
        if (oauthId instanceof Integer) {
            return ((Integer) oauthId).intValue();
        }
        try {
            return Integer.parseInt(oauthId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The account identifier '" + oauthId.toString() + "' cannot be parsed as an integer.", e);
        }
    }
}
