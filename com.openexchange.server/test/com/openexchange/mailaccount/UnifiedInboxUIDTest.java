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

package com.openexchange.mailaccount;

import org.junit.Assert;
import org.junit.Test;
import com.openexchange.mail.MailPath;

/**
 * {@link UnifiedInboxUIDTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UnifiedInboxUIDTest {
    /**
     * Initializes a new {@link UnifiedInboxUIDTest}.
     */
    public UnifiedInboxUIDTest() {
        super();
    }

     @Test
     public void testExtractPossibleNestedMailPath() {
        String s = "default371/INBOX/%64%65%66%61ult0%2FIN%42OX%2F396847";
        MailPath path = UnifiedInboxUID.extractPossibleNestedMailPath(s);
        Assert.assertNotNull(path);

        s = "default0/INBOX/396847";
        path = UnifiedInboxUID.extractPossibleNestedMailPath(s);
        Assert.assertNull(path);

        s = "default0/INBOX/Drafts/2846";
        path = UnifiedInboxUID.extractPossibleNestedMailPath(s);
        Assert.assertNull(path);
    }

}
