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

package com.openexchange.mail.json.compose;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;

/**
 * {@link ComposeTransportResult} - The result for transporting one or more messages plus providing the message representation that is
 * supposed to be saved into standard Sent folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface ComposeTransportResult {

    /**
     * Gets the messages that shall be transported.
     *
     * @return The messages to transport
     */
    List<? extends ComposedMailMessage> getTransportMessages();

    /**
     * Gets the message representation that is supposed to be saved into standard Sent folder.
     *
     * @return The sent message
     */
    ComposedMailMessage getSentMessage();

    /**
     * Signals whether first transport message is equal to the message representation that is supposed to be saved into standard Sent folder.
     *
     * @return <code>true</code> if transport is equal to sent version; otherwise <code>false</code>
     */
    boolean isTransportEqualToSent();

    /**
     * Commits this transport result to signal successful execution
     *
     * @throws OXException If commit fails
     */
    void commit() throws OXException;

    /**
     * Rolls-back this transport result to signal failed execution
     *
     * @throws OXException If roll-back fails
     */
    void rollback() throws OXException;

    /**
     * Finishes this transport result to clear up any used resources.
     *
     * @throws OXException If finishing fails
     */
    void finish() throws OXException;

}
