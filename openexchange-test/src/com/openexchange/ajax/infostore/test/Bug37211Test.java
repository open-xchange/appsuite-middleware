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

package com.openexchange.ajax.infostore.test;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug37211Test}
 *
 * Moving drive folders from Trash to Public Folder creates undeletable folders
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug37211Test extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link Bug37211Test}.
     *
     * @param name The test name
     */
    public Bug37211Test(final String name) {
        super(name);
    }

    public void testMoveFolderFromTrash() throws Exception {
        /*
         * create folder below trash
         */
        int trashFolderID = client.getValues().getInfostoreTrashFolder();
        String name = UUIDs.getUnformattedStringFromRandom();
        FolderObject folder = fMgr.generatePrivateFolder(name, FolderObject.INFOSTORE, trashFolderID, client.getValues().getUserId());
        folder = fMgr.insertFolderOnServer(folder);
        FolderObject reloadedFolder = fMgr.getFolderFromServer(folder.getObjectID());
        assertEquals("folder type wrong", FolderObject.TRASH, reloadedFolder.getType());
        /*
         * move to public folders
         */
        FolderObject toUpdate = new FolderObject();
        toUpdate.setLastModified(folder.getLastModified());
        toUpdate.setObjectID(folder.getObjectID());
        toUpdate.setParentFolderID(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        fMgr.updateFolderOnServer(toUpdate);
        /*
         * check folder type
         */
        reloadedFolder = fMgr.getFolderFromServer(folder.getObjectID());
        assertEquals("folder type wrong", FolderObject.PUBLIC, reloadedFolder.getType());
        /*
         * verify deletion
         */
        fMgr.deleteFolderOnServer(folder, true);
    }

}
