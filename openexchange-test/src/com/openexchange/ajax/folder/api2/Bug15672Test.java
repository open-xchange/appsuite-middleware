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

import java.util.List;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug15672Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15672Test extends AbstractAJAXSession {

    private AJAXClient client;
    private FolderObject folder;

    public Bug15672Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folder = new FolderObject();
        folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        folder.setFolderName("Bug 15672 Test folder " + System.currentTimeMillis());
        folder.setModule(FolderObject.TASK);
//        folder.setPermissionsAsArray(new OCLPermission[0]);
        InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, folder);
        CommonInsertResponse response = client.execute(request);
        response.fillObject(folder);
    }

    @Override
    protected void tearDown() throws Exception {
        DeleteRequest request = new DeleteRequest(EnumAPI.OUTLOOK, folder);
        client.execute(request);
        super.tearDown();
    }

    public void test4ProperPermissions() throws Throwable {
        GetRequest request = new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID());
        GetResponse response = client.execute(request);
        FolderObject testFolder = response.getFolder();
        List<OCLPermission> permissions = testFolder.getPermissions();
        assertEquals("Folder should have at least 1 defined permission.", 1, permissions.size());
        OCLPermission permission = permissions.get(0);
        assertEquals("Current user should be folder administrator.", client.getValues().getUserId(), permission.getEntity());
        assertTrue(permission.isFolderAdmin());
        assertFalse(permission.isSystem());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getFolderPermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getReadPermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getWritePermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getDeletePermission());
    }
}
