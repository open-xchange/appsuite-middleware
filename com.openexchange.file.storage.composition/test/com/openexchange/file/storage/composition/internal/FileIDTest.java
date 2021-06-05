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

package com.openexchange.file.storage.composition.internal;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.file.storage.composition.FileID;


/**
 * {@link FileIDTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileIDTest {
    @Test
    public void shouldDefaultToInfostoreAndDefaultAccount() {
        FileID fileID = new FileID("12");
        assertEquals("com.openexchange.infostore", fileID.getService());
        assertEquals("infostore", fileID.getAccountId());
        assertEquals(null, fileID.getFolderId());
        assertEquals("12", fileID.getFileId());
    }

    @Test
    public void shouldReturnSimpleUniqueIDForInfostore() {
        FileID fileID = new FileID("12");
        assertEquals("12", fileID.toUniqueID());
    }

    @Test
    public void shouldReturnSimpleUniqueIDForInfostore2() {
        FileID fileID = new FileID("123/12");
        assertEquals("12", fileID.getFileId());
        assertEquals("123", fileID.getFolderId());
        assertEquals("com.openexchange.infostore", fileID.getService());
    }
}
