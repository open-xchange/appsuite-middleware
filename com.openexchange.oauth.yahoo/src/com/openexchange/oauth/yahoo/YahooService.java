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

package com.openexchange.oauth.yahoo;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link YahooService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> (Javadoc)
 */
public interface YahooService {

    /**
     * Retrieves a list with OX {@link Contact} objects from the Yahoo! provider
     * 
     * @param session The groupware {@link Session}
     * @param user The user identifier
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @return A {@link List} with {@link Contact} objects
     * @throws OXException if the contacts cannot be retrieved
     */
    List<Contact> getContacts(Session session, int user, int contextId, int accountId) throws OXException;

    /**
     * Gets the OX display name of the specified Yahoo! account
     * 
     * @param session The groupware {@link Session}
     * @param user The user identifier
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @return The display name of the Yahoo! account
     */
    String getAccountDisplayName(Session session, int user, int contextId, int accountId);

}
