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

package com.openexchange.ajax.task;

import java.util.Date;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AbstractTaskRequest;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11075Test extends AbstractTaskTest {

    /**
     * @param name
     */
    public Bug11075Test(final String name) {
        super(name);
    }

    /**
     * Creates two public task folder where the user has only permission to read
     * own objects. Then a SQL command for searching tasks failed.
     * @throws Throwable if some exception occurs.
     */
    public void testBug() throws Throwable {
        final AJAXClient client = getClient();
        final InsertRequest[] inserts = new InsertRequest[2];
        for (int i = 0; i < inserts.length; i++) {
            inserts[i] = new InsertRequest(EnumAPI.OX_OLD, createFolder("Bug11075Test_" + i,
                client.getValues().getUserId()));
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));
        final int[] folderIds = new int[inserts.length];
        Date timestamp = new Date(0);
        for (int i = 0; i < folderIds.length; i++) {
            final CommonInsertResponse response = mInsert.getResponse(i);
            folderIds[i] = response.getId();
            if (response.getTimestamp().after(timestamp)) {
                timestamp = response.getTimestamp();
            }
        }
        try {
            final TaskSearchObject search = new TaskSearchObject();
            search.setPattern("");
            final SearchRequest request = new SearchRequest(search,
                AbstractTaskRequest.GUI_COLUMNS);
            final SearchResponse response = TaskTools.search(client, request);
            assertFalse("Searching over all folders failed.", response.hasError());
        } finally {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, folderIds, timestamp));
        }
    }

    private static final FolderObject createFolder(final String name,
        final int userId) {
        final FolderObject folder = new FolderObject();
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        folder.setFolderName(name);
        folder.setModule(FolderObject.TASK);
        folder.setType(FolderObject.PUBLIC);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(userId);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.WRITE_OWN_OBJECTS,
            OCLPermission.DELETE_OWN_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1 });
        return folder;
    }
}
