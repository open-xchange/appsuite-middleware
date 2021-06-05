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

import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FallbackAwareContactsProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public interface FallbackAwareContactsProvider extends ContactsProvider {

    /**
     * Initializes a <i>fallback</i> {@link ContactsAccess} for a specific contacts account to be used as placeholder after the regular
     * access could not be established due to an error.
     *
     * @param session The user's session
     * @param account The contacts account to connect to
     * @param parameters Additional contacts parameters
     * @param error The error to include in the accesses' contact settings or folders, or <code>null</code> if not defined
     * @return A fallback contacts access for the calendar account
     */
    ContactsAccess connectFallback(Session session, ContactsAccount account, ContactsParameters parameters, OXException error);

}
