/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
