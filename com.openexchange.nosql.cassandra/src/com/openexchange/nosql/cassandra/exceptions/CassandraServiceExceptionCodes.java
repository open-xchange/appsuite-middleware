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

package com.openexchange.nosql.cassandra.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link CassandraServiceExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraServiceExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * None of the Cassandra contact points '%1$s' is reachable
     */
    CONTACT_POINTS_NOT_REACHABLE("None of the Cassandra contact points '%1$s' is reachable", Category.CATEGORY_CONNECTIVITY, 2),
    /**
     * None of the Cassandra contact points is reachable
     */
    CONTACT_POINTS_NOT_REACHABLE_SIMPLE("None of the Cassandra contact points is reachable", Category.CATEGORY_CONNECTIVITY, 2),
    /**
     * An authentication error while contacting the initial contact points '%1$s'
     */
    AUTHENTICATION_ERROR("An authentication error while contacting the initial contact points '%1$s'", Category.CATEGORY_ERROR, 3),
    /**
     * Failed to execute query: %1$s
     * <p>
     * This corresponds to the exception that Cassandra throws when a (valid) query cannot be executed (TimeoutException, UnavailableException, ...).
     */
    QUERY_EXECUTION_ERROR("Failed to execute query: %1$s", Category.CATEGORY_ERROR, 4),
    /**
     * Invalid query: %1$s
     * <p>
     * An exception indicating that a query cannot be executed because it is syntactically incorrect, invalid, unauthorized or any other reason.
     */
    QUERY_VALIDATION_ERROR("Invalid query: %1$s", Category.CATEGORY_ERROR, 5),
    /**
     * Could not initialise the connection to the Cassandra cluster: %1$s
     */
    CANNOT_INITIALISE_CLUSTER("Could not initialise the connection to the Cassandra cluster: %1$s", Category.CATEGORY_ERROR, 6),
    /**
     * The keyspace '%1$s' does not exist.
     */
    KEYSPACE_DOES_NOT_EXIST("The keyspace '%1$s' does not exist.", CATEGORY_ERROR, 7),
    ;

    private final int number;
    private final Category category;
    private static final String PREFIX = "CASSANDRA-SERVICE";
    private final String message;
    private final String displayMessage;

    /**
     * Initialises a new {@link CassandraServiceExceptionCodes}.
     */
    private CassandraServiceExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.displayMessage = message;
        this.category = category;
        this.number = number;

    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
