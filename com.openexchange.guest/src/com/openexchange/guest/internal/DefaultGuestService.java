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

package com.openexchange.guest.internal;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.validator.routines.EmailValidator;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.GuestExceptionCodes;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.storage.GuestStorage;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.user.UserService;

/**
 * Default implementation of {@link GuestService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultGuestService implements GuestService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultGuestService.class);

    private final UserService userService;

    private final ContextService contextService;

    private final ContactUserStorage contactUserStorage;

    /**
     * Initializes a new {@link DefaultGuestService}.
     *
     * @param userService
     * @param contextService
     * @param contactUserStorage
     */
    public DefaultGuestService(UserService userService, ContextService contextService, ContactUserStorage contactUserStorage) {
        this.userService = userService;
        this.contextService = contextService;
        this.contactUserStorage = contactUserStorage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuest(String mailAddress, int contextId, int userId) throws OXException {
        if (Strings.isEmpty(mailAddress)) {
            LOG.info("Cannot add user with id {} in context {} as a guest as the provided mail address is empty.", userId, contextId);
            return;
        }

        check(mailAddress);

        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true);
        try {
            connectionHelper.start();

            long guestId = GuestStorage.getInstance().getGuestId(mailAddress, connectionHelper.getConnection());

            if (GuestStorage.getInstance().isAssignmentExisting(guestId, contextId, userId, connectionHelper.getConnection())) {
                LOG.info("Guest with mail address '{}' in context {} with id {} already existing. Will not add him to mapping as a new guest.", mailAddress, contextId, userId);
                return;
            }

            if (guestId != GuestStorage.NOT_FOUND) { // already existing, only add assignment
                GuestStorage.getInstance().addGuestAssignment(guestId, contextId, userId, connectionHelper.getConnection());
                connectionHelper.commit();
                return;
            }

            long newGuest = GuestStorage.getInstance().addGuest(mailAddress, connectionHelper.getConnection());
            if (newGuest == GuestStorage.NOT_FOUND) {
                throw GuestExceptionCodes.GUEST_CREATION_ERROR.create();
            }
            GuestStorage.getInstance().addGuestAssignment(newGuest, contextId, userId, connectionHelper.getConnection());

            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGuest(int contextId, int userId) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true);
        try {
            connectionHelper.start();

            final long relatedGuestId = GuestStorage.getInstance().getGuestId(contextId, userId, connectionHelper.getConnection()); // this has to happen before guest assignment is removed!

            if (relatedGuestId == GuestStorage.NOT_FOUND) {
                LOG.info("Guest with context {} and user id {} cannot be removed! No internal guest to remove found.", contextId, userId);
                return;
            }

            GuestStorage.getInstance().removeGuestAssignment(relatedGuestId, contextId, userId, connectionHelper.getConnection());

            long numberOfAssignments = GuestStorage.getInstance().getNumberOfAssignments(relatedGuestId, connectionHelper.getConnection());
            if ((numberOfAssignments == 0) && (relatedGuestId != GuestStorage.NOT_FOUND)) {
                GuestStorage.getInstance().removeGuest(relatedGuestId, connectionHelper.getConnection());
            }

            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void removeGuests(int contextId) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true);
        try {
            connectionHelper.start();

            List<Long> affectedGuests = GuestStorage.getInstance().resolveGuestAssignments(contextId, connectionHelper.getConnection());
            if (affectedGuests.isEmpty()) {
                return;
            }

            GuestStorage.getInstance().removeGuestAssignments(contextId, connectionHelper.getConnection());

            for (long guestId : affectedGuests) {
                long numberOfAssignments = GuestStorage.getInstance().getNumberOfAssignments(guestId, connectionHelper.getConnection());
                if ((numberOfAssignments == 0) && (guestId != GuestStorage.NOT_FOUND)) {
                    GuestStorage.getInstance().removeGuest(guestId, connectionHelper.getConnection());
                }
            }

            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String mailAddress, String password) throws OXException {
        check(mailAddress);

        if (Strings.isEmpty(password)) {
            LOG.error("Password update for guests with mail address {} not possible. New password should be set to empty.", mailAddress);
            throw GuestExceptionCodes.PASSWORD_EMPTY_ERROR.create(mailAddress);
        }

        List<GuestAssignment> guestAssignments = getExistingAssignments(mailAddress);

        String encodedPassword = null;
        try {
            encodedPassword = PasswordMech.BCRYPT.encode(password);

            for (GuestAssignment guestAssignment : guestAssignments) {
                int contextId = guestAssignment.getContextId();
                int userId = guestAssignment.getUserId();

                User storageUser = userService.getUser(userId, contextId);
                UserImpl updatedUser = new UserImpl(storageUser);

                updatedUser.setUserPassword(encodedPassword);
                Context context = contextService.getContext(contextId);
                userService.updateUser(updatedUser, context);
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw GuestExceptionCodes.PASSWORD_RESET_ERROR.create(e, mailAddress);
        }
    }

    /**
     * Checks if the provided mail address is valid.
     *
     * @param mailAddress - address to validate
     * @throws OXException thrown if mail address is not valid
     */
    private void check(String mailAddress) throws OXException {
        if ((mailAddress == null) || (!EmailValidator.getInstance().isValid(mailAddress))) {
            throw GuestExceptionCodes.INVALID_EMAIL_ADDRESS.create(mailAddress);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGuest(User user, int contextId) throws OXException {
        List<GuestAssignment> guestAssignments = retrieveGuestAssignments(contextId, user.getId());

        ContactField[] contactFields = ContactField.values();
        Contact baseContact = contactUserStorage.getGuestContact(contextId, user.getId(), contactFields);

        if (guestAssignments != null) {
            for (GuestAssignment guestAssignment : guestAssignments) {

                ConnectionHelper contextConnectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true, guestAssignment.getContextId());
                try {
                    contextConnectionHelper.start();

                    updateUser(user, contextConnectionHelper.getConnection(), guestAssignment);
                    updateContact(baseContact, contextConnectionHelper.getConnection(), guestAssignment);

                    contextConnectionHelper.commit();
                } finally {
                    contextConnectionHelper.finish();
                }
            }
        }
    }

    /**
     * Returns all assignments for the user associated with the given context and user id.
     *
     * @param contextId - context id to find the guest for
     * @param userId - user id to find the guest for
     * @return
     * @throws OXException
     */
    private List<GuestAssignment> retrieveGuestAssignments(int contextId, int userId) throws OXException {
        List<GuestAssignment> guestAssignments = new ArrayList<GuestAssignment>();
        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), false);
        try {
            connectionHelper.start();
            long guestId = GuestStorage.getInstance().getGuestId(contextId, userId, connectionHelper.getConnection());

            guestAssignments.addAll(GuestStorage.getInstance().getGuestAssignments(guestId, connectionHelper.getConnection()));
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        return guestAssignments;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<GuestAssignment> getExistingAssignments(String mailAddress) throws OXException {
        List<GuestAssignment> guestAssignments = new ArrayList<GuestAssignment>();

        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), false);
        try {
            connectionHelper.start();

            final long guestId = GuestStorage.getInstance().getGuestId(mailAddress, connectionHelper.getConnection());
            if (guestId == GuestStorage.NOT_FOUND) {
                LOG.warn("Guest for mail address {} not found. Cannot update password.", mailAddress);
                return guestAssignments;
            }

            guestAssignments.addAll(GuestStorage.getInstance().getGuestAssignments(guestId, connectionHelper.getConnection()));
            if (guestAssignments.size() == 0) {
                LOG.error("No assignment for the guest with mail address {} found. This might indicate incosistences as there is a guest existing without assignments. Guest id: {}.", mailAddress, guestId);
                throw GuestExceptionCodes.GUEST_WITHOUT_ASSIGNMENT_ERROR.create(mailAddress, Long.toString(guestId));
            }
        } finally {
            connectionHelper.finish();
        }

        return guestAssignments;
    }

    /**
     * Copies information from the given {@link User} to the {@link User} that is associated with the given {@link GuestAssignment}. To be able to update the user within the correct context the provided {@link Connection} should be valid for
     * the context id provided within the {@link GuestAssignment}<br>
     * <br>
     * Currently only the password, passwordMech, timezone and mail address are updated for the given user.
     *
     * @param user - the {@link User} the information should be copied from
     * @param contextConnection - the {@link Connection} with up to date information
     * @param assignment - the assignment that should be updated
     * @throws OXException
     */
    private void updateUser(User user, Connection contextConnection, GuestAssignment assignment) throws OXException {
        Context context = contextService.getContext(assignment.getContextId());
        User baseUser = userService.getUser(contextConnection, assignment.getUserId(), context);
        UserImpl updatedUser = new UserImpl(baseUser);

        updatedUser.setUserPassword(user.getUserPassword());
        updatedUser.setPasswordMech(user.getPasswordMech());
        updatedUser.setTimeZone(user.getTimeZone());
        updatedUser.setMail(user.getMail());

        userService.updateUser(contextConnection, updatedUser, context);
        userService.invalidateUser(context, updatedUser.getId());
    }

    /**
     * Copies information from the given {@link Contact} to the {@link Contact} that is associated with the given {@link GuestAssignment}. To be able to update the contact within the correct context the provided {@link Connection} should be valid for
     * the context id provided within the {@link GuestAssignment}
     *
     * @param contact - the {@link Contact} that should be copied
     * @param contextConnection - the {@link Connection} with up to date information.
     * @param assignment - the assignment that should be updated
     * @throws OXException
     */
    private void updateContact(Contact contact, Connection contextConnection, GuestAssignment assignment) throws OXException {
        ContactField[] contactFields = ContactField.values();
        Contact contactToUpdate = contactUserStorage.getGuestContact(assignment.getContextId(), assignment.getUserId(), contactFields);

        Contact updatedContact = contactToUpdate.clone();
        updatedContact.setDisplayName(contact.getDisplayName());
        updatedContact.setBirthday(contact.getBirthday());

        contactUserStorage.updateGuestContact(assignment.getContextId(), updatedContact.getObjectID(), updatedContact, contextConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserImpl createUserCopy(String mailAddress) throws OXException {
        List<GuestAssignment> existingAssignments = getExistingAssignments(mailAddress);

        if ((existingAssignments == null) || (existingAssignments.isEmpty())) {
            return null;
        }
        GuestAssignment existingAssignment = existingAssignments.get(0);
        User existingUser = userService.getUser(existingAssignment.getUserId(), existingAssignment.getContextId());

        UserImpl user = new UserImpl(existingUser);
        user.setDisplayName(existingUser.getDisplayName());
        user.setMail(existingUser.getMail());
        user.setLoginInfo(existingUser.getMail());
        user.setPasswordMech(existingUser.getPasswordMech());
        user.setUserPassword(existingUser.getUserPassword());
        user.setTimeZone(existingUser.getTimeZone());
        user.setAliases(existingUser.getAliases());
        user.setPreferredLanguage(existingUser.getPreferredLanguage());

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact createContactCopy(String mailAddress, int contextId, int createdById) throws OXException {
        List<GuestAssignment> existingAssignments = getExistingAssignments(mailAddress);

        if ((existingAssignments == null) || (existingAssignments.isEmpty())) {
            return null;
        }

        GuestAssignment existingAssignment = existingAssignments.get(0);

        ContactField[] contactFields = ContactField.values();
        Contact existingContact = contactUserStorage.getGuestContact(existingAssignment.getContextId(), existingAssignment.getUserId(), contactFields);

        Contact contactCopy = existingContact.clone();
        contactCopy.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        contactCopy.setCreatedBy(createdById);
        contactCopy.setContextId(contextId);
        contactCopy.setEmail1(mailAddress);

        return contactCopy;
    }
}
