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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertNotEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.ExamineRequest;
import com.openexchange.ajax.mail.actions.ExamineResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;
import com.openexchange.test.common.configuration.AJAXConfig;

public class ExamineTest extends AbstractMailTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

    @Test
    public void testExamineTest() throws OXException, IOException, JSONException {
        UserValues values = getClient().getValues();
        String folder = values.getInboxFolder();

        String name = "examineTest" + System.currentTimeMillis();
        String fullName = folder + "/" + name;
        FolderObject subFolder = Create.createPrivateFolder(name, FolderObject.MAIL, values.getUserId());
        subFolder.setFullName(fullName);
        InsertRequest subFolderReq = new InsertRequest(EnumAPI.OX_NEW, subFolder, true);
        getClient().execute(subFolderReq);
        subFolder.setLastModified(new Date(0));

        StringBuilder sb = new StringBuilder(8192);
        {
            InputStreamReader streamReader = null;
            try {
                streamReader = new InputStreamReader(new FileInputStream(new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "mail010.eml")), "UTF-8");
                char[] buf = new char[2048];
                for (int read; (read = streamReader.read(buf, 0, 2048)) > 0;) {
                    sb.append(buf, 0, read);
                }
            } finally {
                Streams.close(streamReader);
            }
        }

        InputStream inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest importMailRequest = new ImportMailRequest(subFolder.getFullName(), MailFlag.SEEN.getValue(), inputStream);
        ImportMailResponse importResp = getClient().execute(importMailRequest);
        String[] id1 = importResp.getIds()[0];

        ExamineRequest examineReq = new ExamineRequest(subFolder.getFullName(), true);
        ExamineResponse examineRes = getClient().execute(examineReq);
        JSONObject jValidity = (JSONObject) examineRes.getData();
        String validity1 = jValidity.getString("validity");

        com.openexchange.ajax.folder.actions.DeleteRequest fDel = new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, subFolder);
        getClient().execute(fDel);

        subFolder = Create.createPrivateFolder(name, FolderObject.MAIL, values.getUserId());
        subFolder.setFullName(fullName);
        subFolderReq = new InsertRequest(EnumAPI.OX_NEW, subFolder, true);
        getClient().execute(subFolderReq);
        subFolder.setLastModified(new Date(0));

        inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), getClient().getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        importMailRequest = new ImportMailRequest(subFolder.getFullName(), MailFlag.SEEN.getValue(), inputStream);
        importResp = getClient().execute(importMailRequest);
        String[] id2 = importResp.getIds()[0];

        examineReq = new ExamineRequest(subFolder.getFullName(), true);
        examineRes = getClient().execute(examineReq);
        jValidity = (JSONObject) examineRes.getData();
        String validity2 = jValidity.getString("validity");
        if (validity1.equals(validity2)) {
            assertNotEquals("Expected diffent uid than " + id1[1], id1[1], id2[1]);
        } else {
            assertNotEquals("Expected diffent validity than " + validity1, validity1, validity2);
        }
    }

}
