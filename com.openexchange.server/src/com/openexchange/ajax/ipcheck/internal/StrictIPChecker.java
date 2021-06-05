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

package com.openexchange.ajax.ipcheck.internal;

import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.ajax.ipcheck.IPCheckers;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link StrictIPChecker} - The strict IP checker; IP addresses need to be equal in order to allow further session validation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class StrictIPChecker implements IPChecker {

    private static final StrictIPChecker INSTANCE = new StrictIPChecker();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static StrictIPChecker getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link StrictIPChecker}.
     */
    private StrictIPChecker() {
        super();
    }

    @Override
    public String getId() {
        return BuiltInChecker.STRICT.getId();
    }

    @Override
    public void handleChangedIp(String current, String previous, Session session, IPCheckConfiguration configuration) throws OXException {
        boolean whiteListedClient = IPCheckers.isWhitelistedClient(session, configuration);
        if (!whiteListedClient && !IPCheckers.isWhiteListedAddress(current, previous, configuration)) {
            IPCheckers.kick(current, session);
        }

        IPCheckers.updateIPAddress(current, session, whiteListedClient);
    }

}
