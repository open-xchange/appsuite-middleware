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

package com.openexchange.logging;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * {@link LogUtility} - Utility class for logging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class LogUtility {

    /**
     * Initializes a new {@link LogUtility}.
     */
    private LogUtility() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class IntArrayString {

        private final int[] integers;

        IntArrayString(int[] integers) {
            super(); this.integers = integers;
        }

        @Override
        public String toString() {
            return Arrays.toString(integers);
        }
    }

    /**
     * Creates a {@link #toString()} object for given integer array.
     *
     * @param integers The integer array
     * @return The object providing content of given array if {@link #toString()} is invoked
     */
    public static Object toStringObjectFor(int[] integers) {
        if (integers == null) {
            return "null";
        }

        if (integers.length <= 0) {
            return "[]";
        }

        return new IntArrayString(integers);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ObjectArrayString<O> {

        private final O[] objects;

        ObjectArrayString(O[] objects) {
            super(); this.objects = objects;
        }

        @Override
        public String toString() {
            return Arrays.toString(objects);
        }
    }

    /**
     * Creates a {@link #toString()} object for given object array.
     *
     * @param objects The object array
     * @return The object providing content of given array if {@link #toString()} is invoked
     */
    public static <O> Object toStringObjectFor(O[] objects) {
        if (objects == null) {
            return "null";
        }

        if (objects.length <= 0) {
            return "[]";
        }

        return new ObjectArrayString<O>(objects);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ObjectCollectionString<O> {

        private final Collection<O> objects;

        ObjectCollectionString(Collection<O> objects) {
            super(); this.objects = objects;
        }

        @Override
        public String toString() {
            return objects.toString();
        }
    }

    /**
     * Creates a {@link #toString()} object for given object collection.
     *
     * @param objects The object collection
     * @return The object providing content of given collection if {@link #toString()} is invoked
     */
    public static <O> Object toStringObjectFor(Collection<O> objects) {
        if (objects == null) {
            return "null";
        }

        if (objects.isEmpty()) {
            return "[]";
        }

        return new ObjectCollectionString<O>(objects);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ObjectString<O> {

        private final Supplier<O> supplier;

        ObjectString(Supplier<O> supplier) {
            super(); this.supplier = supplier;
        }

        @Override
        public String toString() {
            O stringForMe = supplier.get();
            return stringForMe == null ? "null" : stringForMe.toString();
        }
    }

    /**
     * Creates a {@link #toString()} object for given supplier.
     *
     * @param supplier The supplier
     * @return The object providing content of given supplier if {@link #toString()} is invoked
     */
    public static <O> Object toStringObjectFor(Supplier<O> supplier) {
        if (supplier == null) {
            return "null";
        }

        return new ObjectString<O>(supplier);
    }

}
