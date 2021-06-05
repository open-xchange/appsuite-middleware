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

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.google.access.GoogleOAuthAccess;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link GoogleCalendarOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleCalendarOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    private static final String ROOT_FOLDER = "0";

    private final CalendarAccount calendarAccount;

    /**
     * Initialises a new {@link GoogleCalendarOAuthAccountAssociation}.
     */
    public GoogleCalendarOAuthAccountAssociation(int accountId, int userId, int contextId, CalendarAccount calendarAccount) {
        super(accountId, userId, contextId);
        this.calendarAccount = calendarAccount;
    }

    @Override
    public String getServiceId() {
        return calendarAccount.getProviderId();
    }

    @Override
    public String getId() {
        return Integer.toString(calendarAccount.getAccountId());
    }

    @Override
    public String getDisplayName() {
        return getInternalConfigProperty("name");
    }

    @Override
    public String getModule() {
        return Module.CALENDAR.getModuleName();
    }

    @Override
    public String getFolder() {
        return IDMangler.mangle("cal", getId(), ROOT_FOLDER);
    }

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        return new GoogleOAuthAccess(getOAuthAccountId(), session);
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.calendar);
    }

    /**
     * Returns the value of the specified property or an empty string
     * if no such property exists.
     * 
     * @param key The property's name
     * @return The property's value or an empty string if no such property exists
     */
    private String getInternalConfigProperty(String key) {
        if (calendarAccount.getInternalConfiguration() == null || calendarAccount.getInternalConfiguration().isEmpty()) {
            return "";
        }
        return calendarAccount.getInternalConfiguration().optString(key);
    }
}
