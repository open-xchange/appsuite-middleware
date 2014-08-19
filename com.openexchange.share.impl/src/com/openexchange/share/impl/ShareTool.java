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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.Guest;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;


/**
 * {@link ShareTool}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ShareTool {

    public static final String SHARE_SERVLET = "share";

    private static final long LOW_BITS = 0x00000000FFFFFFFFL;

    private static final long HIGH_BITS = 0xFFFFFFFF00000000L;

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
     * Gets permission bits suitable for a guest user being allowed to access a module.
     *
     * @param module The identifier of the module that should be added to the permissions
     * @return The permission bits
     */
    private static int getUserPermissionBits(int module) {
        Set<Permission> perms = new HashSet<Permission>();
        perms.add(Permission.DENIED_PORTAL);
        perms.add(Permission.EDIT_PUBLIC_FOLDERS);
//        perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        Permission modulePermission = Module.getForFolderConstant(module).getPermission();
        if (null != modulePermission) {
            perms.add(modulePermission);
        }
        return Permission.toBits(perms);
    }

    /**
     * Gets permission bits suitable for a guest user being allowed to access a module and folder type.
     *
     * @param module The identifier of the module that should be added to the permissions
     * @param type The identifier of the folder type (currently shared or public) that should be added to the permissions
     * @return The permission bits
     */
    public static int getUserPermissionBits(int module, int type) {
        Set<Permission> perms = new HashSet<Permission>();
        perms.add(Permission.DENIED_PORTAL);
        if (SharedType.getInstance().getType() == type) {
            perms.add(Permission.READ_CREATE_SHARED_FOLDERS);
        } else if (PublicType.getInstance().getType() == type) {
            perms.add(Permission.EDIT_PUBLIC_FOLDERS);
        } else {
            throw new UnsupportedOperationException("Unsupported type: " + type);
        }
        Permission modulePermission = Module.getForFolderConstant(module).getPermission();
        if (null != modulePermission) {
            perms.add(modulePermission);
        }
        return Permission.toBits(perms);
    }

    /**
     * Prepares a new share for a folder.
     *
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param module The module ID
     * @param folder The folder ID
     * @param expires The expiry date, or <code>null</code> if not defined
     * @param authenticationMode The authentication mode
     * @return The share
     */
    public static Share prepareShare(int contextID, User guestUser, int module, String folder, Date expires, AuthenticationMode authenticationMode) {
        Date now = new Date();
        DefaultShare share = new DefaultShare();
        share.setToken(ShareTool.generateToken(contextID));
        share.setAuthentication(authenticationMode.getID());
        share.setExpires(expires);
        share.setContextID(contextID);
        share.setCreated(now);
        share.setLastModified(now);
        share.setCreatedBy(guestUser.getCreatedBy());
        share.setModifiedBy(guestUser.getCreatedBy());
        share.setGuest(guestUser.getId());
        share.setModule(module);
        share.setFolder(folder);
        return share;
    }

    /**
     * Prepares a guest user instance.
     *
     * @param services The service lookup reference
     * @param sharingUser The sharing user
     * @param guest The guest description
     * @return The guest user
     * @throws OXException
     */
    public static UserImpl prepareGuestUser(ServiceLookup services, User sharingUser, Guest guest) throws OXException {
        UserImpl guestUser = new UserImpl();
        guestUser.setCreatedBy(sharingUser.getId());
        guestUser.setPreferredLanguage(sharingUser.getPreferredLanguage());
        guestUser.setTimeZone(sharingUser.getTimeZone());
        guestUser.setDisplayName(guest.getDisplayName());
        guestUser.setMailEnabled(true);
        guestUser.setPasswordMech("{CRYPTO_SERVICE}");
        AuthenticationMode authenticationMode = guest.getAuthenticationMode();
        if (authenticationMode != null && authenticationMode != AuthenticationMode.ANONYMOUS) {
            guestUser.setMail(guest.getMailAddress());
            guestUser.setUserPassword(services.getService(ShareCryptoService.class).encrypt(guest.getPassword()));
        } else {
            guestUser.setMail(""); // not null
        }
        if (false == Strings.isEmpty(guest.getContactID()) && false == Strings.isEmpty(guest.getContactFolderID())) {
            Map<String, Set<String>> attributes = guestUser.getAttributes();
            if (null == attributes) {
                attributes = new HashMap<String, Set<String>>(2);
            }
            attributes.put("com.openexchange.user.guestContactFolderID", Collections.singleton(guest.getContactFolderID()));
            attributes.put("com.openexchange.user.guestContactID", Collections.singleton(guest.getContactID()));
            guestUser.setAttributes(attributes);
        }
        return guestUser;
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
                if (share.isExpired()) {
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

}
