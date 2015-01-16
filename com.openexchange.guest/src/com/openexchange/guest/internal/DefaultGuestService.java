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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.commons.validator.routines.EmailValidator;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
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

    /**
     * Initializes a new {@link DefaultGuestService}.
     *
     * @param userService
     * @param contextService
     */
    public DefaultGuestService(UserService userService, ContextService contextService) {
        this.userService = userService;
        this.contextService = contextService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuest(String mailAddress, int contextId, int userId) throws OXException {
        check(mailAddress);

        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true);
        try {
            connectionHelper.start();

            int guestId = GuestStorage.getInstance().getGuestId(mailAddress, connectionHelper.getConnection());

            if (GuestStorage.getInstance().isAssignmentExisting(guestId, contextId, userId, connectionHelper.getConnection())) {
                LOG.info("Guest with mail address '{}' in context {} with id {} already existing. Will not add him to mapping as a new guest.", mailAddress, contextId, userId);
                return;
            }

            if (guestId != GuestStorage.NOT_FOUND) { // already existing, only add assignment
                GuestStorage.getInstance().addGuestAssignment(guestId, contextId, userId, connectionHelper.getConnection());
                return;
            }

            int newGuest = GuestStorage.getInstance().addGuest(mailAddress, connectionHelper.getConnection());
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

            final int relatedGuestId = GuestStorage.getInstance().getGuestId(contextId, userId, connectionHelper.getConnection()); // this has to happen before guest assignment is removed!

            if (relatedGuestId == GuestStorage.NOT_FOUND) {
                LOG.warn("Guest with context {} and user id {} cannot be removed! No internal guest to remove found.", contextId, userId);
                return;
            }

            GuestStorage.getInstance().removeGuestAssignment(relatedGuestId, contextId, userId, connectionHelper.getConnection());

            int numberOfAssignments = GuestStorage.getInstance().getNumberOfAssignments(relatedGuestId, connectionHelper.getConnection());
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

            List<Integer> removedGuests = GuestStorage.getInstance().removeGuests(contextId, connectionHelper.getConnection());

            for (int guestId : removedGuests) {
                int numberOfAssignments = GuestStorage.getInstance().getNumberOfAssignments(guestId, connectionHelper.getConnection());
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
            LOG.error("Password update for guests with mail address {} not possible. New Password is empty.");
            throw GuestExceptionCodes.PASSWORD_EMPTY_ERROR.create(mailAddress);
        }

        ConnectionHelper connectionHelper = new ConnectionHelper(GuestStorageServiceLookup.get(), true);
        try {
            connectionHelper.start();

            final int guestId = GuestStorage.getInstance().getGuestId(mailAddress, connectionHelper.getConnection());
            if (guestId == GuestStorage.NOT_FOUND) {
                LOG.warn("Guest for mail address {} not found. Cannot update password.", mailAddress);
                return;
            }

            List<Serializable> guestAssignments = GuestStorage.getInstance().getGuestAssignments(guestId, connectionHelper.getConnection());
            if (guestAssignments.size() == 0) {
                LOG.error("No assignment for the guest with mail address {} found. This might indicate incosistences as there is a guest existing without assignments. Guest id: {}.", mailAddress, guestId);
                throw GuestExceptionCodes.GUEST_WITHOUT_ASSIGNMENT_ERROR.create(mailAddress, Integer.toString(guestId));
            }

            String encodedPassword = null;
            try {
                encodedPassword = PasswordMech.BCRYPT.encode(password);

                for (Serializable assignment : guestAssignments) {
                    if (assignment instanceof GuestAssignment) {
                        GuestAssignment guestAssignment = (GuestAssignment) assignment;
                        if (guestId != guestAssignment.getGuestId()) {
                            continue;
                        }

                        int contextId = guestAssignment.getContextId();
                        int userId = guestAssignment.getUserId();
                        User storageUser = userService.getUser(userId, contextId);
                        UserImpl updatedUser = new UserImpl(storageUser);

                        updatedUser.setUserPassword(encodedPassword);
                        Context context = contextService.getContext(contextId);
                        userService.updateUser(connectionHelper.getConnection(), updatedUser, context);
                        connectionHelper.commit();  //TODO check if it is possible to commit multiple times

                        userService.invalidateUser(context, userId);
                    }
                }
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                throw GuestExceptionCodes.PASSWORD_RESET_ERROR.create(e, mailAddress);
            }

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
    private void check(String mailAddress) throws OXException {
        if ((mailAddress == null) || (!EmailValidator.getInstance().isValid(mailAddress))) {
            throw GuestExceptionCodes.INVALID_EMAIL_ADDRESS.create(mailAddress);
        }
    }
}
