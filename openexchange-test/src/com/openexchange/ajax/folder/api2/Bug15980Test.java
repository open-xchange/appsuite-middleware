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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.PermissionTools;

/**
 * {@link Bug15980Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15980Test extends AbstractAJAXSession {

    private AJAXClient client;
    private FolderObject testFolder;

    public Bug15980Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.CALENDAR);
        testFolder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        testFolder.setPermissions(PermissionTools.P(I(client.getValues().getUserId()), PermissionTools.ADMIN));
        testFolder.setFolderName("testFolder4Bug15980");
        InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, testFolder);
        InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(testFolder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        testFolder.setLastModified(new Date());
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(EnumAPI.OUTLOOK, testFolder));
        super.tearDown();
    }

    public void testPath() throws Throwable {
        PathRequest request = new PathRequest(EnumAPI.OUTLOOK, testFolder.getObjectID(), new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME });
        PathResponse response = client.execute(request);
        Object[][] objects = response.getArray();
        int idPos = response.getColumnPos(FolderObject.OBJECT_ID);
        assertTrue("Response should contain folder identifier.", idPos >= 0);
        int namePos = response.getColumnPos(FolderObject.FOLDER_NAME);
        assertTrue("Response should contain folder names.", namePos >= 0);
        assertEquals("Path on Outlook like tree should have 3 parts.", 3, objects.length);
        assertEquals("Path should start with test folder but is folder " + objects[0][namePos], Integer.toString(testFolder.getObjectID()), objects[0][idPos]);
        assertEquals("Parent of created folder should be public folders but is " + objects[1][namePos], FolderStorage.PUBLIC_ID, objects[1][idPos]);
        assertEquals("Root folder should be IPM_ROOT but is " + objects[2][namePos], FolderStorage.PRIVATE_ID, objects[2][idPos]);
    }
}
