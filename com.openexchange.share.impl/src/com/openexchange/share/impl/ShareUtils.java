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

package com.openexchange.share.impl;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.AddressException;
import org.json.JSONException;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.passwordmechs.IPasswordMech;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.ShareConstants;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ShareUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareUtils {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ShareUtils}.
     *
     * @param services The service lookup reference
     */
    public ShareUtils(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type, throwing an appropriate excpetion if it's missing.
     *
     * @param clazz The service's class
     * @return The service
     */
    public <S extends Object> S requireService(Class<? extends S> clazz) throws OXException {
        S service = services.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.absentService(clazz);
        }
        return service;
    }

    /**
     * Gets the context where the session's user is located in.
     *
     * @param session The session
     * @return The context
     */
    public Context getContext(Session session) throws OXException {
        if (ServerSession.class.isInstance(session)) {
            return ((ServerSession) session).getContext();
        }
        return requireService(ContextService.class).getContext(session.getContextId());
    }

    /**
     * Gets the session's user.
     *
     * @param session The session
     * @return The user
     */
    public User getUser(Session session) throws OXException {
        if (ServerSession.class.isInstance(session)) {
            return ((ServerSession) session).getUser();
        }
        return requireService(UserService.class).getUser(session.getUserId(), session.getContextId());
    }

    /**
     * Prepares a guest user instance based on the supplied share recipient.
     *
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @param target The share target
     * @return The guest user
     */
    public UserImpl prepareGuestUser(int contextId, User sharingUser, ShareRecipient recipient, ShareTarget target) throws OXException {
        if (AnonymousRecipient.class.isInstance(recipient)) {
            return prepareGuestUser(sharingUser, (AnonymousRecipient) recipient, target);
        } else if (GuestRecipient.class.isInstance(recipient)) {
            return prepareGuestUser(contextId, sharingUser, (GuestRecipient) recipient);
        } else {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("unsupported share recipient: " + recipient);
        }
    }

    /**
     * Prepares a (named) guest user instance. If no password is defined in the supplied guest recipient, an auto-generated one is used.
     *
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @return The guest user
     */
    private UserImpl prepareGuestUser(int contextId, User sharingUser, GuestRecipient recipient) throws OXException {
        /*
         * extract and validate the recipient's e-mail address
         */
        String emailAddress;
        try {
            QuotedInternetAddress address = new QuotedInternetAddress(recipient.getEmailAddress(), true);
            emailAddress = address.getAddress();
        } catch (AddressException e) {
            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(recipient.getEmailAddress());
        }
        /*
         * try to lookup & reuse data from existing guest in other context via guest service
         */
        String groupId = requireService(ConfigViewFactory.class).getView(sharingUser.getId(), contextId).opt("com.openexchange.context.group", String.class, "default");
        UserImpl copiedUser = requireService(GuestService.class).createUserCopy(emailAddress, groupId, contextId);
        if (copiedUser != null) {
            return prepareGuestUser(sharingUser, copiedUser);
        }
        /*
         * prepare new guest user for recipient & set "was created" marker
         */
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName(recipient.getDisplayName());
        guestUser.setMail(emailAddress);
        guestUser.setLoginInfo(emailAddress);

        PasswordMechFactory passwordMechFactory = services.getService(PasswordMechFactory.class);
        IPasswordMech iPasswordMech = passwordMechFactory.get(IPasswordMech.BCRYPT);
        guestUser.setPasswordMech(iPasswordMech.getIdentifier());
        if (Strings.isNotEmpty(recipient.getPassword())) {
            guestUser.setUserPassword(iPasswordMech.encode(recipient.getPassword()));
        }
        return guestUser;
    }

    /**
     * Prepares an anonymous guest user instance.
     *
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @param target The link target
     * @return The guest user
     */
    private UserImpl prepareGuestUser(User sharingUser, AnonymousRecipient recipient, ShareTarget target) throws OXException {
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName("Guest");
        guestUser.setMail("");
        if (null != recipient.getPassword()) {
            String encodedPassword = services.getService(PasswordMechFactory.class).get(ShareConstants.PASSWORD_MECH_ID).encode(recipient.getPassword());
            guestUser.setUserPassword(encodedPassword);
            guestUser.setPasswordMech(ShareConstants.PASSWORD_MECH_ID);
        } else {
            guestUser.setPasswordMech("");
        }
        if (null != recipient.getExpiryDate()) {
            String expiryDateValue = String.valueOf(recipient.getExpiryDate().getTime());
            ShareTool.assignUserAttribute(guestUser, ShareTool.EXPIRY_DATE_USER_ATTRIBUTE, expiryDateValue);
        }
        try {
            ShareTool.assignUserAttribute(guestUser, ShareTool.LINK_TARGET_USER_ATTRIBUTE, ShareTool.targetToJSON(target).toString());
        } catch (JSONException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return guestUser;
    }

    /**
     * Prepares a guest user instance based on a "parent" sharing user.
     *
     * @param sharingUser The sharing user
     * @return The guest user
     */
    private UserImpl prepareGuestUser(User sharingUser) {
        UserImpl guestUser = new UserImpl();
        guestUser.setCreatedBy(sharingUser.getId());
        guestUser.setPreferredLanguage(sharingUser.getPreferredLanguage());
        guestUser.setTimeZone(sharingUser.getTimeZone());
        guestUser.setMailEnabled(true);
        ShareToken.assignBaseToken(guestUser);
        return guestUser;
    }

    /**
     * Prepares a guest user instance based on a "parent" sharing user.
     *
     * @param sharingUser The sharing user
     * @param guestUser The existing guest user to prepare
     * @return The guest user
     */
    private UserImpl prepareGuestUser(User sharingUser, UserImpl guestUser) {
        if (guestUser == null) {
            return prepareGuestUser(sharingUser);
        }
        guestUser.setCreatedBy(sharingUser.getId());
        guestUser.setPreferredLanguage(sharingUser.getPreferredLanguage());
        guestUser.setTimeZone(sharingUser.getTimeZone());
        guestUser.setMailEnabled(true);
        ShareToken.assignBaseToken(guestUser);
        return guestUser;
    }

    /**
     * Prepares a user contact for a guest user.
     *
     * @param contextId The context identifier
     * @param sharingUser The sharing user
     * @param guestUser The guest user
     * @return The guest contact
     */
    public Contact prepareGuestContact(int contextId, User sharingUser, User guestUser) throws OXException {
        String groupId = requireService(ConfigViewFactory.class).getView(sharingUser.getId(), contextId).opt("com.openexchange.context.group", String.class, "default");
        /*
         * try to lookup & reuse data from existing guest in other context via guest service
         */
        Contact copiedContact = requireService(GuestService.class).createContactCopy(guestUser.getMail(), groupId, contextId, guestUser.getId());
        if (null != copiedContact) {
            return copiedContact;
        }
        /*
         * prepare new contact for recipient
         */
        Contact contact = new Contact();
        contact.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        contact.setCreatedBy(sharingUser.getId());
        contact.setDisplayName(guestUser.getDisplayName());
        contact.setEmail1(guestUser.getMail());
        return contact;
    }

    /**
     * Sets a user's permission bits. This includes assigning initial permission bits, as well as updating already existing permissions.
     *
     * @param connection The database connection to use
     * @param context The context
     * @param userID The identifier of the user to set the permission bits for
     * @param permissionBits The permission bits to set
     * @param merge <code>true</code> to merge with the previously assigned permissions, <code>false</code> to overwrite
     * @return The updated permission bits
     */
    public UserPermissionBits setPermissionBits(Connection connection, Context context, int userID, int permissionBits, boolean merge) throws OXException {
        UserPermissionService userPermissionService = requireService(UserPermissionService.class);
        UserPermissionBits userPermissionBits = null;
        try {
            userPermissionBits = userPermissionService.getUserPermissionBits(connection, userID, context);
        } catch (OXException e) {
            if (false == UserConfigurationCodes.NOT_FOUND.equals(e)) {
                throw e;
            }
        }
        if (null == userPermissionBits) {
            /*
             * save permission bits
             */
            userPermissionBits = new UserPermissionBits(permissionBits, userID, context);
            userPermissionService.saveUserPermissionBits(connection, userPermissionBits);
        } else if (userPermissionBits.getPermissionBits() != permissionBits) {
            /*
             * update permission bits
             */
            userPermissionBits.setPermissionBits(merge ? permissionBits | userPermissionBits.getPermissionBits() : permissionBits);
            userPermissionService.saveUserPermissionBits(connection, userPermissionBits);
            /*
             * invalidate affected user configuration
             */
            requireService(UserConfigurationService.class).removeUserConfiguration(userID, context);
        }
        return userPermissionBits;
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access the supplied share target. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param recipient The share recipient
     * @param target The share target
     * @return The permission bits
     */
    public int getRequiredPermissionBits(ShareRecipient recipient, ShareTarget target) throws OXException {
        return getRequiredPermissionBits(ShareTool.getAuthenticationMode(recipient), Collections.singleton(target.getModule()));
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied modules. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param guest The guest user
     * @param modules The module identifiers
     * @return The permission bits
     */
    public int getRequiredPermissionBits(User guest, Collection<Integer> modules) throws OXException {
        return getRequiredPermissionBits(ShareTool.getAuthenticationMode(guest), modules);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied modules. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param guest The guest user
     * @param modules The module identifiers
     * @return The permission bits
     */
    private int getRequiredPermissionBits(AuthenticationMode authentication, Collection<Integer> modules) throws OXException {
        Set<Permission> perms = new HashSet<Permission>(8);
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        if (AuthenticationMode.GUEST == authentication || AuthenticationMode.GUEST_PASSWORD == authentication) {
            perms.add(Permission.EDIT_PASSWORD);
        }
        for (Integer module : modules) {
            addModulePermissions(perms, module.intValue());
        }
        return Permission.toBits(perms);
    }

    /**
     * Adds a module permission to the supplied permission set.
     *
     * @param perms The permission set
     * @param module The module to add the permissions for
     * @return The adjusted permission set
     */
    private Set<Permission> addModulePermissions(Set<Permission> perms, int module) throws OXException {
        Module matchingModule = Module.getForFolderConstant(module);
        if (null != matchingModule) {
            Permission modulePermission = matchingModule.getPermission();
            if (null == modulePermission) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No module permission for module " + matchingModule);
            }
            perms.add(modulePermission);
        }
        return perms;
    }
}
