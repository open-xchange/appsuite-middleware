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

package com.openexchange.control.consoleold.internal;


/**
 * {@link AbstractValue} - Abstract super class for parsed values.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class AbstractValue {

    /**
     * Type to indicate this value is associated to a name.
     */
    public static final int VALUE_PAIR = 1;

    /**
     * Type to indicate a sole value.
     */
    public static final int VALUE = 2;

    protected String value;

    /**
     * Initializes a new {@link AbstractValue}.
     */
    protected AbstractValue() {
        super();
    }

    /**
     * Gets this value's type; either {@link #VALUE_PAIR} or {@link #VALUE}.
     *
     * @return The value's type.
     */
    protected abstract int getType();

    /**
     * Gets the value as a string.
     *
     * @return The value as a string.
     */
    public final String getValue() {
        return value;
    }

}
