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

package com.openexchange.imap.cache;

import java.util.List;
import java.util.Set;
import javax.mail.Folder;
import com.sun.mail.imap.ACL;

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
     * Indicates whether associated IMAP folder exists and does not hold <code>"\NonExistent"</code> attribute.
     *
     * @return <code>true</code> if associated IMAP folder <i>really</i> exists; otherwise <code>false</code>
     */
    boolean existsAndIsNotNonExistent();

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
     * Gets the original full name (present in case the full name advertised by IMAP server is different from chosen one)
     *
     * @return The original full name or <code>null</code>
     */
    default String optOriginalFullName() {
        return null;
    }

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
