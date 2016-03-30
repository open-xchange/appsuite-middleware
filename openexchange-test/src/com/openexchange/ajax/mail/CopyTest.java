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

import java.io.IOException;
import javax.mail.internet.AddressException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.CopyRequest;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.exception.OXException;

/**
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CopyTest extends AbstractMailTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CopyTest.class);

    private String mailObject_25kb;

    public CopyTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        clearFolder(getDraftsFolder());

        /*
         * Create JSON mail object
         */
        mailObject_25kb = createSelfAddressed25KBMailObject().toString();
    }

    @Override
    public void tearDown() throws Exception {
        /*
         * Clean everything
         */
        // clearFolder(getInboxFolder());
        // clearFolder(getSentFolder());
        // clearFolder(getTrashFolder());
        // clearFolder(getDraftsFolder());
        super.tearDown();
    }

    public void testCopyingOneFolder() throws IOException, SAXException, JSONException, AddressException, OXException {
        String destinationFolderID = getDraftsFolder();

        /*
         * Insert a mail through a send request
         */
        SendResponse sr = getClient().execute(new SendRequest(mailObject_25kb));
        String mailID = sr.getFolderAndID()[1];
        LOG.info("Sent one mail, id : " + mailID);

        // Assert that there is only a single mail in the folder
        AllResponse allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        assertEquals("There should be only one message in the source folder", 1, allR.getMailMessages(COLUMNS_DEFAULT_LIST).length);

        // Assert that the destination folder is empty
        allR = Executor.execute(getSession(), new AllRequest(destinationFolderID, COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        assertEquals("There should be no messages in the destination folder", 0, allR.getMailMessages(COLUMNS_DEFAULT_LIST).length);

        Executor.execute(getSession(), new CopyRequest(mailID, getInboxFolder(), destinationFolderID));
        // String newMailID = cr.getID();
        // System.out.println("***** newMailID : " + newMailID);
        //
        // get the new mailID
        allR = Executor.execute(getSession(), new AllRequest(destinationFolderID, COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        assertEquals("There should be exactly one message in the destination folder", 1, allR.getMailMessages(COLUMNS_DEFAULT_LIST).length);
        //
        //
        // // Assert the message is in the destination folder
        // GetResponse gr = getClient().execute(new GetRequest(destinationFolderID, newMailID));
        // if (gr.hasError()) {
        // fail(allR.getException().toString());
        // }
        // assertEquals("The mail messages are not identical", MAIL_SUBJECT, gr.getMail(TimeZone.getDefault()).getSubject());

    }
}
