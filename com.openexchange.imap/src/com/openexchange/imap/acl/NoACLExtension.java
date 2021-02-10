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
