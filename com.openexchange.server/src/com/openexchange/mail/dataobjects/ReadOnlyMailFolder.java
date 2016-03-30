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
import java.util.Collection;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;

/**
 * {@link ReadOnlyMailFolder} - a data container object for a mail folder
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ReadOnlyMailFolder extends MailFolder implements Serializable {

    private static final long serialVersionUID = 2415550550414867792L;

    private final MailFolder delegate;

    /**
     * Initializes a new {@link ReadOnlyMailFolder}
     */
    public ReadOnlyMailFolder(final MailFolder delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String getFullname() {
        return delegate.getFullname();
    }

    @Override
    public boolean containsFullname() {
        return delegate.containsFullname();
    }

    @Override
    public void removeFullname() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFullname(final String fullname) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this mail folder has subfolders.
     *
     * @return <code>true</code> if this mail folder has subfolders; otherwise <code>false</code>
     */
    @Override
    public boolean hasSubfolders() {
        return delegate.hasSubfolders();
    }

    /**
     * Checks if has-subfolders flag was set through {@link #setSubfolders(boolean)}.
     *
     * @return <code>true</code> if has-subfolders flag is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsSubfolders() {
        return delegate.containsSubfolders();
    }

    /**
     * Removes the has-subfolders flag.
     */
    @Override
    public void removeSubfolders() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets if this mail folder has subfolders.
     *
     * @param hasSubfolders the has-subfolders flag to set
     */
    @Override
    public void setSubfolders(final boolean hasSubfolders) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this mail folder has subscribed subfolders.
     *
     * @return <code>true</code> if this mail folder has subscribed subfolders; otherwise <code>false</code>
     */
    @Override
    public boolean hasSubscribedSubfolders() {
        return delegate.hasSubscribedSubfolders();
    }

    /**
     * Checks if the has-subscribed-subfolders flag was set through {@link #setSubscribedSubfolders(boolean)}.
     *
     * @return <code>true</code> if the has-subscribed-subfolders flag was set; otherwise <code>false</code>
     */
    @Override
    public boolean containsSubscribedSubfolders() {
        return delegate.containsSubscribedSubfolders();
    }

    /**
     * Removes has-subscribed-subfolders flag.
     */
    @Override
    public void removeSubscribedSubfolders() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets if this mail folder has subscribed subfolders.
     *
     * @param hasSubscribedSubfolders the has-subscribed-subfolders flag to set
     */
    @Override
    public void setSubscribedSubfolders(final boolean hasSubscribedSubfolders) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the name.
     *
     * @return The name
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * Checks if name was set through {@link #setName(String)}.
     *
     * @return <code>true</code> if name is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsName() {
        return delegate.containsName();
    }

    /**
     * Removes the name.
     */
    @Override
    public void removeName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets this mail folder's name.
     * <p>
     * If this mail folder denotes the root folder, {@link #DEFAULT_FOLDER_NAME} is supposed to be set as name.
     *
     * @param name the name to set
     */
    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether the denoted mail folder is subscribed or not.
     * <p>
     * If mailing system does not support subscription, <code>true</code> is supposed to be returned.
     *
     * @return Whether the denoted mail folder is subscribed or not
     */
    @Override
    public boolean isSubscribed() {
        return delegate.isSubscribed();
    }

    /**
     * Checks if subscribed status was set through {@link #setSubscribed(boolean)}.
     *
     * @return <code>true</code> if subscribed is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsSubscribed() {
        return delegate.containsSubscribed();
    }

    /**
     * Removes the subscription status.
     */
    @Override
    public void removeSubscribed() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the subscription status for this mail folder.
     * <p>
     * If mailing system does not support subscription, <code>true</code> is supposed to be set as subscription status.
     *
     * @param subscribed the subscription status to set
     */
    @Override
    public void setSubscribed(final boolean subscribed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the number of messages marked for deletion in this folder
     *
     * @return The number of messages marked for deletion in this folder or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    @Override
    public int getDeletedMessageCount() {
        return delegate.getDeletedMessageCount();
    }

    /**
     * Checks if the number of messages was set through {@link #setDeletedMessageCount(int)}.
     *
     * @return <code>true</code> if deletedMessageCount is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsDeletedMessageCount() {
        return delegate.containsDeletedMessageCount();
    }

    /**
     * Removes the number of messages marked for deletion in this folder.
     */
    @Override
    public void removeDeletedMessageCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the number of messages marked for deletion in this folder.
     *
     * @param deletedMessageCount The number of messages marked for deletion or <code>-1</code> if this mail folder does not hold messages
     */
    @Override
    public void setDeletedMessageCount(final int deletedMessageCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this folder exists.
     *
     * @return <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     */
    @Override
    public boolean exists() {
        return delegate.exists();
    }

    /**
     * Checks if folder existence status was set through {@link #setExists(boolean)}.
     *
     * @return <code>true</code> if exists status is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsExists() {
        return delegate.containsExists();
    }

    /**
     * Removes exists status.
     */
    @Override
    public void removeExists() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the exists status.
     *
     * @param exists <code>true</code> if folder exists in mailbox; otherwise <code>false</code>
     */
    @Override
    public void setExists(final boolean exists) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the number of messages.
     *
     * @return The number of messages or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    @Override
    public int getMessageCount() {
        return delegate.getMessageCount();
    }

    /**
     * Checks if number of messages was set through {@link #setMessageCount(int)}.
     *
     * @return <code>true</code> if messageCount is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsMessageCount() {
        return delegate.containsMessageCount();
    }

    /**
     * Removes the message-count.
     */
    @Override
    public void removeMessageCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the number of messages.
     *
     * @param messageCount The number of messages or <code>-1</code> if this mail folder does not hold messages
     */
    @Override
    public void setMessageCount(final int messageCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the number of new messages (since last time this folder was accessed).
     *
     * @return The number of new messages or <code>-1</code> if this mail folder does not hold messages.
     * @see #isHoldsMessages()
     */
    @Override
    public int getNewMessageCount() {
        return delegate.getNewMessageCount();
    }

    /**
     * Checks if the number of new messages was set through {@link #setNewMessageCount(int)}.
     *
     * @return <code>true</code> if newMessageCount is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsNewMessageCount() {
        return delegate.containsNewMessageCount();
    }

    /**
     * Removes the new-message-count.
     */
    @Override
    public void removeNewMessageCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the number of new messages.
     *
     * @param newMessageCount The number of new messages or <code>-1</code> if this mail folder does not hold messages
     */
    @Override
    public void setNewMessageCount(final int newMessageCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the number of unread messages.
     *
     * @return The number of unread messages or <code>-1</code> if this mail folder does not hold messages
     * @see #isHoldsMessages()
     */
    @Override
    public int getUnreadMessageCount() {
        return delegate.getUnreadMessageCount();
    }

    /**
     * Checks if the number of unread messages was set through {@link #setUnreadMessageCount(int)}.
     *
     * @return <code>true</code> if unreadMessageCount is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsUnreadMessageCount() {
        return delegate.containsUnreadMessageCount();
    }

    /**
     * Removes the unread-message-count.
     */
    @Override
    public void removeUnreadMessageCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the number of unread messages.
     *
     * @param unreadMessageCount The number of unread messages or <code>-1</code> if this mail folder does not hold messages
     */
    @Override
    public void setUnreadMessageCount(final int unreadMessageCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the separator character.
     *
     * @see MailConfig#getDefaultSeparator()
     * @return The separator character.
     */
    @Override
    public char getSeparator() {
        return delegate.getSeparator();
    }

    /**
     * Checks if seperator character was set through {@link #setSeparator(char)}.
     *
     * @return <code>true</code> if separator is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsSeparator() {
        return delegate.containsSeparator();
    }

    /**
     * Removes the separator character.
     */
    @Override
    public void removeSeparator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the separator character.
     * <p>
     * If mailing system does not support a separator character, {@link MailConfig#getDefaultSeparator()} should to be used.
     *
     * @param separator the separator to set
     */
    @Override
    public void setSeparator(final char separator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the parent fullname.
     *
     * @return The parent fullname or <code>null</code> if this mail folder denotes the root folder
     */
    @Override
    public String getParentFullname() {
        return delegate.getParentFullname();
    }

    /**
     * Checks if parent fullname was set through {@link #setParentFullname(String)}.
     *
     * @return <code>true</code> if parentFullname is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsParentFullname() {
        return delegate.containsParentFullname();
    }

    /**
     * Removes the parent fullname.
     */
    @Override
    public void removeParentFullname() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the parent fullname.
     * <p>
     * If this mail folder denotes the root folder, <code>null</code> is supposed to be set.
     *
     * @param parentFullname the parent fullname to set
     */
    @Override
    public void setParentFullname(final String parentFullname) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this folder is able to hold messages.
     *
     * @return <code>true</code> if this folder is able to hold messages; otherwise <code>false</code>
     */
    @Override
    public boolean isHoldsMessages() {
        return delegate.isHoldsMessages();
    }

    /**
     * Checks if he holds-messages flag was set through {@link #setHoldsMessages(boolean)}.
     *
     * @return <code>true</code> if the holds-messages flag is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsHoldsMessages() {
        return delegate.containsHoldsMessages();
    }

    /**
     * Removes the holds-messages flag
     */
    @Override
    public void removeHoldsMessages() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets if this folder holds messages.
     *
     * @param holdsMessages <code>true</code> if folder holds messages; otherwise <code>false</code>
     */
    @Override
    public void setHoldsMessages(final boolean holdsMessages) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this folder is able to hold folders.
     *
     * @return <code>true</code> if this folder is able to hold folders; otherwise <code>false</code>
     */
    @Override
    public boolean isHoldsFolders() {
        return delegate.isHoldsFolders();
    }

    /**
     * Checks if the holds-folder flag was set through {@link #setHoldsFolders(boolean)}.
     *
     * @return <code>true</code> if this folder has the holds-folder flag set; otherwise <code>false</code>
     */
    @Override
    public boolean containsHoldsFolders() {
        return delegate.containsHoldsFolders();
    }

    /**
     * Removes the holds-folders flag
     */
    @Override
    public void removeHoldsFolders() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets if this folder holds folders.
     *
     * @param holdsFolders <code>true</code> if folder holds folders; otherwise <code>false</code>
     */
    @Override
    public void setHoldsFolders(final boolean holdsFolders) {
        throw new UnsupportedOperationException();
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
    @Override
    public MailPermission getOwnPermission() {
        return delegate.getOwnPermission();
    }

    /**
     * Checks if own permission was set through {@link #setOwnPermission(MailPermission)}.
     *
     * @return <code>true</code> if own permission is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsOwnPermission() {
        return delegate.containsOwnPermission();
    }

    /**
     * Removes the own permission.
     */
    @Override
    public void removeOwnPermission() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets own permission.
     * <p>
     * Apply an instance of {@link DefaultMailPermission} if mailing system does not support permissions, except if this mail folder denotes
     * the root folder, then apply altered instance of {@link DefaultMailPermission} with no object permissions but properly reflects folder
     * permission as described in {@link #getOwnPermission()}.
     *
     * @param ownPermission the own permission to set
     */
    @Override
    public void setOwnPermission(final MailPermission ownPermission) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this folder denotes the root folder
     *
     * @return <code>true</code> if this folder denotes the root folder; otherwise <code>false</code>
     */
    @Override
    public boolean isRootFolder() {
        return delegate.isRootFolder();
    }

    /**
     * Checks if root-folder flag was set through {@link #setRootFolder(boolean)}.
     *
     * @return <code>true</code> if root-folder flag is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsRootFolder() {
        return delegate.containsRootFolder();
    }

    /**
     * Removes the root folder flag.
     */
    @Override
    public void removeRootFolder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the root folder flag.
     *
     * @param rootFolder the root folder flag to set
     */
    @Override
    public void setRootFolder(final boolean rootFolder) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this folder denotes a default folder (Drafts, Sent, Trash, etc.)
     *
     * @return <code>true</code> if this folder denotes a default folder; otherwise <code>false</code>
     */
    @Override
    public boolean isDefaultFolder() {
        return delegate.isDefaultFolder();
    }

    /**
     * Checks if default-folder flag was set through {@link #setDefaultFolder(boolean)}.
     *
     * @return <code>true</code> if default-folder flag is set; otherwise <code>false</code>
     */
    @Override
    public boolean containsDefaultFolder() {
        return delegate.containsDefaultFolder();
    }

    @Override
    public void removeDefaultFolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultFolder(final boolean defaultFolder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPermission(final MailPermission permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPermissions(final MailPermission[] permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPermissions(final Collection<? extends MailPermission> permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsPermissions() {
        return delegate.containsPermissions();
    }

    @Override
    public void removePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MailPermission[] getPermissions() {
        return delegate.getPermissions();
    }

    @Override
    public boolean isSupportsUserFlags() {
        return delegate.isSupportsUserFlags();
    }

    @Override
    public boolean containsSupportsUserFlags() {
        return delegate.containsSupportsUserFlags();
    }

    @Override
    public void removeSupportsUserFlags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSupportsUserFlags(final boolean supportsUserFlags) {
        throw new UnsupportedOperationException();
    }

}
