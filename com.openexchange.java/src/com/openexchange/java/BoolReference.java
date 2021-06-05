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

package com.openexchange.java;

import javax.annotation.concurrent.NotThreadSafe;


/**
 * {@link BoolReference} - A simple reference class for a <code>boolean</code> value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@NotThreadSafe
public final class BoolReference {

    /** The value */
    private boolean value;

    /**
     * Initializes a new {@link BoolReference} with <code>false</code> boolean value.
     */
    public BoolReference() {
        this(false);
    }

    /**
     * Initializes a new {@link BoolReference}.
     *
     * @param value The value to set
     */
    public BoolReference(boolean value) {
        super();
        this.value = value;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Sets the value
     *
     * @param value The value to set
     * @return This reference with new value applied
     */
    public BoolReference setValue(boolean value) {
        this.value = value;
        return this;
    }

}
