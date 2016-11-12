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
