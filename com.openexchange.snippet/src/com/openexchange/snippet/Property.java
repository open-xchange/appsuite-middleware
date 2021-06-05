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

package com.openexchange.snippet;

import java.util.LinkedHashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link Property} - A snippet's properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Property {

    /**
     * The snippet's properties.
     */
    PROPERTIES(null),
    /**
     * The snippet's content.
     */
    CONTENT(null),
    /**
     * The snippet's attachments.
     */
    ATTACHMENTS(null),
    /**
     * The property for the identifier.
     */
    ID("id"),
    /**
     * The property for the account identifier.
     */
    ACCOUNT_ID("accountid"),
    /**
     * The property for the type; e.g. <code>"signature"</code>.
     */
    TYPE("type"),
    /**
     * The property for the display name.
     */
    DISPLAY_NAME("displayname"),
    /**
     * The property for the module identifier; e.g. <code>"com.openexchange.mail"</code>.
     */
    MODULE("module"),
    /**
     * The property for the creator.
     */
    CREATED_BY("createdby"),
    /**
     * The property for the shared flag.
     */
    SHARED("shared"),
    /**
     * The property for the optional miscellaneous JSON data.
     */
    MISC("misc"),

    ;

    private final String propName;

    private Property(final String propName) {
        this.propName = propName;
    }

    /**
     * Gets the property name
     *
     * @return The property name or <code>null</code> if no property is associated
     */
    public String getPropName() {
        return propName;
    }

    @Override
    public String toString() {
        return propName;
    }

    /**
     * Invokes appropriate switcher's method for this property.
     *
     * @param switcher The switcher
     * @return The possible result object or <code>null</code>
     */
    public Object doSwitch(final PropertySwitch switcher) {
        switch (this) {
        case ACCOUNT_ID:
            return switcher.accountId();
        case ATTACHMENTS:
            return switcher.attachments();
        case CONTENT:
            return switcher.content();
        case CREATED_BY:
            return switcher.createdBy();
        case DISPLAY_NAME:
            return switcher.displayName();
        case ID:
            return switcher.id();
        case MISC:
            return switcher.misc();
        case MODULE:
            return switcher.module();
        case PROPERTIES:
            return switcher.properties();
        case SHARED:
            return switcher.shared();
        case TYPE:
            return switcher.type();
        default:
            throw new IllegalArgumentException();
        }
    }

    private static final Set<String> PROP_NAMES;
    static {
        final Property[] props = values();
        final Set<String> names = new LinkedHashSet<String>(props.length);
        for (final Property property : props) {
            String name = property.propName;
            if (null != name) {
                names.add(name);
            }
        }
        PROP_NAMES = ImmutableSet.copyOf(names);
    }

    /**
     * Gets an unmodifiable set containing all property names.
     *
     * @return An unmodifiable set containing all property names
     */
    public static Set<String> getPropertyNames() {
        return PROP_NAMES;
    }

}
