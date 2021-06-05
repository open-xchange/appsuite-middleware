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

package com.openexchange.imap.protection;

import com.openexchange.exception.OXException;
import com.openexchange.imap.protection.impl.ConfigUsingIMAPSelfProtection;
import com.openexchange.session.Session;

/**
 * {@link IMAPSelfProtectionFactory} - The factory for IMAP self-protection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class IMAPSelfProtectionFactory {

    private static final IMAPSelfProtectionFactory INSTANCE = new IMAPSelfProtectionFactory();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static IMAPSelfProtectionFactory getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link IMAPSelfProtectionFactory}.
     */
    private IMAPSelfProtectionFactory() {
        super();
    }

    /**
     * Creates the IMAP self-protection for session-associated user.
     *
     * @param session The session providing user information
     * @return The IMAP self-protection
     * @throws OXException If IMAP self-protection cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public IMAPSelfProtection createSelfProtectionFor(Session session) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }

        return createSelfProtectionFor(session.getUserId(), session.getContextId());
    }

    /**
     * Creates the IMAP self-protection for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The IMAP self-protection
     * @throws OXException If IMAP self-protection cannot be returned
     */
    public IMAPSelfProtection createSelfProtectionFor(int userId, int contextId) throws OXException {
        return new ConfigUsingIMAPSelfProtection(userId, contextId);
    }

}
