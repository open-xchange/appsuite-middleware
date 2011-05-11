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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.imap.cache;

import java.util.List;
import java.util.Set;
import javax.mail.Folder;
import com.sun.mail.imap.ACL;

/**
 * A LIST/LSUB entry.
 */
public interface ListLsubEntry {

    public static enum ChangeState {
        CHANGED, UNCHANGED, UNDEFINED;
    }

    /**
     * Indicates whether associated IMAP folder is subscribed.
     * 
     * @return <code>true</code> if associated IMAP folder is subscribed; otherwise <code>false</code>
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    boolean isSubscribed();

    /**
     * Indicates whether associated IMAP folder exists.
     * 
     * @return <code>true</code> if associated IMAP folder exists; otherwise <code>false</code>
     */
    boolean exists();

    /**
     * Gets this LIST/LSUB entry's parent or <code>null</code> if no parent exists.
     * 
     * @return The parent or <code>null</code> for no parent
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    ListLsubEntry getParent();

    /**
     * Gets the children of this LIST/LSUB entry.
     * 
     * @return The children
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    List<ListLsubEntry> getChildren();

    /**
     * Gets the full name
     * 
     * @return The full name
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    String getFullName();

    /**
     * Gets the attributes
     * 
     * @return The attributes
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    Set<String> getAttributes();

    /**
     * Gets the separator
     * 
     * @return The separator
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    char getSeparator();

    /**
     * Gets the name.
     * 
     * @return The name
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    String getName();

    /**
     * Gets the change state
     * 
     * @return The change state
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    ListLsubEntry.ChangeState getChangeState();

    /**
     * Gets the has-inferiors flag.
     * <p>
     * {@link Folder#HOLDS_FOLDERS}
     * 
     * @return The has-inferiors flag
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    boolean hasInferiors();

    /**
     * Gets the can-open flag (folder is selectable).
     * <p>
     * {@link Folder#HOLDS_MESSAGES}
     * 
     * @return The can-open flag
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    boolean canOpen();

    /**
     * Gets the folder's type.
     * 
     * @return The type
     * @see Folder#HOLDS_FOLDERS
     * @see Folder#HOLDS_MESSAGES
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    int getType();

    /**
     * Gets the ACL list.
     * 
     * @return The ACL list or <code>null</code> if undetermined
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    List<ACL> getACLs();

    /**
     * Gets the number of messages as returned by <i>STATUS</i> command.
     * 
     * @return The number of messages or <code>-1</code> if undetermined
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    int getMessageCount();

    /**
     * Gets the number of new messages as returned by <i>STATUS</i> command.
     * 
     * @return The number of new messages or <code>-1</code> if undetermined
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    int getNewMessageCount();

    /**
     * Gets the number of unread messages as returned by <i>STATUS</i> command.
     * 
     * @return The number of unread messages or <code>-1</code> if undetermined
     * @throws ListLsubRuntimeException If entry is deprecated
     */
    int getUnreadMessageCount();

}
