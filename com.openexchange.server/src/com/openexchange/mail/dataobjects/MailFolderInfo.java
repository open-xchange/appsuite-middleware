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
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;

/**
 * {@link MailFolderInfo} - A light-weight object providing basic information for a mail folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailFolderInfo implements Serializable, Cloneable {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -3074261820696719502L;

    private MailFolder.DefaultFolderType defaulFolderType;
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

    private boolean rootFolder;
    private boolean b_rootFolder;

    private boolean defaultFolder;
    private boolean b_defaultFolder;

    private int accountId;

    private char separator = 0;

    private String displayName;
    private String cachedFullDisplayName;

    private int numSubfolders;
    private boolean b_numSubfolders;

    /**
     * Initializes a new {@link MailFolderInfo}
     */
    public MailFolderInfo() {
        super();
        defaulFolderType = MailFolder.DefaultFolderType.NONE;
        numSubfolders = -1;
    }

    @Override
    public MailFolderInfo clone() {
        try {
            return (MailFolderInfo) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError("Clone failed although Cloneable is implemented.");
        }
    }

    /**
     * Sets the separator
     *
     * @param separator The separator to set
     */
    public void setSeparator(final char separator) {
        this.separator = separator;
        cachedFullDisplayName = null;
    }

    /**
     * Gets the separator
     *
     * @return The separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
        cachedFullDisplayName = null;
    }

    /**
     * Gets the display name
     *
     * @return The display name
     */
    public String getDisplayName() {
        return null == displayName ? name : displayName;
    }

    /**
     * Sets the account identifier.
     *
     * @param accountId The account identifier to set
     */
    public void setAccountId(final int accountId) {
        this.accountId = accountId;
    }

    /**
     * Gets the account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
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
     * Gets the full display name.
     *
     * @return The full display name
     */
    public String getFullDisplayName() {
        String tmp = cachedFullDisplayName;
        if (null == tmp) {
            if (null != displayName && separator > 0) {
                final int pos = fullname.lastIndexOf(separator);
                if (pos > 0) {
                    tmp = new StringBuilder(fullname.substring(0, pos)).append(displayName).toString();
                } else {
                    tmp = fullname;
                }
            } else {
                tmp = fullname;
            }
            cachedFullDisplayName = tmp;
        }
        return tmp;
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
     * If this mail folder denotes the root folder, {@link #DEFAULT_FOLDER_ID} is supposed to be set as full name.
     *
     * @param fullname the full name to set
     */
    public void setFullname(final String fullname) {
        this.fullname = fullname;
        b_fullname = true;
        cachedFullDisplayName = null;
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
    public MailFolder.DefaultFolderType getDefaultFolderType() {
        return defaulFolderType;
    }

    /**
     * Checks if this mail folder denotes the INBOX folder.
     *
     * @return <code>true</code> if this mail folder denotes the INBOX folder; otherwise <code>false</code>
     */
    public boolean isInbox() {
        return MailFolder.DefaultFolderType.INBOX.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the DRAFTS folder.
     *
     * @return <code>true</code> if this mail folder denotes the DRAFTS folder; otherwise <code>false</code>
     */
    public boolean isDrafts() {
        return MailFolder.DefaultFolderType.DRAFTS.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the SENT folder.
     *
     * @return <code>true</code> if this mail folder denotes the SENT folder; otherwise <code>false</code>
     */
    public boolean isSent() {
        return MailFolder.DefaultFolderType.SENT.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the SPAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the SPAM folder; otherwise <code>false</code>
     */
    public boolean isSpam() {
        return MailFolder.DefaultFolderType.SPAM.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the TRASH folder.
     *
     * @return <code>true</code> if this mail folder denotes the TRASH folder; otherwise <code>false</code>
     */
    public boolean isTrash() {
        return MailFolder.DefaultFolderType.TRASH.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the CONFIRMED_SPAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the CONFIRMED_SPAM folder; otherwise <code>false</code>
     */
    public boolean isConfirmedSpam() {
        return MailFolder.DefaultFolderType.CONFIRMED_SPAM.equals(defaulFolderType);
    }

    /**
     * Checks if this mail folder denotes the CONFIRMED_HAM folder.
     *
     * @return <code>true</code> if this mail folder denotes the CONFIRMED_HAM folder; otherwise <code>false</code>
     */
    public boolean isConfirmedHam() {
        return MailFolder.DefaultFolderType.CONFIRMED_HAM.equals(defaulFolderType);
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
        defaulFolderType = MailFolder.DefaultFolderType.NONE;
        b_defaulFolderType = false;
    }

    /**
     * Sets default folder type.
     *
     * @param name the name to set
     */
    public void setDefaultFolderType(final MailFolder.DefaultFolderType defaulFolderType) {
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
     * Gets the parent full name.
     *
     * @return The parent full name or <code>null</code> if this mail folder denotes the root folder
     */
    public String getParentFullname() {
        return parentFullname;
    }

    /**
     * Checks if parent full name was set through {@link #setParentFullname(String)}.
     *
     * @return <code>true</code> if parentFullname is set; otherwise <code>false</code>
     */
    public boolean containsParentFullname() {
        return b_parentFullname;
    }

    /**
     * Removes the parent full name.
     */
    public void removeParentFullname() {
        parentFullname = null;
        b_parentFullname = false;
    }

    /**
     * Sets the parent full name.
     * <p>
     * If this mail folder denotes the root folder, <code>null</code> is supposed to be set.
     *
     * @param parentFullname the parent full name to set
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
     * Gets the number of sub-folders.
     *
     * @return The number of sub-folders.
     */
    public int getNumSubfolders() {
        return numSubfolders;
    }

    /**
     * Checks if number of sub-folders was set through {@link #setNumSubfolders(int)}.
     *
     * @return <code>true</code> if number of sub-folders is set; otherwise <code>false</code>
     */
    public boolean containsNumSubfolders() {
        return b_numSubfolders;
    }

    /**
     * Removes the number of sub-folders information.
     */
    public void removeNumSubfolders() {
        numSubfolders = -1;
        b_numSubfolders = false;
    }

    /**
     * Sets the number of sub-folders.
     *
     * @param numSubfolders The number of sub-folders.
     * @return This reference
     */
    public MailFolderInfo setNumSubfolders(final int numSubfolders) {
        this.numSubfolders = numSubfolders;
        b_numSubfolders = true;
        return this;
    }

    @Override
    public String toString() {
        return containsFullname() ? getFullname() : "[no fullname]";
    }

}
