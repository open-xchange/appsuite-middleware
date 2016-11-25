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

package com.openexchange.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Enums} - A utility class for working with <b><code>enum</code></b> classes.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Enums {

    /**
     * Parses the supplied value to an enumeration constant, ignore-case.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param name The name of the constant to return
     * @return The <code>enum</code> constant
     * @throws IllegalArgumentException If there's no suitable enum constant for the supplied name
     * @throws NullPointerException If <code>enumeration</code> is <code>null</code>
     */
    public static <T extends Enum<T>> T parse(Class<T> enumeration, String name) {
        T value = parse(enumeration, name, null);
        if (null != value) {
            return value;
        }
        throw new IllegalArgumentException("No enum value '" + name + "' in Enum " + enumeration.getClass().getName());
    }

    /**
     * Parses the supplied value to an enumeration constant, ignoring case, and assuming '<code>_</code>' for '<code>-</code>' characters.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param name The name of the constant to return
     * @param defaultValue The enumeration constant to return if parsing fails
     * @return The <code>enum</code> constant
     * @throws NullPointerException If <code>enumeration</code> is <code>null</code>
     */
    public static <T extends Enum<T>> T parse(Class<T> enumeration, String name, T defaultValue) {
        for (T value : enumeration.getEnumConstants()) {
            if (value.name().replace('-', '_').equalsIgnoreCase(name)) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * Parses specified names to enumeration constants, ignore-case.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param names The names of the constants to return
     * @return The <code>enum</code> constants (having <code>null</code> elements for non-matching names)
     * @throws NullPointerException If <code>enumeration</code> is <code>null</code>
     */
    public static <T extends Enum<T>> List<T> parse(Class<T> enumeration, String... names) {
        if (null == names) {
            return null;
        }
        if (names.length <= 0) {
            return Collections.emptyList();
        }
        ArrayList<T> elements = new ArrayList<T>(names.length);
        for (String name : names) {
            elements.add(parse(enumeration, name, null));
        }
        return elements;
    }

    /**
     * Parses specified comma-separated names to enumeration constants, ignore-case.
     *
     * @param enumeration The enumeration class from which to return a constant
     * @param csv The comma-separated names
     * @return The <code>enum</code> constants (having <code>null</code> elements for non-matching names)
     * @throws NullPointerException If <code>enumeration</code> is <code>null</code>
     */
    public static <T extends Enum<T>> List<T> parseCsv(Class<T> enumeration, String csv) {
        if (Strings.isEmpty(csv)) {
            return Collections.emptyList();
        }
        return parse(enumeration, Strings.splitByComma(Strings.unquote(csv)));
    }

    private Enums() {
        // prevent instantiation
        super();
    }

}
