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

package com.openexchange.user.interceptor;

import java.util.Collections;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link UserServiceInterceptor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface UserServiceInterceptor {

    /**
     * The constant for empty properties.
     */
    public static final Map<String, Object> EMPTY_PROPS = Collections.emptyMap();

    public static final String PROP_CONNECTION = "connection";

    /**
     * Gets the ranking for this interceptor, allowing a defined execution order of multiple interceptor implementations. Execution order is
     * highest first.
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Invoked before a user is created.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param context The context
     * @param user A reference to the user data of the user being created. Possibly <code>null</code>.
     * @param contactData A reference to the contact data of the user being created. Possibly <code>null</code>.
     * @throws OXException If interception fails
     */
    void beforeCreate(Context context, User user, Contact contactData) throws OXException;

    /**
     * Invoked after a user is created. Default implementation uses {@link #afterCreate(Context, User, Contact)}
     *
     * @param context The context
     * @param user A reference to the user data of the created user.
     * @param contactData A reference to the contact data of the created user. Possibly <code>null</code>.
     * @param properties Arbitrary properties associated with this interception call; may be <code>null</code>
     * @throws OXException If interception fails
     * @see #EMPTY_PROPS
     */
    default void afterCreate(Context context, User user, Contact contactData, @SuppressWarnings("unused") Map<String, Object> properties) throws OXException{
        afterCreate(context, user, contactData);
    }

    /**
     * Invoked after a user is created.
     *
     * @param context The context
     * @param user A reference to the user data of the created user.
     * @param contactData A reference to the contact data of the created user. Possibly <code>null</code>.
     * @throws OXException If interception fails
     * @see #afterCreate(Context, User, Contact, Map)
     */
    void afterCreate(Context context, User user, Contact contactData) throws OXException;

    /**
     * Invoked before a user is updated.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param context The context
     * @param user A reference to the user data of the user being updated, or <code>null</code> if not affected by the update
     * @param contactData A reference to the contact data of the user being created, or <code>null</code> if not affected by the update
     * @param properties Arbitrary properties associated with this interception call; may be <code>null</code>
     * @throws OXException If interception fails
     * @see #EMPTY_PROPS
     */
    void beforeUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException;

    /**
     * Invoked after a user is updated.
     *
     * @param context The context
     * @param user A reference to the user data of the updated user, or <code>null</code> if not affected by the update
     * @param contactData A reference to the contact data of the updated user, or <code>null</code> if not affected by the update
     * @param properties Arbitrary properties associated with this interception call; may be <code>null</code>
     * @throws OXException If interception fails
     * @see #EMPTY_PROPS
     */
    void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException;

    /**
     * Invoked before a user is deleted.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param context The context
     * @param user A reference to the user data of the user being deleted.
     * @param contactData A reference to the contact data of the user being deleted.
     * @throws OXException If interception fails
     */
    void beforeDelete(Context context, User user, Contact contactData) throws OXException;

    /**
     * Invoked after a user is deleted.
     *
     * @param context The context
     * @param user A reference to the user data of the deleted user
     * @param contactData A reference to the contact data of the deleted user
     * @throws OXException If interception fails
     */
    void afterDelete(Context context, User user, Contact contactData) throws OXException;

}
