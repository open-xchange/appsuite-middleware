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
     * The category for an error using error log level.
     */
    public static final Category CATEGORY_ERROR = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.ERROR;
        }

        @Override
        public Type getType() {
            return Category.EnumType.ERROR;
        }

        @Override
        public String toString() {
            return Category.EnumType.ERROR.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for an invalid user input using debug log level.
     */
    public static final Category CATEGORY_USER_INPUT = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.USER_INPUT;
        }

        @Override
        public String toString() {
            return Category.EnumType.USER_INPUT.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a configuration issue (e.g. missing required property).
     */
    public static final Category CATEGORY_CONFIGURATION = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.ERROR;
        }

        @Override
        public Type getType() {
            return Category.EnumType.CONFIGURATION;
        }

        @Override
        public String toString() {
            return Category.EnumType.CONFIGURATION.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a permission-denied issue using debug log level.
     */
    public static final Category CATEGORY_PERMISSION_DENIED = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.PERMISSION_DENIED;
        }

        @Override
        public String toString() {
            return Category.EnumType.PERMISSION_DENIED.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a try-again issue using debug log level.
     */
    public static final Category CATEGORY_TRY_AGAIN = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.TRY_AGAIN;
        }

        @Override
        public String toString() {
            return Category.EnumType.TRY_AGAIN.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a connectivity issue (e.g. broken/lost TCP connection) using debug log level.
     */
    public static final Category CATEGORY_CONNECTIVITY = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.TRY_AGAIN;
        }

        @Override
        public String toString() {
            return Category.EnumType.TRY_AGAIN.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a missing service or system (e.g. database) using debug log level.
     */
    public static final Category CATEGORY_SERVICE_DOWN = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.SERVICE_DOWN;
        }

        @Override
        public String toString() {
            return Category.EnumType.SERVICE_DOWN.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for truncated data using error log level.
     */
    public static final Category CATEGORY_TRUNCATED = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.TRUNCATED;
        }

        @Override
        public String toString() {
            return Category.EnumType.TRUNCATED.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for conflicting data using debug log level.
     */
    public static final Category CATEGORY_CONFLICT = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.CONFLICT;
        }

        @Override
        public String toString() {
            return Category.EnumType.CONFLICT.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for if a 3rd party system reported capacity restrictions (e.g. quota).
     */
    public static final Category CATEGORY_CAPACITY = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.CAPACITY;
        }

        @Override
        public String toString() {
            return Category.EnumType.CAPACITY.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a warning displayed to user.
     */
    public static final Category CATEGORY_WARNING = new Category() {

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public Type getType() {
            return Category.EnumType.WARNING;
        }

        @Override
        public String toString() {
            return Category.EnumType.WARNING.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * A category's type.
     */
    public static interface Type {

        /**
         * Gets the name for parental category.
         *
         * @return The name
         */
        String getName();
    }

    /**
     * An enumeration for common {@link Type types}.
     */
    public static enum EnumType implements Type {
        /**
         * The default error type.
         */
        ERROR,
        /**
         * The try-again type
         */
        TRY_AGAIN,
        /**
         * The user-input type
         */
        USER_INPUT,
        /**
         * The permission-denied type
         */
        PERMISSION_DENIED,
        /**
         * The configuration type
         */
        CONFIGURATION,
        /**
         * The connectivity type.
         */
        CONNECTIVITY,
        /**
         * The service-down type.
         */
        SERVICE_DOWN,
        /**
         * The category for truncated data.
         */
        TRUNCATED,
        /**
         * The category for a conflicting modification operation.
         */
        CONFLICT,
        /**
         * The category if a 3rd party system reported capacity restrictions (e.g. quota).
         */
        CAPACITY,
        /**
         * The warning type
         */
        WARNING;

        @Override
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
