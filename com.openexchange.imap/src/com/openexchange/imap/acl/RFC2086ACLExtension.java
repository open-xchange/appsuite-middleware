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
import com.sun.mail.imap.Rights.Right;

/**
 * {@link RFC2086ACLExtension} - The ACL extension according to <small><b><a href="http://www.rfc-archive.org/getrfc.php?rfc=2086">RFC
 * 2086</a></b></small>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class RFC2086ACLExtension extends AbstractACLExtension {

    /**
     * "acl": {@link Right#ADMINISTER} + {@link Right#CREATE} + {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_ADMIN = new Rights("acl");

    /**
     * "l": {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_VISIBLE = new Rights("l");

    /**
     * "il": {@link Right#INSERT} {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_CREATE_OBJECTS = new Rights("il");

    /**
     * "cil": {@link Right#CREATE} + {@link Right#INSERT} + {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_CREATE_SUBFOLDERS = new Rights("cil");

    /**
     * "rs": {@link Right#READ} + {@link Right#KEEP_SEEN}
     */
    private static final transient Rights RIGHTS_READ_ALL_KEEP_SEEN = new Rights("rs");

    /**
     * "r": {@link Right#READ}
     */
    private static final transient Rights RIGHTS_READ_ALL = new Rights("r");

    /**
     * "w": {@link Right#WRITE}
     */
    private static final transient Rights RIGHTS_WRITE_ALL = new Rights("w");

    /**
     * "d": {@link Right#DELETE}
     */
    private static final transient Rights RIGHTS_DELETE_ALL = new Rights("d");

    /**
     * "p": {@link Right#POST}
     */
    private static final transient Rights RIGHTS_UNMAPPABLE = new Rights("p");

    private final Rights fullRights;

    /**
     * Initializes a new {@link RFC2086ACLExtension}.
     */
    RFC2086ACLExtension() {
        super();
        fullRights = new ReadOnlyRights("acdilprsw");
    }

    @Override
    public boolean canGetACL(Rights rights) {
        return rights.contains(Rights.Right.READ) || rights.contains(Rights.Right.ADMINISTER);
    }

    @Override
    public boolean canSetACL(Rights rights) {
        return rights.contains(Rights.Right.ADMINISTER);
    }

    @Override
    public boolean canCreate(Rights rights) {
        return rights.contains(Rights.Right.CREATE);
    }

    @Override
    public boolean canDeleteMailbox(Rights rights) {
        return rights.contains(Rights.Right.CREATE);
    }

    @Override
    public boolean canDeleteMessages(Rights rights) {
        return rights.contains(Rights.Right.DELETE);
    }

    @Override
    public boolean canExpunge(Rights rights) {
        return rights.contains(Rights.Right.DELETE);
    }

    @Override
    public Rights getFullRights() {
        return fullRights;
    }

    @Override
    public void addFolderAdminRights(Rights rights) {
        rights.add(RIGHTS_FOLDER_ADMIN);
    }

    @Override
    public boolean containsFolderAdminRights(Rights rights) {
        return rights.contains(RIGHTS_FOLDER_ADMIN);
    }

    @Override
    public void addFolderVisibility(Rights rights) {
        rights.add(RIGHTS_FOLDER_VISIBLE);
    }

    @Override
    public boolean containsFolderVisibility(Rights rights) {
        return rights.contains(RIGHTS_FOLDER_VISIBLE);
    }

    @Override
    public void addCreateObjects(Rights rights) {
        rights.add(RIGHTS_FOLDER_CREATE_OBJECTS);
    }

    @Override
    public void addCreateSubfolders(Rights rights) {
        rights.add(RIGHTS_FOLDER_CREATE_SUBFOLDERS);
    }

    @Override
    public void addDeleteAll(Rights rights) {
        rights.add(RIGHTS_DELETE_ALL);
    }

    @Override
    public void addNonMappable(Rights rights) {
        rights.add(RIGHTS_UNMAPPABLE);
    }

    @Override
    public void addReadAll(Rights rights) {
        rights.add(RIGHTS_READ_ALL);
    }

    @Override
    public void addReadAllKeepSeen(Rights rights) {
        rights.add(RIGHTS_READ_ALL_KEEP_SEEN);
    }

    @Override
    public void addWriteAll(Rights rights) {
        rights.add(RIGHTS_WRITE_ALL);
    }

    @Override
    public boolean containsCreateObjects(Rights rights) {
        return rights.contains(RIGHTS_FOLDER_CREATE_OBJECTS);
    }

    @Override
    public boolean containsCreateSubfolders(Rights rights) {
        return rights.contains(RIGHTS_FOLDER_CREATE_SUBFOLDERS);
    }

    @Override
    public boolean containsDeleteAll(Rights rights) {
        return rights.contains(RIGHTS_DELETE_ALL);
    }

    @Override
    public boolean containsNonMappable(Rights rights) {
        return rights.contains(RIGHTS_UNMAPPABLE);
    }

    @Override
    public boolean containsReadAll(Rights rights) {
        return rights.contains(RIGHTS_READ_ALL);
    }

    @Override
    public boolean containsReadAllKeepSeen(Rights rights) {
        return rights.contains(RIGHTS_READ_ALL_KEEP_SEEN);
    }

    @Override
    public boolean containsWriteAll(Rights rights) {
        return rights.contains(RIGHTS_WRITE_ALL);
    }

}
