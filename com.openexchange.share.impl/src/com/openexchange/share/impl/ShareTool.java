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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.util.UUIDs;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link ShareTool}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTool {

    public static final String SHARE_SERVLET = "share";

    private static final long LOW_BITS = 0x00000000FFFFFFFFL;
    private static final long HIGH_BITS = 0xFFFFFFFF00000000L;
    private static Pattern TOKEN_PATTERN = Pattern.compile("[a-f0-9]{32}", Pattern.CASE_INSENSITIVE);

    public static int extractContextId(String token) {
        UUID uuid = UUIDs.fromUnformattedString(token);
        long mostSignificantBits = uuid.getMostSignificantBits();
        return (int) ((mostSignificantBits &= HIGH_BITS) >>> 32);
    }

    public static String generateToken(int contextId) {
        UUID randomUUID = UUID.randomUUID();
        long mostSignificantBits = randomUUID.getMostSignificantBits();
        mostSignificantBits &= LOW_BITS;
        mostSignificantBits |= (((long)contextId) << 32);
        String token = UUIDs.getUnformattedString(new UUID(mostSignificantBits, randomUUID.getLeastSignificantBits()));
        return token;
    }

    public static String getShareUrl(Share share, String protocol, String fallbackHostname) {
        String hostname = getHostname(share.getCreatedBy(), share.getContextID());
        if (hostname == null) {
            hostname = fallbackHostname;
        }

        String prefix = getServletPrefix();
        return protocol + hostname + prefix + SHARE_SERVLET + '/' + share.getToken();
    }

    public static String getHostname(final int userID, final int contextID) {
        try {
            return ServiceCallWrapper.doServiceCall(ShareTool.class, HostnameService.class, new ServiceUser<HostnameService, String>() {
                @Override
                public String call(HostnameService service) throws Exception {
                    return service.getHostname(userID, contextID);
                }
            });
        } catch (ServiceException e) {
            return null;
        }
    }

    public static String getServletPrefix() {
        try {
            return ServiceCallWrapper.doServiceCall(ShareTool.class, DispatcherPrefixService.class, new ServiceUser<DispatcherPrefixService, String>() {
                @Override
                public String call(DispatcherPrefixService service) throws Exception {
                    return service.getPrefix();
                }
            });
        } catch (ServiceException e) {
           return DispatcherPrefixService.DEFAULT_PREFIX;
        }
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all supplied shares. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param module The identifier of the module that should be added to the permissions
     * @return The permission bits
     * @throws OXException
     */
    public static int getUserPermissionBits(List<Share> shares) throws OXException {
        Set<Permission> perms = new HashSet<Permission>(8);
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        for (Share share : shares) {
            if (AuthenticationMode.GUEST_PASSWORD == share.getAuthentication()) {
                perms.add(Permission.EDIT_PASSWORD);
            }
            for (ShareTarget target : share.getTargets()) {
                addModulePermissions(perms, target.getModule());
            }
        }
        return Permission.toBits(perms);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access all targets in a share.. Besides the concrete module
     * permission(s), this includes the permission bits to access shared and public folders, as well as the bit to turn off portal
     * access.
     *
     * @param share The share to build the permissions bits for
     * @return The permission bits
     * @throws OXException
     */
    public static int getUserPermissionBits(Share share) throws OXException {
        Set<Permission> perms = new HashSet<Permission>(8);
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        if (AuthenticationMode.GUEST_PASSWORD == share.getAuthentication()) {
            perms.add(Permission.EDIT_PASSWORD);
        }
        for (ShareTarget target : share.getTargets()) {
            addModulePermissions(perms, target.getModule());
        }
        return Permission.toBits(perms);
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
    public static int getUserPermissionBitsForTargets(ShareRecipient recipient, List<ShareTarget> targets) throws OXException {
        Set<Permission> perms = new HashSet<Permission>(8);
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        for (ShareTarget target : targets) {
            addModulePermissions(perms, target.getModule());
        }
        if (RecipientType.GUEST == recipient.getType()) {
            perms.add(Permission.EDIT_PASSWORD);
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
    public static Share prepareShare(int contextID, User sharingUser, int guestUserID, List<ShareTarget> targets, ShareRecipient recipient) {
        Date now = new Date();
        DefaultShare share = new DefaultShare();
        share.setToken(ShareTool.generateToken(contextID));
        share.setAuthentication(getAuthenticationMode(recipient));
        share.setTargets(targets);
        share.setContextID(contextID);
        share.setCreated(now);
        share.setLastModified(now);
        share.setCreatedBy(sharingUser.getId());
        share.setModifiedBy(sharingUser.getId());
        share.setGuest(guestUserID);
        return share;
    }

    private static AuthenticationMode getAuthenticationMode(ShareRecipient recipient) {
        if (AnonymousRecipient.class.isInstance(recipient)) {
            return null == ((AnonymousRecipient) recipient).getPassword() ?
                AuthenticationMode.ANONYMOUS : AuthenticationMode.ANONYMOUS_PASSWORD;
        }
        if (GuestRecipient.class.isInstance(recipient)) {
            return AuthenticationMode.GUEST_PASSWORD;
        }
        throw new IllegalArgumentException("recipient");
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
    public static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, AnonymousRecipient recipient) throws OXException {
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName("Guest");
        guestUser.setMail("");
        if (null != recipient.getPassword()) {
            guestUser.setUserPassword(services.getService(ShareCryptoService.class).encrypt(recipient.getPassword()));
            guestUser.setPasswordMech("{CRYPTO_SERVICE}");
        } else {
            guestUser.setPasswordMech("");
        }
        return guestUser;
    }

    /**
     * Prepares a (named) guest user instance.
     *
     * @param services The service lookup reference
     * @param sharingUser The sharing user
     * @param recipient The recipient description
     * @return The guest user
     * @throws OXException
     */
    public static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, GuestRecipient recipient) throws OXException {
        UserImpl guestUser = prepareGuestUser(sharingUser);
        guestUser.setDisplayName(recipient.getDisplayName());
        guestUser.setMail(recipient.getEmailAddress());
        guestUser.setLoginInfo(recipient.getEmailAddress());
        guestUser.setPasswordMech(PasswordMech.BCRYPT.getIdentifier());
        try {
            guestUser.setUserPassword(PasswordMech.BCRYPT.encode(recipient.getPassword()));
        } catch (UnsupportedEncodingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return guestUser;
    }

    private static UserImpl prepareGuestUser(User sharingUser) throws OXException {
        UserImpl guestUser = new UserImpl();
        guestUser.setCreatedBy(sharingUser.getId());
        guestUser.setPreferredLanguage(sharingUser.getPreferredLanguage());
        guestUser.setTimeZone(sharingUser.getTimeZone());
        guestUser.setMailEnabled(true);
        return guestUser;
    }

    /**
     * Filters out all expired shares from the supplied list.
     *
     * @param shares The shares to filter
     * @return The expired shares that were removed from the supplied list, or <code>null</code> if no shares were expired
     */
//    public static List<Share> filterExpiredShares(List<Share> shares) {
//        List<Share> expiredShares = null;
//        if (null != shares && 0 < shares.size()) {
//            Iterator<Share> iterator = shares.iterator();
//            while (iterator.hasNext()) {
//                Share share = iterator.next();
//                if (share.isExpired()) {
//                    if (null == expiredShares) {
//                        expiredShares = new ArrayList<Share>();
//                    }
//                    iterator.remove();
//                    expiredShares.add(share);
//                }
//            }
//        }
//        return expiredShares;
//    }

    /**
     * Finds a share by its token in the supplied list of shares.
     *
     * @param shares The shares to search
     * @param token The token
     * @return The share, or <code>null</code> if not found
     */
    public static Share findShare(List<Share> shares, String token) {
        if (null != shares && 0 < shares.size()) {
            for (Share share : shares) {
                if (token.equals(share.getToken())) {
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
     * Extracts all tokens from the supplied shares.
     *
     * @param shares The shares to get the tokens for
     * @return The tokens
     */
    public static List<String> extractTokens(List<Share> shares) {
        if (null == shares) {
            return null;
        }
        List<String> tokens = new ArrayList<String>(shares.size());
        for (Share share : shares) {
            tokens.add(share.getToken());

        }
        return tokens;
    }

    /**
     * Checks a token for validity, throwing an exception if validation fails.
     *
     * @param token The token to validate
     * @throws OXException
     */
    public static void validateToken(String token) throws OXException {
        if (false == TOKEN_PATTERN.matcher(token).matches()) {
            throw ShareExceptionCodes.INVALID_LINK.create(token);
        }
    }

}
