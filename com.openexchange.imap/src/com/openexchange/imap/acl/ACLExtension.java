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

package com.openexchange.imap.acl;

import com.sun.mail.imap.Rights;

/**
 * {@link ACLExtension} - Represents an ACL extension.
 * <p>
 * Implementation should be state-less and should only work on provided {@link Rights rights}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ACLExtension {

    /**
     * Indicates if this extension actually represents an ACL implementation or not.
     *
     * @return <code>true</code> if this extension actually represents an ACL implementation; otherwise <code>false</code>
     */
    public boolean aclSupport();

    /**
     * Checks if specified rights allow to administer; meaning to perform SETACL/DELETEACL.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allow to perform SETACL/DELETEACL; otherwise <code>false</code>.
     */
    public boolean canSetACL(Rights rights);

    /**
     * Checks if specified rights allow to perform GETACL/LISTRIGHTS.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allow to perform GETACL/LISTRIGHTS; otherwise <code>false</code>.
     */
    public boolean canGetACL(Rights rights);

    /**
     * Checks if read access is granted by specified rights; meaning being allowed to SELECT the mailbox or to perform CHECK, FETCH,
     * PARTIAL, SEARCH, COPY from mailbox.
     *
     * @param rights The rights to check
     * @return <code>true</code> if read access is granted by specified rights; otherwise <code>false</code>
     */
    public boolean canRead(Rights rights);

    /**
     * Checks if look-up access is granted by specified rights; meaning mailbox is visible to LIST/LSUB commands, SUBSCRIBE mailbox.
     *
     * @param rights The rights to check
     * @return <code>true</code> if look-up access is granted by specified rights; otherwise <code>false</code>
     */
    public boolean canLookUp(Rights rights);

    /**
     * Checks if specified rights allows to keep seen/unseen information across sessions; meaning to set or clear \SEEN flag via STORE, also
     * set \SEEN during APPEND/COPY/ FETCH BODY[...].
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to keep seen/unseen information across sessions; otherwise <code>false</code>
     */
    public boolean canKeepSeen(Rights rights);

    /**
     * Checks if write access is granted by specified rights; meaning being allowed to set or clear flags other than \SEEN and \DELETED via
     * STORE, also set them during APPEND/COPY.
     *
     * @param rights The rights to check
     * @return <code>true</code> if write access is granted by specified rights; otherwise <code>false</code>
     */
    public boolean canWrite(Rights rights);

    /**
     * Checks if specified rights allows to insert; meaning to perform APPEND, COPY into mailbox.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to insert; otherwise <code>false</code>
     */
    public boolean canInsert(Rights rights);

    /**
     * Checks if specified rights allows to post; meaning to send mail to submission address for mailbox which is not enforced by IMAP4
     * itself.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to post; otherwise <code>false</code>
     */
    public boolean canPost(Rights rights);

    /**
     * Checks if specified rights allows to create; meaning to CREATE new sub-mailboxes in any implementation-defined hierarchy, parent
     * mailbox for the new mailbox name in RENAME.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to create; otherwise <code>false</code>
     */
    public boolean canCreate(Rights rights);

    /**
     * Checks if specified rights allows to delete mailbox; meaning to DELETE mailbox, old mailbox name in RENAME.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to delete mailbox; otherwise <code>false</code>
     */
    public boolean canDeleteMailbox(Rights rights);

    /**
     * Checks if specified rights allows to delete messages; meaning to set or clear \DELETED flag via STORE, set \DELETED flag during
     * APPEND/COPY.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to delete messages; otherwise <code>false</code>
     */
    public boolean canDeleteMessages(Rights rights);

    /**
     * Checks if specified rights allows to expunge; meaning to perform EXPUNGE and expunge as a part of CLOSE.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights allows to expunge; otherwise <code>false</code>
     */
    public boolean canExpunge(Rights rights);

    /**
     * Returns read-only rights containing all rights supported by ACL extension.
     *
     * @return Read-only rights containing all rights supported by ACL extension.
     */
    public Rights getFullRights();

    /**
     * Adds folder administrator right to specified rights.
     *
     * @param rights The rights to enhance by folder administrator right
     */
    public void addFolderAdminRights(Rights rights);

    /**
     * Checks if specified rights contain folder administrator right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain folder administrator right; otherwise <code>false</code>
     */
    public boolean containsFolderAdminRights(Rights rights);

    /**
     * Adds folder visibility to specified rights.
     *
     * @param rights The rights to enhance by folder visibility
     */
    public void addFolderVisibility(Rights rights);

    /**
     * Checks if specified rights contain folder visibility.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain folder visibility; otherwise <code>false</code>
     */
    public boolean containsFolderVisibility(Rights rights);

    /**
     * Adds create-objects right to specified rights.
     *
     * @param rights The rights to enhance by create-objects right.
     */
    public void addCreateObjects(Rights rights);

    /**
     * Checks if specified rights contain create-objects right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain create-objects right; otherwise <code>false</code>
     */
    public boolean containsCreateObjects(Rights rights);

    /**
     * Adds create-subfolders right to specified rights.
     *
     * @param rights The rights to enhance by create-subfolders right.
     */
    public void addCreateSubfolders(Rights rights);

    /**
     * Checks if specified rights contain create-subfolders right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain subfolders create-objects right; otherwise <code>false</code>
     */
    public boolean containsCreateSubfolders(Rights rights);

    /**
     * Adds read-all and keep-seen rights to specified rights.
     *
     * @param rights The rights to enhance by read-all and keep-seen rights.
     */
    public void addReadAllKeepSeen(Rights rights);

    /**
     * Checks if specified rights contain read-all and keep-seen rights.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain read-all and keep-seen rights; otherwise <code>false</code>
     */
    public boolean containsReadAllKeepSeen(Rights rights);

    /**
     * Adds read-all right to specified rights.
     *
     * @param rights The rights to enhance by read-all right.
     */
    public void addReadAll(Rights rights);

    /**
     * Checks if specified rights contain read-all right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain read-all right; otherwise <code>false</code>
     */
    public boolean containsReadAll(Rights rights);

    /**
     * Adds write-all right to specified rights.
     *
     * @param rights The rights to enhance by write-all right.
     */
    public void addWriteAll(Rights rights);

    /**
     * Checks if specified rights contain write-all right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain write-all right; otherwise <code>false</code>
     */
    public boolean containsWriteAll(Rights rights);

    /**
     * Adds delete-all right to specified rights.
     *
     * @param rights The rights to enhance by delete-all right.
     */
    public void addDeleteAll(Rights rights);

    /**
     * Checks if specified rights contain delete-all right.
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain delete-all right; otherwise <code>false</code>
     */
    public boolean containsDeleteAll(Rights rights);

    /**
     * Adds non-mappable right(s) to specified rights.
     *
     * @param rights The rights to enhance by non-mappable right(s).
     */
    public void addNonMappable(Rights rights);

    /**
     * Checks if specified rights contain non-mappable right(s).
     *
     * @param rights The rights to check
     * @return <code>true</code> if specified rights contain non-mappable right(s); otherwise <code>false</code>
     */
    public boolean containsNonMappable(Rights rights);
}
