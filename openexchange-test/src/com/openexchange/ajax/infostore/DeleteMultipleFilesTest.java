/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;

/**
 * {@link DeleteMultipleFilesTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleFilesTest extends InfostoreAJAXTest {

    private List<String> itemIds;
    private List<String> folderIds;

    /**
     * Initializes a new {@link DeleteMultipleFilesTest}.
     *
     * @param name
     */
    public DeleteMultipleFilesTest() {
        super();

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        itemIds = new ArrayList<String>();
        folderIds = new ArrayList<String>();
        java.io.File f1 = java.io.File.createTempFile("file1", "txt");
        writeBytes("Hello World", f1);

        final File data1 = new DefaultFile();
        data1.setFileName(f1.getName());
        data1.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        data1.setCreated(new Date());
        final NewInfostoreRequest newReq1 = new NewInfostoreRequest(data1, f1);
        final NewInfostoreResponse newRes1 = getClient().execute(newReq1);
        itemIds.add(newRes1.getID());
        folderIds.add(data1.getFolderId());

        java.io.File f2 = java.io.File.createTempFile("file2", "txt");
        writeBytes("Hello World2", f2);

        final File data2 = new DefaultFile();
        data2.setFileName(f2.getName());
        data2.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        data2.setCreated(new Date());
        final NewInfostoreRequest newReq2 = new NewInfostoreRequest(data2, f2);
        final NewInfostoreResponse newRes2 = getClient().execute(newReq2);
        itemIds.add(newRes2.getID());
        folderIds.add(data2.getFolderId());
    }

    private void writeBytes(final String string, final java.io.File ods) {
        PrintWriter p = null;
        try {
            p = new PrintWriter(new FileWriter(ods));
            p.write(string);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (p != null) {
                p.close();
            }
        }
    }

    @Test
    public void testDeleteMultipleFiles() throws Exception {
        Date lastModified;
        {
            final GetInfostoreRequest get = new GetInfostoreRequest(itemIds.get(0));
            get.setFailOnError(true);
            lastModified = getClient().execute(get).getTimestamp();
        }
        {
            final GetInfostoreRequest get = new GetInfostoreRequest(itemIds.get(1));
            get.setFailOnError(true);
            final Date tmp = getClient().execute(get).getTimestamp();
            lastModified = lastModified.before(tmp) ? tmp : lastModified;
        }

        final DeleteInfostoreRequest delReq = new DeleteInfostoreRequest(itemIds, folderIds, lastModified);
        final DeleteInfostoreResponse delRes = getClient().execute(delReq);
        final JSONArray json = (JSONArray) delRes.getData();
        final int len = json.length();
        for (int i = 0; i < len; i++) {
            final JSONObject jObject = json.getJSONObject(i);
            assertFalse("Delete failed: " + delRes.getResponse().toString(), itemIds.contains(jObject.getString("id")));
        }
        //assertTrue("Delete failed: " + delRes.getResponse().toString(), json.isNull(0));
    }

}
