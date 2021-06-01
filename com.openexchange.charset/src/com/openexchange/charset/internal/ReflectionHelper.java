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

package com.openexchange.charset.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * {@link ReflectionHelper} - The reflection helper class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReflectionHelper {

    /**
     * Initializes a new {@link ReflectionHelper}.
     */
    private ReflectionHelper() {
        super();
    }

    /**
     * Sets the final static field.
     *
     * @param field The field to set
     * @param value The new value to apply
     * @throws NoSuchFieldException If there is no such field
     * @throws IllegalAccessException If access is prohibited
     */
    public static void setStaticFinalField(final Field field, final Object value) throws NoSuchFieldException, IllegalAccessException {
        // Mark the field to be public
        field.setAccessible(true);

        // Change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the static final field
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);

        // Blank out the final bit in the modifiers integer
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);
        final sun.reflect.FieldAccessor fa = sun.reflect.ReflectionFactory.getReflectionFactory().newFieldAccessor(field, false);
        fa.set(null, value);
    }

}
