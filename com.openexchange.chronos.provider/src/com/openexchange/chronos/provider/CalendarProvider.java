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

package com.openexchange.chronos.provider;

import java.util.EnumSet;
import java.util.Locale;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link CalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarProvider {

    /**
     * Gets the identifier of the calendar provider.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the provider's display name.
     *
     * @param locale The current locale to get the display name in
     * @return The display name
     */
    String getDisplayName(Locale locale);

    /**
     * Gets the <b>default</b> maximum number of allowed accounts within this calendar provider.
     * <p/>
     * <i>Note: The value may still be overridden by the corresponding configuration property.</i>
     *
     * @return The default maximum number of accounts, or <code>0</code> if not restricted
     * @see CalendarProviders#getMaxAccountsPropertyName(CalendarProvider)
     */
    int getDefaultMaxAccounts();

    /**
     * Gets the supported capabilities for a calendar access of this calendar provider, describing the usable extended feature set.
     *
     * @return The supported calendar capabilities, or an empty set if no extended functionality is available
     */
    EnumSet<CalendarCapability> getCapabilities();

    /**
     * Callback routine that is invoked after a new account for the calendar provider has been created.
     *
     * @param session The user's session
     * @param account The calendar account that was created
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been updated.
     *
     * @param session The user's session
     * @param account The calendar account that was updated
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been deleted.
     *
     * @param session The user's session
     * @param account The calendar account that was deleted
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the calendar provider has been deleted (and the user's session is
     * not available).
     *
     * @param context The context
     * @param account The calendar account that was deleted
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Initializes the connection to a specific calendar account.
     *
     * @param session The user's session
     * @param account The calendar account to connect to
     * @param parameters Additional calendar parameters
     * @return The connected calendar access
     */
    CalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Checks whether the provider is available for the given session.
     * <p/>
     * This method is invoked from the capability checker for calendar providers, and the general capability check for this calendar
     * provider is already done at that stage.
     * <p/>
     * Returns <code>true</code> by default, and may be overridden to perform additional, provider-specific checks.
     *
     * @param session The session
     * @return <code>true</code> if it is available, <code>false</code> otherwise
     */
    default boolean isAvailable(Session session) {
        return true;
    }

}
