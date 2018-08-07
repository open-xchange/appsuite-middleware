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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider#getAssociationsFor(int, com.openexchange.session.Session)
     */
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
