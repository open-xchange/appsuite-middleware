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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.folderstorage.contact;

import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;

/**
 * {@link ContactsFolderType}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsFolderType implements FolderType {

    private static final String CONTACTS_PREFIX = "con://";

    /**
     * Initializes a new {@link ContactsFolderType}.
     */
    ContactsFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    @Override
    public boolean servesParentId(String folderId) {
        return FolderStorage.PRIVATE_ID.equals(folderId) || FolderStorage.SHARED_ID.equals(folderId) || FolderStorage.PUBLIC_ID.equals(folderId) || servesFolderId(folderId);
    }

    /**
     * At the moment the detection relies on the {@value #CONTACTS_PREFIX} prefix,
     * i.e. checks if the folder id starts with that prefix. This leads to folders
     * from the internal contact providers being directly handled by the database
     * folder storage. This appears to be okay for now, since the
     * {@link InternalContactsAccess} mostly passes through to the folder service again.
     */
    @Override
    public boolean servesFolderId(String folderId) {
        if (null == folderId) {
            return false;
        }
        // Check if a real provider is defined
        return folderId.startsWith(CONTACTS_PREFIX);
    }
}
