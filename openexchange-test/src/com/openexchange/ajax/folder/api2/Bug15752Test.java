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

import java.io.ByteArrayInputStream;
import java.util.Date;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mail.actions.DeleteRequest;
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

    public Bug15752Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
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

    @Override
    protected void tearDown() throws Exception {
        if (null != mailIds) {
            client.execute(new DeleteRequest(mailIds, true));
        }
        if (null != testFolder) {
            client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OUTLOOK, testFolder));
        }
        super.tearDown();
    }

    public void testGetMailwithUnseen() throws Throwable {
        GetRequest request = new com.openexchange.ajax.mail.actions.GetRequest(mailIds[0], mailIds[1], true, true);
        request.setUnseen(true);
        client.execute(request);
    }

    private static final String MAIL =
        "From: #ADDR#\n" +
        "To: #ADDR#\n" +
        "Subject: Test for bug 15572\n" +
        "Mime-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: 8bit\n" +
        "\n" +
        "Test for bug 15572\n" +
        "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\n";
}
