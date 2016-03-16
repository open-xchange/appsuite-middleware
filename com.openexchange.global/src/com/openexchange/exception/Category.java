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

package com.openexchange.exception;

import java.io.Serializable;

/**
 * {@link Category} - The category for an {@link OXException} determines its behavior during exception handling and logging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Category extends Serializable {

    /**
     * The category for an error using error log level.
     */
    public static final Category CATEGORY_ERROR = new Category() {

        private static final long serialVersionUID = 8788884465853335875L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for an invalid user input using debug log level.
     */
    public static final Category CATEGORY_USER_INPUT = new Category() {

        private static final long serialVersionUID = -1061550386431721392L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a configuration issue (e.g. missing required property).
     */
    public static final Category CATEGORY_CONFIGURATION = new Category() {

        private static final long serialVersionUID = 4740329857692772795L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a permission-denied issue using debug log level.
     */
    public static final Category CATEGORY_PERMISSION_DENIED = new Category() {

        private static final long serialVersionUID = -4586701001738617769L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a try-again issue using debug log level.
     */
    public static final Category CATEGORY_TRY_AGAIN = new Category() {

        private static final long serialVersionUID = 1904303883657823251L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a connectivity issue (e.g. broken/lost TCP connection) using debug log level.
     */
    public static final Category CATEGORY_CONNECTIVITY = new Category() {

        private static final long serialVersionUID = 5564096898988894716L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
        }

        @Override
        public Type getType() {
            return Category.EnumType.CONNECTIVITY;
        }

        @Override
        public String toString() {
            return Category.EnumType.CONNECTIVITY.getName();
        }

        @Override
        public int compareTo(final Category other) {
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a missing service or system (e.g. database) using debug log level.
     */
    public static final Category CATEGORY_SERVICE_DOWN = new Category() {

        private static final long serialVersionUID = 7140947565971113147L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for truncated data using error log level.
     */
    public static final Category CATEGORY_TRUNCATED = new Category() {

        private static final long serialVersionUID = 4682222945052171797L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for conflicting data using debug log level.
     */
    public static final Category CATEGORY_CONFLICT = new Category() {

        private static final long serialVersionUID = -8923913404605944423L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for if a 3rd party system reported capacity restrictions (e.g. quota).
     */
    public static final Category CATEGORY_CAPACITY = new Category() {

        private static final long serialVersionUID = -5312069374248973061L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
        }

    };

    /**
     * The default category for a warning displayed to user.
     */
    public static final Category CATEGORY_WARNING = new Category() {

        private static final long serialVersionUID = 6009692480995110169L;

        @Override
        public LogLevel getLogLevel() { // The higher chosen log level is, the more likely will it be logged
            return LogLevel.ERROR;
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
            return null == other ? 1 : LogLevel.COMPARATOR.compare(this.getLogLevel(), other.getLogLevel());
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
     * An enumeration for known {@link Category categories}.
     */
    public static enum EnumCategory implements Category {

        /**
         * The category for an error using error log level.
         */
        ERROR(Category.CATEGORY_ERROR),
        /**
         * The default category for an invalid user input using debug log level.
         */
        USER_INPUT(Category.CATEGORY_USER_INPUT),
        /**
         * The default category for a configuration issue (e.g. missing required property).
         */
        CONFIGURATION(Category.CATEGORY_CONFIGURATION),
        /**
         * The default category for a permission-denied issue using debug log level.
         */
        PERMISSION_DENIED(Category.CATEGORY_PERMISSION_DENIED),
        /**
         * The default category for a try-again issue using debug log level.
         */
        TRY_AGAIN(Category.CATEGORY_TRY_AGAIN),
        /**
         * The default category for a connectivity issue (e.g. broken/lost TCP connection) using debug log level.
         */
        CONNECTIVITY(Category.CATEGORY_CONNECTIVITY),
        /**
         * The default category for a missing service or system (e.g. database) using debug log level.
         */
        SERVICE_DOWN(Category.CATEGORY_SERVICE_DOWN),
        /**
         * The default category for truncated data using error log level.
         */
        TRUNCATED(Category.CATEGORY_TRUNCATED),
        /**
         * The default category for conflicting data using debug log level.
         */
        CONFLICT(Category.CATEGORY_CONFLICT),
        /**
         * The default category for if a 3rd party system reported capacity restrictions (e.g. quota).
         */
        CAPACITY(Category.CATEGORY_CAPACITY),
        /**
         * The default category for a warning displayed to user.
         */
        WARNING(Category.CATEGORY_WARNING),

        ;

        private final Category impl;

        private EnumCategory(final Category impl) {
            this.impl = impl;
        }

        @Override
        public int compareTo(final Category o) {
            return impl.compareTo(o);
        }

        @Override
        public LogLevel getLogLevel() {
            return impl.getLogLevel();
        }

        @Override
        public Type getType() {
            return impl.getType();
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

    /**
     * Compares this category with the specified category for order. Returns a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *
     * @param other The other category to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    public int compareTo(Category other);

}
