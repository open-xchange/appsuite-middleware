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

package com.openexchange.pns;

import com.openexchange.exception.OXException;

/**
 * {@link PushMessageGenerator} - Generates a message for a specific client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushMessageGenerator {

    /**
     * Gets the identifier of the client this generator is bound to.
     *
     * @return The client identifier
     */
    String getClient();

    /**
     * Generates the message suitable for given transport.
     *
     * @param transportId The identifier of the transport, with which the message is supposed to be published
     * @param notification The notification from which to create the message
     * @return The message
     * @throws OXException If generating the message fails
     */
    Message<?> generateMessageFor(String transportId, PushNotification notification) throws OXException;

}
