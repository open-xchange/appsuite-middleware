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

package com.openexchange.ajax.ipcheck.spi;

import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link IPChecker} - Checks for changed IP addresses of active sessions and handles it appropriately.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface IPChecker {

    /**
     * Handles specified changed IP address.
     *
     * @param current The current/changed IP address
     * @param previous The previous IP address
     * @param session The associated session
     * @param configuration The IP check configuration to use
     * @throws OXException In case changed IP address is supposed to let session verification fail
     * @see SessionExceptionCodes#WRONG_CLIENT_IP
     */
    void handleChangedIp(String current, String previous, Session session, IPCheckConfiguration configuration) throws OXException;

    /**
     * Gets this checker's identifier
     *
     * @return The identifier
     */
    String getId();

}
