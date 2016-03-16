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

import java.util.UUID;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link Bug32004Test}
 *
 * Wrong type for new folders below trash
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32004Test extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link Bug32004Test}.
     *
     * @param name The test name
     */
    public Bug32004Test(final String name) {
        super(name);
    }

    public void testCreateFolderBelowTrash() throws Exception {
        /*
         * create folder below trash
         */
        int trashFolderID = client.getValues().getInfostoreTrashFolder();
        String name = UUID.randomUUID().toString();
        FolderObject folder = fMgr.generatePrivateFolder(name, FolderObject.INFOSTORE, trashFolderID, client.getValues().getUserId());
        folder = fMgr.insertFolderOnServer(folder);
        /*
         * reload folder in different trees and check name
         */
        for (EnumAPI api : new EnumAPI[] { EnumAPI.OUTLOOK, EnumAPI.OX_NEW, EnumAPI.OX_OLD }) {
            folder = loadFolder(api, folder.getObjectID());
            assertNotNull("folder not found", folder);
            assertEquals("folder name wrong", name, folder.getFolderName());
            assertEquals("folder type wrong", FolderObject.TRASH, folder.getType());
        }
    }

    private FolderObject loadFolder(EnumAPI api, int folderID) throws Exception {
        GetRequest request = new GetRequest(api, String.valueOf(folderID), FolderObject.ALL_COLUMNS);
        return client.execute(request).getFolder();
    }

}
