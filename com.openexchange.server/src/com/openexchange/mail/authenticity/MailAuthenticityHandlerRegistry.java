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

package com.openexchange.mail.authenticity;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityHandlerRegistry} - A registry service for {@link MailAuthenticityHandler authenticity handlers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
@SingletonService
public interface MailAuthenticityHandlerRegistry {

    /**
     * Tests if mail authenticity verification is <b>not</b> enabled for session-associated user
     *
     * @param session The user's session
     * @return <code>true</code> if disabled; otherwise <code>false</code> if enabled
     * @throws OXException If test fails
     */
    boolean isNotEnabledFor(Session session) throws OXException;

    /**
     * Tests if mail authenticity verification is enabled for session-associated user
     *
     * @param session The user's session
     * @return <code>true</code> if enabled; otherwise <code>false</code> if disabled
     * @throws OXExceptionIf test fails
     */
    boolean isEnabledFor(Session session) throws OXException;

    /**
     * Gets the date threshold (the number of milliseconds since January 1, 1970, 00:00:00 GMT) that defines which messages to consider.
     * <p>
     * Only such messages shall be considered whose received date is equal to or greater than date threshold.
     *
     * @param session The user's session
     * @return The date threshold or <code>0</code> (zero)
     * @throws OXException
     */
    long getDateThreshold(Session session) throws OXException;

    /**
     * Gets the highest-ranked handler for session-associated user.
     *
     * @param session The user's session
     * @return The highest-ranked handler or <code>null</code>
     * @throws OXException If sorted listing cannot be returned
     */
    MailAuthenticityHandler getHighestRankedHandlerFor(Session session) throws OXException;

    /**
     * Gets all currently available handlers.
     *
     * @return All currently available handlers
     * @throws OXException If handlers cannot be returned
     */
    Collection<MailAuthenticityHandler> getHandlers() throws OXException;

}
