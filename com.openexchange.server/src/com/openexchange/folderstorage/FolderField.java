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

package com.openexchange.folderstorage;

import java.io.Serializable;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderField} - A pair of a field and its name.
 * <p>
 * Equality is only determined by field value, not its name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderField implements Serializable {

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

    /**
     * Deserializes a folder property from its serialized representation.
     *
     * @param value The value to parse
     * @return The parsed folder property
     */
    public FolderProperty parse(Object value) {
        return null == value ? null : new FolderProperty(name, value);
    }

    /**
     * Serializes a folder property.
     *
     * @param property The folder property to write
     * @param session The underlying session, or <code>null</code> if not available
     * @return The serialized value
     */
    public Object write(FolderProperty property, ServerSession session) {
        return null == property ? defaultValue : property.getValue();
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
