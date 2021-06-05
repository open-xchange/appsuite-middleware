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

package com.openexchange.mail.transport.listener;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.session.Session;


/**
 * {@link MailTransportListener} - A listener for message transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface MailTransportListener {

    /**
     * Checks if specified security settings are orderly handled.
     *
     * @param securitySettings The security settings to consider
     * @param session The associated session
     * @return <code>true</code> if handled; otherwise <code>false</code>
     * @throws OXException If check fails unexpectedly
     */
    boolean checkSettings(SecuritySettings securitySettings, Session session) throws OXException;

    /**
     * Called before a message transport takes place.
     *
     * @param message The message about to send
     * @param recipients An array of recipients
     * @param securitySettings The optional security settings to consider or <code>null</code>
     * @param session The associated session
     * @return The processing result
     * @throws OXException If processing the message fails
     */
    Result onBeforeMessageTransport(MimeMessage message, Address[] recipients, SecuritySettings securitySettings, Session session) throws OXException;

    /**
     * Called after a message transport took place.
     *
     * @param message The sent message
     * @param exception The possible exception that occurred while attempting to transport the message; otherwise <code>null</code>
     * @param session The associated session
     * @return The processing result
     * @throws OXException If processing the sent message fails
     */
    void onAfterMessageTransport(MimeMessage message, Exception exception, Session session) throws OXException;

}
