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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link CreateFileWithIllegalCharactersTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class CreateFileWithIllegalCharactersTest extends AbstractInfostoreTest {

    private final String[] RESERVED_NAMES = new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "CON", "NUL", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "AUX", "PRN" };

    /**
     * Initializes a new {@link CreateFileWithIllegalCharactersTest}.
     * 
     * @param name
     */
    public CreateFileWithIllegalCharactersTest() {
        super();
    }

    @Test
    public void testCreateFileWithIllegalCharacters() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("invalid<>:/?*\"\\|");
        NewInfostoreRequest req = new NewInfostoreRequest(file);
        NewInfostoreResponse resp = getClient().execute(req);
        assertNotNull(resp);
        assertTrue(resp.hasError());
        OXException e = resp.getException();
        assertEquals(FileStorageExceptionCodes.ILLEGAL_CHARACTERS.getNumber(), e.getCode());
        assertTrue(e.getMessage().contains("<"));
        assertTrue(e.getMessage().contains(">"));
        assertTrue(e.getMessage().contains(":"));
        assertTrue(e.getMessage().contains("/"));
        assertTrue(e.getMessage().contains("?"));
        assertTrue(e.getMessage().contains("*"));
        assertTrue(e.getMessage().contains("\""));
        assertTrue(e.getMessage().contains("\\"));
        assertTrue(e.getMessage().contains("|"));
    }

    @Test
    public void testCreateFileWithReservedName() throws Exception {
        for (String name : RESERVED_NAMES) {
            File file = new DefaultFile();
            file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
            file.setFileName(name);
            NewInfostoreRequest req = new NewInfostoreRequest(file);
            NewInfostoreResponse resp = getClient().execute(req);
            assertNotNull(resp);
            assertTrue(resp.hasError());
            OXException e = resp.getException();
            assertEquals(FileStorageExceptionCodes.RESERVED_NAME.getNumber(), e.getCode());
            assertTrue(e.getMessage().contains(name));
        }
    }

    @Test
    public void testCreateFilenameEndsWithWithespace() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("test ");
        NewInfostoreRequest req = new NewInfostoreRequest(file);
        NewInfostoreResponse resp = getClient().execute(req);
        assertNotNull(resp);
        assertTrue(resp.hasError());
        OXException e = resp.getException();
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), e.getCode());
        assertTrue(e.getMessage().contains("whitespace"));
    }

    @Test
    public void testCreateFilenameEndsWithDot() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("test.");
        NewInfostoreRequest req = new NewInfostoreRequest(file);
        NewInfostoreResponse resp = getClient().execute(req);
        assertNotNull(resp);
        assertTrue(resp.hasError());
        OXException e = resp.getException();
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), e.getCode());
        assertTrue(e.getMessage().contains("dot"));
    }

    @Test
    public void testCreateReservedFolderName() throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        file.setFileName("..");
        NewInfostoreRequest req = new NewInfostoreRequest(file);
        NewInfostoreResponse resp = getClient().execute(req);
        assertNotNull(resp);
        assertTrue(resp.hasError());
        OXException e = resp.getException();
        assertEquals(FileStorageExceptionCodes.ONLY_DOTS_NAME.getNumber(), e.getCode());
        assertTrue(e.getMessage().contains(".."));
    }

}
