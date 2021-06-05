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

import static com.openexchange.mail.MailListField.FLAGS;
import static com.openexchange.mail.MailListField.ID;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
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
    private TimeZone timeZone;
    private String folder;
    private String address;
    private String[][] ids;

    public Bug15608Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        timeZone = getClient().getValues().getTimeZone();
        folder = getClient().getValues().getInboxFolder();
        address = getClient().getValues().getSendAddress();
        final String mail = TestMails.replaceAddresses(TestMails.UMLAUT_MAIL, address);
        final ByteArrayInputStream[] massMails = new ByteArrayInputStream[100];
        for (int i = 0; i < massMails.length; i++) {
            massMails[i] = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        }
        final ImportMailRequest request = new ImportMailRequest(folder, ORIG_FLAGS, massMails);
        final ImportMailResponse response = getClient().execute(request);
        ids = response.getIds();
    }

    @Test
    public void testFlags() throws Throwable {
        {
            final ListRequest request = new ListRequest(ids, ATTRIBUTES);
            final CommonListResponse response = getClient().execute(request);
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
                final GetResponse response = getClient().execute(request);
                final MailMessage mail = response.getMail(timeZone);
                final int testFlags = mail.getFlags();
                assertEquals("Wanted flags are not set.", ORIG_FLAGS, testFlags);
            }
        }
    }
}
