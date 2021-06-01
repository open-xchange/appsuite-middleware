/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folderstorage;

import static com.openexchange.chronos.provider.CalendarCapability.getCapabilities;
import static com.openexchange.chronos.provider.CalendarCapability.getCapabilityNames;
import static com.openexchange.chronos.provider.composition.IDMangling.getQualifiedAccountId;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.b;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.AccountAwareCalendarFolder;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.UsedForSync;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.calendar.CalendarAccountErrorField;
import com.openexchange.folderstorage.calendar.CalendarConfigField;
import com.openexchange.folderstorage.calendar.CalendarFolderStorage;
import com.openexchange.folderstorage.calendar.CalendarProviderField;
import com.openexchange.folderstorage.calendar.CalendarStorageFolder;
import com.openexchange.folderstorage.calendar.ExtendedPropertiesField;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link CalendarFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolderConverter {

    public static final FolderField EXTENDED_PROPERTIES_FIELD = ExtendedPropertiesField.getInstance();
    public static final FolderField CALENDAR_CONFIG_FIELD = CalendarConfigField.getInstance();
    public static final FolderField CALENDAR_PROVIDER_FIELD = CalendarProviderField.getInstance();
    public static final FolderField CALENDAR_ACCOUNT_ERROR_FIELD = CalendarAccountErrorField.getInstance();

    /**
     * Checks if given folder belongs to default account.
     *
     * @param calendarFolder The folder to check
     * @return <code>true</code> if folder belongs to default account; otherwise <code>false</code>
     */
    public static boolean isDefaultAccountFolder(CalendarFolder calendarFolder) {
        String id = calendarFolder == null ? null : calendarFolder.getId();
        return id != null && id.startsWith(CalendarUtils.DEFAULT_ACCOUNT_PREFIX);
    }

    /**
     * Converts a calendar folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param calendarFolder The calendar folder to convert
     * @param providerId The identifier of the corresponding calendar provider
     * @param accountId The identifier of the corresponding calendar account
     * @param userConfig Arbitrary configuration data for the calendar account, or <code>null</code> if not applicable
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, CalendarFolder calendarFolder, String providerId, int accountId, JSONObject userConfig) {
        ParameterizedFolder folder = new CalendarStorageFolder(treeId, contentType, isDefaultAccountFolder(calendarFolder));
        folder.setAccountID(getQualifiedAccountId(accountId));
        folder.setID(calendarFolder.getId());
        folder.setLastModified(calendarFolder.getLastModified());
        folder.setName(calendarFolder.getName());
        folder.setPermissions(getStoragePermissions(calendarFolder.getPermissions()));
        folder.setSubscribed(null == calendarFolder.isSubscribed() || b(calendarFolder.isSubscribed()));
        folder.setUsedForSync(getStorageUsedForSync(calendarFolder.getUsedForSync()));
        folder.setSubfolderIDs(new String[0]);
        folder.setSubscribedSubfolders(false);
        folder.setProperty(EXTENDED_PROPERTIES_FIELD, calendarFolder.getExtendedProperties());
        folder.setProperty(CALENDAR_PROVIDER_FIELD, providerId);
        folder.setProperty(CALENDAR_CONFIG_FIELD, userConfig);
        folder.setProperty(CALENDAR_ACCOUNT_ERROR_FIELD, calendarFolder.getAccountError());
        folder.setSupportedCapabilities(getCapabilityNames(calendarFolder.getSupportedCapabilites()));
        if (GroupwareCalendarFolder.class.isInstance(calendarFolder)) {
            GroupwareCalendarFolder groupwareCalendarFolder = (GroupwareCalendarFolder) calendarFolder;
            folder.setCreatedFrom(groupwareCalendarFolder.getCreatedFrom());
            folder.setCreatedBy(null != folder.getCreatedFrom() ? folder.getCreatedFrom().getEntity() : 0);
            folder.setCreationDate(groupwareCalendarFolder.getCreationDate());
            folder.setModifiedFrom(groupwareCalendarFolder.getModifiedFrom());
            folder.setModifiedBy(null != folder.getModifiedFrom() ? folder.getModifiedFrom().getEntity() : 0);
            folder.setParentID(groupwareCalendarFolder.getParentId());
            folder.setType(getStorageType(groupwareCalendarFolder.getType()));
            folder.setDefault(groupwareCalendarFolder.isDefaultFolder());
            folder.setMeta(groupwareCalendarFolder.getMeta());
        } else {
            folder.setParentID(CalendarFolderStorage.PRIVATE_ID);
            folder.setType(PrivateType.getInstance());
        }
        return folder;
    }

    /**
     * Converts multiple calendar folders into folder storage compatible folders.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param calendarFolders The calendar folders to convert
     * @return The folder-storage compatible folders
     */
    public static List<Folder> getStorageFolders(String treeId, ContentType contentType, List<AccountAwareCalendarFolder> calendarFolders) {
        if (null == calendarFolders) {
            return null;
        }
        List<Folder> folders = new ArrayList<Folder>(calendarFolders.size());
        for (AccountAwareCalendarFolder calendarFolder : calendarFolders) {
            folders.add(getStorageFolder(treeId, contentType, calendarFolder));
        }
        return folders;
    }

    /**
     * Converts a calendar folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param calendarFolder The calendar folder to convert
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, AccountAwareCalendarFolder calendarFolder) {
        return getStorageFolder(treeId, contentType, calendarFolder, calendarFolder.getAccount());
    }

    /**
     * Converts a calendar folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param calendarFolder The calendar folder to convert
     * @param account The corresponding calendar account
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, CalendarFolder calendarFolder, CalendarAccount account) {
        return getStorageFolder(treeId, contentType, calendarFolder, account.getProviderId(), account.getAccountId(), account.getUserConfiguration());
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
     * Converts the calendar used-for-sync settings into its folder-storage compatible counterpart.
     *
     * @param usedForSync The calendar used-for-sync to convert
     * @return The folder-storage compatible used-for-sync
     */
    public static com.openexchange.folderstorage.UsedForSync getStorageUsedForSync(UsedForSync usedForSync) {
        return null == usedForSync ? null : new com.openexchange.folderstorage.UsedForSync(usedForSync.isUsedForSync(), usedForSync.isProtected());
    }

    /**
     * Converts a calendar permission into a folder-storage compatible permission.
     *
     * @param calendarPermission The calendar permission to convert
     * @return The folder-storage compatible permission
     */
    public static Permission getStoragePermission(CalendarPermission calendarPermission) {
        BasicPermission permission = new BasicPermission();
        permission.setType(FolderPermissionType.NORMAL);
        permission.setIdentifier(calendarPermission.getIdentifier());
        permission.setEntity(calendarPermission.getEntity());
        permission.setEntityInfo(calendarPermission.getEntityInfo());
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
     * Gets a groupware calendar folder representing the supplied folder storage folder.
     *
     * @param folder The storage folder as used by the folder service
     * @return The groupware calendar folder
     */
    public static DefaultGroupwareCalendarFolder getCalendarFolder(Folder folder) {
        DefaultGroupwareCalendarFolder calendarFolder = new DefaultGroupwareCalendarFolder();
        if (null != folder.getCreatedFrom()) {
            calendarFolder.setCreatedFrom(folder.getCreatedFrom());
        } else if (0 < folder.getCreatedBy()) {
            calendarFolder.setCreatedFrom(new EntityInfo(String.valueOf(folder.getCreatedBy()), null, null, null, null, null, folder.getCreatedBy(), null, EntityInfo.Type.USER));
        }
        if (null != folder.getModifiedFrom()) {
            calendarFolder.setModifiedFrom(folder.getModifiedFrom());
        } else if (0 < folder.getModifiedBy()) {
            calendarFolder.setModifiedFrom(new EntityInfo(String.valueOf(folder.getModifiedBy()), null, null, null, null, null, folder.getModifiedBy(), null, EntityInfo.Type.USER));
        }
        calendarFolder.setCreationDate(folder.getCreationDate());
        calendarFolder.setDefaultFolder(folder.isDefault());
        calendarFolder.setFolderType(getCalendarType(folder.getType()));
        calendarFolder.setId(folder.getID());
        calendarFolder.setLastModified(folder.getLastModified());
        calendarFolder.setModifiedFrom(folder.getModifiedFrom());
        calendarFolder.setName(folder.getName());
        calendarFolder.setParentId(folder.getParentID());
        calendarFolder.setPermissions(getCalendarPermissions(folder.getPermissions()));
        calendarFolder.setExtendedProperties(optExtendedProperties(folder));
        calendarFolder.setAccountError(optAccountError(folder));
        calendarFolder.setSupportedCapabilites(getCapabilities(folder.getSupportedCapabilities()));
        if(folder instanceof SetterAwareFolder) {
            if(((SetterAwareFolder) folder).containsSubscribed()) {
                calendarFolder.setSubscribed(B(folder.isSubscribed()));
            }
        } else {
            calendarFolder.setSubscribed(B(folder.isSubscribed()));
        }
        calendarFolder.setUsedForSync(getCalendarUsedForSync(folder.getUsedForSync()));
        calendarFolder.setMeta(folder.getMeta());
        return calendarFolder;
    }

    public static ExtendedProperties optExtendedProperties(Folder folder) {
        return optPropertyValue(folder, EXTENDED_PROPERTIES_FIELD, ExtendedProperties.class, null);
    }

    public static OXException optAccountError(Folder folder) {
        return optPropertyValue(folder, CALENDAR_ACCOUNT_ERROR_FIELD, OXException.class, null);
    }

    public static String optCalendarProvider(Folder folder) {
        return optPropertyValue(folder, CALENDAR_PROVIDER_FIELD, String.class, null);
    }

    public static JSONObject optCalendarConfig(Folder folder) {
        return optPropertyValue(folder, CALENDAR_CONFIG_FIELD, JSONObject.class, null);
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
            permission.getIdentifier(),
            permission.getEntity(),
            permission.getEntityInfo(),
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
     * Converts the folder-storage's used-for-sync settings into its calendar provider compatible counterpart.
     *
     * @param usedForSync The storage folder's used-for-sync to convert
     * @return The calendar provider compatible used-for-sync
     */
    public static UsedForSync getCalendarUsedForSync(com.openexchange.folderstorage.UsedForSync usedForSync) {
        return null == usedForSync ? null : new UsedForSync(usedForSync.isUsedForSync(), usedForSync.isProtected());
    }

    /**
     * Extracts the extended calendar folder properties from the supplied storage folder.
     *
     * @param folder The storage folder as used by the folder service
     * @return The extracted extended calendar properties, or <code>null</code> if not set
     */
    public static ExtendedProperties getExtendedProperties(Folder folder) {
        if (ParameterizedFolder.class.isInstance(folder)) {
            return optPropertyValue(((ParameterizedFolder) folder).getProperties(), EXTENDED_PROPERTIES_FIELD, ExtendedProperties.class, null);
        }
        return null;
    }

    /**
     * Applies an extended calendar property in the supplied storage folder. Any previous property value with the same name is replaced
     * implicitly, while others are untouched.
     *
     * @param folder The storage folder as used by the folder service
     * @param property The extended property to set
     * @return <code>true</code> if the folder was actually changed, <code>false</code>, otherwise
     */
    public static boolean setExtendedProperty(ParameterizedFolder folder, ExtendedProperty property) {
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(property);
        return setExtendedProperties(folder, extendedProperties, true);
    }

    /**
     * Applies an extended properties container in the supplied storage folder.
     *
     * @param folder The storage folder as used by the folder service
     * @param properties The extended properties to set
     * @param merge <code>true</code> to merge with an already existing properties container in the folder, <code>false</code> to replace
     * @return <code>true</code> if the folder was actually changed, <code>false</code>, otherwise
     */
    public static boolean setExtendedProperties(ParameterizedFolder folder, ExtendedProperties properties, boolean merge) {
        ExtendedProperties folderProperties = getExtendedProperties(folder);
        if (null == folderProperties || null == properties || false == merge) {
            folder.setProperty(EXTENDED_PROPERTIES_FIELD, properties);
            return true;
        }
        boolean changed = false;
        for (ExtendedProperty property : properties) {
            ExtendedProperty originalProperty = folderProperties.get(property.getName());
            if (null == originalProperty) {
                changed = folderProperties.add(property);
            } else if (false == originalProperty.equals(property)) {
                folderProperties.remove(originalProperty);
                changed = folderProperties.add(property);
            }
        }
        return changed;
    }

    /**
     * Gets a folder property value, falling back to a custom default value if not set.
     *
     * @param folder The folder to extract the extended property value from
     * @param field The folder field to get the value from
     * @param clazz The value's target type
     * @param defaultValue The default value to use as fallback if the field is not set
     * @return The property value, or the passed default value if not set
     */
    private static <T> T optPropertyValue(Folder folder, FolderField field, Class<T> clazz, T defaultValue) {
        if (null == folder || false == ParameterizedFolder.class.isInstance(folder)) {
            return null;
        }
        return optPropertyValue(((ParameterizedFolder) folder).getProperties(), field, clazz, defaultValue);
    }

    /**
     * Gets a folder property value, falling back to a custom default value if not set.
     *
     * @param folderProperties The folder properties to extract the value from
     * @param field The folder field to get the value from
     * @param clazz The value's target type
     * @param defaultValue The default value to use as fallback if the field is not set
     * @return The property value, or the passed default value if not set
     */
    private static <T> T optPropertyValue(Map<FolderField, FolderProperty> folderProperties, FolderField field, Class<T> clazz, T defaultValue) {
        if (null == folderProperties) {
            return null;
        }
        FolderProperty folderProperty = folderProperties.get(field);
        if (null != folderProperty && null != folderProperty.getValue()) {
            try {
                return clazz.cast(folderProperty.getValue());
            } catch (ClassCastException e) {
                org.slf4j.LoggerFactory.getLogger(CalendarFolderConverter.class).warn(
                    "Unexpected value for folder field {}, falling back to {}.", field, defaultValue, e);
            }
        }
        return defaultValue;
    }

    /**
     * Initializes a new {@link CalendarFolderConverter}.
     */
    private CalendarFolderConverter() {
        super();
    }

}
