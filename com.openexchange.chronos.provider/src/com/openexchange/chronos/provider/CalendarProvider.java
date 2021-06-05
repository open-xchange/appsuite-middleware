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

package com.openexchange.chronos.provider;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
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
     * Gets a value indicating whether the calendar provider is enabled by <b>default</b> or not.
     * <p/>
     * <i>Note: The value may still be overridden by the corresponding configuration property.</i>
     * <p/>
     * Returns <code>true</code> by default, override if applicable.
     *
     * @return <code>true</code> if the provider is enabled by default, <code>false</code>, otherwise
     * @see CalendarProviders#getEnabledPropertyName(CalendarProvider)
     */
    default boolean getDefaultEnabled() {
        return true;
    }

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

    /**
     * Gets a collection of <i>secret</i> properties (as used as keys within the account's <i>user</i> configuration object). Each key
     * indicated here will lead to the associated value being encrypted automatically when account data of the provider is stored, and
     * decrypted automatically when account data of the provider is loaded again from the storage.
     * <p/>
     * Returns an ampty set by default, override as needed.
     *
     * @return The <i>secret</i> properties in account configurations of this calendar provider, or an empty set if there are none
     */
    default Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

}
