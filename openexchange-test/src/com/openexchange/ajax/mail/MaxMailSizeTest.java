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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
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
    public MaxMailSizeTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(getClient(), true);
    }

    @Test
    public void testSendWithManager() throws OXException, IOException, JSONException {
        UserValues values = getClient().getValues();

        // Should work
        TestMail mail = new TestMail();
        mail.setSubject("Test MaxMailSize");
        mail.setFrom(values.getSendAddress());
        mail.setTo(Arrays.asList(new String[] { values.getSendAddress() }));
        mail.setContentType(MailContentType.PLAIN.toString());
        mail.setBody("Test Mail");
        mail.sanitize();

        TestMail inSentBox = manager.send(mail, new FooInputStream(3500000L), "text/plain"); // Results in approx. 4800000 Byte Mail Size
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
        manager.send(mail, new FooInputStream(3800000L), "text/plain"); // Results in > 5000000 Byte Mail Size
        assertTrue("Should not pass", manager.getLastResponse().hasError());
        OXException exception = manager.getLastResponse().getException();
        assertEquals("Wrong exception.", MailExceptionCode.MAX_MESSAGE_SIZE_EXCEEDED.getNumber(), exception.getCode());
    }

}
