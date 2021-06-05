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

package com.openexchange.websockets.grizzly;

import org.apache.commons.lang.StringUtils;

/**
 * {@link GrizzlyWebSocketUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GrizzlyWebSocketUtils {

    /**
     * Initializes a new {@link GrizzlyWebSocketUtils}.
     */
    private GrizzlyWebSocketUtils() {
        super();
    }

    /**
     * Yields an object whose toString() returns an abbreviation of specified message.
     *
     * @param message The message to abbreviate
     * @return The encapsulating object
     */
    public static Object abbreviateMessageArg(String message) {
        return new AbbreviatedMessage(message, 24);
    }

    // ----------------------------------------------------------------------------------------------------

    private static final class AbbreviatedMessage {

        private final String message;
        private final int maxWidth;

        AbbreviatedMessage(String message, int maxWidth) {
            super();
            this.message = message;
            this.maxWidth = maxWidth;
        }

        @Override
        public String toString() {
            return StringUtils.abbreviate(message, maxWidth);
        }
    }

}
