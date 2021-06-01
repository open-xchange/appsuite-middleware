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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.CheckNameRequest;
import com.openexchange.ajax.infostore.actions.CheckNameResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link CheckNameActionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class CheckNameActionTest extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link CheckNameActionTest}.
     * 
     * @param name
     */
    public CheckNameActionTest() {
        super();
    }

    @Test
    public void testValidFilename() throws Exception {
        CheckNameRequest req = new CheckNameRequest("thisShouldNotFail", false);
        CheckNameResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
    }

    @Test
    public void testInvalidCharacter() throws Exception {
        CheckNameRequest req = new CheckNameRequest("withInvalidCharacters<>:/?*\"\\|", false);
        CheckNameResponse resp = getClient().execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.ILLEGAL_CHARACTERS.getNumber(), resp.getException().getCode());
        OXException e = resp.getException();
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
    public void testReservedNames() throws Exception {
        String[] RESERVED_NAMES = new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "CON", "NUL", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "AUX", "PRN" };
        for (String name : RESERVED_NAMES) {
            CheckNameRequest req = new CheckNameRequest(name, false);
            CheckNameResponse resp = getClient().execute(req);
            assertTrue(resp.hasError());
            assertEquals(FileStorageExceptionCodes.RESERVED_NAME.getNumber(), resp.getException().getCode());
            assertTrue(resp.getErrorMessage().contains(name));
        }
    }

    @Test
    public void testMatchOnlyExactReservedNames() throws Exception {
        String[] RESERVED_NAMES = new String[] { "COM", "CON1", "NULL", "LPT12", "AUXQWERT", "PRN2" };
        for (String name : RESERVED_NAMES) {
            CheckNameRequest req = new CheckNameRequest(name, false);
            CheckNameResponse resp = getClient().execute(req);
            assertFalse(resp.hasError());
        }
    }

    @Test
    public void testOnlyDotsInName() throws Exception {
        CheckNameRequest req = new CheckNameRequest("..", false);
        CheckNameResponse resp = getClient().execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.ONLY_DOTS_NAME.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains(".."));
    }

    @Test
    public void testEndsWithWithespace() throws Exception {
        CheckNameRequest req = new CheckNameRequest("willFailToo ", false);
        CheckNameResponse resp = getClient().execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains("whitespace"));
    }

    @Test
    public void testEndsWithDot() throws Exception {
        CheckNameRequest req = new CheckNameRequest("willFailToo.", false);
        CheckNameResponse resp = getClient().execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains("whitespace"));
    }

}
