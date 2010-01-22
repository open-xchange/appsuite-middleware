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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;

/**
 * {@link DefaultMessagingFolder} - The default messaging folder providing setter methods.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultMessagingFolder implements MessagingFolder {

    private boolean b_defaultFolderType;

    private DefaultFolderType defaultFolderType;

    private List<String> capabilities;

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

    private boolean subscribed;

    /**
     * Initializes a new {@link DefaultMessagingFolder}.
     */
    public DefaultMessagingFolder() {
        super();
    }

    public boolean containsDefaultFolderType() {
        return b_defaultFolderType;
    }

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

    public List<String> getCapabilities() {
        if (null == capabilities) {
            return Collections.emptyList();
        }
        return new ArrayList<String>(capabilities);
    }

    /**
     * Sets the capabilities.
     * 
     * @param capabilities The capabilities
     */
    public void setCapabilities(final List<String> capabilities) {
        if (capabilities == null) {
            this.capabilities = Collections.emptyList();
        } else {
            this.capabilities = new ArrayList<String>(capabilities);
        }
    }

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

    public List<MessagingPermission> getPermissions() {
        if (null == permissions) {
            return Collections.emptyList();
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
            this.permissions = Collections.emptyList();
        } else {
            this.permissions = new ArrayList<MessagingPermission>(permissions);
        }
    }

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
    }

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
    }

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
    }

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
    }

    public boolean isHoldsMessages() {
        return holdsMessages;
    }

    /**
     * Sets whether this folder has the capability to hold messages.
     * 
     * @param holdsMessages <code>true</code> if this folder has the capability to hold messages; otherwise <code>false</code>
     */
    public void setHoldsMessages(final boolean holdsMessages) {
        this.holdsMessages = holdsMessages;
    }

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
    }

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
    }

}
