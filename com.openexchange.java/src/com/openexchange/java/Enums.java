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
        throw new IllegalArgumentException("No enum value '" + name + "' in Enum " + enumeration);
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
        if (null == name) {
            return defaultValue;
        }
        String checkedName = name.replace('-', '_');
        for (T value : enumeration.getEnumConstants()) {
            if (checkedName.equalsIgnoreCase(value.name())) {
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
     * Generates a comma-separated string for specified enumeration names.
     *
     * @param <E> The enumeration type
     * @param enumeration The enumeration
     * @return The resulting comma-separated string
     */
    public static <E extends Enum<E>> String toCommaSeparatedList(E enumeration[]) {
        StringBuilder sb = new StringBuilder(enumeration.length << 3);
        for (E e : enumeration) {
            sb.append(e.name()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
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
