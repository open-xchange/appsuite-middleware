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

package com.openexchange.log.audit.slf4j;

import java.util.Date;
import com.openexchange.java.ISO8601Utils;

/**
 * {@link ISO8601DateFormatter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ISO8601DateFormatter implements DateFormatter {

    private static final ISO8601DateFormatter INSTANCE = new ISO8601DateFormatter();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ISO8601DateFormatter getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ISO8601DateFormatter}.
     */
    private ISO8601DateFormatter() {
        super();
    }

    @Override
    public String format(Date date) {
        return ISO8601Utils.format(date, false);
    }

}
