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

package com.openexchange.guest;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;

/**
 * {@link GuestService} defines methods to handle guests across context boundaries so, for instance, it is possible to set a password for a guest only once even it is a guest within multiple contexts.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface GuestService {

    /**
     * Returns if the current configuration/implementation is configured to deal with cross context handling based on the global database.
     *
     * @return <code>true</code>, if cross context handling is enabled; otherwise <code>false</code>
     */
    boolean isCrossContextGuestHandlingEnabled();

    /**
     * Authenticates the given password against the given user object.
     *
     * @param user user that password is compared with given one.
     * @param password password to check.
     * @return <code>true</code> if the password matches.
     * @throws OXException If password check mechanism has problems.
     */
    boolean authenticate(User user, int contextId, String password) throws OXException;

    /**
     * Adapts the given {@link User} based on the information stored within the global database (e. g. update user information for the password)
     *
     * @param user - user that should be aligned to cross context information
     * @param contextId - context id the user is assigned to
     * @return new {@link User} with the updated information
     * @throws OXException
     */
    User alignUserWithGuest(User user, int contextId) throws OXException;

    /**
     * Adds a new guest or creates a new assignment for the guest if the provided mail address is already registered because the user was added from another context.
     *
     * @param mailAddress - mail address of the guest to add
     * @param groupId - id of the group the guest is assigned to
     * @param contextId - context id the guest should be assigned to
     * @param userId - user id of the guest to add in the given context
     * @param password - password of the user to handle as guest
     * @param passwordMech - mechanism the password has been encrypted with
     * @throws OXException
     */
    void addGuest(String mailAddress, String groupId, int contextId, int userId, String password, String passwordMech) throws OXException;

    /**
     * Remove an existing guest. If the last assignment for a guest was removed even the complete guest will be removed.
     *
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
     * Remove existing guests for the given group id. This should only be called in case the group id isn't used any more.
     *
     * @param groupId - group id the guest should be removed for
     * @throws OXException
     */
    void removeGuests(String groupId) throws OXException;

    /**
     * Updates all existing contacts (over all contexts) that are available for the given contact.
     *
     * @param contact - contact whose information should be spread to other contexts
     * @param contextId - id of the context the contact to copy is registered
     * @throws OXException
     */
    void updateGuestContact(Contact contact, int contextId) throws OXException;

    /**
     * Updates all existing user (over all contexts) that are available for the given user.
     *
     * @param user - user whose information should be spread to other contexts
     * @param contextId - id of the context the user to copy is registered
     * @throws OXException
     */
    void updateGuestUser(User user, int contextId) throws OXException;

    /**
     * Tries to create a copy of a user if there is an existing user for the given mail address within a different context. Returns the user is there is at least one within a different context or null if there is no existing one.
     *
     * @param mailAddress - the mail address to verify if there is already a contact in a different context existing
     * @param groupId - the id of the group the guest is assigned to
     * @param contextId - the context id the new contact should be in
     * @return UserImpl copy if there is an existing one that could be copied based on the given mail address or <code>null</code>
     * @throws OXException
     */
    UserImpl createUserCopy(String mailAddress, String groupId, int contextId) throws OXException;

    /**
     * Tries to create a copy of a contact if there is an existing contact for the given mail address within a different context. Returns the contact is there is at least one within a different context or null if there is no existing one.
     *
     * @param mailAddress - the mail address to verify if there is already a contact in a different context existing
     * @param groupId - the id of the group the guest is assigned to
     * @param contextId - the context id the new contact should be in
     * @param createdById - the user id of the share creator
     * @return {@link Contact} copy created based on the given mail address or null if no contact could be found
     * @throws OXException
     */
    Contact createContactCopy(String mailAddress, String groupId, int contextId, int createdById) throws OXException;

    /**
     * Returns the {@link GuestAssignment}s the given mail address is registered for as a guest which means that the new guest is already known.
     *
     * @param mailAddress - the mail address to get the {@link GuestAssignment}s from different contexts
     * @param groupId - the id of the group the guest is assigned to
     * @return List with all assignments the user is currently known as a guest
     * @throws OXException
     */
    List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) throws OXException;
}
