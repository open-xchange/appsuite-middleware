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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.guest;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link GuestService} defines methods to handle guests across context boundaries so, for instance, it is possible to set a password for a guest only once even it is a guest within multiple contexts.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@SingletonService
public interface GuestService {

    /**
     * Adds a new guest or creates a new assignment for the guest if the provided mail address is already registered because the user was added from another context.
     *
     * @param mailAddress - mail address of the guest to add
     * @param contextId - context id the guest should be assigned to
     * @param userId - user id of the guest to add in the given context
     * @throws OXException
     */
    void addGuest(String mailAddress, int contextId, int userId) throws OXException;

    /**
     * Remove an existing guest. If the last assignment for a guest was removed even the complete guest will be removed.
     *
     * @param mailAddress - mail address of the guest to remove
     * @param contextId - context id the guest is assigned to
     * @param userId - user id in the given context the guest is assigned to
     * @throws OXException
     */
    void removeGuest(int contextId, int userId) throws OXException;

    /**
     * Remove existing guests for the given context. This should only be called in case the given context was deleted. If the last assignment of a guest was removed even the complete guest will be removed.
     *
     * @param contextId - context id the guest is assigned to
     * @throws OXException
     */
    void removeGuests(int contextId) throws OXException;

    /**
     * Sets the password for all occurrences of the guest which means for all internal users over all contexts the provided mail address is registered for. After updating the user he will also removed from the cache if caching is used.<br>
     * <br>
     * <b>Important notice:</b> You have to provide the unencoded password as the implementation will try to encode it before saving.
     *
     * @param mailAddress - mail address to identify the guest and to find associated users
     * @param password - new (unencoded) password to set.
     * @throws OXException
     */
    void setPassword(String mailAddress, String password) throws OXException;

    /**
     * Updates all guest users and its contacts based on the given {@link User}. That means all {@link User} and {@link Contact} in different contexts will be updated to the user with the given information.
     *
     * @param user - {@link User} that will be copied to all existing guests
     * @param contextId - context id the user that should be copied to the guest user is assigned to.
     * @throws OXException
     */
    void updateGuest(User user, int contextId) throws OXException;

    /**
     * Returns the {@link GuestAssignment}s the given mail address is registered for as a guest which means that the new guest is already known.
     *
     * @param mailAddress - the mail address to get the {@link GuestAssignment}s from different contexts
     * @return List with all assignments the user is currently known as a guest
     * @throws OXException
     */
    List<GuestAssignment> getExistingAssignments(String mailAddress) throws OXException;

    /**
     * Tries to copy user information to the provided {@link UserImpl} param if there is an existing User for the given mail address within a different context. Returns true if the user object has been changed. Otherwise false
     *
     * @param userToUpdate - the {@link User} to enrich with existing information from users in other contexts that are registered for the given mail address
     * @param mailAddress - the mail address to verify if there is already a user in a different context existing
     * @return UserImpl copy if there is an existing one that could be copied based on the given mail address or <code>null</code>
     * @throws OXException
     */
    UserImpl createUserCopy(String mailAddress) throws OXException;

    /**
     * Tries to copy contact information to the provided {@link Contact} param if there is an existing contact for the given mail address within a different context. Returns true if the contact object has been changed. Otherwise false
     *
     * @param mailAddress - the mail address to verify if there is already a contact in a different context existing
     * @param contextId - the context id the new contact should be in
     * @param createdById - the user id of the share creator
     * @return {@link Contact} copy created based on the given mail address or null if no contact could be found
     * @throws OXException
     */
    Contact createContactCopy(String mailAddress, int contextId, int createdById) throws OXException;
}
