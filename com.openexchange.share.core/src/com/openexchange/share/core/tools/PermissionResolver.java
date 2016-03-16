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

package com.openexchange.share.core.tools;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
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
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link PermissionResolver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PermissionResolver {

    private static final ContactField[] CONTACT_FIELDS = {
        ContactField.INTERNAL_USERID, ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED,
        ContactField.EMAIL1, ContactField.DISPLAY_NAME, ContactField.TITLE, ContactField.SUR_NAME, ContactField.GIVEN_NAME,
        ContactField.NUMBER_OF_IMAGES, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE_LAST_MODIFIED
    };

    private final ServiceLookup services;
    private final ServerSession session;
    private final Map<Integer, User> knownUsers;
    private final Map<Integer, GuestInfo> knownGuests;
    private final Map<Integer, Contact> knownUserContacts;
    private final Map<Integer, Group> knownGroups;

    /**
     * Initializes a new {@link PermissionResolver}.
     * <p/>
     * <b>Note: </b> The service lookup reference should provide access to the following services:
     * <ul>
     * <li>{@link ModuleSupport}</li>
     * <li>{@link ShareService}</li>
     * <li>{@link ContactUserStorage}</li>
     * <li>{@link GroupService}</li>
     * <li>{@link UserService}</li>
     * <li>{@link GroupService}</li>
     * <li>{@link ContactService}</li>
     * </ul>
     *
     * @param services The service lookup reference
     * @param session The server session
     */
    public PermissionResolver(ServiceLookup services, ServerSession session) {
        super();
        this.services = services;
        this.session = session;
        knownGroups = new HashMap<Integer, Group>();
        knownGuests = new HashMap<Integer, GuestInfo>();
        knownUserContacts = new HashMap<Integer, Contact>();
        knownUsers = new HashMap<Integer, User>();
    }

    /**
     * Gets information about the share behind a guest permission entity of a specific folder.
     *
     * @param folder The folder to get the share for
     * @param guestID The guest entity to get the share for
     * @return The share, or <code>null</code> if not found
     */
    public ShareInfo getShare(FolderObject folder, int guestID) {
        return getLink(folder.getModule(), String.valueOf(folder.getObjectID()), null, guestID);
    }

    /**
     * Gets information about the share behind a guest permission entity of a specific folder.
     *
     * @param folder The folder to get the share for
     * @param guestID The guest entity to get the share for
     * @return The share, or <code>null</code> if not found
     */
    public ShareInfo getShare(FileStorageFolder folder, int guestID) {
        return getLink(FolderObject.INFOSTORE, folder.getId(), null, guestID);
    }

    /**
     * Gets information about the share behind a guest permission entity of a specific folder.
     *
     * @param file The file to get the share for
     * @param guestID The guest entity to get the share for
     * @return The share, or <code>null</code> if not found
     */
    public ShareInfo getLink(File file, int guestID) {
        return getLink(FolderObject.INFOSTORE, file.getFolderId(), file.getId(), guestID);
    }

    /**
     * Gets information about the share behind a guest permission entity of a specific folder or file.
     *
     * @param moduleID The module identifier
     * @param folder The folder
     * @param item The item, or <code>null</code> if not applicable
     * @param guestID The guest entity to get the share for
     * @return The share, or <code>null</code> if not found
     */
    private ShareInfo getLink(int moduleID, String folder, String item, int guestID) {
        String module = services.getService(ModuleSupport.class).getShareModule(moduleID);
        ShareTarget target = new ShareTarget(moduleID, folder, item);
        try {
            return services.getService(ShareService.class).optLink(session, target);
        } catch (OXException e) {
            getLogger(PermissionResolver.class).error("Error getting share link for folder {}, item {} in module {}", folder, item, module, e);
        }
        return null;
    }

    /**
     * Gets a specific guest.
     *
     * @param guestID The identifier of the guest to get
     * @return The guest, or <code>null</code> if it can't be resolved
     */
    public GuestInfo getGuest(int guestID) {
        Integer key = I(guestID);
        GuestInfo guest = knownGuests.get(key);
        if (null == guest) {
            try {
                guest = services.getService(ShareService.class).getGuestInfo(session, guestID);
                if (guest != null) {
                    knownGuests.put(key, guest);
                }
            } catch (OXException e) {
                getLogger(PermissionResolver.class).error("Error getting guest {}", key, e);
            }
        }
        return guest;
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
                    getLogger(PermissionResolver.class).error("Error generating image URL for user {}", I(userID), e);
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
                getLogger(PermissionResolver.class).error("Error getting group {}", key, e);
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
                getLogger(PermissionResolver.class).error("Error getting user {}", key, e);
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
                    getLogger(PermissionResolver.class).debug("Error getting user contact {}", key, e);
                } else {
                    getLogger(PermissionResolver.class).error("Error getting user contact {}", key, e);
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
    public void cacheFileStorageFolderPermissionEntities(List<FileStorageFolder> folders) {
        /*
         * collect user- and group identifiers
         */
        Set<Integer> userIDs = new HashSet<Integer>();
        Set<Integer> groupIDs = new HashSet<Integer>();
        for (FileStorageFolder folder : folders) {
            List<FileStoragePermission> permissions = folder.getPermissions();
            if (null == permissions ) {
                continue;
            }
            for (FileStoragePermission permission : permissions) {
                if (permission.isGroup()) {
                    groupIDs.add(I(permission.getEntity()));
                } else {
                    userIDs.add(I(permission.getEntity()));
                }
            }
        }
        cachePermissionEntities(userIDs, groupIDs);
    }

    /**
     * Caches the permission entities found in the supplied list of folders.
     *
     * @param folders The folders to cache the permission entities for
     */
    public void cacheFolderPermissionEntities(List<FolderObject> folders) {
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
        cachePermissionEntities(userIDs, groupIDs);
    }

    /**
     * Caches the permission entities found in the supplied list of files.
     *
     * @param files The files to cache the permission entities for
     */
    public void cacheFilePermissionEntities(List<File> files) {
        /*
         * collect user- and group identifiers
         */
        Set<Integer> userIDs = new HashSet<Integer>();
        Set<Integer> groupIDs = new HashSet<Integer>();
        for (File file : files) {
            List<FileStorageObjectPermission> objectPermissions = file.getObjectPermissions();
            if (null == objectPermissions ) {
                continue;
            }
            for (FileStorageObjectPermission objectPermission : objectPermissions) {
                if (objectPermission.isGroup()) {
                    groupIDs.add(I(objectPermission.getEntity()));
                } else {
                    userIDs.add(I(objectPermission.getEntity()));
                }
            }
        }
        cachePermissionEntities(userIDs, groupIDs);
    }

    /**
     * Caches the permission entities found in the supplied list of folders.
     *
     * @param userIDs The identifiers of the users to cache
     * @param groupIDs The identifiers of the groups to cache
     */
    private void cachePermissionEntities(Set<Integer> userIDs, Set<Integer> groupIDs) {
        /*
         * fetch users & user contacts
         */
        if (0 < userIDs.size()) {
            List<Integer> guestUserIDs = new ArrayList<Integer>();
            List<Integer> regularUserIDs = new ArrayList<Integer>();
            try {
                for (User user : services.getService(UserService.class).getUser(session.getContext(), I2i(userIDs))) {
                    Integer id = I(user.getId());
                    knownUsers.put(id, user);
                    if (user.isGuest()) {
                        guestUserIDs.add(id);
                    } else {
                        regularUserIDs.add(id);
                    }
                }
            } catch (OXException e) {
                getLogger(PermissionResolver.class).error("Error getting users for permission entities", e);
            }
            if (0 < regularUserIDs.size()) {
                SearchIterator<Contact> searchIterator = null;
                try {
                    searchIterator = services.getService(ContactService.class).getUsers(session, I2i(regularUserIDs), CONTACT_FIELDS);
                    while (searchIterator.hasNext()) {
                        Contact userContact = searchIterator.next();
                        knownUserContacts.put(I(userContact.getInternalUserId()), userContact);
                    }
                } catch (OXException e) {
                    getLogger(PermissionResolver.class).error("Error getting user contacts for permission entities", e);
                } finally {
                    SearchIterators.close(searchIterator);
                }
            }
            if (0 < guestUserIDs.size()) {
                ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
                for (Integer guestID : guestUserIDs) {
                    try {
                        Contact guestContact = contactUserStorage.getGuestContact(session.getContextId(), i(guestID), CONTACT_FIELDS);
                        knownUserContacts.put(guestID, guestContact);
                    } catch (OXException e) {
                        getLogger(PermissionResolver.class).error("Error getting user contact for guest permission entity", e);
                    }
                }
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
                    getLogger(PermissionResolver.class).error("Error getting groups for permission entities", e);
                }
            }
        }
    }

}
