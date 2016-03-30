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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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

    private AJAXClient client;
    private List<String> itemIds;
    private List<String> folderIds;

    /**
     * Initializes a new {@link DeleteMultipleFilesTest}.
     * @param name
     */
    public DeleteMultipleFilesTest(final String name) {
        super(name);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        itemIds = new ArrayList<String>();
        folderIds = new ArrayList<String>();
        java.io.File f1 = java.io.File.createTempFile("file1", "txt");
        writeBytes("Hello World", f1);

        final File data1 = new DefaultFile();
        data1.setFileName(f1.getName());
        data1.setFolderId(String.valueOf(client.getValues().getPrivateInfostoreFolder()));
        data1.setCreated(new Date());
        final NewInfostoreRequest newReq1 = new NewInfostoreRequest(data1, f1);
        final NewInfostoreResponse newRes1 = client.execute(newReq1);
        itemIds.add(newRes1.getID());
        folderIds.add(data1.getFolderId());

        java.io.File f2 = java.io.File.createTempFile("file2", "txt");
        writeBytes("Hello World2", f2);

        final File data2 = new DefaultFile();
        data2.setFileName(f2.getName());
        data2.setFolderId(String.valueOf(client.getValues().getPrivateInfostoreFolder()));
        data2.setCreated(new Date());
        final NewInfostoreRequest newReq2 = new NewInfostoreRequest(data2, f2);
        final NewInfostoreResponse newRes2 = client.execute(newReq2);
        itemIds.add(newRes2.getID());
        folderIds.add(data2.getFolderId());
    }

    private void writeBytes(final String string, final java.io.File ods) {
    	PrintWriter p = null;
    	try {
			p = new PrintWriter(new FileWriter(ods));
			p.write(string);
		} catch (final IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			if (p != null) { p.close(); }
		}
	}

	@Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testDeleteMultipleFiles() throws Exception {
        Date lastModified;
        {
            final GetInfostoreRequest get = new GetInfostoreRequest(itemIds.get(0));
            get.setFailOnError(true);
            lastModified = client.execute(get).getTimestamp();
        }
        {
            final GetInfostoreRequest get = new GetInfostoreRequest(itemIds.get(1));
            get.setFailOnError(true);
            final Date tmp = client.execute(get).getTimestamp();
            lastModified = lastModified.before(tmp) ? tmp : lastModified;
        }

        final DeleteInfostoreRequest delReq = new DeleteInfostoreRequest(itemIds, folderIds, lastModified);
        final DeleteInfostoreResponse delRes = client.execute(delReq);
        final JSONArray json = (JSONArray) delRes.getData();
        final int len = json.length();
        for (int i = 0; i < len; i++) {
            final JSONObject jObject = json.getJSONObject(i);
            assertFalse("Delete failed: " + delRes.getResponse().toString(), itemIds.contains(jObject.getString("id")));
        }
        //assertTrue("Delete failed: " + delRes.getResponse().toString(), json.isNull(0));
    }

}
