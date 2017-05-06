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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * This class contains some examples of tests created for FolderTestManager
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class ExemplaryFolderTestManagerTest extends AbstractAJAXSession {

    private FolderObject folderObject1;
    private FolderObject folderObject2;

    public ExemplaryFolderTestManagerTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        AJAXClient client = getClient();
        // create a folder
        folderObject1 = new FolderObject();
        folderObject1.setFolderName("ExemplaryFolderTestManagerTest-folder1" + System.currentTimeMillis());
        folderObject1.setType(FolderObject.PUBLIC);
        folderObject1.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        folderObject1.setModule(FolderObject.INFOSTORE);
        // create permissions
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(client.getValues().getUserId());
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folderObject1.setPermissionsAsArray(new OCLPermission[] { perm1 });
        ftm.insertFolderOnServer(folderObject1);

        // create another folder
        folderObject2 = new FolderObject();
        folderObject2.setFolderName("ExemplaryFolderTestManagerTest-folder2" + System.currentTimeMillis());
        folderObject2.setType(FolderObject.PUBLIC);
        folderObject2.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        folderObject2.setModule(FolderObject.INFOSTORE);
        // create permissions
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(client.getValues().getUserId());
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(true);
        perm2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folderObject2.setPermissionsAsArray(new OCLPermission[] { perm2 });
        ftm.insertFolderOnServer(folderObject2);
    }

    @Test
    public void testCreatedFoldersAreReturnedByGetRequest() throws Exception {
        final FolderObject fo = ftm.getFolderFromServer(folderObject1.getObjectID());
        assertEquals("The folder was not returned.", fo.getFolderName(), folderObject1.getFolderName());
    }

    @Test
    public void testCreatedFoldersAppearInListRequest() throws Exception {
        boolean found1 = false;
        boolean found2 = false;
        final FolderObject[] allFolders = ftm.listFoldersOnServer(getClient().getValues().getPrivateInfostoreFolder());
        for (int i = 0; i < allFolders.length; i++) {
            final FolderObject fo = allFolders[i];
            if (fo.getObjectID() == folderObject1.getObjectID()) {
                found1 = true;
            }
            if (fo.getObjectID() == folderObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First folder was not found.", found1);
        assertTrue("Second folder was not found.", found2);
    }

    @Test
    public void testCreatedFoldersAppearAsUpdatedSinceYesterday() throws Exception {
        boolean found1 = false;
        boolean found2 = false;
        final Date date = new Date();
        date.setDate(date.getDate() - 1);
        final FolderObject[] allFolders = ftm.getUpdatedFoldersOnServer(date);
        for (int i = 0; i < allFolders.length; i++) {
            final FolderObject co = allFolders[i];
            if (co.getObjectID() == folderObject1.getObjectID()) {
                found1 = true;
            }
            if (co.getObjectID() == folderObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First folder was not found.", found1);
        assertTrue("Second folder was not found.", found2);
    }
}
