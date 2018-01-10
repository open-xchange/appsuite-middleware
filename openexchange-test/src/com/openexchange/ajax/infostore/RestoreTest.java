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

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.RestoreRequest;
import com.openexchange.ajax.infostore.actions.RestoreResponse;
import com.openexchange.ajax.infostore.actions.RestoreResponse.RestoreItem;
import com.openexchange.ajax.infostore.thirdparty.actions.DeleteFolderRequest;
import com.openexchange.test.TestInit;


/**
 * {@link RestoreTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class RestoreTest extends InfostoreAJAXTest {

    @Test
    public void testRestore() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "testRestore.txt");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);
        String id = data.getId();

        DeleteInfostoreRequest delReq = new DeleteInfostoreRequest();
        delReq.setIds(Collections.singletonList(id));
        delReq.setFolders(Collections.singletonList(String.valueOf(folderId)));
        delReq.setHardDelete(false);
        delReq.setTimestamp(new Date());
        getClient().execute(delReq);
        id = toTrash(id);

        RestoreRequest restoreReq = new RestoreRequest(Collections.singletonList(id), String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        RestoreResponse restoreResp = getClient().execute(restoreReq);
        List<RestoreItem> items = restoreResp.getRestoreItems();
        for (RestoreItem item : items) {
            assertTrue(item.isSuccess());
            assertTrue(null != item.getPath());
        }
    }

    @Test
    public void testRestoreRecreateFolders() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "testRestoreRecreateFolders.txt");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);
        String id = data.getId();

        DeleteInfostoreRequest delReq = new DeleteInfostoreRequest();
        delReq.setIds(Collections.singletonList(id));
        delReq.setFolders(Collections.singletonList(String.valueOf(folderId)));
        delReq.setHardDelete(false);
        delReq.setTimestamp(new Date());
        getClient().execute(delReq);
        id = toTrash(id);

        DeleteFolderRequest delFolderReq = new DeleteFolderRequest(String.valueOf(folderId), 0);
        getClient().execute(delFolderReq);

        RestoreRequest restoreReq = new RestoreRequest(Collections.singletonList(id), String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        RestoreResponse restoreResp = getClient().execute(restoreReq);
        List<RestoreItem> items = restoreResp.getRestoreItems();
        for (RestoreItem item : items) {
            assertTrue(item.isSuccess());
            assertTrue(null != item.getPath());
        }
    }

    private String toTrash(String id) throws Exception {
        String objId = id.split("/")[1];
        return getClient().getValues().getInfostoreTrashFolder() + "/" + objId;
    }

}
