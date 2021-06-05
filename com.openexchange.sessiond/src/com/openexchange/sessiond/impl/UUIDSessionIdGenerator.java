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

package com.openexchange.sessiond.impl;

import java.util.UUID;
import com.openexchange.java.util.UUIDs;

/**
 * {@link UUIDSessionIdGenerator} - The session ID generator based on {@link UUID#randomUUID()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UUIDSessionIdGenerator extends SessionIdGenerator {

    private static final UUIDSessionIdGenerator INSTANCE = new UUIDSessionIdGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static UUIDSessionIdGenerator getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link UUIDSessionIdGenerator}
     */
    private UUIDSessionIdGenerator() {
        super();
    }

    @Override
    public String createSessionId(final String loginName) {
        return randomUUID();
    }

    @Override
    public String createSecretId(final String loginName) {
        return randomUUID();
    }

    @Override
    public String createRandomId() {
        return randomUUID();
    }

    /**
     * Generates a UUID using {@link UUID#randomUUID()} and removes all dashes; e.g.:<br>
     * <i>a5aa65cb-6c7e-4089-9ce2-b107d21b9d15</i> would be <i>a5aa65cb6c7e40899ce2b107d21b9d15</i>
     *
     * @return A UUID string
     */
    public static String randomUUID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }
}
