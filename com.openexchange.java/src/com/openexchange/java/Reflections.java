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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * {@link Reflections} - A helper class for Java Reflection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Reflections {

    /**
     * Initializes a new {@link Reflections}.
     */
    private Reflections() {
        super();
    }

    /**
     * Forces the specified <code>private final</code> field to be accessible.
     * <p>
     * As described in <a href="http://zarnekow.blogspot.de/2013/01/java-hacks-changing-final-fields.html">Sebastian Zarnekow's Blog</a>.
     *
     * @param field The field
     * @throws NoSuchFieldException If associated class' <code>"modifiers"</code> cannot be accessed
     * @throws SecurityException If the request is denied.
     * @throws IllegalAccessException If new class' <code>"modifiers"</code> cannot be set
     * @throws IllegalArgumentException If new class' <code>"modifiers"</code> cannot be set
     */
    public static void makeModifiable(Field field) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            Field modifierField = field.getClass().getDeclaredField("modifiers");
            modifiers = modifiers & ~Modifier.FINAL;
            modifierField.setAccessible(true);
            modifierField.setInt(field, modifiers);
        }
    }

}
