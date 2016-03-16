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

import java.nio.charset.Charset;
import com.openexchange.ajax.mail.actions.AttachmentRequest;
import com.openexchange.ajax.mail.actions.AttachmentResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;


/**
 * {@link Bug29437Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug29437Test extends AbstractMailTest {

    private static final String TEST_MAIL = "Received: from cyrus.qa.open-xchange.com ([unix socket]);  \n" +
        "  by cyrus (Cyrus v2.4.16-Debian-2.4.16-4+deb7u1) with LMTPA;;   Fri, 18 Oct 2013 13:48:44 +0200\n" +
        "Received: from zapox.oxoe.int (zapox.oxoe.int [192.168.32.185]);    by cyrus.qa.open-xchange.com (Postfix) with ESMTP id DD37C63CCB;    for <olox2.four@qa.open-xchange.com>; Fri, 18 Oct 2013 13:48:43 +0200 (CEST)\n" +
        "Date: Fri, 18 Oct 2013 13:48:43 +0200\n" +
        "From: OLOX2 One <olox2.one@qa.open-xchange.com>\n" +
        "To: OLOX2 Four <olox2.four@qa.open-xchange.com>\n" +
        "Message-ID: <1888507157.60.1382096924476.open-xchange@zapox.oxoe.int>\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: multipart/mixed; boundary=\"----=_Part_40_207613153.1382519651476\"\n" +
        "X-Mailer: Open-Xchange Mailer v7.4.0-Rev14\n" +
        "Thread-Index: Ac7L9/8zOkGUEhG2SRW4xkTHtO2G9Q==\n" +
        "Importance: normal\n" +
        "Priority: normal\n" +
        "X-OX-Marker: 903a05d6-a874-4854-9d6f-7230742652db\n" +
        "\n" +
        "------=_Part_40_207613153.1382519651476\n" +
        "Content-Type: text/plain; charset=utf-8\n" +
        "Content-Transfer-Encoding: 7bit\n" +
        "\n" +
        " \n" +
        "\n" +
        "\n" +
        "------=_Part_40_207613153.1382519651476\n" +
        "Content-Type: application/binary; name=\"New Text Document.txt\"\n" +
        "Content-Transfer-Encoding: 7bit\n" +
        "Content-Disposition: attachment; filename=\"New Text Document.txt\"\n" +
        "\n" +
        "\n" +
        "------=_Part_40_207613153.1382519651476--";

    private String[][] folderAndId;

    /**
     * Initializes a new {@link Bug29437Test}.
     * @param name
     */
    public Bug29437Test(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        ImportMailRequest importReq = new ImportMailRequest("INBOX", 0, Charset.forName("UTF-8"), TEST_MAIL);
        ImportMailResponse importResp= getClient().execute(importReq);
        folderAndId = importResp.getIds();
    }

    public void tearDown() throws Exception {
        DeleteRequest deleteReq = new DeleteRequest(folderAndId, true);
        getClient().execute(deleteReq);
        super.tearDown();
    }

    public void testGetStructure() throws Exception {
        /*
         * Nothing really to test here. But this test has to run without ugly error logs on the server. Otherwise we have a regression.
         */
        GetRequest getReq = new GetRequest(folderAndId[0][0], folderAndId[0][1], true, true);
        getClient().execute(getReq);
    }

    public void testGetAppointment() throws Exception {
        AttachmentRequest attachmentReq = new AttachmentRequest(new String[] { folderAndId[0][0], folderAndId[0][1], "2" }, false);
        AttachmentResponse attachmentResp = getClient().execute(attachmentReq);
        assertNull("No attachment expected!", attachmentResp.getResponse().getData());
    }



}
