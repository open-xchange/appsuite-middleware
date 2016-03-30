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

import org.json.JSONObject;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.java.Charsets;

/**
 * {@link Bug28913Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Bug28913Test extends AbstractMailTest {

    private String folder;
    private String[] ids;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Bug28913Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folder = client.getValues().getInboxFolder();
        final String mail = "From: Stefan Gabler <stefan.gabler@open-xchange.com>\n" +
            "Content-Type: multipart/alternative; boundary=\"Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\"\n" +
            "Date: Tue, 18 Jun 2013 16:22:02 -0400\n" +
            "Subject: Bug 26878\n" +
            "To: Tsapanidis Nikolaos <nikolaos.tsapanidis@open-xchange.com>\n" +
            "Message-Id: <75109DA3-4569-4A56-904C-505960E4976B@open-xchange.com>\n" +
            "Mime-Version: 1.0 (Mac OS X Mail 6.5 \\(1508\\))\n" +
            "X-Mailer: Apple Mail (2.1508)\n" +
            "\n" +
            "\n" +
            "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "Content-Type: text/plain;\n" +
            "    charset=us-ascii\n" +
            "\n" +
            "Hi,\n" +
            "\n" +
            "blah blah blah blah\n" +
            "blah blah blah blah.\n" +
            "\n" +
            "blah blah blah blah?\n" +
            "\n" +
            "Danke \n" +
            "Stefan\n" +
            "\n" +
            "\n" +
            "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "Content-Type: text/html;\n" +
            "    charset=us-ascii\n" +
            "\n" +
            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html charset=us-ascii\"></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">Hi,<div><br></div><div>blah blah blah blah?</div><div><pre class=\"bz_comment_text\" id=\"comment_text_4\" style=\"font-size: 12px; white-space: pre-wrap; width: 71em; \">blah blah blah blah.</pre><div><br></div></div><div>blah blah blah blah?</div><div><br></div><div>Danke&nbsp;</div><div>Stefan</div><div><br></div></body></html>\n" +
            "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E--";
        final ImportMailRequest request = new ImportMailRequest(folder, 0, Charsets.UTF_8, mail);
        final ImportMailResponse response = client.execute(request);
        ids = response.getIds()[0];
    }

    @Override
    protected void tearDown() throws Exception {
        client.executeSafe(new DeleteRequest(ids, true));
        super.tearDown();
    }

    /**
     * Tests the <code>action=get_structure</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testGet() throws Throwable {
        {
            final GetRequest request = new GetRequest(folder, ids[1], true, true);
            GetResponse response = client.execute(request);

            final JSONObject jData = (JSONObject) response.getResponse().getData();
            assertTrue("Missing field \"received_date\".", jData.hasAndNotNull("received_date"));
        }
    }

}
