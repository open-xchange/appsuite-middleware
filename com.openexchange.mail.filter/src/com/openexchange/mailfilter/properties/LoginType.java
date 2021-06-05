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

package com.openexchange.mailfilter.properties;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;

/**
 * {@link LoginType}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum LoginType {
    /**
     * Use the sieve server given in this config file for all users
     */
    GLOBAL("global"),
    /**
     * Use the imap server setting stored for user in the database
     */
    USER("user");

    private static final Map<String, LoginType> MAP;
    static {
        ImmutableMap.Builder<String, LoginType> b = ImmutableMap.builder();
        for (LoginType loginType : LoginType.values()) {
            b.put(loginType.name, loginType);
        }
        MAP = b.build();
    }

    public final String name;

    /**
     * Initialises a new {@link LoginType}.
     * 
     * @param name The login type name
     */
    private LoginType(final String name) {
        this.name = name;
    }

    /**
     * The name of the {@link LoginType}
     * 
     * @param name The name of the {@link LoginType} as string
     * @return The {@link LoginType}
     * @throws OXException if an invalid {@link LoginType} is requested
     */
    public static LoginType loginTypeFor(String name) throws OXException {
        LoginType loginType = MAP.get(name);
        if (loginType == null) {
            throw MailFilterExceptionCode.NO_VALID_LOGIN_TYPE.create();
        }
        return loginType;
    }
}
