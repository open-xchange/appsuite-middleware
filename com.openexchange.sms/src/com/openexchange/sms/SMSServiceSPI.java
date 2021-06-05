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

package com.openexchange.sms;

import com.openexchange.exception.OXException;

/**
 * {@link SMSServiceSPI} - The SMS service provider interface, which is called to send an SMS to given phone numbers on behalf of a certain user.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added <code>userId</code> and <code>contextId</code> parameters to {@link #sendMessage(String[], String, int, int) sendMessage()}
 * @since v7.8.1
 */
public interface SMSServiceSPI {

    /**
     * Send an SMS message to multiple recipients.<br>
     * Use {@link com.openexchange.sms.SMSExceptionCode} in case of errors.
     *
     * @param recipients An array contains recipients' phone numbers in E.123 format, e.g. <code>"+49 123 4567890"</code>
     * @param message The message to send
     * @param userId The identifier of the user on whose behalf the SMS is supposed to be sent
     * @param contextId The identifier of the context in which the user resides
     * @throws OXException If SMS cannot be sent
     */
    void sendMessage(String[] recipients, String message, int userId, int contextId) throws OXException;

}
