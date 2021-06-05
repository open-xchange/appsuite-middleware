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
 * {@link PasswordSource}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum PasswordSource {
    /**
     * The user's individual system's password is taken
     */
    SESSION("session"),
    /**
     * The the value specified through property {@link MailFilterProperty#masterPassword} is taken.
     */
    GLOBAL("global");

    private static final Map<String, PasswordSource> MAP;
    static {
        ImmutableMap.Builder<String, PasswordSource> b = ImmutableMap.builder();
        for (PasswordSource pwSrc : PasswordSource.values()) {
            b.put(pwSrc.name, pwSrc);
        }
        MAP = b.build();
    }

    public final String name;

    /**
     * Initialises a new {@link PasswordSource}.
     * 
     * @param name
     */
    private PasswordSource(final String name) {
        this.name = name;
    }

    /**
     * The name of the {@link PasswordSource}
     * 
     * @param name The name of the {@link PasswordSource} as string
     * @return The {@link PasswordSource}
     * @throws OXException if an invalid {@link PasswordSource} is requested
     */
    public static PasswordSource passwordSourceFor(String name) throws OXException {
        PasswordSource passwordSource = MAP.get(name);
        if (passwordSource == null) {
            throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }
        return passwordSource;
    }
}
