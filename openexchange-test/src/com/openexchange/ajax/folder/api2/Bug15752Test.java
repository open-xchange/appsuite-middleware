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

package com.openexchange.ajax.folder.api2;

import java.io.ByteArrayInputStream;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.mail.actions.MoveMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * Tries to reproduce the issue described in bug 15752. OX complains about no keep-seen access although this should be there.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15752Test extends AbstractAJAXSession {

    private AJAXClient client;
    private String inboxFolder;
    private String[] mailIds;
    private FolderObject testFolder;

    public Bug15752Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        inboxFolder = client.getValues().getInboxFolder();
        String mail = MAIL.replaceAll("#ADDR#", client.getValues().getSendAddress());
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(inboxFolder, 0, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        mailIds = response.getIds()[0];
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFolderName("testFolder4Bug15752-2-" + System.currentTimeMillis());
        testFolder.setFullName(inboxFolder + '/' + testFolder.getFolderName());
        InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, testFolder);
        InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(testFolder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        testFolder.setLastModified(new Date());
        UpdateMailResponse uResp = client.execute(new MoveMailRequest(inboxFolder, testFolder.getFullName(), mailIds[1], true));
        mailIds = new String[] { uResp.getFolder(), uResp.getID() };
    }

    @Test
    public void testGetMailwithUnseen() throws Throwable {
        GetRequest request = new com.openexchange.ajax.mail.actions.GetRequest(mailIds[0], mailIds[1], true, true);
        request.setUnseen(true);
        client.execute(request);
    }

    private static final String MAIL = "From: #ADDR#\n" + "To: #ADDR#\n" + "Subject: Test for bug 15572\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "Test for bug 15572\n" + "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\n";
}
