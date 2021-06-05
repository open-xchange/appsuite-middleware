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

package com.openexchange.sms.tools;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SMSBucketService} provides a user based token-bucket for sms tokens
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface SMSBucketService {

    /**
     * Retrieves the number of available sms tokens for the given user and reduce the amount by one.
     * 
     * @param session The user session
     * @return The previous amount of sms tokens
     * @throws OXException if it was unable to retrieve the sms token or if the sms limit is reached
     */
    public int getSMSToken(Session session) throws OXException;

    /**
     * Checks if the user sms limit is enabled for the given user
     * @param session The user session
     * @return true if SMSUserLimit is enabled, false otherwise
     * @throws OXException
     */
    public boolean isEnabled(Session session) throws OXException;

    /**
     * Retrieves the refresh interval in hours rounded up
     * 
     * @param session The user session
     * @return The time in hours rounded up
     * @throws OXException if it was unable to retrieve the interval
     */
    public int getRefreshInterval(Session session) throws OXException;
}
