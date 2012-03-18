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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.util;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link CharsetValidator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharsetValidator {

    private static final CharsetValidator INSTANCE = new CharsetValidator();

    /**
     * Gets the {@link CharsetValidator} instance.
     *
     * @return The {@link CharsetValidator} instance
     */
    public static CharsetValidator getInstance() {
        return INSTANCE;
    }

    /*-
     * Member stuff
     */

    private final ConcurrentMap<IgnoreCaseString, Boolean> map;

    /**
     * Initializes a new {@link CharsetValidator}.
     */
    private CharsetValidator() {
        super();
        map = new ConcurrentHashMap<IgnoreCaseString, Boolean>(8);
    }

    private static final String TEST = "foo";

    /**
     * Check charset.
     *
     * @param charset The charset to check
     * @throws UnsupportedEncodingException If charset check fails
     */
    public void checkCharset(final String charset) throws UnsupportedEncodingException {
        final IgnoreCaseString key = IgnoreCaseString.valueOf(charset);
        final Boolean b = map.get(key);
        if (Boolean.FALSE.equals(b)) {
            throw new UnsupportedEncodingException(charset);
        }
        if (null == b) {
            try {
                TEST.getBytes(charset);
                map.put(key, Boolean.TRUE);
            } catch (final UnsupportedEncodingException e) {
                /*
                 * Put to map and re-throw exception
                 */
                map.put(key, Boolean.FALSE);
                throw e;
            }
        }
    }

    private static final class IgnoreCaseString implements Comparable<IgnoreCaseString>, Cloneable {

        /**
         * Initializes a new ignore-case string from specified string.
         *
         * @param s The string
         * @return The new ignore-case string
         */
        public static IgnoreCaseString valueOf(final String s) {
            return new IgnoreCaseString(s);
        }

        private final String s;

        private final int hashcode;

        /**
         * No direct instantiation
         */
        private IgnoreCaseString(final String s) {
            super();
            this.s = s;
            hashcode = null == s ? 0 : s.toLowerCase(Locale.ENGLISH).hashCode();
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                /*
                 * Cannot not occur since Cloneable is implemented
                 */
                throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
            }
        }

        @Override
        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof IgnoreCaseString)) {
                return s.equalsIgnoreCase(((IgnoreCaseString) other).s);
            }
            return false;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public int hashCode() {
            return hashcode;
        }

        @Override
        public int compareTo(final IgnoreCaseString other) {
            return s.compareToIgnoreCase(other.s);
        }
    }

}
