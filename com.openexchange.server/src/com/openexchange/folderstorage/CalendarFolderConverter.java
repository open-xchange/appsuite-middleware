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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.folderstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.folderstorage.calendar.CalendarFolderStorage;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;

/**
 * {@link CalendarFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolderConverter {

    /**
     * Converts a calendar folder into a folder-storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param accountId The fully-qualified account identifier to take over
     * @param contentType The context type to take over
     * @param calendarFolder The calendar folder to convert
     * @return The folder-storage compatible folder
     */
    public static Folder getStorageFolder(String treeId, String accountId, ContentType contentType, CalendarFolder calendarFolder) {
        if (GroupwareCalendarFolder.class.isInstance(calendarFolder)) {
            return getStorageFolder(treeId, accountId, contentType, (GroupwareCalendarFolder) calendarFolder);
        }
        Folder folder = newStorageFolder(treeId, accountId, contentType, false);
        folder.setID(calendarFolder.getId());
        folder.setName(calendarFolder.getName());
        folder.setParentID(CalendarFolderStorage.PRIVATE_ID);
        folder.setType(PrivateType.getInstance());
        folder.setPermissions(getStoragePermissions(calendarFolder.getPermissions()));
        folder.setSubfolderIDs(new String[0]);
        folder.setSubscribedSubfolders(false);
        Map<String, Object> meta = new HashMap<String, Object>();
        String color = calendarFolder.getColor();
        if (null != color) {
            meta.put("color", color);
            meta.put("color_label", Integer.valueOf(Event2Appointment.getColorLabel(color)));
        }
        return folder;
    }

    /**
     * Converts a groupware calendar folder into a folder-storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param accountId The fully-qualified account identifier to take over
     * @param contentType The context type to take over
     * @param calendarFolder The groupware calendar folder to convert
     * @return The folder-storage compatible folder
     */
    public static Folder getStorageFolder(String treeId, String accountId, ContentType contentType, GroupwareCalendarFolder calendarFolder) {
        Folder folder = newStorageFolder(treeId, accountId, contentType, true);
        folder.setAccountID(accountId);
        folder.setID(calendarFolder.getId());
        folder.setName(calendarFolder.getName());
        folder.setCreatedBy(calendarFolder.getCreatedBy());
        folder.setCreationDate(calendarFolder.getCreationDate());
        folder.setLastModified(calendarFolder.getLastModified());
        folder.setModifiedBy(calendarFolder.getModifiedBy());
        folder.setParentID(calendarFolder.getParentId());
        folder.setPermissions(getStoragePermissions(calendarFolder.getPermissions()));
        folder.setType(getStorageType(calendarFolder.getType()));
        folder.setDefault(calendarFolder.isDefaultFolder());
        //        folder.setSupportedCapabilities(capabilities);
        Map<String, Object> meta = new HashMap<String, Object>();
        String color = calendarFolder.getColor();
        if (null != color) {
            meta.put("color", color);
            meta.put("color_label", Integer.valueOf(Event2Appointment.getColorLabel(color)));
        }
        folder.setMeta(meta);
        return folder;
    }

    /**
     * Converts a groupware calendar folder type into a folder-storage compatible type.
     *
     * @param type The groupware calendar folder type to convert
     * @return The folder-storage compatible type
     */
    public static Type getStorageType(GroupwareFolderType type) {
        if (null == type) {
            return null;
        }
        switch (type) {
            case PUBLIC:
                return PublicType.getInstance();
            case SHARED:
                return SharedType.getInstance();
            default:
                return PrivateType.getInstance();
        }
    }

    /**
     * Converts a list of calendar permissions into an array of folder-storage compatible permissions.
     *
     * @param calendarPermissions The calendar permissions to convert
     * @return The folder-storage compatible permissions
     */
    public static Permission[] getStoragePermissions(List<CalendarPermission> calendarPermissions) {
        if (null == calendarPermissions) {
            return null;
        }
        Permission[] permissions = new Permission[calendarPermissions.size()];
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = getStoragePermission(calendarPermissions.get(i));
        }
        return permissions;
    }

    /**
     * Converts a calendar permission into a folder-storage compatible permission.
     *
     * @param calendarPermission The calendar permission to convert
     * @return The folder-storage compatible permission
     */
    public static Permission getStoragePermission(CalendarPermission calendarPermission) {
        DefaultPermission permission = new DefaultPermission();
        permission.setEntity(calendarPermission.getEntity());
        permission.setGroup(calendarPermission.isGroup());
        permission.setAdmin(calendarPermission.isAdmin());
        permission.setSystem(calendarPermission.getSystem());
        permission.setAllPermissions(
            calendarPermission.getFolderPermission(),
            calendarPermission.getReadPermission(),
            calendarPermission.getWritePermission(),
            calendarPermission.getDeletePermission()
        );
        return permission;
    }

    /**
     * Gets a groupware calendar folder representing the supplied userized folder.
     *
     * @param folder The userized folder as used by the folder service
     * @return The groupware calendar folder
     */
    public static GroupwareCalendarFolder getCalendarFolder(Folder folder) {
        DefaultGroupwareCalendarFolder calendarFolder = new DefaultGroupwareCalendarFolder();
        calendarFolder.setCreatedBy(folder.getCreatedBy());
        calendarFolder.setCreationDate(folder.getCreationDate());
        calendarFolder.setDefaultFolder(folder.isDefault());
        calendarFolder.setFolderType(getCalendarType(folder.getType()));
        calendarFolder.setId(folder.getID());
        calendarFolder.setLastModified(folder.getLastModified());
        calendarFolder.setModifiedBy(folder.getModifiedBy());
        calendarFolder.setName(folder.getName());
        calendarFolder.setParentId(getCalendarParent(folder.getParentID()));
        calendarFolder.setPermissions(getCalendarPermissions(folder.getPermissions()));
        Map<String, Object> meta = folder.getMeta();
        if (null != meta) {
            Object colorValue = meta.get("color");
            if (null != colorValue && String.class.isInstance(colorValue)) {
                calendarFolder.setColor((String) colorValue);
            }
            Object colorLabelValue = meta.get("color_label");
            if (null != colorLabelValue && Integer.class.isInstance(colorLabelValue)) {
                calendarFolder.setColor(Appointment2Event.getColor(((Integer) colorLabelValue).intValue()));
            }
        }
        return calendarFolder;
    }

    /**
     * Converts a folder-storage type into the corresponding groupware calendar folder type.
     *
     * @param type The folder-storage type to convert
     * @return The groupware calendar folder type
     */
    public static GroupwareFolderType getCalendarType(Type type) {
        if (null == type) {
            return null;
        }
        if (PublicType.getInstance().equals(type)) {
            return GroupwareFolderType.PUBLIC;
        }
        if (SharedType.getInstance().equals(type)) {
            return GroupwareFolderType.SHARED;
        }
        return GroupwareFolderType.PRIVATE;
    }

    /**
     * Converts a folder-storage permission into a calendar permission.
     *
     * @param permission The folder-storage permission to convert
     * @return The calendar permission
     */
    public static CalendarPermission getCalendarPermission(Permission permission) {
        return new DefaultCalendarPermission(
            permission.getEntity(),
            permission.getFolderPermission(),
            permission.getReadPermission(),
            permission.getWritePermission(),
            permission.getDeletePermission(),
            permission.isAdmin(),
            permission.isGroup(),
            permission.getSystem()
        );
    }

    /**
     * Converts a folder-storage permission array into a list of calendar permission.
     *
     * @param permissions The folder-storage permissions to convert
     * @return The calendar permissions
     */
    public static List<CalendarPermission> getCalendarPermissions(Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        List<CalendarPermission> calendarPermissions = new ArrayList<CalendarPermission>(permissions.length);
        for (Permission permission : permissions) {
            calendarPermissions.add(getCalendarPermission(permission));
        }
        return calendarPermissions;
    }

    /**
     * Initializes a new folder as used by the internal folder storage.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param accountId The fully-qualified account identifier to take over
     * @param contentType The context type to take over
     * @param global <code>true</code> if the folder is globally unique, <code>false</code> if it is bound to a certain user only
     * @return A new folder instance
     */
    private static Folder newStorageFolder(String treeId, String accountId, ContentType contentType, final boolean global) {
        Folder folder = new AbstractFolder() {

            private static final long serialVersionUID = 4412370864216762652L;

            @Override
            public boolean isGlobalID() {
                return global;
            }
        };
        folder.setTreeID(treeId);
        folder.setAccountID(accountId);
        folder.setSubscribed(true);
        folder.setContentType(contentType);
        folder.setDefaultType(contentType.getModule());
        return folder;
    }

    private static String getCalendarParent(String storageParentId) {
        //        if (PRIVATE_FOLDER_ID.equals(storageParentId) || SHARED_FOLDER_ID.equals(storageParentId) || PUBLIC_FOLDER_ID.equals(storageParentId)) {
        //            return null;
        //        }
        return storageParentId;
    }

    /**
     * Initializes a new {@link CalendarFolderConverter}.
     */
    private CalendarFolderConverter() {
        super();
    }

}
