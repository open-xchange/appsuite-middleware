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

package com.openexchange.html.whitelist;


/**
 * {@link AbstractNamedObject} - Represents a named object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractNamedObject {

    private final String name;
    private final int hash;

    /**
     * Initializes a new {@link AbstractNamedObject}.
     *
     * @param name The name for this object
     */
    public AbstractNamedObject(String name) {
        super();
        if (null == name) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.name = name;
        int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        hash = result;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractNamedObject other = (AbstractNamedObject) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Compares this {@code AbstractNamedObject} to another {@code AbstractNamedObject}, ignoring case considerations.
     * <p>
     * Two named objects are considered equal ignoring case if they are of the same length and corresponding characters in the two objects are equal ignoring case.
     * <p>
     * Two characters {@code c1} and {@code c2} are considered the same
     * ignoring case if at least one of the following is true:
     * <ul>
     *   <li> The two characters are the same (as compared by the
     *        {@code ==} operator)
     *   <li> Applying the method {@link
     *        java.lang.Character#toUpperCase(char)} to each character
     *        produces the same result
     *   <li> Applying the method {@link
     *        java.lang.Character#toLowerCase(char)} to each character
     *        produces the same result
     * </ul>
     *
     * @param  anotherNamedObject
     *         The {@code AbstractNamedObject} to compare this {@code AbstractNamedObject} against
     *
     * @return  {@code true} if the argument is not {@code null} and it
     *          represents an equivalent {@code AbstractNamedObject} ignoring case; {@code
     *          false} otherwise
     *
     * @see  #equals(Object)
     */
    public boolean equalsIgnoreCase(AbstractNamedObject anotherNamedObject) {
        if (this == anotherNamedObject) {
            return true;
        }
        if (anotherNamedObject == null) {
            return false;
        }
        if (name == null) {
            if (anotherNamedObject.name != null) {
                return false;
            }
        } else if (!name.equalsIgnoreCase(anotherNamedObject.name)) {
            return false;
        }
        return true;
    }

}
