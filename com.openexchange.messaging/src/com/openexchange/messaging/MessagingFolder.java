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

package com.openexchange.messaging;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * {@link MessagingFolder} - Represents a messaging folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingFolder extends Serializable {

    /**
     * The default folder type.
     */
    public static enum DefaultFolderType {
        NONE, INBOX, DRAFTS, SENT, SPAM, TRASH, CONFIRMED_SPAM, CONFIRMED_HAM, MESSAGING;
    }

    /**
     * The constant for full name of an account's root folder.
     */
    public static final String ROOT_FULLNAME = "";

    /**
     * The capability identifier for permissions support.
     */
    public static final String CAPABILITY_PERMISSIONS = "PERMISSIONS";

    /**
     * The capability identifier for quota support.
     */
    public static final String CAPABILITY_QUOTA = "QUOTA";

    /**
     * The capability identifier for sort support.
     */
    public static final String CAPABILITY_SORT = "SORT";

    /**
     * The capability identifier for subscription support.
     */
    public static final String CAPABILITY_SUBSCRIPTION = "SUBSCRIPTION";

    /**
     * The capability identifier for user flags support.
     */
    public static final String CAPABILITY_USER_FLAGS = "USER_FLAGS";

    /**
     * Gets the capabilities of this folder; e.g <code>"QUOTA"</code>, <code>"PERMISSIONS"</code>, etc.
     *
     * @return The list of capabilities
     */
    public Set<String> getCapabilities();

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getId();

    /**
     * Gets the name.
     *
     * @return The name
     */
    public String getName();

    /**
     * Gets the separator character.
     *
     * @return The separator character.
     */
    public char getSeparator();

    /**
     * Gets the permission for currently logged-in user accessing this folder
     * <p>
     * The returned permission should reflect user's permission regardless if messaging system supports permissions or not. An instance of
     * {@link DefaultMessagingPermission} is supposed to be returned on missing permissions support except for the root folder. The root
     * folder should indicate no object permissions in any case, but the folder permission varies if messaging system allows subfolder
     * creation below root folder or not. The returned permission must reflect the allowed behavior.
     *
     * @return The own permission
     */
    public MessagingPermission getOwnPermission();

    /**
     * Gets the parent identifier or <code>null</code> if this messaging folder denotes the root folder.
     *
     * @return The parent identifier or <code>null</code> if this messaging folder denotes the root folder
     */
    public String getParentId();

    /**
     * Gets the permissions associated with this messaging folder.
     *
     * @return The permissions as a collection of {@link MessagingPermission}
     */
    public List<MessagingPermission> getPermissions();

    /**
     * Checks if this messaging folder has subfolders.
     *
     * @return <code>true</code> if this messaging folder has subfolders; otherwise <code>false</code>
     */
    public boolean hasSubfolders();

    /**
     * Checks if this messaging folder has subscribed subfolders.
     *
     * @return <code>true</code> if this messaging folder has subscribed subfolders; otherwise <code>false</code>
     */
    public boolean hasSubscribedSubfolders();

    /**
     * Checks whether the denoted messaging folder is subscribed or not.
     * <p>
     * If messaging system does not support subscription, <code>true</code> is supposed to be returned.
     *
     * @return Whether the denoted messaging folder is subscribed or not
     */
    public boolean isSubscribed();

    /**
     * Checks if this folder is able to hold folders.
     *
     * @return <code>true</code> if this folder is able to hold folders; otherwise <code>false</code>
     */
    public boolean isHoldsFolders();

    /**
     * Checks if this folder is able to hold messages.
     *
     * @return <code>true</code> if this folder is able to hold messages; otherwise <code>false</code>
     */
    public boolean isHoldsMessages();

    /**
     * Checks if this folder denotes the root folder
     *
     * @return <code>true</code> if this folder denotes the root folder; otherwise <code>false</code>
     */
    public boolean isRootFolder();

    /**
     * Checks if this folder denotes a default folder (Drafts, Sent, Trash, etc.)
     *
     * @return <code>true</code> if this folder denotes a default folder; otherwise <code>false</code>
     */
    public boolean isDefaultFolder();

    /**
     * Gets the number of messages.
     *
     * @return The number of messages or <code>-1</code> if this messaging folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getMessageCount();

    /**
     * Gets the number of new messages (since last time this folder was accessed).
     *
     * @return The number of new messages or <code>-1</code> if this messaging folder does not hold messages.
     * @see #isHoldsMessages()
     */
    public int getNewMessageCount();

    /**
     * Gets the number of unread messages.
     *
     * @return The number of unread messages or <code>-1</code> if this messaging folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getUnreadMessageCount();

    /**
     * Gets the number of messages marked for deletion in this folder
     *
     * @return The number of messages marked for deletion in this folder or <code>-1</code> if this messaging folder does not hold messages
     * @see #isHoldsMessages()
     */
    public int getDeletedMessageCount();

    /**
     * Gets the default folder type.
     *
     * @return The default folder type or {@link DefaultFolderType#NONE} if not available
     */
    public DefaultFolderType getDefaultFolderType();

    /**
     * Checks if default folder type was applied to this messaging folder.
     *
     * @return <code>true</code> if default folder type is applied; otherwise <code>false</code>
     */
    public boolean containsDefaultFolderType();

}
