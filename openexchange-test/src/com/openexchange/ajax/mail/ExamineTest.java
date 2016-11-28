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

import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.ExamineRequest;
import com.openexchange.ajax.mail.actions.ExamineResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;

public class ExamineTest extends AbstractMailTest {

    FolderObject subFolder;

	public ExamineTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

    @Override
    public void tearDown() throws Exception {
        if(subFolder!=null){
            com.openexchange.ajax.folder.actions.DeleteRequest fDel = new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, subFolder);
            client.execute(fDel);
        }
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        super.tearDown();
    }

    public void testExamineTest() throws OXException, IOException, JSONException {

		AJAXClient client = getClient();
		UserValues values = client.getValues();
		String folder = values.getInboxFolder();

		String name = "examineTest"+System.currentTimeMillis();
        String fullName = folder + "/"+ name;
		subFolder = Create.createPrivateFolder(name, FolderObject.MAIL, values.getUserId());
        subFolder.setFullName(fullName);
		InsertRequest subFolderReq = new InsertRequest(EnumAPI.OX_NEW, subFolder, true);
        client.execute(subFolderReq);
        subFolder.setLastModified(new Date(0));

        StringBuilder sb = new StringBuilder(8192);
        {
            InputStreamReader streamReader = null;
            try {
                streamReader = new InputStreamReader(new FileInputStream(new File(MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR), "mail010.eml")), "UTF-8");
                char[] buf = new char[2048];
                for (int read; (read = streamReader.read(buf, 0, 2048)) > 0;) {
                    sb.append(buf, 0, read);
                }
            } finally {
                Streams.close(streamReader);
            }
        }

        InputStream inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest importMailRequest = new ImportMailRequest(subFolder.getFullName(), MailFlag.SEEN.getValue(), inputStream);
        ImportMailResponse importResp = client.execute(importMailRequest);
        String[] id1 = importResp.getIds()[0];

		ExamineRequest examineReq = new ExamineRequest(subFolder.getFullName(), true);
		ExamineResponse examineRes = client.execute(examineReq);
		JSONObject jValidity = (JSONObject) examineRes.getData();
		String validity1 = jValidity.getString("validity");

        com.openexchange.ajax.folder.actions.DeleteRequest fDel = new com.openexchange.ajax.folder.actions.DeleteRequest(
                EnumAPI.OX_NEW,
                subFolder);
            client.execute(fDel);


		subFolder = Create.createPrivateFolder(name, FolderObject.MAIL, values.getUserId());
		subFolder.setFullName(fullName);
		subFolderReq = new InsertRequest(EnumAPI.OX_NEW, subFolder, true);
        client.execute(subFolderReq);
        subFolder.setLastModified(new Date(0));

        inputStream = Streams.newByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), client.getValues().getSendAddress()).getBytes(com.openexchange.java.Charsets.UTF_8));
        importMailRequest = new ImportMailRequest(subFolder.getFullName(), MailFlag.SEEN.getValue(), inputStream);
        importResp = client.execute(importMailRequest);
        String[] id2 = importResp.getIds()[0];


		examineReq = new ExamineRequest(subFolder.getFullName(), true);
		examineRes = client.execute(examineReq);
        jValidity = (JSONObject) examineRes.getData();
        String validity2 = jValidity.getString("validity");
        if (validity1.equals(validity2)) {
            assertNotEquals("Expected diffent uid than " + id1[1], id1[1], id2[1]);
        } else {
            assertNotEquals("Expected diffent validity than " + validity1, validity1, validity2);
        }

	}

}