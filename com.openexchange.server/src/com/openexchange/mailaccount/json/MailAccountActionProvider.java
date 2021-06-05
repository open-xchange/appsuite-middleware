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

package com.openexchange.mailaccount.json;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Password;
import com.openexchange.session.Session;

/**
 * {@link MailAccountActionProvider} - A provider for actions of the mail account JSON interface.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface MailAccountActionProvider {

    /**
     * Checks if this provider is applicable to session-associated user.
     *
     * @param session The session providing user data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If check for applicability fails unexpectedly
     */
    boolean isApplicableFor(Session session) throws OXException;

    /**
     * Retrieves the appropriate <code>AJAXActionService</code> instance associated with given action identifier.
     *
     * @return The associated action or <code>null</code>
     */
    AJAXActionService getAction(String action);

    /**
     * Gets the password from specified account.
     *
     * @param id The account identifier
     * @param session The associated session
     * @return The password
     * @throws OXException If such a password cannot be returned
     */
    Password getPassword(String id, Session session) throws OXException;

}
