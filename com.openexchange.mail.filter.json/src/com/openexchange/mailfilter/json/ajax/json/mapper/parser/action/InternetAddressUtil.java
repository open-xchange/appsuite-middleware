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

package com.openexchange.mailfilter.json.ajax.json.mapper.parser.action;

import javax.mail.internet.AddressException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link InternetAddressUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class InternetAddressUtil {

    /**
     * Validates the specified address
     * 
     * @param address The Internet address to validate
     * @param strict Whether or not to use strict mode
     * @throws AddressException If parsing the address fails
     */
    @SuppressWarnings("unused")
    static void validateInternetAddress(String address, boolean strict) throws AddressException {
        if (Strings.isEmpty(address)) {
            throw new AddressException("The address can neither be empty nor 'null'");
        }
        new QuotedInternetAddress(address, true);
    }
}
