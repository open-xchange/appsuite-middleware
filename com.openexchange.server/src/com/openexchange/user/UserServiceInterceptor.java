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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.user;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;

/**
 * {@link UserServiceInterceptor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface UserServiceInterceptor {

    /**
     * Gets the ranking for this interceptor, allowing a defined execution order of multiple interceptor implementations.
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Invoked before a user is created.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param user A reference to the user data of the user being created.
     * @param contactData A reference to the contact data of the user being created.
     * @throws OXException If interception fails
     */
    void beforeCreate(User user, Contact contactData) throws OXException;

    /**
     * Invoked after a user is created.
     *
     * @param user A reference to the user data of the created user.
     * @param contactData A reference to the contact data created user.
     * @throws OXException If interception fails
     */
    void afterCreate(User user, Contact contactData) throws OXException;

    /**
     * Invoked before a user is updated.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param user A reference to the user data of the user being updated, or <code>null</code> if not affected by the update
     * @param contactData A reference to the contact data of the user being created, or <code>null</code> if not affected by the update
     * @throws OXException If interception fails
     */
    void beforeUpdate(User user, Contact contactData) throws OXException;

    /**
     * Invoked after a user is updated.
     *
     * @param user A reference to the user data of the updated user, or <code>null</code> if not affected by the update
     * @param contactData A reference to the contact data updated user, or <code>null</code> if not affected by the update
     * @throws OXException If interception fails
     */
    void afterUpdate(User user, Contact contactData) throws OXException;

    /**
     * Invoked before a user is deleted.
     * <p>
     * Note that further processing is aborted if an exception occurs during invocation.
     *
     * @param user A reference to the user data of the user being deleted.
     * @param contactData A reference to the contact data of the user being deleted.
     * @throws OXException If interception fails
     */
    void beforeDelete(User user, Contact contactData) throws OXException;

    /**
     * Invoked after a user is deleted.
     *
     * @param user A reference to the user data of the deleted user
     * @param contactData A reference to the contact data deleted user
     * @throws OXException If interception fails
     */
    void afterDelete(User user, Contact contactData) throws OXException;

}
