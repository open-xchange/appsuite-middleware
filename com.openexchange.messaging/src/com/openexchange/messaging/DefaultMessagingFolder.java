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

package com.openexchange.messaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link DefaultMessagingFolder} - The default messaging folder providing setter methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultMessagingFolder implements MessagingFolder {

    private static final long serialVersionUID = -4838573824386482477L;

    private boolean b_defaultFolderType;

    private DefaultFolderType defaultFolderType;

    private Set<String> capabilities;

    private int deletedMessageCount;

    private String id;

    private int messageCount;

    private String name;

    private int newMessageCount;

    private MessagingPermission ownPermission;

    private String parentId;

    private List<MessagingPermission> permissions;

    private int unreadMessageCount;

    private boolean subfolders;

    private boolean subscribedSubfolders;

    private boolean defaultFolder;

    private boolean holdsFolders;

    private boolean holdsMessages;

    private boolean rootFolder;

    private boolean b_rootFolder;

    private boolean subscribed;

    private boolean b_subscribed;

    private boolean b_holdsFolders;

    private boolean b_holdsMessages;

    private boolean b_defaultFolder;

    private boolean b_subscribedSubfolders;

    private boolean b_subfolders;

    private boolean exists;

    private char separator;

    private boolean b_separator;

    /**
     * Initializes a new {@link DefaultMessagingFolder}.
     */
    public DefaultMessagingFolder() {
        super();
        deletedMessageCount = -1;
        messageCount = -1;
        unreadMessageCount = -1;
        newMessageCount = -1;
    }

    @Override
    public boolean containsDefaultFolderType() {
        return b_defaultFolderType;
    }

    @Override
    public DefaultFolderType getDefaultFolderType() {
        return defaultFolderType;
    }

    /**
     * Sets the default folder type.
     *
     * @param defaultFolderType The default folder type
     */
    public void setDefaultFolderType(final DefaultFolderType defaultFolderType) {
        this.defaultFolderType = defaultFolderType;
        b_defaultFolderType = true;
    }

    /**
     * Removes the default folder type.
     */
    public void removeDefaultFolderType() {
        defaultFolderType = null;
        b_defaultFolderType = false;
    }

    /**
     * Gets the capabilities of this folder; e.g <code>"QUOTA"</code>, <code>"PERMISSIONS"</code>, etc.
     *
     * @return The list of capabilities or <code>null</code> if not set
     */
    @Override
    public Set<String> getCapabilities() {
        if (null == capabilities) {
            return null;
        }
        return new HashSet<String>(capabilities);
    }

    /**
     * Sets the capabilities.
     *
     * @param capabilities The capabilities
     */
    public void setCapabilities(final Set<String> capabilities) {
        if (capabilities == null) {
            this.capabilities = null;
        } else {
            this.capabilities = new HashSet<String>(capabilities);
        }
    }

    @Override
    public int getDeletedMessageCount() {
        return deletedMessageCount;
    }

    /**
     * Sets the deleted message count.
     *
     * @param deletedMessageCount The deleted message count
     */
    public void setDeletedMessageCount(final int deletedMessageCount) {
        this.deletedMessageCount = deletedMessageCount;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the folder identifier.
     *
     * @param id The folder identifier
     */
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Sets the message count.
     *
     * @param messageCount The message count
     */
    public void setMessageCount(final int messageCount) {
        this.messageCount = messageCount;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int getNewMessageCount() {
        return newMessageCount;
    }

    /**
     * Sets the new message count.
     *
     * @param newMessageCount The new message count
     */
    public void setNewMessageCount(final int newMessageCount) {
        this.newMessageCount = newMessageCount;
    }

    @Override
    public MessagingPermission getOwnPermission() {
        return ownPermission;
    }

    /**
     * Sets the own permission.
     *
     * @param ownPermission The own permission
     */
    public void setOwnPermission(final MessagingPermission ownPermission) {
        this.ownPermission = ownPermission;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parent identifier.
     *
     * @param parentId The parent identifier
     */
    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    @Override
    public List<MessagingPermission> getPermissions() {
        if (null == permissions) {
            return null;
        }
        return new ArrayList<MessagingPermission>(permissions);
    }

    /**
     * Sets the permissions
     *
     * @param permissions The permissions
     */
    public void setPermissions(final List<MessagingPermission> permissions) {
        if (permissions == null) {
            this.permissions = null;
        } else {
            this.permissions = new ArrayList<MessagingPermission>(permissions);
        }
    }

    /**
     * Adds given permission.
     *
     * @param permission The permission
     */
    public void addPermission(final MessagingPermission permission) {
        if (null == permissions) {
            permissions = new ArrayList<MessagingPermission>(4);
        }
        permissions.add(permission);
    }

    @Override
    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    /**
     * Sets the unread message count.
     *
     * @param unreadMessageCount The unread message count
     */
    public void setUnreadMessageCount(final int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    @Override
    public boolean hasSubfolders() {
        return subfolders;
    }

    /**
     * Sets whether this folder has subfolders.
     *
     * @param subfolders <code>true</code> if this folder has subfolders; otherwise <code>false</code>
     */
    public void setSubfolders(final boolean subfolders) {
        this.subfolders = subfolders;
        b_subfolders = true;
    }

    /**
     * Indicates whether this folder has the has-subfolders flag set
     *
     * @return <code>true</code> if this folder has the has-subfolders flag set; otherwise <code>false</code>
     */
    public boolean containsSubfolders() {
        return b_subfolders;
    }

    /**
     * Removes whether this folder has subfolders.
     */
    public void removeSubfolders() {
        subfolders = false;
        b_subfolders = false;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return subscribedSubfolders;
    }

    /**
     * Sets whether this folder has subscribed subfolders.
     *
     * @param subscribedSubfolders <code>true</code> if this folder has subscribed subfolders; otherwise <code>false</code>
     */
    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        this.subscribedSubfolders = subscribedSubfolders;
        b_subscribedSubfolders = true;
    }

    /**
     * Indicates whether this folder has the has-subscribed-subfolders flag set
     *
     * @return <code>true</code> if this folder has the has-subscribed-subfolders flag set; otherwise <code>false</code>
     */
    public boolean containsSubscribedSubfolders() {
        return b_subscribedSubfolders;
    }

    /**
     * Removes whether this folder has subscribed subfolders.
     */
    public void removeSubscribedSubfolders() {
        subscribedSubfolders = false;
        b_subscribedSubfolders = false;
    }

    @Override
    public boolean isDefaultFolder() {
        return defaultFolder;
    }

    /**
     * Sets whether this folder is a default folder.
     *
     * @param defaultFolder <code>true</code> if this folder is a default folder; otherwise <code>false</code>
     */
    public void setDefaultFolder(final boolean defaultFolder) {
        this.defaultFolder = defaultFolder;
        b_defaultFolder = true;
    }

    /**
     * Indicates whether this folder has the default-folder flag set
     *
     * @return <code>true</code> if this folder has the default-folder flag set; otherwise <code>false</code>
     */
    public boolean containsDefaultFolder() {
        return b_defaultFolder;
    }

    /**
     * Removes whether this folder is a default folder.
     */
    public void removeDefaultFolder() {
        defaultFolder = false;
        b_defaultFolder = false;
    }

    @Override
    public boolean isHoldsFolders() {
        return holdsFolders;
    }

    /**
     * Sets whether this folder has the capability to hold subfolders.
     *
     * @param holdsFolders <code>true</code> if this folder has the capability to hold subfolders; otherwise <code>false</code>
     */
    public void setHoldsFolders(final boolean holdsFolders) {
        this.holdsFolders = holdsFolders;
        b_holdsFolders = true;
    }

    /**
     * Indicates whether this folder has the holds-folders flag set
     *
     * @return <code>true</code> if this folder has the holds-folders flag set; otherwise <code>false</code>
     */
    public boolean containsHoldsFolders() {
        return b_holdsFolders;
    }

    /**
     * Removes whether this folder holds folders.
     */
    public void removeHoldsFolders() {
        holdsFolders = false;
        b_holdsFolders = false;
    }

    @Override
    public boolean isHoldsMessages() {
        return holdsMessages;
    }

    /**
     * Indicates whether this folder has the holds-messages flag set
     *
     * @return <code>true</code> if this folder has the holds-messages flag set; otherwise <code>false</code>
     */
    public boolean containsHoldsMessages() {
        return b_holdsMessages;
    }

    /**
     * Removes whether this folder holds messages.
     */
    public void removeHoldsMessages() {
        holdsMessages = false;
        b_holdsMessages = false;
    }

    /**
     * Sets whether this folder has the capability to hold messages.
     *
     * @param holdsMessages <code>true</code> if this folder has the capability to hold messages; otherwise <code>false</code>
     */
    public void setHoldsMessages(final boolean holdsMessages) {
        this.holdsMessages = holdsMessages;
        b_holdsMessages = true;
    }

    @Override
    public boolean isRootFolder() {
        return rootFolder;
    }

    /**
     * Sets if this folder is the root folder.
     *
     * @param rootFolder <code>true</code> if this folder is the root folder; otherwise <code>false</code>
     */
    public void setRootFolder(final boolean rootFolder) {
        this.rootFolder = rootFolder;
        b_rootFolder = true;
    }

    /**
     * Indicates whether this folder has the root-folder flag set
     *
     * @return <code>true</code> if this folder has the root-folder flag set; otherwise <code>false</code>
     */
    public boolean containsRootFolder() {
        return b_rootFolder;
    }

    /**
     * Removes whether this folder is the root folder.
     */
    public void removeRootFolder() {
        rootFolder = false;
        b_rootFolder = false;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Sets if this folder is subscribed.
     *
     * @param subscribed <code>true</code> if this folder is subscribed; otherwise <code>false</code>
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
        b_subscribed = true;
    }

    /**
     * Indicates whether this folder has the subscribed flag set
     *
     * @return <code>true</code> if this folder has the subscribed flag set; otherwise <code>false</code>
     */
    public boolean containsSubscribed() {
        return b_subscribed;
    }

    /**
     * Removes whether this folder is subscribed.
     */
    public void removeSubscribed() {
        subscribed = false;
        b_subscribed = false;
    }

    /**
     * Indicates whether this folder exists in folder storage.
     *
     * @return <code>true</code> if this folder exists in folder storage; otherwise <code>false</code>
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Sets whether this folder exists in folder storage.
     *
     * @param exists <code>true</code> if this folder exists in folder storage; otherwise <code>false</code>
     */
    public void setExists(final boolean exists) {
        this.exists = exists;
    }

    @Override
    public char getSeparator() {
        return separator;
    }

    /**
     * Checks if separator character was set through {@link #setSeparator(char)}.
     *
     * @return <code>true</code> if separator is set; otherwise <code>false</code>
     */
    public boolean containsSeparator() {
        return b_separator;
    }

    /**
     * Removes the separator character.
     */
    public void removeSeparator() {
        separator = '0';
        b_separator = false;
    }

    /**
     * Sets the separator character.
     * <p>
     * If mailing system does not support a separator character, {@link MailConfig#getDefaultSeparator()} should to be used.
     *
     * @param separator the separator to set
     */
    public void setSeparator(final char separator) {
        this.separator = separator;
        b_separator = true;
    }

}
