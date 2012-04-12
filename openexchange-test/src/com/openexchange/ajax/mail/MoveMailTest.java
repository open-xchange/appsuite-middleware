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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;

/**
 * {@link MoveMailTest}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MoveMailTest extends AbstractMailTest {

    private UserValues values;

    public MoveMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    protected void tearDown() throws Exception {
        clearFolder(values.getSentFolder());
        clearFolder(values.getInboxFolder());
        clearFolder(values.getDraftsFolder());
        super.tearDown();
    }

    public void testShouldMoveFromSentToDrafts() throws OXException, IOException, SAXException, JSONException {
        MailTestManager manager = new MailTestManager(client, false);

        String mail = values.getSendAddress();
        sendMail(createEMail(mail, "Move a mail", "ALTERNATE", "Move from sent to drafts").toString());

        String origin = values.getInboxFolder();
        String destination = values.getDraftsFolder();

        TestMail myMail = new TestMail(getFirstMailInFolder(origin));
        String oldID = myMail.getId();

        TestMail movedMail = manager.move(myMail, destination);
        String newID = movedMail.getId();

        manager.get(destination, newID);
        assertTrue("Should produce no errors when getting moved e-mail", !manager.getLastResponse().hasError());
        assertTrue("Should produce no conflicts when getting moved e-mail", !manager.getLastResponse().hasConflicts());

        manager.get(origin, oldID);
        assertTrue("Should produce errors when trying to get moved e-mail from original place", manager.getLastResponse().hasError());
    }

    public void testShouldNotMoveToNonExistentFolder() throws OXException, IOException, SAXException, JSONException {
        MailTestManager manager = new MailTestManager(client, false);

        String mail = values.getSendAddress();
        sendMail(createEMail(mail, "Move another mail", "ALTERNATE", "Move from sent to drafts").toString());

        String origin = values.getSentFolder();
        String destination = values.getDraftsFolder() + "doesn't exist";

        TestMail myMail = new TestMail(getFirstMailInFolder(origin));
        String oldID = myMail.getId();

        manager.move(myMail, destination);
        assertTrue("Should produce error message when trying to move to nonexistent folder", manager.getLastResponse().hasError());
        assertEquals("Should produce proper error message ", "IMAP-1002", manager.getLastResponse().getException().getErrorCode());

        manager.get(origin, oldID);
        assertTrue("Should still have e-mail at original location", !manager.getLastResponse().hasError());
        assertTrue("Should produce no conflicts when getting e-mail from original location", !manager.getLastResponse().hasConflicts());
    }

    public void testShouldNotTryToMoveToSameFolder() throws Exception {
        MailTestManager manager = new MailTestManager(client, true);
        String sendAddress = values.getSendAddress();
        //Send mail to myself
        TestMail sendMail = manager.send(new TestMail(
            sendAddress,
            sendAddress,
            "Move me from sent to sent",
            MailContentType.PLAIN.toString(),
            "text"));
        // Check if mail exists in sent folder
        TestMail sentMail = manager.get(new String[] { getSentFolder(), sendMail.getId() });
        assertNotNull("Sent mail may not be null and has to be found in sent-items folder", sentMail);
        // Try to move to sent-items
        TestMail movedMail = manager.move(sentMail, getSentFolder());
        assertNotNull("Moved mail may not be null", movedMail);
        // Check that mail remains in the original location
        assertEquals("Mail should not be moved and remain in the original folder.", getSentFolder(), movedMail.getFolder());
        // Check that sent mail wasn't duplicated
        assertEquals("Mail shouldn't have been duplicated", 1, manager.findSimilarMailsInSameFolder(sentMail, client).size());
    }

}
