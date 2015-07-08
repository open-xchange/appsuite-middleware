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

package com.openexchange.share.json.folders;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link ExtendedPermissionResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedPermissionResolver {

    private static final ContactField[] CONTACT_FIELDS = {
        ContactField.INTERNAL_USERID, ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED,
        ContactField.EMAIL1, ContactField.DISPLAY_NAME, ContactField.TITLE, ContactField.SUR_NAME, ContactField.GIVEN_NAME,
        ContactField.NUMBER_OF_IMAGES, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE_LAST_MODIFIED
    };

    private final ServiceLookup services;
    private final ServerSession session;
    private final Map<Integer, User> knownUsers;
    private final Map<Integer, Contact> knownUserContacts;
    private final Map<Integer, Group> knownGroups;

    /**
     * Initializes a new {@link ExtendedPermissionResolver}.
     *
     * @param services The service lookup reference
     * @param session The server session
     */
    public ExtendedPermissionResolver(ServiceLookup services, ServerSession session) {
        super();
        this.services = services;
        this.session = session;
        knownGroups = new HashMap<Integer, Group>();
        knownUserContacts = new HashMap<Integer, Contact>();
        knownUsers = new HashMap<Integer, User>();
    }

    /**
     * Gets information about the share behind a guest permission entity of a specific folder.
     *
     * @param folder The folder to get the share for
     * @param permission The guest permission entity to get the share for
     * @return The share, or <code>null</code> if not found
     */
    public ShareInfo getShare(FolderObject folder, OCLPermission permission) {
        String module = services.getService(ModuleSupport.class).getShareModule(folder.getModule());
        List<ShareInfo> shares = null;
        try {
            shares = services.getService(ShareService.class).getShares(session, module, String.valueOf(folder.getObjectID()), null);
        } catch (OXException e) {
            getLogger(ExtendedPermissionResolver.class).error("Error shares for folder {}", I(folder.getObjectID()), e);
        }
        if (null != shares && 0 < shares.size()) {
            for (ShareInfo share : shares) {
                if (share.getGuest().getGuestID() == permission.getEntity()) {
                    return share;
                }
            }
        }
        return null;
    }

    /**
     * Gets the URL for a user's contact image.
     *
     * @param userID The user to get the image URL for
     * @return The image URL, or <code>null</code> if not available
     */
    public String getImageURL(int userID) {
        Contact userContact = getUserContact(userID);
        if (null != userContact && 0 < userContact.getNumberOfImages()) {
            Pair<ImageDataSource, ImageLocation> imageData = ContactUtil.prepareImageData(userContact);
            if (null != imageData) {
                try {
                    return imageData.getFirst().generateUrl(imageData.getSecond(), session);
                } catch (OXException e) {
                    getLogger(ExtendedPermissionResolver.class).error("Error generating image URL for user {}", I(userID), e);
                }
            }
        }
        return null;
    }

    /**
     * Gets a specific group.
     *
     * @param groupID The identifier of the group to get
     * @return The group, or <code>null</code> if it can't be resolved
     */
    public Group getGroup(int groupID) {
        Integer key = I(groupID);
        Group group = knownGroups.get(key);
        if (null == group) {
            try {
                group = services.getService(GroupService.class).getGroup(session.getContext(), groupID);
                knownGroups.put(key, group);
            } catch (OXException e) {
                getLogger(ExtendedPermissionResolver.class).error("Error getting group {}", key, e);
            }
        }
        return group;
    }

    /**
     * Gets a specific user.
     *
     * @param userID The identifier of the user to get
     * @return The user, or <code>null</code> if it can't be resolved
     */
    public User getUser(int userID) {
        Integer key = I(userID);
        User user = knownUsers.get(key);
        if (null == user) {
            try {
                user = services.getService(UserService.class).getUser(userID, session.getContext());
                knownUsers.put(key, user);
            } catch (OXException e) {
                getLogger(ExtendedPermissionResolver.class).error("Error getting user {}", key, e);
            }
        }
        return user;
    }

    /**
     * Gets a specific user contact.
     *
     * @param userID The identifier of the user contact to get
     * @return The user contact, or <code>null</code> if it can't be resolved
     */
    public Contact getUserContact(int userID) {
        Integer key = I(userID);
        Contact userContact = knownUserContacts.get(key);
        if (null == userContact) {
            try {
                userContact = services.getService(ContactService.class).getUser(session, userID, CONTACT_FIELDS);
                knownUserContacts.put(key, userContact);
            } catch (OXException e) {
                if ("CON-0125".equals(e.getErrorCode())) {
                    getLogger(ExtendedPermissionResolver.class).debug("Error getting user contact {}", key, e);
                } else {
                    getLogger(ExtendedPermissionResolver.class).error("Error getting user contact {}", key, e);
                }
            }
        }
        return userContact;
    }

    /**
     * Caches the permission entities found in the supplied list of folders.
     *
     * @param folders The folders to cache the permission entities for
     */
    public void cachePermissionEntities(List<FolderObject> folders) {
        /*
         * collect user- and group identifiers
         */
        Set<Integer> userIDs = new HashSet<Integer>();
        Set<Integer> groupIDs = new HashSet<Integer>();
        for (FolderObject folder : folders) {
            List<OCLPermission> oclPermissions = folder.getPermissions();
            if (null == oclPermissions ) {
                continue;
            }
            for (OCLPermission oclPermission : oclPermissions) {
                if (oclPermission.isGroupPermission()) {
                    groupIDs.add(I(oclPermission.getEntity()));
                } else {
                    userIDs.add(I(oclPermission.getEntity()));
                }
            }
        }
        /*
         * fetch users & user contacts
         */
        if (0 < userIDs.size()) {
            int[] ids = I2i(userIDs);
            try {
                for (User user : services.getService(UserService.class).getUser(session.getContext(), ids)) {
                    knownUsers.put(I(user.getId()), user);
                }
            } catch (OXException e) {
                getLogger(ExtendedPermissionResolver.class).error("Error getting users for permission entities", e);
            }
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = services.getService(ContactService.class).getUsers(session, ids, CONTACT_FIELDS);
                while (searchIterator.hasNext()) {
                    Contact userContact = searchIterator.next();
                    knownUserContacts.put(I(userContact.getInternalUserId()), userContact);
                }
            } catch (OXException e) {
                getLogger(ExtendedPermissionResolver.class).error("Error getting user contacts for permission entities", e);
                e.printStackTrace();
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        /*
         * fetch groups
         */
        if (0 < groupIDs.size()) {
            GroupService groupService = services.getService(GroupService.class);
            for (Integer groupID : groupIDs) {
                try {
                    knownGroups.put(groupID, groupService.getGroup(session.getContext(), i(groupID)));
                } catch (OXException e) {
                    getLogger(ExtendedPermissionResolver.class).error("Error getting groups for permission entities", e);
                }
            }
        }
    }

}
