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

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link GetVirtualTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetVirtualTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link GetVirtualTest}.
     *
     * @param name name of the test.
     */
    public GetVirtualTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testGetVirtual() throws Throwable {
        final GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, new int[] {
            FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        final GetResponse getResponse = client.execute(getRequest);
        assertFalse("GET request failed.", getResponse.hasError());
        final FolderObject folder = getResponse.getFolder();
        assertEquals("Unexpected object ID: ", FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, folder.getObjectID());

        final OCLPermission[] perms = folder.getNonSystemPermissionsAsArray();
        assertNotNull("Missing permissions", perms);
        assertEquals("Unexpected number of permissions: ", 1, perms.length);

        final OCLPermission p = perms[0];
        assertNotNull("Missing permission", p);
        assertEquals("Unexpected entity: ", OCLPermission.ALL_GROUPS_AND_USERS, p.getEntity());
        assertEquals("Unexpected folder permission: ", OCLPermission.READ_FOLDER, p.getFolderPermission());
        assertEquals("Unexpected read permission: ", OCLPermission.NO_PERMISSIONS, p.getReadPermission());
        assertEquals("Unexpected write permission: ", OCLPermission.NO_PERMISSIONS, p.getWritePermission());
        assertEquals("Unexpected delete permission: ", OCLPermission.NO_PERMISSIONS, p.getDeletePermission());
        assertEquals("Unexpected folder admin flag: ", false, p.isFolderAdmin());
        assertEquals("Unexpected group flag: ", true, p.isGroupPermission());
    }

}
