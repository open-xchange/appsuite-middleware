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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.b;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.DefaultContactsPermission;
import com.openexchange.contact.common.DefaultGroupwareContactsFolder;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.contact.ContactsStorageFolder;
import com.openexchange.folderstorage.contact.field.ContactsAccountErrorField;
import com.openexchange.folderstorage.contact.field.ContactsConfigField;
import com.openexchange.folderstorage.contact.field.ContactsProviderField;
import com.openexchange.folderstorage.contact.field.ExtendedPropertiesField;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link ContactsFolderConverter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public final class ContactsFolderConverter {

    public static final FolderField EXTENDED_PROPERTIES_FIELD = ExtendedPropertiesField.getInstance();
    public static final FolderField CONTACTS_CONFIG_FIELD = ContactsConfigField.getInstance();
    public static final FolderField CONTACTS_PROVIDER_FIELD = ContactsProviderField.getInstance();
    public static final FolderField CONTACTS_ACCOUNT_ERROR_FIELD = ContactsAccountErrorField.getInstance();

    /**
     * Checks if given folder belongs to default account.
     *
     * @param contactsFolder The folder to check
     * @return <code>true</code> if folder belongs to default account; otherwise <code>false</code>
     */
    public static boolean isDefaultAccountFolder(ContactsFolder contactsFolder) {
        String id = contactsFolder == null ? null : contactsFolder.getId();
        return id != null && id.startsWith(ContactUtil.DEFAULT_ACCOUNT_PREFIX);
    }

    /**
     * Converts a contacts folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param contactsFolder The contacts folder to convert
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, AccountAwareContactsFolder contactsFolder) {
        return getStorageFolder(treeId, contentType, contactsFolder, contactsFolder.getAccount());
    }

    /**
     * Converts a contacts folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param contactsFolder The contacts folder to convert
     * @param account The corresponding contacts account
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, ContactsFolder contactsFolder, ContactsAccount account) {
        return getStorageFolder(treeId, contentType, contactsFolder, account.getProviderId(), account.getAccountId(), account.getUserConfiguration());
    }

    /**
     * Converts a contacts folder into a folder storage compatible folder.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param contactsFolder The contacts folder to convert
     * @param providerId The identifier of the corresponding contacts provider
     * @param accountId The identifier of the corresponding contacts account
     * @param userConfig Arbitrary configuration data for the contacts account, or <code>null</code> if not applicable
     * @return The folder-storage compatible folder
     */
    public static ParameterizedFolder getStorageFolder(String treeId, ContentType contentType, ContactsFolder contactsFolder, String providerId, int accountId, JSONObject userConfig) {
        ParameterizedFolder folder = new ContactsStorageFolder(treeId, contentType, isDefaultAccountFolder(contactsFolder));
        folder.setAccountID(getQualifiedAccountID(accountId));
        folder.setID(contactsFolder.getId());
        folder.setLastModified(contactsFolder.getLastModified());
        folder.setName(contactsFolder.getName());
        folder.setPermissions(getStoragePermissions(contactsFolder.getPermissions()));
        folder.setSubscribed(null == contactsFolder.isSubscribed() || b(contactsFolder.isSubscribed()));
        folder.setUsedForSync(getStorageUsedForSync(contactsFolder.getUsedForSync()));
        folder.setSubfolderIDs(new String[0]);
        folder.setSubscribedSubfolders(false);
        folder.setProperty(EXTENDED_PROPERTIES_FIELD, contactsFolder.getExtendedProperties());
        folder.setProperty(CONTACTS_PROVIDER_FIELD, providerId);
        folder.setProperty(CONTACTS_CONFIG_FIELD, userConfig);
        folder.setProperty(CONTACTS_ACCOUNT_ERROR_FIELD, contactsFolder.getAccountError());
        if (GroupwareContactsFolder.class.isInstance(contactsFolder)) {
            GroupwareContactsFolder groupwareContactsFolder = (GroupwareContactsFolder) contactsFolder;
            folder.setCreatedBy(groupwareContactsFolder.getCreatedBy());
            folder.setCreationDate(groupwareContactsFolder.getCreationDate());
            folder.setModifiedBy(groupwareContactsFolder.getModifiedBy());
            folder.setParentID(groupwareContactsFolder.getParentId());
            folder.setType(getStorageType(groupwareContactsFolder.getType()));
            folder.setDefault(groupwareContactsFolder.isDefaultFolder());
            folder.setMeta(groupwareContactsFolder.getMeta());
        } else {
            folder.setParentID(FolderStorage.PRIVATE_ID);
            folder.setType(PrivateType.getInstance());
        }
        return folder;
    }

    /**
     * Converts multiple folders into folder storage compatible folders.
     *
     * @param treeId The identifier of the folder tree to take over
     * @param contentType The context type to take over
     * @param folders The folders to convert
     * @return The folder-storage compatible folders
     */
    public static List<Folder> getStorageFolders(String treeId, ContentType contentType, List<AccountAwareContactsFolder> folders) {
        if (null == folders) {
            return null;
        }
        return folders.stream().map(folder -> getStorageFolder(treeId, contentType, folder)).collect(Collectors.toList());
    }

    /**
     * Gets a groupware contacts folder representing the supplied folder storage folder.
     *
     * @param folder The storage folder as used by the folder service
     * @return The groupware contacts folder
     */
    public static DefaultGroupwareContactsFolder getContactsFolder(Folder folder) {
        DefaultGroupwareContactsFolder contactsFolder = new DefaultGroupwareContactsFolder();
        contactsFolder.setCreatedBy(folder.getCreatedBy());
        contactsFolder.setCreationDate(folder.getCreationDate());
        contactsFolder.setDefaultFolder(folder.isDefault());
        contactsFolder.setFolderType(getFolderType(folder.getType()));
        contactsFolder.setId(folder.getID());
        contactsFolder.setLastModified(folder.getLastModified());
        contactsFolder.setModifiedBy(folder.getModifiedBy());
        contactsFolder.setName(folder.getName());
        contactsFolder.setParentId(folder.getParentID());
        contactsFolder.setPermissions(getContactsPermissions(folder.getPermissions()));
        contactsFolder.setExtendedProperties(optExtendedProperties(folder));
        contactsFolder.setAccountError(optAccountError(folder));
        if (folder instanceof SetterAwareFolder) {
            if (((SetterAwareFolder) folder).containsSubscribed()) {
                contactsFolder.setSubscribed(B(folder.isSubscribed()));
            }
        } else {
            contactsFolder.setSubscribed(B(folder.isSubscribed()));
        }
        contactsFolder.setUsedForSync(getContactsUsedForSync(folder.getUsedForSync()));
        contactsFolder.setMeta(folder.getMeta());
        return contactsFolder;
    }

    /**
     * Converts a groupware contacts folder type into a folder-storage compatible type.
     *
     * @param type The groupware contacts folder type to convert
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
     * Converts a folder-storage type into the corresponding groupware contacts folder type.
     *
     * @param type The folder-storage type to convert
     * @return The groupware contacts folder type
     */
    public static GroupwareFolderType getFolderType(Type type) {
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
     * Converts the folder-storage's used-for-sync settings into its contacts provider compatible counterpart.
     *
     * @param usedForSync The storage folder's used-for-sync to convert
     * @return The contacts provider compatible used-for-sync
     */
    private static com.openexchange.contact.common.UsedForSync getContactsUsedForSync(com.openexchange.folderstorage.UsedForSync usedForSync) {
        return null == usedForSync ? null : new com.openexchange.contact.common.UsedForSync(usedForSync.isUsedForSync(), usedForSync.isProtected());
    }

    /**
     * Converts the contacts used-for-sync settings into its folder-storage compatible counterpart.
     *
     * @param usedForSync The contacts used-for-sync to convert
     * @return The folder-storage compatible used-for-sync
     */
    private static com.openexchange.folderstorage.UsedForSync getStorageUsedForSync(com.openexchange.contact.common.UsedForSync usedForSync) {
        return null == usedForSync ? null : new com.openexchange.folderstorage.UsedForSync(usedForSync.isUsedForSync(), usedForSync.isProtected());
    }

    /**
     * Converts a list of contacts permissions into an array of folder-storage compatible permissions.
     *
     * @param contactsPermissions The contacts permissions to convert
     * @return The folder-storage compatible permissions
     */
    private static Permission[] getStoragePermissions(List<ContactsPermission> contactsPermissions) {
        if (null == contactsPermissions) {
            return null;
        }
        Permission[] permissions = new Permission[contactsPermissions.size()];
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = getStoragePermission(contactsPermissions.get(i));
        }
        return permissions;
    }

    /**
     * Converts a contacts permission into a folder-storage compatible permission.
     *
     * @param contactsPermissions The contacts permission to convert
     * @return The folder-storage compatible permission
     */
    private static Permission getStoragePermission(ContactsPermission contactsPermissions) {
        BasicPermission permission = new BasicPermission();
        permission.setType(FolderPermissionType.NORMAL);
        permission.setEntity(contactsPermissions.getEntity());
        permission.setGroup(contactsPermissions.isGroup());
        permission.setAdmin(contactsPermissions.isAdmin());
        permission.setSystem(contactsPermissions.getSystem());
        permission.setAllPermissions(contactsPermissions.getFolderPermission(), contactsPermissions.getReadPermission(), contactsPermissions.getWritePermission(), contactsPermissions.getDeletePermission());
        return permission;
    }

    /**
     * Converts a folder-storage permission array into a list of contacts permission.
     *
     * @param permissions The folder-storage permissions to convert
     * @return The contacts permissions
     */
    private static List<ContactsPermission> getContactsPermissions(Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        List<ContactsPermission> contactsPermissions = new ArrayList<>(permissions.length);
        for (Permission permission : permissions) {
            contactsPermissions.add(getContactsPermission(permission));
        }
        return contactsPermissions;
    }

    /**
     * Converts a folder-storage permission into a contacts permission.
     *
     * @param permission The folder-storage permission to convert
     * @return The contacts permission
     */
    private static ContactsPermission getContactsPermission(Permission permission) {
        //@formatter:off
        return new DefaultContactsPermission(
            permission.getEntity(),
            permission.getFolderPermission(),
            permission.getReadPermission(),
            permission.getWritePermission(),
            permission.getDeletePermission(),
            permission.isAdmin(),
            permission.isGroup(),
            permission.getSystem()
        );
       // @formatter:on
    }

    private static String getQualifiedAccountID(int accountId) {
        return IDMangler.mangle("contacts", String.valueOf(accountId));
    }

    private static ExtendedProperties optExtendedProperties(Folder folder) {
        return optPropertyValue(folder, EXTENDED_PROPERTIES_FIELD, ExtendedProperties.class, null);
    }

    private static OXException optAccountError(Folder folder) {
        return optPropertyValue(folder, CONTACTS_ACCOUNT_ERROR_FIELD, OXException.class, null);
    }

    public static String optContactsProvider(Folder folder) {
        return optPropertyValue(folder, CONTACTS_PROVIDER_FIELD, String.class, null);
    }

    public static JSONObject optContactsConfig(Folder folder) {
        return optPropertyValue(folder, CONTACTS_CONFIG_FIELD, JSONObject.class, null);
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
                org.slf4j.LoggerFactory.getLogger(ContactsFolderConverter.class).warn("Unexpected value for folder field {}, falling back to {}.", field, defaultValue, e);
            }
        }
        return defaultValue;
    }
}
