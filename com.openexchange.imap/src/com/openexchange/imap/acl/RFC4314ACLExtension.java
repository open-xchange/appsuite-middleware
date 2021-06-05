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
 * {@link RFC4314ACLExtension} - The ACL extension according to <small><b><a href="http://www.rfc-archive.org/getrfc.php?rfc=4314">RFC
 * 4314</a></b></small>.
 * <p>
 * Due to ambiguity in <small><b><a href="http://www.rfc-archive.org/getrfc.php?rfc=2086">RFC 2086</a></b></small>, the "create" right is
 * the union of the "k" and "x" rights, and the "delete" right is the union of the "e" and "t" rights. For compatibility with <small><b><a
 * href="http://www.rfc-archive.org/getrfc.php?rfc=2086">RFC 2086</a></b></small>, this ACL extension defines two virtual rights "d" and
 * "c".
 * <p>
 * If a client includes the "d" right in a rights list, then it MUST be treated as if the client had included every member of the "delete"
 * right.
 * <p>
 * If a client includes the "c" right in a rights list, then it MUST be treated as if the client had included every member of the "create"
 * right.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class RFC4314ACLExtension extends AbstractACLExtension {

    /**
     * "aclkx": {@link Right#ADMINISTER} + {@link Right#CREATE} + {@link Right#LOOKUP} + {@link RFC4314Rights#CREATE_MAILBOXES} +
     * {@link RFC4314Rights#DELETE_MAILBOX}.
     */
    private static final transient Rights RIGHTS_FOLDER_ADMIN = new Rights("aclkx");

    /**
     * "al": {@link Right#ADMINISTER} + {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_ADMIN_UNION = new Rights("al");

    /**
     * "l": {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_VISIBLE = new Rights("l");

    /**
     * "il": {@link Right#INSERT} {@link Right#LOOKUP}
     */
    private static final transient Rights RIGHTS_FOLDER_CREATE_OBJECTS = new Rights("il");

    /**
     * "cilkx": {@link Right#CREATE} + {@link Right#INSERT} + {@link Right#LOOKUP} + {@link RFC4314Rights#CREATE_MAILBOXES} +
     * {@link RFC4314Rights#DELETE_MAILBOX}.
     */
    private static final transient Rights RIGHTS_FOLDER_CREATE_SUBFOLDERS = new Rights("cilkx");

    /**
     * The "create" right is the union of the "k" and "x" rights.<br>
     * "kx": {@link RFC4314Rights#CREATE_MAILBOXES} + {@link RFC4314Rights#DELETE_MAILBOX}.
     */
    private static final transient Rights RIGHTS_CREATE_RFC4314 = new Rights("kx");

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
     * "det": {@link Right#DELETE} + {@link RFC4314Rights#EXPUNGE} + {@link RFC4314Rights#DELETE_MESSAGES} +
     */
    private static final transient Rights RIGHTS_DELETE_ALL = new Rights("det");

    /**
     * The "delete" right is the union of the "e" and "t" rights.<br>
     * "et": {@link RFC4314Rights#EXPUNGE} + {@link RFC4314Rights#DELETE_MESSAGES}
     */
    private static final transient Rights RIGHTS_DELETE_UNION = new Rights("et");

    /**
     * "p": {@link Right#POST}
     */
    private static final transient Rights RIGHTS_UNMAPPABLE = new Rights("p");

    private final Rights fullRights;

    /**
     * Initializes a new {@link RFC4314ACLExtension}.
     */
    RFC4314ACLExtension() {
        super();
        fullRights = new ReadOnlyRights("lrswipkxtecda");
    }

    @Override
    public boolean canGetACL(Rights rights) {
        return rights.contains(Rights.Right.ADMINISTER);
    }

    @Override
    public boolean canSetACL(Rights rights) {
        return rights.contains(Rights.Right.ADMINISTER);
    }

    @Override
    public boolean canCreate(Rights rights) {
        return rights.contains(RFC4314Rights.CREATE_MAILBOXES) || rights.contains(Rights.Right.CREATE);
    }

    @Override
    public boolean canDeleteMailbox(Rights rights) {
        return rights.contains(RFC4314Rights.DELETE_MAILBOX) || rights.contains(Rights.Right.CREATE);
    }

    @Override
    public boolean canDeleteMessages(Rights rights) {
        return rights.contains(RFC4314Rights.DELETE_MESSAGES) || rights.contains(Rights.Right.DELETE);
    }

    @Override
    public boolean canExpunge(Rights rights) {
        return rights.contains(RFC4314Rights.EXPUNGE) || rights.contains(Rights.Right.DELETE);
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
        return rights.contains(RIGHTS_FOLDER_ADMIN_UNION) && (rights.contains(Rights.Right.CREATE) || rights.contains(RIGHTS_CREATE_RFC4314));
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
        return rights.contains(RIGHTS_FOLDER_CREATE_OBJECTS) && (rights.contains(Rights.Right.CREATE) || rights.contains(RIGHTS_CREATE_RFC4314));
    }

    @Override
    public boolean containsDeleteAll(Rights rights) {
        return rights.contains(Rights.Right.DELETE) || rights.contains(RIGHTS_DELETE_UNION);
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
