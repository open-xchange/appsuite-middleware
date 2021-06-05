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

package javax.mail.internet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import org.junit.Assert;
import org.junit.Test;


/**
 * {@link MimeUtilityTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeUtilityTest {

    /**
     * Initializes a new {@link MimeUtilityTest}.
     */
    public MimeUtilityTest() {
        super();
    }

     @Test
     public void testImprovedDetectionOfTransferEncoding() throws MessagingException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("U29tZSB0ZXN0IHRleHQ=".getBytes());
        InputStream decodedStream = MimeUtility.decode(in, "base64 ");

        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        for (int b; (b = decodedStream.read()) > 0;) {
            out.write(b);
        }
        String test = out.toString("US-ASCII");

        Assert.assertEquals("Base64 decoding failed.", "Some test text", test);
    }

}
