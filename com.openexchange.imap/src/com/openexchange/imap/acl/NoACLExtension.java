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
 * {@link NoACLExtension} - Represents no ACL support and therefore no access restrictions, except on {@link #canGetACL(Rights)} and
 * {@link #canSetACL(Rights)} since corresponding commands SETACL, DELETEACL, GETACL, and LISTRIGHTS are not supported on missing ACL
 * capability.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NoACLExtension implements ACLExtension {

    private static final ACLExtension instance = new NoACLExtension();

    /**
     * Gets the {@link ACLExtension} for no ACL support.
     *
     * @return The {@link ACLExtension} for no ACL support.
     */
    public static ACLExtension getInstance() {
        return instance;
    }

    /**
     * The full rights containing every possible ASCII character: (<code>'a' &lt;= c &lt;= 'z'</code>)
     */
    private final Rights fullRights;

    /**
     * Initializes a new {@link NoACLExtension}.
     */
    private NoACLExtension() {
        super();
        final Rights tmp = new Rights();
        for (char c = 'a'; c <= 'z'; c++) {
            tmp.add(Rights.Right.getInstance(c));
        }
        fullRights = new ReadOnlyRights(tmp);
    }

    @Override
    public boolean canRead(Rights rights) {
        return true;
    }

    @Override
    public boolean canLookUp(Rights rights) {
        return true;
    }

    @Override
    public boolean canKeepSeen(Rights rights) {
        return true;
    }

    @Override
    public boolean canWrite(Rights rights) {
        return true;
    }

    @Override
    public boolean canInsert(Rights rights) {
        return true;
    }

    @Override
    public boolean canPost(Rights rights) {
        return true;
    }

    @Override
    public boolean canGetACL(Rights rights) {
        // GETACL/LISTRIGHTS must not be performed on missing ACL support
        return false;
    }

    @Override
    public boolean canSetACL(Rights rights) {
        // SETACL/DELETEACL must not be performed on missing ACL support
        return false;
    }

    @Override
    public boolean canCreate(Rights rights) {
        return true;
    }

    @Override
    public boolean canDeleteMailbox(Rights rights) {
        return true;
    }

    @Override
    public boolean canDeleteMessages(Rights rights) {
        return true;
    }

    @Override
    public boolean canExpunge(Rights rights) {
        return true;
    }

    /**
     * Creates an empty rights object.
     *
     * @return An empty rights object.
     */
    @Override
    public Rights getFullRights() {
        return fullRights;
    }

    @Override
    public void addFolderAdminRights(Rights rights) {
        // Nothing to do
    }

    @Override
    public boolean containsFolderAdminRights(Rights rights) {
        return true;
    }

    @Override
    public void addFolderVisibility(Rights rights) {
        // Nothing to do
    }

    @Override
    public boolean containsFolderVisibility(Rights rights) {
        return true;
    }

    @Override
    public void addCreateObjects(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addCreateSubfolders(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addDeleteAll(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addNonMappable(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addReadAll(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addReadAllKeepSeen(Rights rights) {
        // Nothing to do
    }

    @Override
    public void addWriteAll(Rights rights) {
        // Nothing to do
    }

    @Override
    public boolean containsCreateObjects(Rights rights) {
        return true;
    }

    @Override
    public boolean containsCreateSubfolders(Rights rights) {
        return true;
    }

    @Override
    public boolean containsDeleteAll(Rights rights) {
        return true;
    }

    @Override
    public boolean containsNonMappable(Rights rights) {
        return true;
    }

    @Override
    public boolean containsReadAll(Rights rights) {
        return true;
    }

    @Override
    public boolean containsReadAllKeepSeen(Rights rights) {
        return true;
    }

    @Override
    public boolean containsWriteAll(Rights rights) {
        return true;
    }

    @Override
    public boolean aclSupport() {
        return false;
    }

}
