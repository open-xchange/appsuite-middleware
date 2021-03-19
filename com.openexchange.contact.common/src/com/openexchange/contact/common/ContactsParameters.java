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
 *    trademarks of the OX Software GmbH. group of companies.
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
