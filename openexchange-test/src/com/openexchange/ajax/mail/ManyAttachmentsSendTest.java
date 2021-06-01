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
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;

/**
 * {@link ManyAttachmentsSendTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - tests with manager
 */
public final class ManyAttachmentsSendTest extends AbstractMailTest {

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public ManyAttachmentsSendTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Tests the <code>action=new</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testSend() throws Throwable {
        /*
         * Create JSON mail object
         */
        final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();
        final SendRequest sendRequest = new SendRequest(mailObject_25kb);

        class FooInputStream extends InputStream {

            private final long size;

            private long read = 0L;

            public FooInputStream(long size) {
                super();
                this.size = size;
            }

            @Override
            public int read() throws IOException {
                return read++ < size ? (read % 76 == 0 ? '\n' : 'a') : -1;
            }

        }

        for (int i = 0; i < 100; i++) {
            sendRequest.addUpload(new FooInputStream(8192));
        }

        final SendResponse response = Executor.execute(getSession(), sendRequest);
        assertTrue("Send request failed", response.getFolderAndID() != null && response.getFolderAndID().length > 0);
    }

}
