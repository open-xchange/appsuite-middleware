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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.database.DBPoolingExceptionMessages.*;

import com.openexchange.database.internal.DBPoolingExceptionFactory;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * Error codes for the database pooling exception.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum DBPoolingExceptionCodes implements OXErrorMessage {

    /**
     * Cannot get connection to config DB.
     */
    NO_CONFIG_DB(NO_CONFIG_DB_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 1),
    /**
     * Resolving database for context %1$d and server %2$d not possible!
     */
    RESOLVE_FAILED(RESOLVE_FAILED_MSG, Category.CODE_ERROR, 2),
    /**
     * Cannot get connection to database %d.
     */
    NO_CONNECTION(NO_CONNECTION_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 3),
    /**
     * Cannot set schema in database connection.
     */
    SCHEMA_FAILED(SCHEMA_FAILED_MSG, Category.SOCKET_CONNECTION, 4),
    /**
     * Null is returned to connection pool.
     */
    NULL_CONNECTION(NULL_CONNECTION_MSG, Category.CODE_ERROR, 5),
    /**
     * Problem with executing SQL: %s
     */
    SQL_ERROR(SQL_ERROR_MSG, Category.CODE_ERROR, 6),
    /**
     * Cannot get information for pool %d.
     */
    NO_DBPOOL("Cannot get information for pool %d.", Category.CODE_ERROR, 7),
    /**
     * Driver class %1$s missing.
     */
    NO_DRIVER(NO_DRIVER_MSG, Category.SETUP_ERROR, 8),
    /**
     * Cannot return connection to pool %d.
     */
    RETURN_FAILED(RETURN_FAILED_MSG, Category.CODE_ERROR, 9),
    /**
     * Server name is not defined.
     */
    NO_SERVER_NAME(NO_SERVER_NAME_MSG, Category.SETUP_ERROR, 10),
    /**
     * %1$s is not initialized.
     */
    NOT_INITIALIZED(NOT_INITIALIZED_MSG, Category.CODE_ERROR, 11),
    /**
     * Connection used for %1$d milliseconds.
     */
    TOO_LONG(TOO_LONG_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 12),
    /**
     * %1$d statements aren't closed.
     */
    ACTIVE_STATEMENTS(ACTIVE_STATEMENTS_MSG, Category.CODE_ERROR, 13),
    /**
     * Connection not reset to auto commit.
     */
    NO_AUTOCOMMIT("Connection not reset to auto commit.", Category.CODE_ERROR, 14),
    /**
     * Parsing problem in URL parameter "%1$s".
     */
    PARAMETER_PROBLEM(PARAMETER_PROBLEM_MSG, Category.SETUP_ERROR, 15),
    /**
     * Configuration file for database configuration is missing.
     */
    MISSING_CONFIGURATION(MISSING_CONFIGURATION_MSG, Category.SETUP_ERROR, 16),
    /**
     * Property "%1$s" is not defined.
     */
    PROPERTY_MISSING(PROPERTY_MISSING_MSG, Category.SETUP_ERROR, 17),
    /**
     * %1$s is already initialized.
     */
    ALREADY_INITIALIZED(ALREADY_INITIALIZED_MSG, Category.CODE_ERROR, 18),
    /**
     * Cannot resolve server id for server %1$s.
     */
    NOT_RESOLVED_SERVER(NOT_RESOLVED_SERVER_MSG, Category.SETUP_ERROR, 19);

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
    private DBPoolingExceptionCodes(String message, Category category, int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    public Category getCategory() {
        return category;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return detailNumber;
    }

    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public DBPoolingException create(final Object... messageArgs) {
        return DBPoolingExceptionFactory.getInstance().create(this, messageArgs);
    }

    public DBPoolingException create(final Throwable cause, final Object... messageArgs) {
        return DBPoolingExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}