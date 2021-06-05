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

import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link StorageParametersUtility} - A utility class for {@link StorageParameters}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StorageParametersUtility {

    /**
     * Initializes a new {@link StorageParametersUtility}.
     */
    private StorageParametersUtility() {
        super();
    }

    /**
     * Checks whether to hand-down permissions on update operation.
     *
     * @param params The storage parameters
     * @return <code>true</code> to hand down; otherwise <code>false</code>
     */
    public static boolean isHandDownPermissions(final StorageParameters params) {
        final FolderServiceDecorator decorator = params.getDecorator();
        if (null == decorator) {
            return false;
        }
        final Object permissionsHandling = decorator.getProperty("permissions");
        return null != permissionsHandling && "inherit".equalsIgnoreCase(permissionsHandling.toString());
    }

    /**
     * Gets specified boolean parameter.
     *
     * @param name The name
     * @param params The storage parameters
     * @return <code>true</code> if boolean parameter is present and set to <code>true</code>; otherwise <code>false</code>
     */
    public static boolean getBoolParameter(final String name, final StorageParameters params) {
        final FolderServiceDecorator decorator = params.getDecorator();
        if (null == decorator) {
            return false;
        }
        final Object tmp = decorator.getProperty(name);
        return null != tmp && ((tmp instanceof Boolean) ? ((Boolean) tmp).booleanValue() : parseBoolParameter(tmp.toString()));
    }

    private static final Set<String> BOOL_VALS = ImmutableSet.of(
        "true",
        "1",
        "yes",
        "y",
        "on");

    /**
     * Parses denoted <tt>boolean</tt> value from specified <tt>String</tt> parameter.
     * <p>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or
     * "1".
     *
     * @param value The parameter value to check
     * @return The parsed <tt>boolean</tt> value (<code>false</code> on absence)
     */
    public static boolean parseBoolParameter(final String value) {
        return (null != value) && BOOL_VALS.contains(com.openexchange.java.Strings.toLowerCase(value.trim()));
    }
}
