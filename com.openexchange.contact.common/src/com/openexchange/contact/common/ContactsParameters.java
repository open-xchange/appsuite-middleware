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

package com.openexchange.contact.common;

import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link ContactsParameters}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface ContactsParameters {

    /**
     * Array of {@link ContactField}
     * <p/>
     * Allows to restrict the returned properties of retrieved contact data.
     */
    static final String PARAMETER_FIELDS = "fields";

    /**
     * {@link ContactField}
     * <p/>
     * Specifies the field for sorting the results.
     * <p/>
     */
    static final String PARAMETER_ORDER_BY = "sort";

    /**
     * {@link Order}
     * <p/>
     * The sort order to apply, either <code>ASC</code> for ascending, or <code>DESC</code> for descending.
     * <p/>
     */
    static final String PARAMETER_ORDER = "order";

    /**
     * {@link String}
     * <p/>
     * A collation name to sort the contacts by. Only supports "gbk" and "gb2312", not needed for other languages.
     * Parameter {@link #PARAMETER_ORDER_BY} should be set for this to work.
     */
    static final String PARAMETER_COLLATION = "collation";

    /**
     * {@link Integer}
     * <p/>
     * A positive integer number to specify the "left-hand" limit of the range to return.
     */
    static final String PARAMETER_LEFT_HAND_LIMIT = "left_hand_limit";

    /**
     * {@link Integer}
     * <p/>
     * A positive integer number to specify the "right-hand" limit of the range to return.
     */
    static final String PARAMETER_RIGHT_HAND_LIMIT = "right_hand_limit";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates whether cached contact data should forcibly be updated prior performing the operation or not.
     */
    static final String PARAMETER_UPDATE_CACHE = "updateCache";

    /**
     * {@link Boolean}
     * <p/>
     * Indicates that the returned contacts should have at least one e-mail address defined (used during searches).
     */
    static final String PARAMETER_REQUIRE_EMAIL = "require_email";

    /**
     * {@link Boolean}
     * <p/>
     * Specifies that distribution lists shall be ignored and not be part of the returned results (used during searches).
     */
    static final String PARAMETER_IGNORE_DISTRIBUTION_LISTS = "ignore_distribution_lists";

    /**
     * {@link java.sql.Connection}
     * <p/>
     * The (dynamic) parameter name where the underlying database connection is held during transactions, or when slipstreaming a
     * surrounding connection to the storage. Empty by default.
     *
     * @return The parameter name where the underlying database connection is held during transactions
     */
    static String PARAMETER_CONNECTION() {
        return new StringBuilder(java.sql.Connection.class.getName()).append('@').append(Thread.currentThread().getId()).toString();
    }

    /**
     * Sets a parameter.
     * <p/>
     * A value of <code>null</code> removes the parameter.
     *
     * @param parameter The parameter name to set
     * @param value The value to set, or <code>null</code> to remove the parameter
     * @return A self reference
     */
    <T> ContactsParameters set(String parameter, T value);

    /**
     * Gets a parameter.
     *
     * @param parameter The parameter name
     * @param clazz The value's target type
     * @return The parameter value, or <code>null</code> if not set
     */
    <T> T get(String parameter, Class<T> clazz);

    /**
     * Gets a parameter, falling back to a custom default value if not set.
     *
     * @param parameter The parameter name
     * @param clazz The value's target type
     * @param defaultValue The default value to use as fallback if the parameter is not set
     * @return The parameter value, or the passed default value if not set
     */
    <T> T get(String parameter, Class<T> clazz, T defaultValue);

    /**
     * Gets a value indicating whether a specific parameter is set.
     *
     * @param parameter The parameter name
     * @return <code>true</code> if the parameter is set, <code>false</code>, otherwise
     */
    boolean contains(String parameter);

    /**
     * Gets a set of all configured parameters.
     *
     * @return All parameters as set
     */
    Set<Entry<String, Object>> entrySet();
}
