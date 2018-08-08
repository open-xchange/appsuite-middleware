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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

    private final CalendarAccount calendarAccount;

    /**
     * Initialises a new {@link GoogleCalendarOAuthAccountAssociation}.
     */
    public GoogleCalendarOAuthAccountAssociation(int accountId, int userId, int contextId, CalendarAccount calendarAccount) {
        super(accountId, userId, contextId);
        this.calendarAccount = calendarAccount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getServiceId()
     */
    @Override
    public String getServiceId() {
        return calendarAccount.getProviderId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getId()
     */
    @Override
    public String getId() {
        return Integer.toString(calendarAccount.getAccountId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return getInternalConfigProperty("name");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getModule()
     */
    @Override
    public String getModule() {
        return Module.CALENDAR.getModuleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#optFolder()
     */
    @Override
    public String getFolder() {
        return IDMangler.mangle("cal", getId(), "0"); // FIXME: constant for root folder?
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#newAccess(com.openexchange.session.Session)
     */
    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        return new GoogleOAuthAccess(getOAuthAccountId(), session);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getScopes()
     */
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
