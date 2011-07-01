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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.exception;


/**
 * {@link Category} - The category for an {@link OXException} determines its behavior during exception handling and logging.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Category extends Comparable<Category> {

    /**
     * The category for an error.
     */
    public static final Category CATEGORY_ERROR = new Category() {

        public LogLevel getLogLevel() {
            return LogLevel.ERROR;
        }

        public Type getType() {
            return Category.EnumType.ERROR;
        }

        @Override
        public String toString() {
            return Category.EnumType.ERROR.getName();
        }

        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for an invalid user input.
     */
    public static final Category CATEGORY_USER_INPUT = new Category() {

        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        public Type getType() {
            return Category.EnumType.USER_INPUT;
        }

        @Override
        public String toString() {
            return Category.EnumType.USER_INPUT.getName();
        }

        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * A category's type.
     */
    public static interface Type {

        /**
         * Gets the name for this category.
         * 
         * @return The name
         */
        String getName();
    }

    /**
     * An enumeration for common {@link Type types}.
     */
    public static enum EnumType implements Type {
        ERROR, TRY_AGAIN, USER_INPUT, PERMISSION_DENIED, WARNING;

        public String getName() {
            return toString();
        }
    }

    /**
     * Gets the log level in which associated exception shall be logged.
     * 
     * @return The log level
     */
    LogLevel getLogLevel();

    /**
     * Gets this category's type.
     * 
     * @return The type
     */
    Type getType();

}
