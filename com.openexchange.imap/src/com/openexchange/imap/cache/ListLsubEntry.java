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

package com.openexchange.imap.cache;

import java.util.List;
import java.util.Set;
import javax.mail.Folder;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;

/**
 * A LIST/LSUB entry.
 */
public interface ListLsubEntry {

    /**
     * A LIST/LSUB entry's change state.
     */
    public static enum ChangeState {
        CHANGED, UNCHANGED, UNDEFINED;
    }

    /**
     * Indicates whether associated IMAP folder is subscribed.
     *
     * @return <code>true</code> if associated IMAP folder is subscribed; otherwise <code>false</code>
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
     */
    ListLsubEntry getParent();

    /**
     * Gets the children of this LIST/LSUB entry.
     *
     * @return The children
     */
    List<ListLsubEntry> getChildren();

    /**
     * Gets the full name
     *
     * @return The full name
     */
    String getFullName();

    /**
     * Gets the attributes
     *
     * @return The attributes
     */
    Set<String> getAttributes();

    /**
     * Gets the separator
     *
     * @return The separator
     */
    char getSeparator();

    /**
     * Gets the name.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the change state
     *
     * @return The change state
     */
    ListLsubEntry.ChangeState getChangeState();

    /**
     * Gets the has-inferiors flag.
     * <p>
     * {@link Folder#HOLDS_FOLDERS}
     *
     * @return The has-inferiors flag
     */
    boolean hasInferiors();

    /**
     * Gets the can-open flag (folder is selectable).
     * <p>
     * {@link Folder#HOLDS_MESSAGES}
     *
     * @return The can-open flag
     */
    boolean canOpen();

    /**
     * Indicates whether this entry denotes a namespace folder.
     *
     * @return <code>true</code> for namespace folder; otherwise <code>false</code>
     */
    boolean isNamespace();

    /**
     * Indicates whether this entry has children.
     *
     * @return <code>true</code> if children exist; otherwise <code>false</code>
     */
    boolean hasChildren();

    /**
     * Gets the folder's type.
     *
     * @return The type
     * @see Folder#HOLDS_FOLDERS
     * @see Folder#HOLDS_MESSAGES
     */
    int getType();

    /**
     * Gets MYRIGHTS.
     *
     * @return MYRIGHTS or <code>null</code> if absent
     */
    public Rights getMyRights();

    /**
     * Gets the ACL list.
     *
     * @return The ACL list or <code>null</code> if undetermined
     */
    List<ACL> getACLs();

    /**
     * Remembers specified ACLs.
     *
     * @param aclList The ACL list
     */
    void rememberACLs(List<ACL> aclList);

    /**
     * Gets the number of messages as returned by <i>STATUS</i> command.
     *
     * @return The number of messages or <code>-1</code> if undetermined
     */
    int getMessageCount();

    /**
     * Gets the number of new messages as returned by <i>STATUS</i> command.
     *
     * @return The number of new messages or <code>-1</code> if undetermined
     */
    int getNewMessageCount();

    /**
     * Gets the number of unread messages as returned by <i>STATUS</i> command.
     *
     * @return The number of unread messages or <code>-1</code> if undetermined
     */
    int getUnreadMessageCount();

    /**
     * Remembers specified counts.
     *
     * @param total The total count
     * @param recent The recent count
     * @param unseen The unseen count
     */
    void rememberCounts(int total, int recent, int unseen);

}
