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
import java.io.InputStream;
import java.util.Arrays;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link MaxMailSizeTest} Tests the Parameter com.openexchange.mail.maxMailSize with a value of 5000000 (Must be set at server startup).
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MaxMailSizeTest extends AbstractMailTest {

    private MailTestManager manager;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public MaxMailSizeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(client, true);
    }

    @Override
    protected void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    public void testSendWithManager() throws OXException, IOException, SAXException, JSONException {
        UserValues values = client.getValues();

        // Should work
        TestMail mail = new TestMail();
        mail.setSubject("Test MaxMailSize");
        mail.setFrom(values.getSendAddress());
        mail.setTo(Arrays.asList(new String[] { values.getSendAddress() }));
        mail.setContentType(MailContentType.PLAIN.toString());
        mail.setBody("Test Mail");
        mail.sanitize();

        class FooInputStream extends InputStream {

            private final long size;

            private long read = 0L;

            public FooInputStream(long size) {
                super();
                this.size = size;
            }

            @Override
            public int read() throws IOException {
                return read++ < size ? 'a' : -1;
            }

        }

        TestMail inSentBox = manager.send(mail, new FooInputStream(3500000L)); // Results in approx. 4800000 Byte Mail Size
        assertFalse("Sending resulted in error.", manager.getLastResponse().hasError());
        assertEquals("Mail went into inbox", values.getSentFolder(), inSentBox.getFolder());

        // Should fail
        mail = new TestMail();
        mail.setSubject("Test MaxMailSize");
        mail.setFrom(values.getSendAddress());
        mail.setTo(Arrays.asList(new String[] { values.getSendAddress() }));
        mail.setContentType(MailContentType.PLAIN.toString());
        mail.setBody("Test Mail");
        mail.sanitize();

        manager.setFailOnError(false);
        manager.send(mail, new FooInputStream(3800000L)); // Results in > 5000000 Byte Mail Size
        assertTrue("Should not pass", manager.getLastResponse().hasError());
        OXException exception = manager.getLastResponse().getException();
        assertEquals("Wrong exception.", MailExceptionCode.MAX_MESSAGE_SIZE_EXCEEDED.getNumber(), exception.getCode());
    }

}
