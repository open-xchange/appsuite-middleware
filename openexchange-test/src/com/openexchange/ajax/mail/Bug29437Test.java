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

import static org.junit.Assert.assertNull;
import java.nio.charset.Charset;
import org.junit.Test;
import com.openexchange.ajax.mail.actions.AttachmentRequest;
import com.openexchange.ajax.mail.actions.AttachmentResponse;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;

/**
 * {@link Bug29437Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug29437Test extends AbstractMailTest {

	private static final String TEST_MAIL = "Received: from cyrus.qa.open-xchange.com ([unix socket]);  \n"
			+ "  by cyrus (Cyrus v2.4.16-Debian-2.4.16-4+deb7u1) with LMTPA;;   Fri, 18 Oct 2013 13:48:44 +0200\n"
			+ "Received: from zapox.oxoe.int (zapox.oxoe.int [192.168.32.185]);    by cyrus.qa.open-xchange.com (Postfix) with ESMTP id DD37C63CCB;    for <olox2.four@qa.open-xchange.com>; Fri, 18 Oct 2013 13:48:43 +0200 (CEST)\n"
			+ "Date: Fri, 18 Oct 2013 13:48:43 +0200\n" + "From: OLOX2 One <olox2.one@qa.open-xchange.com>\n"
			+ "To: OLOX2 Four <olox2.four@qa.open-xchange.com>\n"
			+ "Message-ID: <1888507157.60.1382096924476.open-xchange@zapox.oxoe.int>\n" + "MIME-Version: 1.0\n"
			+ "Content-Type: multipart/mixed; boundary=\"----=_Part_40_207613153.1382519651476\"\n"
			+ "X-Mailer: Open-Xchange Mailer v7.4.0-Rev14\n" + "Thread-Index: Ac7L9/8zOkGUEhG2SRW4xkTHtO2G9Q==\n"
			+ "Importance: normal\n" + "Priority: normal\n" + "X-OX-Marker: 903a05d6-a874-4854-9d6f-7230742652db\n"
			+ "\n" + "------=_Part_40_207613153.1382519651476\n" + "Content-Type: text/plain; charset=utf-8\n"
			+ "Content-Transfer-Encoding: 7bit\n" + "\n" + " \n" + "\n" + "\n"
			+ "------=_Part_40_207613153.1382519651476\n"
			+ "Content-Type: application/binary; name=\"New Text Document.txt\"\n" + "Content-Transfer-Encoding: 7bit\n"
			+ "Content-Disposition: attachment; filename=\"New Text Document.txt\"\n" + "\n" + "\n"
			+ "------=_Part_40_207613153.1382519651476--";

	private String[][] folderAndId;

	/**
	 * Initializes a new {@link Bug29437Test}.
	 *
	 * @param name
	 */
	public Bug29437Test() {
		super();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		ImportMailRequest importReq = new ImportMailRequest(getInboxFolder(), 0, Charset.forName("UTF-8"), TEST_MAIL);
		ImportMailResponse importResp = getClient().execute(importReq);
		folderAndId = importResp.getIds();
	}

	@Test
	public void testGetStructure() throws Exception {
		/*
		 * Nothing really to test here. But this test has to run without ugly error logs
		 * on the server. Otherwise we have a regression.
		 */
		GetRequest getReq = new GetRequest(folderAndId[0][0], folderAndId[0][1], true, true);
		getClient().execute(getReq);
	}

	@Test
	public void testGetAppointment() throws Exception {
		AttachmentRequest attachmentReq = new AttachmentRequest(
				new String[] { folderAndId[0][0], folderAndId[0][1], "2" }, false);
		AttachmentResponse attachmentResp = getClient().execute(attachmentReq);
		assertNull("No attachment expected!", attachmentResp.getResponse().getData());
	}

}
