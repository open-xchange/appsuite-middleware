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

package com.openexchange.config.lean.internal.parser;

import com.openexchange.java.Strings;

/**
 * {@link AbstractPropertyValueParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractPropertyValueParser {

    /**
     * Initialises a new {@link AbstractPropertyValueParser}.
     */
    public AbstractPropertyValueParser() {
        super();
    }

    /**
     * Checks if the specified value is empty or <code>null</code> and throws an {@link IllegalArgumentException}
     * 
     * @param value The value to check
     * @throws IllegalArgumentException if the specified value is either <code>null</code> or empty
     */
    void checkEmpty(String value) {
        if (Strings.isEmpty(value)) {
            throw new IllegalArgumentException("The value can be neither 'null' nor empty");
        }
    }
}
