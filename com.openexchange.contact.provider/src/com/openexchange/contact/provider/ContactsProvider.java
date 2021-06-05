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

package com.openexchange.contact.provider;

import java.util.EnumSet;
import java.util.Locale;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link ContactsProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface ContactsProvider {

    /**
     * Gets the identifier of the contact provider.
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
     * Gets the supported capabilities for a contacts access
     * of this contacts provider, describing the usable extended feature set.
     *
     * @return The supported contacts capabilities, or an empty set if
     *         no extended functionality is available
     */
    EnumSet<ContactsAccessCapability> getCapabilities();

    /**
     * Initialises the connection to a specific contact account.
     *
     * @param session The user's session
     * @param account The contact account to connect to
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The connected contact access
     */
    ContactsAccess connect(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the contacts provider has been deleted.
     *
     * @param session The user's session
     * @param account The contacts account that was deleted
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the contacts provider has been deleted (and the user's session is
     * not available).
     *
     * @param context The context
     * @param account The contacts account that was deleted
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Context context, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Checks whether the provider is available for the given session.
     * <p/>
     * This method is invoked from the capability checker for contact providers, and the general capability check for this contact
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
