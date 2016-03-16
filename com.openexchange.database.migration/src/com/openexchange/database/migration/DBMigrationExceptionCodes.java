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

package com.openexchange.database.migration;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link DBMigrationExceptionCodes} - Enumeration of all errors.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public enum DBMigrationExceptionCodes implements OXExceptionCode {

    /**
     * An unexpected error occurred.
     */
    UNEXPECTED_ERROR(DBMigrationExceptionCodes.UNEXPECTED_ERROR_MSG, Category.CATEGORY_ERROR, 1),
    /**
     * An error occurred while database migration: %1$s
     */
    DB_MIGRATION_ERROR(DBMigrationExceptionCodes.DB_MIGRATION_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * Validation of DatabaseChangeLog failed.
     */
    VALIDATION_FAILED_ERROR(DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Error while using/executing liquibase.
     */
    LIQUIBASE_ERROR(DBMigrationExceptionCodes.LIQUIBASE_ERROR_MSG, Category.CATEGORY_ERROR, 4),
    /**
     * No changelog file for database migration with name %1$s found! Execution for that file will be skipped.
     */
    CHANGELOG_FILE_NOT_FOUND_ERROR(DBMigrationExceptionCodes.CHANGELOG_FILE_NOT_FOUND_ERROR_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * Error while reading/writing data from/to the database.
     */
    SQL_ERROR(DBMigrationExceptionCodes.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Wrong type of data provided for rollback. Only Integer and String are supported.
     */
    WRONG_TYPE_OF_DATA_ROLLBACK_ERROR(DBMigrationExceptionCodes.WRONG_TYPE_OF_DATA_ROLLBACK_ERROR_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * An error occurred while reading database locks.
     */
    READING_LOCK_ERROR(DBMigrationExceptionCodes.READING_LOCK_ERROR_MSG, Category.CATEGORY_ERROR, 8),

    ;

    public static final String UNEXPECTED_ERROR_MSG = "An unexpected error occurred.";

    public static final String DB_MIGRATION_ERROR_MSG = "An error occurred while database migration.";

    public static final String VALIDATION_FAILED_ERROR_MSG = "Validation of DatabaseChangeLog failed.";

    public static final String LIQUIBASE_ERROR_MSG = "Error while using/executing liquibase.";

    public static final String CHANGELOG_FILE_NOT_FOUND_ERROR_MSG = "No changelog file for database migration with name %1$s found! Execution for that file will be skipped.";

    public static final String SQL_ERROR_MSG = "Error while reading/writing data from/to the database.";

    public static final String WRONG_TYPE_OF_DATA_ROLLBACK_ERROR_MSG = "Wrong type of data provided for rollback. Only Integer and String are supported.";

    public static final String READING_LOCK_ERROR_MSG = "An error occurred while reading database lock.";

    /**
     * The error code prefix for database migration module.
     */
    private static final String PREFIX = "DBMIGR";

    private final Category category;

    private final int detailNumber;

    private final String message;

    private DBMigrationExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumber() {
        return detailNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
