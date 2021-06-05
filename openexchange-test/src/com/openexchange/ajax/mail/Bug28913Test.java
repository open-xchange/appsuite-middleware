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

import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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
    public Bug28913Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = getClient().getValues().getInboxFolder();
        final String mail = "From: Stefan Gabler <stefan.gabler@open-xchange.com>\n" + "Content-Type: multipart/alternative; boundary=\"Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\"\n" + "Date: Tue, 18 Jun 2013 16:22:02 -0400\n" + "Subject: Bug 26878\n" + "To: Tsapanidis Nikolaos <nikolaos.tsapanidis@open-xchange.com>\n" + "Message-Id: <75109DA3-4569-4A56-904C-505960E4976B@open-xchange.com>\n" + "Mime-Version: 1.0 (Mac OS X Mail 6.5 \\(1508\\))\n" + "X-Mailer: Apple Mail (2.1508)\n" + "\n" + "\n" + "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\n" + "Content-Transfer-Encoding: 7bit\n" + "Content-Type: text/plain;\n" + "    charset=us-ascii\n" + "\n" + "Hi,\n" + "\n" + "blah blah blah blah\n" + "blah blah blah blah.\n" + "\n" + "blah blah blah blah?\n" + "\n" + "Danke \n" + "Stefan\n" + "\n" + "\n" + "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E\n" + "Content-Transfer-Encoding: 7bit\n" + "Content-Type: text/html;\n" + "    charset=us-ascii\n" + "\n" + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html charset=us-ascii\"></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">Hi,<div><br></div><div>blah blah blah blah?</div><div><pre class=\"bz_comment_text\" id=\"comment_text_4\" style=\"font-size: 12px; white-space: pre-wrap; width: 71em; \">blah blah blah blah.</pre><div><br></div></div><div>blah blah blah blah?</div><div><br></div><div>Danke&nbsp;</div><div>Stefan</div><div><br></div></body></html>\n" + "--Apple-Mail=_9D50D660-586B-4827-BE19-00B045E0B10E--";
        final ImportMailRequest request = new ImportMailRequest(folder, 0, Charsets.UTF_8, mail);
        final ImportMailResponse response = getClient().execute(request);
        ids = response.getIds()[0];
    }

    /**
     * Tests the <code>action=get_structure</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testGet() throws Throwable {
        {
            final GetRequest request = new GetRequest(folder, ids[1], true, true);
            GetResponse response = getClient().execute(request);

            final JSONObject jData = (JSONObject) response.getResponse().getData();
            assertTrue("Missing field \"received_date\".", jData.hasAndNotNull("received_date"));
        }
    }

}
