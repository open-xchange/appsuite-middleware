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

package com.openexchange.mail.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.mail.internet.MimeUtility;
import org.junit.Test;


/**
 * {@link MimeUtilityTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public class MimeUtilityTest {
    /**
     * Initializes a new {@link MimeUtilityTest}.
     */
    public MimeUtilityTest() {
        super();
    }

         @Test
     public void testBug31828() {
        try {
            final byte[] emojiBytes = new byte[] { -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87, -16, -97, -110, -87 };
            final String emojiString = new String(emojiBytes, "UTF8");
            final String mailSafeForm = MimeUtility.fold(9, MimeUtility.encodeText(emojiString, "UTF-8", null));

            assertNotNull(mailSafeForm);
            assertTrue("Unexpected mail-safe form", mailSafeForm.startsWith("=?UTF-8?B?8J+SqfCfkqnwn5Kp8J"));

            final String[] splits = mailSafeForm.split("\r?\n");

            assertEquals("Unexpected number of lines", 2, splits.length);
            assertEquals("Unexpected first line", "=?UTF-8?B?8J+SqfCfkqnwn5Kp8J+SqfCfkqnwn5Kp8J+SqQ==?=", splits[0].trim());
            assertEquals("Unexpected second line", "=?UTF-8?B?8J+SqfCfkqnwn5Kp8J+SqfCfkqnwn5Kp8J+SqfCfkqk=?=", splits[1].trim());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
