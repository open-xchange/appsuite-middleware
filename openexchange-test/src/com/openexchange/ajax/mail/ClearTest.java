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
import javax.mail.internet.AddressException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.ClearRequest;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.exception.OXException;

/**
 *
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class ClearTest extends AbstractMailTest {

	private static final Log LOG = LogFactory.getLog(ClearTest.class);
	private String mailObject_25kb;

	public ClearTest(final String name) {
		super(name);
	}

	@Override
    public void setUp() throws Exception{
		super.setUp();
		/*
		 * Clean everything
		 */
		clearFolder(getInboxFolder());
		clearFolder(getSentFolder());
		clearFolder(getTrashFolder());

		/*
		 * Create JSON mail object
		 */
		mailObject_25kb = createSelfAddressed25KBMailObject().toString();
	}

	@Override
    public void tearDown() throws Exception{
		/*
		 * Clean everything
		 */
		clearFolder(getInboxFolder());
		clearFolder(getSentFolder());
		clearFolder(getTrashFolder());
		super.tearDown();
	}

	public void testClearingOneFolder() throws OXException, IOException, SAXException, JSONException, AddressException, OXException {
		/*
		 * Insert <numOfMails> mails through a send request
		 */
		final int numOfMails = 5;
		LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
		for (int i = 0; i < numOfMails; i++) {
		    getClient().execute(new SendRequest(mailObject_25kb));
			LOG.info("Sent " + (i + 1) + ". mail of " + numOfMails);
		}

		// Assert that there are 5 Mails in the folder
		AllResponse allR = Executor.execute(getSession(), new AllRequest(
				getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        assertEquals("There should be 5 messages in the folder", allR.getMailMessages(COLUMNS_DEFAULT_LIST).length, 5);

        // Send the clear request
        String [] array = {getInboxFolder()};
        getClient().execute(new ClearRequest(array));

        // Assert there are no messages in the folder
        allR = Executor.execute(getSession(), new AllRequest(
				getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        assertEquals("There should be no messages in the folder", allR.getMailMessages(COLUMNS_DEFAULT_LIST).length, 0);
	}

}
