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

package com.openexchange.logging;

import java.util.Arrays;
import java.util.Collection;

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

}
