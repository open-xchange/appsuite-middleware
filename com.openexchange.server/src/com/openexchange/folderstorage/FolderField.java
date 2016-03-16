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

package com.openexchange.folderstorage;

import java.io.Serializable;

/**
 * {@link FolderField} - A pair of a field and its name.
 * <p>
 * Equality is only determined by field value, not its name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderField implements Serializable {

    private static final long serialVersionUID = 3017091379073715144L;

    private final int field;
    private final String name;
    private final Object defaultValue;

    /**
     * Initializes a new {@link FolderField}.
     *
     * @param field The field number
     * @param name The field name
     * @param defaulValue The default value if property is missing
     */
    public FolderField(final int field, final String name, final Object defaulValue) {
        super();
        this.field = field;
        this.name = name;
        this.defaultValue = defaulValue;
    }

    /**
     * Gets the default value if associated property is missing
     *
     * @return The default value for this field
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the field.
     *
     * @return The field
     */
    public int getField() {
        return field;
    }

    /**
     * Gets the name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return field;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FolderField)) {
            return false;
        }
        final FolderField other = (FolderField) obj;
        if (field != other.field) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(48);
        builder.append("FieldNamePair [field=").append(field).append(", ");
        if (name != null) {
            builder.append("name=").append(name);
        }
        builder.append(']');
        return builder.toString();
    }

}
