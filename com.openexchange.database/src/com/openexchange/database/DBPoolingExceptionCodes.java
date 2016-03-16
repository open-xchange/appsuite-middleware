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

package com.openexchange.database;

import static com.openexchange.database.DBPoolingExceptionStrings.ACTIVE_STATEMENTS_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.ALREADY_INITIALIZED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.INSERT_FAILED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.INVALID_GLOBALDB_CONFIGURATION_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.MISSING_CONFIGURATION_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NOT_INITIALIZED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NOT_RESOLVED_SERVER_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NO_CONFIG_DB_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NO_CONNECTION_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NO_DRIVER_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NO_GLOBALDB_CONFIG_FOR_GROUP_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NO_SERVER_NAME_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.NULL_CONNECTION_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.PARAMETER_PROBLEM_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.PROPERTY_MISSING_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.RESOLVE_FAILED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.RETURN_FAILED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.SCHEMA_FAILED_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.SQL_ERROR_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.TOO_LONG_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.TRANSACTION_MISSING_MSG;
import static com.openexchange.database.DBPoolingExceptionStrings.UNKNOWN_POOL_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for the database pooling exception.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum DBPoolingExceptionCodes implements OXExceptionCode {

    /**
     * Cannot get connection to config DB.
     */
    NO_CONFIG_DB(NO_CONFIG_DB_MSG, Category.CATEGORY_SERVICE_DOWN, 1),
    /**
     * Database for context %1$d and server %2$d can not be resolved
     */
    RESOLVE_FAILED(RESOLVE_FAILED_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * No connection to database %1$d
     */
    NO_CONNECTION(NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 3),
    /**
     * Schema can not be set on database connection
     */
    SCHEMA_FAILED(SCHEMA_FAILED_MSG, Category.CATEGORY_CONNECTIVITY, 4),
    /**
     * Null is returned to connection pool.
     */
    NULL_CONNECTION(NULL_CONNECTION_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * Problem with executing SQL: %1$s
     */
    SQL_ERROR(SQL_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Cannot get information for pool %d.
     */
    NO_DBPOOL("Cannot get information for pool %d.", Category.CATEGORY_ERROR, 7),
    /**
     * Driver class %1$s missing.
     */
    NO_DRIVER(NO_DRIVER_MSG, Category.CATEGORY_CONFIGURATION, 8),
    /**
     * Object %1$s does not belong to this pool. The object will be removed. If there was a previous
     * message about this object not having been returned to the pool, the object was just in use too long.
     */
    RETURN_FAILED(RETURN_FAILED_MSG, Category.CATEGORY_ERROR, 9),
    /**
     * Server name is not defined.
     */
    NO_SERVER_NAME(NO_SERVER_NAME_MSG, Category.CATEGORY_CONFIGURATION, 10),
    /**
     * %1$s is not initialized.
     */
    NOT_INITIALIZED(NOT_INITIALIZED_MSG, Category.CATEGORY_ERROR, 11),
    /**
     * Connection used for %1$d milliseconds.
     */
    TOO_LONG(TOO_LONG_MSG, Category.CATEGORY_WARNING, 12),
    /**
     * %1$d statements aren't closed.
     */
    ACTIVE_STATEMENTS(ACTIVE_STATEMENTS_MSG, Category.CATEGORY_ERROR, 13),
    /**
     * Connection not reset to auto commit.
     */
    NO_AUTOCOMMIT("Connection not reset to auto commit.", Category.CATEGORY_ERROR, 14),
    /**
     * Parsing problem in URL parameter "%1$s".
     */
    PARAMETER_PROBLEM(PARAMETER_PROBLEM_MSG, Category.CATEGORY_CONFIGURATION, 15),
    /**
     * Configuration file for database configuration is missing.
     */
    MISSING_CONFIGURATION(MISSING_CONFIGURATION_MSG, Category.CATEGORY_CONFIGURATION, 16),
    /**
     * Property "%1$s" is not defined.
     */
    PROPERTY_MISSING(PROPERTY_MISSING_MSG, Category.CATEGORY_CONFIGURATION, 17),
    /**
     * %1$s is already initialized.
     */
    ALREADY_INITIALIZED(ALREADY_INITIALIZED_MSG, Category.CATEGORY_ERROR, 18),
    /**
     * Cannot resolve server id for server %1$s.
     */
    NOT_RESOLVED_SERVER(NOT_RESOLVED_SERVER_MSG, Category.CATEGORY_CONFIGURATION, 19),
    /**
     * Nothing known about pool %1$d.
     */
    UNKNOWN_POOL(UNKNOWN_POOL_MSG, Category.CATEGORY_ERROR, 20),
    /**
     * Transaction counter is missing for context %1$d.
     */
    TRANSACTION_MISSING(TRANSACTION_MISSING_MSG, Category.CATEGORY_CONFIGURATION, 21),
    /**
     * Inserting or updating database assignment for context %1$d and server %2$d failed!
     */
    INSERT_FAILED(INSERT_FAILED_MSG, Category.CATEGORY_ERROR, 22),
    /**
     * Invalid configuration for global databases: %1$s
     */
    INVALID_GLOBALDB_CONFIGURATION(INVALID_GLOBALDB_CONFIGURATION_MSG, Category.CATEGORY_CONFIGURATION, 23),
    /**
     * No global database for context group "%1$s" found.
     */
    NO_GLOBALDB_CONFIG_FOR_GROUP(NO_GLOBALDB_CONFIG_FOR_GROUP_MSG, Category.CATEGORY_CONFIGURATION, 24),

    ;

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;

    /**
     * Default constructor.
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private DBPoolingExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @return The newly created {@link OXException} instance.
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Object... logArguments) {
        return create(null, logArguments);
    }

    /**
     * The prefix code.
     */
    public static final String PREFIX = "DBP";

    /**
     * Creates an {@link OXException} instance using this error code.
     *
     * @param cause The initial cause for {@link OXException}
     * @param logArguments The arguments for log message.
     * @return The newly created {@link OXException} instance.
     */
    public OXException create(final Throwable cause, final Object... logArguments) {
        return new OXException(detailNumber, OXExceptionStrings.MESSAGE, cause).setPrefix(PREFIX).addCategory(category).setLogMessage(
            message,
            logArguments);
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
