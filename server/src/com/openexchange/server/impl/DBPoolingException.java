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



package com.openexchange.server.impl;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * This exception is used if problems occur with the pooling of database
 * connections.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DBPoolingException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -8656698696406966658L;

    /**
     * @param message error message
     * @deprecated since all exceptions should have error codes.
     */
    public DBPoolingException(final String message) {
        super(Component.DB_POOLING, message);
    }

    /**
     * @param exc the cause of the exception.
     * @deprecated since all exceptions should have error codes.
     */
    public DBPoolingException(final Exception exc) {
        super(Component.DB_POOLING, exc);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     */
    public DBPoolingException(final Code code) {
        this(code, null, new Object[0]);
    }

    /**
     * Initializes a new exception using the information provides by the cause.
     * @param cause the cause of the exception.
     */
    public DBPoolingException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public DBPoolingException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public DBPoolingException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.DB_POOLING, code.category, code.detailNumber,
            null == code.message ? cause.getMessage() : code.message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for the database pooling exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Connection to config DB can't be created.
         */
        NO_CONFIG_DB("Cannot get connection to config DB.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 1),
        /**
         * Resolving the database failed.
         */
        RESOLVE_FAILED("Resolving database for context %1$d and server %2$d not "
            + "possible!", Category.CODE_ERROR, 2),
        /**
         * Connection to a database can't be created.
         */
        NO_CONNECTION("Cannot get connection to database %d.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 3),
        /**
         * Setting the schema failed.
         */
        SCHEMA_FAILED("Cannot set schema in database connection.",
            Category.SOCKET_CONNECTION, 4),
        /**
         * Null is returned to the pool instead a connection object.
         */
        NULL_CONNECTION("Null is returned to connection pool.",
            Category.CODE_ERROR, 5),
        /**
         * A SQL problem occures while reading information from the config
         * database.
         */
        SQL_ERROR("Problem with executing SQL: %s", Category.CODE_ERROR,
            6),
        /**
         * A database pool entry can't be found in the config database.
         */
        NO_DBPOOL("Cannot get information for pool %d.",
            Category.CODE_ERROR, 7),
        /**
         * A driver class could not be found.
         */
        NO_DRIVER("Driver class missing.", Category.SETUP_ERROR, 8),
        /**
         * Returning a connection to the pool failed.
         */
        RETURN_FAILED("Cannot return connection to pool %d.",
            Category.CODE_ERROR, 9),
        /**
         * Server name is not defined.
         */
        NO_SERVER_NAME("Server name is not defined.", Category.SETUP_ERROR,
            10),
        /**
         * %s is not initialized.
         */
        NOT_INITIALIZED("%s is not initialized.", Category.CODE_ERROR,
            11),
        /**
         * Connection used for %1$d milliseconds.
         */
        TOO_LONG("Connection used for %1$d milliseconds.", Category
            .SUBSYSTEM_OR_SERVICE_DOWN, 12),
        /**
         * %1$d statements aren't closed.
         */
        ACTIVE_STATEMENTS("%1$d statements aren't closed.", Category
            .CODE_ERROR, 13),
        /**
         * Found not committed transaction.
         */
        IN_TRANSACTION("Found not committed transaction.", Category
            .CODE_ERROR, 14);

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
        private Code(final String message, final Category category,
            final int detailNumber) {
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
    }
}
