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
 * {@link ValuePairObject} - The class representing a name-value-pair; meaning its {@link #getType()} method returns
 * {@link AbstractValue#VALUE_PAIR}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ValuePairObject extends AbstractValue {

    protected String name;

    /**
     * Initializes a new {@link ValuePairObject}.
     *
     * @param name The name
     * @param value The value
     */
    ValuePairObject(final String name, final String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * Gets this value's name.
     *
     * @return The value's name
     */
    public String getName() {
        return name;
    }

    @Override
    protected int getType() {
        return VALUE_PAIR;
    }
}
