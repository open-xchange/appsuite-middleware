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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.AddressException;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.GuestExceptionCodes;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.impl.storage.GuestStorage;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
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


    private final ContactUserStorage contactUserStorage;

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link DefaultGuestService}.
     *
     * @param userService
     * @param contextService
     * @param contactUserStorage
     * @param configViewFactory
     */
    public DefaultGuestService(UserService userService, ContactUserStorage contactUserStorage, ConfigViewFactory configViewFactory) {
        this.userService = userService;
        this.contactUserStorage = contactUserStorage;
        this.configViewFactory = configViewFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuest(String mailAddress, String groupId, int contextId, int userId, String password, String passwordMech) throws OXException {
        check(mailAddress);

        GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), true, contextId);
        try {
            connectionHelper.start();

            long guestId = GuestStorage.getInstance().getGuestId(mailAddress, groupId, connectionHelper.getConnection());

            if (GuestStorage.getInstance().isAssignmentExisting(guestId, contextId, userId, connectionHelper.getConnection())) {
                LOG.info("Guest with mail address '{}' in context {} with id {} already existing. Will not add him to mapping as a new guest.", mailAddress, contextId, userId);
                return;
            }

            if (guestId != GuestStorage.NOT_FOUND) { // already existing, only add assignment
                GuestStorage.getInstance().addGuestAssignment(new GuestAssignment(guestId, contextId, userId, password, passwordMech), connectionHelper.getConnection());
                connectionHelper.commit();
                return;
            }

            long newGuest = GuestStorage.getInstance().addGuest(mailAddress, groupId, connectionHelper.getConnection());
            if (newGuest == GuestStorage.NOT_FOUND) {
                throw GuestExceptionCodes.GUEST_CREATION_ERROR.create();
            }
            GuestStorage.getInstance().addGuestAssignment(new GuestAssignment(newGuest, contextId, userId, password, passwordMech), connectionHelper.getConnection());

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
        GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), true, contextId);
        try {
            connectionHelper.start();

            String groupId = configViewFactory.getView(userId, contextId).opt("com.openexchange.context.group", String.class, "default");
            User user = userService.getUser(userId, contextId);
            final long relatedGuestId = GuestStorage.getInstance().getGuestId(user.getMail(), groupId, connectionHelper.getConnection()); // this has to happen before guest assignment is removed!

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
        GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), true, contextId);
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
     * Checks if the provided mail address is valid.
     *
     * @param mailAddress - address to validate
     * @throws OXException thrown if mail address is not valid
     */
    protected void check(String mailAddress) throws OXException {
        try {
            new QuotedInternetAddress(mailAddress, true);
        } catch (final AddressException e) {
            throw GuestExceptionCodes.INVALID_EMAIL_ADDRESS.create(mailAddress, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGuestUser(User user, int contextId) throws OXException {
        if (user == null) {
            throw GuestExceptionCodes.GUEST_UPDATE_ERROR.create();
        }

        int userId = user.getId();
        String groupId = configViewFactory.getView(userId, contextId).opt("com.openexchange.context.group", String.class, "default");
        List<GuestAssignment> guestAssignments = retrieveGuestAssignments(user.getMail(), groupId);

        if ((guestAssignments != null) && (!guestAssignments.isEmpty())) {
            GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), true, groupId);
            try {
                connectionHelper.start();

                for (GuestAssignment guestAssignment : guestAssignments) {
                    GuestAssignment newAssignment = new GuestAssignment(guestAssignment.getGuestId(), guestAssignment.getContextId(), guestAssignment.getUserId(), user.getUserPassword(), user.getPasswordMech());

                    GuestStorage.getInstance().updateGuestAssignment(newAssignment, connectionHelper.getConnection());
                }
                connectionHelper.commit();
            }
            finally {
                connectionHelper.finish();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGuestContact(Contact contact, int contextId) throws OXException {
        if (contact == null) {
            throw GuestExceptionCodes.GUEST_UPDATE_ERROR.create();
        }

        int userId = contact.getInternalUserId();
        String groupId = configViewFactory.getView(userId, contextId).opt("com.openexchange.context.group", String.class, "default");
        List<GuestAssignment> guestAssignments = retrieveGuestAssignments(contact.getEmail1(), groupId);

        if ((guestAssignments != null) && (!guestAssignments.isEmpty())) {
            for (GuestAssignment guestAssignment : guestAssignments) {

                ContextConnectionHelper contextConnectionHelper = new ContextConnectionHelper(GuestStorageServiceLookup.get(), true, guestAssignment.getContextId());
                try {
                    contextConnectionHelper.start();

                    updateContact(contact, contextConnectionHelper.getConnection(), guestAssignment);

                    contextConnectionHelper.commit();
                } finally {
                    contextConnectionHelper.finish();
                }
            }
        }
    }

    /**
     * Returns all assignments for the user associated with the mail address and group id.
     *
     * @param mailAddress - the mail address of the user to retrieve assignments for
     * @param groupId - the group id the user of the context is assigned to
     * @return List with {@link GuestAssignment}s
     * @throws OXException
     */
    protected List<GuestAssignment> retrieveGuestAssignments(String mailAddress, String groupId) throws OXException {
        List<GuestAssignment> guestAssignments = new ArrayList<GuestAssignment>();
        GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), false, groupId);
        try {
            connectionHelper.start();
            long guestId = GuestStorage.getInstance().getGuestId(mailAddress, groupId, connectionHelper.getConnection());

            guestAssignments.addAll(GuestStorage.getInstance().getGuestAssignments(guestId, connectionHelper.getConnection()));
            connectionHelper.commit();
        } finally {
            connectionHelper.finish();
        }
        return guestAssignments;
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
    protected void updateContact(Contact contact, Connection contextConnection, GuestAssignment assignment) throws OXException {
        ContactField[] contactFields = ContactField.values();
        Contact storageContact = contactUserStorage.getGuestContact(assignment.getContextId(), assignment.getUserId(), contactFields);

        Contact updatedContact = contact.clone();
        updatedContact.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        updatedContact.setCreatedBy(storageContact.getCreatedBy());
        updatedContact.setModifiedBy(storageContact.getModifiedBy());
        updatedContact.setContextId(storageContact.getContextId());
        updatedContact.setObjectID(storageContact.getObjectID());
        updatedContact.setEmail1(storageContact.getEmail1());
        updatedContact.setInternalUserId(storageContact.getInternalUserId());

        contactUserStorage.updateGuestContact(assignment.getContextId(), updatedContact.getObjectID(), updatedContact, contextConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserImpl createUserCopy(String mailAddress, String groupId, int contextId) throws OXException {
        List<GuestAssignment> existingAssignments = getExistingAssignments(mailAddress, groupId);

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
    public Contact createContactCopy(String mailAddress, String groupId, int contextId, int createdById) throws OXException {
        List<GuestAssignment> existingAssignments = getExistingAssignments(mailAddress, groupId);

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

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) throws OXException {
        if (Strings.isEmpty(mailAddress)) {
            LOG.warn("Provided mail address to get assignments for is empty. Return empty list.");
            return Collections.emptyList();
        }

        List<GuestAssignment> guestAssignments = new ArrayList<GuestAssignment>();

        GlobalDBConnectionHelper connectionHelper = new GlobalDBConnectionHelper(GuestStorageServiceLookup.get(), false, groupId);
        try {
            connectionHelper.start();

            final long guestId = GuestStorage.getInstance().getGuestId(mailAddress, groupId, connectionHelper.getConnection());
            if (guestId == GuestStorage.NOT_FOUND) {
                LOG.warn("Guest for mail address {} in group {} not found. Cannot update password.", mailAddress, groupId);
                return guestAssignments;
            }

            guestAssignments.addAll(GuestStorage.getInstance().getGuestAssignments(guestId, connectionHelper.getConnection()));
            if (guestAssignments.size() == 0) {
                LOG.error("No assignment for the guest with mail address {} in group {} found. This might indicate incosistences as there is a guest existing without assignments. Guest id: {}.", mailAddress, groupId, guestId);
                throw GuestExceptionCodes.GUEST_WITHOUT_ASSIGNMENT_ERROR.create(mailAddress, Long.toString(guestId));
            }
        } finally {
            connectionHelper.finish();
        }

        return guestAssignments;
    }
}
