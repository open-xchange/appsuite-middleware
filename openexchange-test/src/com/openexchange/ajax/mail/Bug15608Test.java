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

import static com.openexchange.mail.MailListField.FLAGS;
import static com.openexchange.mail.MailListField.ID;
import java.io.ByteArrayInputStream;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.mail.actions.ListRequest;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link Bug15608Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15608Test extends AbstractAJAXSession {

    private static final int ORIG_FLAGS = 32;
    private static final int[] ATTRIBUTES = { ID.getField(), FLAGS.getField() };
    private AJAXClient client;
    private TimeZone timeZone;
    private String folder;
    private String address;
    private String[][] ids;

    public Bug15608Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        folder = client.getValues().getInboxFolder();
        address = client.getValues().getSendAddress();
        final String mail = TestMails.replaceAddresses(TestMails.UMLAUT_MAIL, address);
        final ByteArrayInputStream[] massMails = new ByteArrayInputStream[100];
        for (int i = 0; i < massMails.length; i++) {
            massMails[i] = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        }
        final ImportMailRequest request = new ImportMailRequest(folder, ORIG_FLAGS, massMails);
        final ImportMailResponse response = client.execute(request);
        ids = response.getIds();
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(ids, true));
        super.tearDown();
    }

    public void testFlags() throws Throwable {
        {
            final ListRequest request = new ListRequest(ids, ATTRIBUTES);
            final CommonListResponse response = client.execute(request);
            final int flagsPos = response.getColumnPos(FLAGS.getField());
            for (final Object[] mail : response) {
                final int testFlags = ((Integer) mail[flagsPos]).intValue();
                assertEquals("Wanted flags are not set.", ORIG_FLAGS, testFlags);
            }
        }
        for (final String[] folderAndId : ids) {
            final String mailId = folderAndId[1];
            if (null != mailId) {
                final GetRequest request = new GetRequest(folder, mailId);
                request.setUnseen(true);
                final GetResponse response = client.execute(request);
                final MailMessage mail = response.getMail(timeZone);
                final int testFlags = mail.getFlags();
                assertEquals("Wanted flags are not set.", ORIG_FLAGS, testFlags);
            }
        }
    }
}
