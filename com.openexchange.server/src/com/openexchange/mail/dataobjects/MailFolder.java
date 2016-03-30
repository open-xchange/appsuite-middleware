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

package com.openexchange.mail.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;

/**
 * {@link MailFolder} - a data container object for a mail folder
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailFolder implements Serializable, Cloneable {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8203697938992090309L;

    /**
     * The default folder type.
     */
    public static enum DefaultFolderType {
        NONE, INBOX, DRAFTS, SENT, SPAM, TRASH, CONFIRMED_SPAM, CONFIRMED_HAM;
    }

    private DefaultFolderType defaulFolderType;
    private boolean b_defaulFolderType;

    private String name;
    private boolean b_name;

    private String fullname;
    private boolean b_fullname;

    private String parentFullname;
    private boolean b_parentFullname;

    private boolean subscribed;
    private boolean b_subscribed;

    /**
     * Flag to indicate whether this mail folder contains subfolders.
     */
    private boolean hasSubfolders;
    private boolean b_hasSubfolders;

    /**
     * Flag to indicate whether this mail folder contains _subscribed_ subfolders.
     */
    private boolean hasSubscribedSubfolders;
    private boolean b_hasSubscribedSubfolders;

    private boolean exists;
    private boolean b_exists;

    /**
     * Flag to indicate whether this mail folder is able to hold messages.
     */
    private boolean holdsMessages;
    private boolean b_holdsMessages;

    /**
     * Flag to indicate whether this mail folder is able to hold (sub)folders.
     */
    private boolean holdsFolders;
    private boolean b_holdsFolders;

    private int messageCount;
    private boolean b_messageCount;

    private int newMessageCount;
    private boolean b_newMessageCount;

    private int unreadMessageCount;
    private boolean b_unreadMessageCount;

    private int deletedMessageCount;
    private boolean b_deletedMessageCount;

    private char separator;
    private boolean b_separator;

    private MailPermission ownPermission;
    private boolean b_ownPermission;

    private boolean supportsUserFlags;
    private boolean b_supportsUserFlags;

    private boolean rootFolder;
    private boolean b_rootFolder;

    private boolean defaultFolder;
    private boolean b_defaultFolder;

    private List<MailPermission> permissions;
    private boolean b_permissions;

    private boolean shared;
    private boolean b_shared;

    private String owner;
    private boolean b_owner;

    private boolean isPublic;
    private boolean b_public;

    private boolean liveAccess;

    private final ConcurrentMap<String, Object> properties;

    /**
     * Virtual name of mailbox's root folder
     *
     * @value "E-Mail"
     */
    public static final String DEFAULT_FOLDER_NAME = "E-Mail";

    /**
     * Virtual full name of mailbox's root folder
     *
     * @value "default"
     */
    public static final String DEFAULT_FOLDER_ID = "default";

    /**
     * Initializes a new {@link MailFolder}
     */
    public MailFolder() {
        super();
        defaulFolderType = DefaultFolderType.NONE;
        properties = new ConcurrentHashMap<String, Object>(4, 0.9f, 1);
        liveAccess = true;
    }

    /**
     * Gets the {@link MailFolderInfo} view for this folder.
     * <p>
     * {@link MailFolderInfo#setNumSubfolders(int)} is <b>not</b> set!
     *
     * @return The appropriate {@link MailFolderInfo} instance
     */
    public MailFolderInfo asMailFolderInfo(final int accountId) {
        final MailFolderInfo mfi = new MailFolderInfo();
        mfi.setAccountId(accountId);
        mfi.setDefaultFolder(defaultFolder);
        mfi.setDefaultFolderType(defaulFolderType);
        mfi.setFullname(fullname);
        mfi.setHoldsFolders(holdsFolders);
        mfi.setHoldsMessages(holdsMessages);
        mfi.setName(name);
        mfi.setParentFullname(parentFullname);
        mfi.setRootFolder(rootFolder);
        mfi.setSeparator(separator);
        mfi.setSubfolders(hasSubfolders);
        mfi.setSubscribed(subscribed);
        mfi.setSubscribedSubfolders(hasSubscribedSubfolders);
        return mfi;
    }

    /**
     * Sets the live-access flag.
     *
     * @param liveAccess The flag to set
     */
    public void setLiveAccess(boolean liveAccess) {
        this.liveAccess = liveAccess;
    }

    /**
     * Signals if mail folder is accessed in a on-the-fly fashion or if there is any kind of caching mechanism.
     * <p>
     * Default is <code>true</code>.
     *
     * @return <code>true</code> for on-the-fly access; otherwise <code>false</code>
     */
    public boolean liveAccess() {
        return liveAccess;
    }

    /**
     * Checks presence of named property.
     *
     * @param name The name
     * @return <code>true</code> if present; otherwise <code>false</code>
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets specified property.
     *
     * @param name The name
     * @return The value or <code>null</code>
     */
    public Object getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * Sets given property
     * <p>
     * If value is <code>null</code>, a remove is performed.
     *
     * @param name The name
     * @param value The value
     */
    public void setProperty(final String name, final Object value) {
        if (null == value) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    /**
     * Sets the property if absent.
     *
     * @param name The name
     * @param value The value
     * @return The previous value or <code>null</code> for successful insertion
     */
    public Object setPropertyIfAbsent(String name, Object value) {
        return properties.putIfAbsent(name, value);
    }

    @Override
    public MailFolder clone() {
        try {
            final MailFolder clone = (MailFolder) super.clone();
            {
                final MailPermission thisOwnPerm = ownPermission;
                if (thisOwnPerm != null) {
                    clone.ownPermission = (MailPermission) thisOwnPerm.clone();
                }
            }
            {
                final List<MailPermission> thisPerms = permissions;
                if (thisPerms != null) {
                    final List<MailPermission> l = new ArrayList<MailPermission>(thisPerms.size());
                    for (final MailPermission mailPermission : thisPerms) {
                        l.add((MailPermission) mailPermission.clone());
                    }
                    clone.permissions = l;
                }
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError("Clone failed although Cloneable is implemented.");
        }
    }

    /**
     * Gets the full name.
     *
     * @return The full name ({@link #DEFAULT_FOLDER_ID} if this mail folder denotes the root folder)
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * Checks if full name is set through {@link #setFullname(String)}.
     *
     * @return <code>true</code> if full name is set; otherwise <code>false</code>
     */
    public boolean containsFullname() {
        return b_fullname;
    }

    /**
     * Removes the full name.
     */
    public void removeFullname() {
        fullname = null;
        b_fullname = false;
    }

    /**
     * Sets this mail folder's full name.
     * <p>
     * If this mail folder denotes the root folder, {@link #DEFAULT_FOLDER_ID} is supposed to be set as fullname.
     *
     * @param fullname the full name to set
     */
    public void setFullname(final String fullname) {
        this.fullname = fullname;
        b_fullname = true;
    }

    /**
     * Checks if this mail folder has subfolders.
     *
     * @return <code>true</code> if this mail folder has subfolders; otherwise <code>false</code>
     */
    public boolean hasSubfolders() {
        return hasSubfolders;
    }

    /**
     * Checks if has-subfolders flag was set through {@link #setSubfolders(boolean)}.
     *
     * @return <code>true</code> if has-subfolders flag is set; otherwise <code>false</code>
     */
    public boolean containsSubfolders() {
        return b_hasSubfolders;
    }

    /**
     * Removes the has-subfolders flag.
     */
    public void removeSubfolders() {
        hasSubfolders = false;
        b_hasSubfolders = false;
    }

    /**
     * Sets if this mail folder has subfolders.
     *
     * @param hasSubfolders the has-subfolders flag to set
     */
    public void setSubfolders(final boolean hasSubfolders) {
        this.hasSubfolders = hasSubfolders;
        b_hasSubfolders = true;
    }

    /**
     * Checks if this mail folder has subscribed subfolders.
     *
     * @return <code>true</code> if this mail folder has subscribed subfolders; otherwise <code>false</code>
     */
    public boolean hasSubscribedSubfolders() {
        return hasSubscribedSubfolders;
    }

    /**
     * Checks if the has-subscribed-subfolders flag was set through {@link #setSubscribedSubfolders(boolean)}.
     *
     * @return <code>true</code> if the has-subscribed-subfolders flag was set; otherwise <code>false</code>
     */
    public boolean containsSubscribedSubfolders() {
        return b_hasSubscribedSubfolders;
    }

    /**
     * Removes has-subscribed-subfolders flag.
     */
    public void removeSubscribedSubfolders() {
        hasSubscribedSubfolders = false;
        b_hasSubscribedSubfolders = false;
    }

    /**
     * Sets if this mail folder has subscribed subfolders.
     *
     * @param hasSubscribedSubfolders the has-subscribed-subfolders flag to set
     */
    public void setSubscribedSubfolders(final boolean hasSubscribedSubfolders) {
        this.hasSubscribedSubfolders = hasSubscribedSubfolders;
        b_hasSubscribedSubfolders = true;
    }

    /**
     * Gets the default folder type.
     *
     * @return The default folder type
     */
    public DefaultFolderType getDefaultFolderType() {
        return defaulFolderType;
    }

    /**
     * Checks if this mail folder denotes the INBOX folder.
     *
     * @return <code>true</code> if this mail folder denotes the INBOX folder; otherwise <code>false</code>
     */
    public boolean isInbox() {
        return DefaultFolderType.INBOX.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the DRAFTS folder.
     *
     * @return <code>true</code> if this mail folder denotes the DRAFTS folder; otherwise <code>false</code>
     */
    public boolean isDrafts() {
        return DefaultFolderType.DRAFTS.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the SENT folder.
     *
     * @return <code>true</code> if this mail folder denotes the SENT folder; otherwise <code>false</code>
     */
    public boolean isSent() {
        return DefaultFolderType.SENT.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the SPAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the SPAM folder; otherwise <code>false</code>
     */
    public boolean isSpam() {
        return DefaultFolderType.SPAM.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the TRASH folder.
     *
     * @return <code>true</code> if this mail folder denotes the TRASH folder; otherwise <code>false</code>
     */
    public boolean isTrash() {
        return DefaultFolderType.TRASH.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the CONFIRMED_SPAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the CONFIRMED_SPAM folder; otherwise <code>false</code>
     */
    public boolean isConfirmedSpam() {
        return DefaultFolderType.CONFIRMED_SPAM.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the CONFIRMED_HAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the CONFIRMED_HAM folder; otherwise <code>false</code>
     */
    public boolean isConfirmedHam() {
        return DefaultFolderType.CONFIRMED_HAM.equals(defaulFolderType);
    }

    /**
     * Checks if default folder type was set through {@link #setDefaultFolderType(DefaultFolderType)}.
     *
     * @return <code>true</code> if default folder type is set; otherwise <code>false</code>
     */
    public boolean containsDefaultFolderType() {
        return b_defaulFolderType;
    }

    /**
     * Removes the default folder type.
     */
    public void removeDefaultFolderType() {
        defaulFolderType = DefaultFolderType.NONE;
        b_defaulFolderType = false;
    }

    /**
     * Sets default folder type.
     *
     * @param name the name to set
     */
    public void setDefaultFolderType(final DefaultFolderType defaulFolderType) {
        this.defaulFolderType = defaulFolderType;
        b_defaulFolderType = true;
    }

    /**
     * Gets the name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if name was set through {@link #setName(String)}.
     *
     * @return <code>true</code> if name is set; otherwise <code>false</code>
     */
    public boolean containsName() {
        return b_name;
    }

    /**
     * Removes the name.
     */
    public void removeName() {
        name = null;
        b_name = false;
    }

    /**
     * Sets this mail folder's name.
     * <p>
     * If this mail folder denotes the root folder, {@link #DEFAULT_FOLDER_NAME} is supposed to be set as name.
     *
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
        b_name = true;
    }

    /**
     * Returns whether the denoted mail folder is subscribed or not.
     * <p>
     * If mailing system does not support subscription, <code>true</code> is supposed to be returned.
     *
     * @return Whether the denoted mail folder is subscribed or not
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Checks if subscribed status was set through {@link #setSubscribed(boolean)}.
     *
     * @return <code>true</code> if subscribed is set; otherwise <code>false</code>
     */
    public boolean containsSubscribed() {
        return b_subscribed;
    }

    /**
     * Removes the subscription status.
     */
    public void removeSubscribed() {
        subscribed = false;
        b_subscribed = false;
    }

    /**
     * Sets the subscription status for this mail folder.
     * <p>
     * If mailing system does not support subscription, <code>true</code> is supposed to be set as subscription status.
     *
     * @param subscribed the subscription status to set
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
        b_subscribed = true;
    }

    /**
     * Gets the number of messages marked for deletion in this folder
     *
     * @return The number of messages marked for deletion in this folder or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getDeletedMessageCount() {
        return deletedMessageCount;
    }

    /**
     * Checks if the number of messages was set through {@link #setDeletedMessageCount(int)}.
     *
     * @return <code>true</code> if deletedMessageCount is set; otherwise <code>false</code>
     */
    public boolean containsDeletedMessageCount() {
        return b_deletedMessageCount;
    }

    /**
     * Removes the number of messages marked for deletion in this folder.
     */
    public void removeDeletedMessageCount() {
        deletedMessageCount = 0;
        b_deletedMessageCount = false;
    }

    /**
     * Sets the number of messages marked for deletion in this folder.
     *
     * @param deletedMessageCount The number of messages marked for deletion or <code>-1</code> if this mail folder does not hold messages
     */
    public void setDeletedMessageCount(final int deletedMessageCount) {
        this.deletedMessageCount = deletedMessageCount;
        b_deletedMessageCount = true;
    }

    /**
     * Checks if this folder exists.
     *
     * @return <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Checks if folder existence status was set through {@link #setExists(boolean)}.
     *
     * @return <code>true</code> if exists status is set; otherwise <code>false</code>
     */
    public boolean containsExists() {
        return b_exists;
    }

    /**
     * Removes exists status.
     */
    public void removeExists() {
        exists = false;
        b_exists = false;
    }

    /**
     * Sets the exists status.
     *
     * @param exists <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     */
    public void setExists(final boolean exists) {
        this.exists = exists;
        b_exists = true;
    }

    /**
     * Gets the number of messages.
     *
     * @return The number of messages or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Checks if number of messages was set through {@link #setMessageCount(int)}.
     *
     * @return <code>true</code> if messageCount is set; otherwise <code>false</code>
     */
    public boolean containsMessageCount() {
        return b_messageCount;
    }

    /**
     * Removes the message-count.
     */
    public void removeMessageCount() {
        messageCount = 0;
        b_messageCount = false;
    }

    /**
     * Sets the number of messages.
     *
     * @param messageCount The number of messages or <code>-1</code> if this mail folder does not hold messages
     */
    public void setMessageCount(final int messageCount) {
        this.messageCount = messageCount;
        b_messageCount = true;
    }

    /**
     * Gets the number of new messages (since last time this folder was accessed).
     *
     * @return The number of new messages or <code>-1</code> if this mail folder does not hold messages.
     * @see #isHoldsMessages()
     */
    public int getNewMessageCount() {
        return newMessageCount;
    }

    /**
     * Checks if the number of new messages was set through {@link #setNewMessageCount(int)}.
     *
     * @return <code>true</code> if newMessageCount is set; otherwise <code>false</code>
     */
    public boolean containsNewMessageCount() {
        return b_newMessageCount;
    }

    /**
     * Removes the new-message-count.
     */
    public void removeNewMessageCount() {
        newMessageCount = 0;
        b_newMessageCount = false;
    }

    /**
     * Sets the number of new messages.
     *
     * @param newMessageCount The number of new messages or <code>-1</code> if this mail folder does not hold messages
     */
    public void setNewMessageCount(final int newMessageCount) {
        this.newMessageCount = newMessageCount;
        b_newMessageCount = true;
    }

    /**
     * Gets the number of unread messages.
     *
     * @return The number of unread messages or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    /**
     * Checks if the number of unread messages was set through {@link #setUnreadMessageCount(int)}.
     *
     * @return <code>true</code> if unreadMessageCount is set; otherwise <code>false</code>
     */
    public boolean containsUnreadMessageCount() {
        return b_unreadMessageCount;
    }

    /**
     * Removes the unread-message-count.
     */
    public void removeUnreadMessageCount() {
        unreadMessageCount = 0;
        b_unreadMessageCount = false;
    }

    /**
     * Sets the number of unread messages.
     *
     * @param unreadMessageCount The number of unread messages or <code>-1</code> if this mail folder does not hold messages
     */
    public void setUnreadMessageCount(final int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
        b_unreadMessageCount = true;
    }

    /**
     * Gets the separator character.
     *
     * @see MailConfig#getDefaultSeparator()
     * @return The separator character.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Checks if seperator character was set through {@link #setSeparator(char)}.
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

    /**
     * Gets the parent fullname.
     *
     * @return The parent fullname or <code>null</code> if this mail folder denotes the root folder
     */
    public String getParentFullname() {
        return parentFullname;
    }

    /**
     * Checks if parent fullname was set through {@link #setParentFullname(String)}.
     *
     * @return <code>true</code> if parentFullname is set; otherwise <code>false</code>
     */
    public boolean containsParentFullname() {
        return b_parentFullname;
    }

    /**
     * Removes the parent fullname.
     */
    public void removeParentFullname() {
        parentFullname = null;
        b_parentFullname = false;
    }

    /**
     * Sets the parent fullname.
     * <p>
     * If this mail folder denotes the root folder, <code>null</code> is supposed to be set.
     *
     * @param parentFullname the parent fullname to set
     */
    public void setParentFullname(final String parentFullname) {
        this.parentFullname = parentFullname;
        b_parentFullname = true;
    }

    /**
     * Checks if this folder is able to hold messages.
     *
     * @return <code>true</code> if this folder is able to hold messages; otherwise <code>false</code>
     */
    public boolean isHoldsMessages() {
        return holdsMessages;
    }

    /**
     * Checks if he holds-messages flag was set through {@link #setHoldsMessages(boolean)}.
     *
     * @return <code>true</code> if the holds-messages flag is set; otherwise <code>false</code>
     */
    public boolean containsHoldsMessages() {
        return b_holdsMessages;
    }

    /**
     * Removes the holds-messages flag
     */
    public void removeHoldsMessages() {
        holdsMessages = false;
        b_holdsMessages = false;
    }

    /**
     * Sets if this folder holds messages.
     *
     * @param holdsMessages <code>true</code> if folder holds messages; otherwise <code>false</code>
     */
    public void setHoldsMessages(final boolean holdsMessages) {
        this.holdsMessages = holdsMessages;
        b_holdsMessages = true;
    }

    /**
     * Checks if this folder is able to hold folders.
     *
     * @return <code>true</code> if this folder is able to hold folders; otherwise <code>false</code>
     */
    public boolean isHoldsFolders() {
        return holdsFolders;
    }

    /**
     * Checks if the holds-folder flag was set through {@link #setHoldsFolders(boolean)}.
     *
     * @return <code>true</code> if this folder has the holds-folder flag set; otherwise <code>false</code>
     */
    public boolean containsHoldsFolders() {
        return b_holdsFolders;
    }

    /**
     * Removes the holds-folders flag
     */
    public void removeHoldsFolders() {
        holdsFolders = false;
        b_holdsFolders = false;
    }

    /**
     * Sets if this folder holds folders.
     *
     * @param holdsFolders <code>true</code> if folder holds folders; otherwise <code>false</code>
     */
    public void setHoldsFolders(final boolean holdsFolders) {
        this.holdsFolders = holdsFolders;
        b_holdsFolders = true;
    }

    /**
     * Gets the permission for currently logged-in user accessing this folder
     * <p>
     * The returned permission should reflect user's permission regardless if mailing system supports permissions or not. An instance of
     * {@link DefaultMailPermission} is supposed to be returned on missing permissions support except for the root folder. The root folder
     * should indicate no object permissions in any case, but the folder permission varies if mailing system allows subfolder creation below
     * root folder or not. The returned permission must reflect the allowed behavior.
     *
     * @return The own permission
     */
    public MailPermission getOwnPermission() {
        return ownPermission;
    }

    /**
     * Checks if own permission was set through {@link #setOwnPermission(MailPermission)}.
     *
     * @return <code>true</code> if own permission is set; otherwise <code>false</code>
     */
    public boolean containsOwnPermission() {
        return b_ownPermission;
    }

    /**
     * Removes the own permission.
     */
    public void removeOwnPermission() {
        ownPermission = null;
        b_ownPermission = false;
    }

    /**
     * Sets own permission.
     * <p>
     * Apply an instance of {@link DefaultMailPermission} if mailing system does not support permissions, except if this mail folder denotes
     * the root folder, then apply <code>null</code> or altered instance of {@link DefaultMailPermission} with no object permissions but properly reflects folder
     * permission as described in {@link #getOwnPermission()}.
     * <p>
     * Please note that even if the mail system does not support permissions the entity must be set on the {@link DefaultMailPermission}
     * object. Please use {@link DefaultMailPermission#setEntity(int)} for this.
     *
     * @param ownPermission The own permission to set
     */
    public void setOwnPermission(final MailPermission ownPermission) {
        this.ownPermission = ownPermission;
        b_ownPermission = true;
    }

    /**
     * Checks if this folder denotes the root folder
     *
     * @return <code>true</code> if this folder denotes the root folder; otherwise <code>false</code>
     */
    public boolean isRootFolder() {
        return rootFolder;
    }

    /**
     * Checks if root-folder flag was set through {@link #setRootFolder(boolean)}.
     *
     * @return <code>true</code> if root-folder flag is set; otherwise <code>false</code>
     */
    public boolean containsRootFolder() {
        return b_rootFolder;
    }

    /**
     * Removes the root folder flag.
     */
    public void removeRootFolder() {
        rootFolder = false;
        b_rootFolder = false;
    }

    /**
     * Sets the root folder flag.
     *
     * @param rootFolder the root folder flag to set
     */
    public void setRootFolder(final boolean rootFolder) {
        this.rootFolder = rootFolder;
        b_rootFolder = true;
    }

    /**
     * Checks if this folder denotes a default folder (Drafts, Sent, Trash, etc.)
     *
     * @return <code>true</code> if this folder denotes a default folder; otherwise <code>false</code>
     */
    public boolean isDefaultFolder() {
        return defaultFolder;
    }

    /**
     * Checks if default-folder flag was set through {@link #setDefaultFolder(boolean)}.
     *
     * @return <code>true</code> if default-folder flag is set; otherwise <code>false</code>
     */
    public boolean containsDefaultFolder() {
        return b_defaultFolder;
    }

    /**
     * Removes the default folder flag.
     */
    public void removeDefaultFolder() {
        defaultFolder = false;
        b_defaultFolder = false;
    }

    /**
     * Sets the default folder flag.
     *
     * @param defaultFolder the default folder flag to set
     */
    public void setDefaultFolder(final boolean defaultFolder) {
        this.defaultFolder = defaultFolder;
        b_defaultFolder = true;
    }

    /**
     * Adds a permission.
     *
     * @param permission The permission to add
     */
    public void addPermission(final MailPermission permission) {
        if (null == permission) {
            return;
        } else if (null == permissions) {
            permissions = new ArrayList<MailPermission>();
            b_permissions = true;
        }
        permissions.add(permission);
    }

    /**
     * Adds an array of permissions.
     *
     * @param permissions The array of permissions to add
     */
    public void addPermissions(final MailPermission[] permissions) {
        if ((null == permissions) || (permissions.length == 0)) {
            return;
        } else if (null == this.permissions) {
            this.permissions = new ArrayList<MailPermission>(permissions.length);
            b_permissions = true;
        }
        this.permissions.addAll(Arrays.asList(permissions));
    }

    /**
     * Adds a collection of permissions.
     *
     * @param permissions The collection of permissions to add
     */
    public void addPermissions(final Collection<? extends MailPermission> permissions) {
        if ((null == permissions) || (permissions.isEmpty())) {
            return;
        } else if (null == this.permissions) {
            this.permissions = new ArrayList<MailPermission>(permissions.size());
            b_permissions = true;
        }
        this.permissions.addAll(permissions);
    }

    /**
     * Checks if permissions were set through {@link #addPermission(MailPermission)}, {@link #addPermissions(Collection)}, or
     * {@link #addPermissions(MailPermission[])}.
     *
     * @return <code>true</code> if permissions are set; otherwise <code>false</code>
     */
    public boolean containsPermissions() {
        return b_permissions;
    }

    /**
     * Removes the permissions.
     */
    public void removePermissions() {
        permissions = null;
        b_permissions = false;
    }

    private static final MailPermission[] EMPTY_PERMS = new MailPermission[0];

    /**
     * @return the permissions as array of {@link MailPermission}
     */
    public MailPermission[] getPermissions() {
        if (null == permissions) {
            return EMPTY_PERMS;
        }
        return permissions.toArray(new MailPermission[permissions.size()]);
    }

    @Override
    public String toString() {
        return containsFullname() ? getFullname() : "[no fullname]";
    }

    /**
     * Checks if this folder supports user flags.
     *
     * @return <code>true</code> if this folder supports user flags; otherwise <code>false</code>
     */
    public boolean isSupportsUserFlags() {
        return supportsUserFlags;
    }

    /**
     * Checks if the supports-user-flags flag was set through {@link #setSupportsUserFlags(boolean)}.
     *
     * @return <code>true</code> if supportsUserFlags is set; otherwise <code>false</code>
     */
    public boolean containsSupportsUserFlags() {
        return b_supportsUserFlags;
    }

    /**
     * Removes the supports-user-flags flag.
     */
    public void removeSupportsUserFlags() {
        supportsUserFlags = false;
        b_supportsUserFlags = false;
    }

    /**
     * Sets the supports-user-flags flag.
     *
     * @param supportsUserFlags the supports-user-flags flag to set
     */
    public void setSupportsUserFlags(final boolean supportsUserFlags) {
        this.supportsUserFlags = supportsUserFlags;
        b_supportsUserFlags = true;
    }

    /**
     * Checks if this folder is shared.
     *
     * @return <code>true</code> if this folder is shared; otherwise <code>false</code>
     */
    public boolean isShared() {
        return shared;
    }

    /**
     * Checks if the shared flag was set.
     *
     * @return <code>true</code> if shared flag is set; otherwise <code>false</code>
     */
    public boolean containsShared() {
        return b_shared;
    }

    /**
     * Removes the shared flag.
     */
    public void removeShared() {
        shared = false;
        b_shared = false;
    }

    /**
     * Sets the shared flag.
     *
     * @param shared The shared flag to set
     */
    public void setShared(final boolean shared) {
        this.shared = shared;
        b_shared = true;
    }

    /**
     * Checks if this folder is public.
     *
     * @return <code>true</code> if this folder is public; otherwise <code>false</code>
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Checks if the public flag was set.
     *
     * @return <code>true</code> if public flag is set; otherwise <code>false</code>
     */
    public boolean containsPublic() {
        return b_public;
    }

    /**
     * Removes the public flag.
     */
    public void removePublic() {
        isPublic = false;
        b_public = false;
    }

    /**
     * Sets the public flag.
     *
     * @param isPublic The public flag to set
     */
    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
        b_public = true;
    }

    /**
     * Gets the shared owner or <code>null</code>.
     * <p>
     * <b>Note:</b> {@link #isShared()} needs to return <code>true</code>.
     *
     * @return The owner or <code>null</code>
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Checks if the owner was set.
     *
     * @return <code>true</code> if owner is set; otherwise <code>false</code>
     */
    public boolean containsOwner() {
        return b_owner;
    }

    /**
     * Removes the owner.
     */
    public void removeOwne() {
        owner = null;
        b_owner = false;
    }

    /**
     * Sets the shared owner or <code>null</code>.
     * <p>
     * <b>Note:</b> {@link #setShared(boolean)} needs to be invoked with <code>true</code>.
     *
     * @param owner The owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
        b_owner = true;
    }

}
