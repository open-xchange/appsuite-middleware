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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.groupware.ldap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user attribute with possible multiple values and their unique identifiers in form of a UUID.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserAttribute {

    private final String name;
    private final Set<AttributeValue> values;

    public UserAttribute(String name) {
        super();
        this.name = name;
        values = new HashSet<AttributeValue>();
    }

    public UserAttribute(String name, Set<String> values) {
        this(name);
        for (String value : values) {
            addValue(value);
        }
    }

    public String getName() {
        return name;
    }

    public Set<AttributeValue> getValues() {
        return values;
    }

    public Set<String> getStringValues() {
        Set<String> retval = new HashSet<String>();
        for (AttributeValue value : values) {
            retval.add(value.getValue());
        }
        return Collections.unmodifiableSet(retval);
    }

    AttributeValue getValue(String value) {
        for (AttributeValue tmp : values) {
            if (tmp.getValue().equals(value)) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Adds specified value aside with its unique identifier.
     * @param value the value
     * @param uuid the associated UUID
     * @return <code>true</code> if successfully added; otherwise <code>false</code> if already present
     */
    public boolean addValue(AttributeValue value) {
        return values.add(value);
    }

    void addValue(String value) {
        values.add(new AttributeValue(value));
    }

    /**
     * Gets the number of attribute values.
     * @return the number of values.
     */
    public int size() {
        return values.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserAttribute)) {
            return false;
        }
        final UserAttribute other = (UserAttribute) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append(name).append("=").append("[");
        for (AttributeValue value : values) {
            builder.append(value.toString()).append(',');
        }
        if (values.size() > 0) {
            builder.setCharAt(builder.length() - 1, ']');
        } else {
            builder.append("]");
        }
        return builder.toString();
    }
}
