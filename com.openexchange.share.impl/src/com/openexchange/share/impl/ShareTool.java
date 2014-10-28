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

package com.openexchange.share.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link ShareTool}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTool {

    public static final String SHARE_SERVLET = "share";

    private static final String GUEST_LAST_MODIFIED_ATTRIBUTE = "com.openexchange.guestLastModified";

    public static String getUserAttribute(User user, String name) {
        Map<String, Set<String>> attributes = user.getAttributes();
        if (attributes == null) {
            return null;
        }

        Set<String> match = attributes.get(name);
        if (match == null || match.isEmpty()) {
            return null;
        }

        return match.iterator().next();
    }

    /**
     * Gets the authentication mode applicable for the supplied guest user.
     *
     * @param guest The guest user
     * @return The authentication mode
     */
    public static AuthenticationMode getAuthenticationMode(User guest) {
        AuthenticationMode authMode = AuthenticationMode.ANONYMOUS;
        if (guest.getUserPassword() != null) {
            String passwordMech = guest.getPasswordMech();
            if (ShareCryptoService.PASSWORD_MECH_ID.equals(passwordMech)) {
                authMode = AuthenticationMode.ANONYMOUS_PASSWORD;
            } else {
                authMode = AuthenticationMode.GUEST_PASSWORD;
            }
        }
        return authMode;
    }

    /**
     * Gets the authentication mode applicable for the supplied share recipient.
     *
     * @param guest The guest user
     * @return The authentication mode, or <code>null</code> if the recipient does not denote guest authentication
     */
    public static AuthenticationMode getAuthenticationMode(ShareRecipient recipient) {

        switch (recipient.getType()) {
        case ANONYMOUS:
            return Strings.isEmpty(((AnonymousRecipient)recipient).getPassword()) ?
                AuthenticationMode.ANONYMOUS : AuthenticationMode.ANONYMOUS_PASSWORD;
        case GUEST:
            return AuthenticationMode.GUEST_PASSWORD;
        default:
            return null;
        }
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied share targets. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param recipient The share recipient
     * @param targets The share targets
     * @return The permission bits
     * @throws OXException
     */
    public static int getRequiredPermissionBits(ShareRecipient recipient, List<ShareTarget> targets) throws OXException {
        Set<Integer> modules = new HashSet<Integer>(targets.size());
        for (ShareTarget target : targets) {
            modules.add(target.getModule());
        }
        return getRequiredPermissionBits(getAuthenticationMode(recipient), modules);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied share. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param guest The guest user
     * @param shares The shares
     * @return The permission bits
     * @throws OXException
     */
    public static int getRequiredPermissionBits(User guest, List<Share> shares) throws OXException {
        Set<Integer> modules = new HashSet<Integer>(shares.size());
        for (Share share : shares) {
            if (null != share.getTarget()) {
                modules.add(share.getTarget().getModule());
            }
        }
        return getRequiredPermissionBits(guest, modules);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied modules. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param guest The guest user
     * @param modules The module identifiers
     * @return The permission bits
     * @throws OXException
     */
    public static int getRequiredPermissionBits(User guest, Collection<Integer> modules) throws OXException {
        return getRequiredPermissionBits(getAuthenticationMode(guest), modules);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied modules. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param guest The guest user
     * @param modules The module identifiers
     * @return The permission bits
     * @throws OXException
     */
    private static int getRequiredPermissionBits(AuthenticationMode authentication, Collection<Integer> modules) throws OXException {
        Set<Permission> perms = new HashSet<Permission>(8);
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        if (AuthenticationMode.GUEST_PASSWORD == authentication) {
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
     * @throws OXException
     */
    private static Set<Permission> addModulePermissions(Set<Permission> perms, int module) throws OXException {
        Module matchingModule = Module.getForFolderConstant(module);
        if (null == matchingModule) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unknwon module: " + module);
        }
        Permission modulePermission = matchingModule.getPermission();
        if (null == modulePermission) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No module permission for module " + matchingModule);
        }
        perms.add(modulePermission);
        return perms;
    }

    /**
     * Prepares a new share.
     *
     * @param contextID The context ID
     * @param sharingUser The sharing user
     * @param guestUserID The guest user ID
     * @param target The share target
     * @param recipient The recipient
     * @return The share
     */
    public static Share prepareShare(int contextID, User sharingUser, int guestUserID, ShareTarget target) {
        Date now = new Date();
        Share share = new Share();
        share.setTarget(target);
        share.setCreated(now);
        share.setModified(now);
        share.setCreatedBy(sharingUser.getId());
        share.setModifiedBy(sharingUser.getId());
        share.setGuest(guestUserID);
        return share;
    }

    /**
     * Prepares a guest user instance based on the supplied share recipient.
     *
     * @param services The service lookup reference
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @return The guest user
     * @throws OXException
     */
    public static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, ShareRecipient recipient) throws OXException {
        if (AnonymousRecipient.class.isInstance(recipient)) {
            return prepareGuestUser(services, sharingUser, (AnonymousRecipient) recipient);
        } else if (GuestRecipient.class.isInstance(recipient)) {
            return prepareGuestUser(services, sharingUser, (GuestRecipient) recipient);
        } else {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("unsupported share recipient: " + recipient);
        }
    }

    /**
     * Prepares a (named) guest user instance. If no password is defined in the supplied guest recipient, an auto-generated one is used.
     *
     * @param services The service lookup reference
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @return The guest user
     * @throws OXException
     */
    private static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, GuestRecipient recipient) throws OXException {
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName(recipient.getDisplayName());
        guestUser.setMail(recipient.getEmailAddress());
        guestUser.setLoginInfo(recipient.getEmailAddress());
        guestUser.setPasswordMech(PasswordMech.BCRYPT.getIdentifier());
        String password = Strings.isEmpty(recipient.getPassword()) ? PasswordUtility.generate() : recipient.getPassword();
        try {
            guestUser.setUserPassword(PasswordMech.BCRYPT.encode(password));
        } catch (UnsupportedEncodingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return guestUser;
    }

    /**
     * Prepares an anonymous guest user instance.
     *
     * @param services The service lookup reference
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @return The guest user
     * @throws OXException
     */
    private static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, AnonymousRecipient recipient) throws OXException {
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName("Guest");
        guestUser.setMail("");
        if (null != recipient.getPassword()) {
            guestUser.setUserPassword(services.getService(ShareCryptoService.class).encrypt(recipient.getPassword()));
            guestUser.setPasswordMech(ShareCryptoService.PASSWORD_MECH_ID);
        } else {
            guestUser.setPasswordMech("");
        }
        return guestUser;
    }

    /**
     * Prepares a guest user instance based on a "parent" sharing user.
     *
     * @param sharingUser The sharing user
     * @return The guest user
     */
    private static UserImpl prepareGuestUser(User sharingUser) {
        UserImpl guestUser = new UserImpl();
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
     * @param sharingUser The sharing user
     * @param guestUser The guest user
     * @return
     */
    public static Contact prepareGuestContact(User sharingUser, User guestUser) {
        Contact contact = new Contact();
        contact.setParentFolderID(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID);
        contact.setCreatedBy(sharingUser.getId());
        contact.setDisplayName(guestUser.getDisplayName());
        contact.setEmail1(guestUser.getMail());
        return contact;
    }

    /**
     * Filters out all expired shares from the supplied list.
     *
     * @param shares The shares to filter
     * @return The expired shares that were removed from the supplied list, or <code>null</code> if no shares were expired
     */
    public static List<Share> filterExpiredShares(List<Share> shares) {
        List<Share> expiredShares = null;
        if (null != shares && 0 < shares.size()) {
            Iterator<Share> iterator = shares.iterator();
            while (iterator.hasNext()) {
                Share share = iterator.next();
                if (null != share.getTarget() && share.getTarget().isExpired()) {
                    if (null == expiredShares) {
                        expiredShares = new ArrayList<Share>();
                    }
                    iterator.remove();
                    expiredShares.add(share);
                }
            }
        }
        return expiredShares;
    }

    public static Set<Integer> getGuestIDs(List<Share> shares) {
        if (null == shares || 0 == shares.size()) {
            return Collections.emptySet();
        }
        Set<Integer> guestIDs = new HashSet<Integer>();
        for (Share share : shares) {
            guestIDs.add(Integer.valueOf(share.getGuest()));
        }
        return guestIDs;
    }

    /**
     * Finds a share by its token in the supplied list of shares.
     *
     * @param shares The shares to search
     * @param token The token
     * @return The share, or <code>null</code> if not found
     */
//    public static Share findShare(List<Share> shares, String token) {
//        if (null != shares && 0 < shares.size()) {
//            for (ShareList share : shares) {
//                if (token.equals(share.getToken())) {
//                    return share;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Finds a share by its guest ID in the supplied list of shares.
     *
     * @param shares The shares to search
     * @param guestID The guest ID
     * @return The share, or <code>null</code> if not found
     */
    public static Share findShareByGuest(List<Share> shares, int guestID) {
        if (null != shares && 0 < shares.size()) {
            for (Share share : shares) {
                if (guestID == share.getGuest()) {
                    return share;
                }
            }
        }
        return null;
    }

    /**
     * Finds a share by its guest ID in the supplied list of shares.
     *
     * @param shares The shares to search
     * @param guestID The guest ID
     * @return The share, or <code>null</code> if not found
     */
    public static Map<Integer, List<Share>> mapSharesByGuest(List<Share> shares, int[] guests) {
        if (null == shares || 0 == shares.size() || null == guests || 0 == guests.length) {
            return Collections.emptyMap();
        }
        Map<Integer, List<Share>> sharesByGuest = new HashMap<Integer, List<Share>>(guests.length);
        for (Share share : shares) {
            Integer guest = I(share.getGuest());
            List<Share> guestShares = sharesByGuest.get(guest);
            if (null == guestShares) {
                guestShares = new ArrayList<Share>();
                sharesByGuest.put(guest, guestShares);
            }
            guestShares.add(share);
        }
        return sharesByGuest;
    }

    /**
     * Extracts all tokens from the supplied shares.
     *
     * @param shares The shares to get the tokens for
     * @return The tokens
     */
//    public static List<String> extractTokens(List<ShareList> shares) {
//        if (null == shares) {
//            return null;
//        }
//        List<String> tokens = new ArrayList<String>(shares.size());
//        for (ShareList share : shares) {
//            tokens.add(share.getToken());
//
//        }
//        return tokens;
//    }


    /**
     * Checks a share target for validity before saving or updating it, throwing an exception if validation fails.
     *
     * @param target The target to validate
     * @throws OXException
     */
    public static void validateTarget(ShareTarget target) throws OXException {
        if (0 == target.getOwnedBy()) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No owned by information specified in share target");
        }
        if (null == target.getItem() && null == target.getFolder()) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("No folder or item specified in share target");
        }
    }

    /**
     * Checks a share target for validity before saving or updating it, throwing an exception if validation fails.
     *
     * @param targets The targets to validate
     * @throws OXException
     */
    public static void validateTargets(Collection<ShareTarget> targets) throws OXException {
        for (ShareTarget target : targets) {
            validateTarget(target);
        }
    }

    /**
     * Gets whether this guest user was not modified since a given
     * date. The detection is based on the user attribute <code>com.openexchange.guestLastModified</code>.
     *
     * @param minLastModified The minimum date to check against.
     * @return <code>true</code> if this user has not been modified since the given date.
     */
    public static boolean userNotModifiedSince(User guest, Date minLastModified) {
        String attribute = getUserAttribute(guest, GUEST_LAST_MODIFIED_ATTRIBUTE);
        if (attribute == null) {
            return true;
        }
        try {
            Date lastModified = new Date(Long.parseLong(attribute));
            return lastModified.before(minLastModified);
        } catch (NumberFormatException e) {
            return true;
        }
    }

}
