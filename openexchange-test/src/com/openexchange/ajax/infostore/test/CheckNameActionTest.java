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

package com.openexchange.ajax.infostore.test;

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
     * @param name
     */
    public CheckNameActionTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidFilename() throws Exception {
        CheckNameRequest req = new CheckNameRequest("thisShouldNotFail", false);
        CheckNameResponse resp = client.execute(req);
        assertFalse(resp.hasError());
    }

    public void testInvalidCharacter() throws Exception {
        CheckNameRequest req = new CheckNameRequest("withInvalidCharacters<>:/?*\"\\|", false);
        CheckNameResponse resp = client.execute(req);
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

    public void testReservedNames() throws Exception {
        String[] RESERVED_NAMES = new String[] {"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "CON", "NUL",
                                                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "AUX", "PRN" };
        for (String name : RESERVED_NAMES) {
            CheckNameRequest req = new CheckNameRequest(name, false);
            CheckNameResponse resp = client.execute(req);
            assertTrue(resp.hasError());
            assertEquals(FileStorageExceptionCodes.RESERVED_NAME.getNumber(), resp.getException().getCode());
            assertTrue(resp.getErrorMessage().contains(name));
        }
    }

    public void testMatchOnlyExactReservedNames() throws Exception {
        String[] RESERVED_NAMES = new String[] {"COM", "CON1", "NULL", "LPT12", "AUXQWERT", "PRN2" };
        for (String name : RESERVED_NAMES) {
            CheckNameRequest req = new CheckNameRequest(name, false);
            CheckNameResponse resp = client.execute(req);
            assertFalse(resp.hasError());
        }
    }

    public void testOnlyDotsInName() throws Exception {
        CheckNameRequest req = new CheckNameRequest("..", false);
        CheckNameResponse resp = client.execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.ONLY_DOTS_NAME.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains(".."));
    }

    public void testEndsWithWithespace() throws Exception {
        CheckNameRequest req = new CheckNameRequest("willFailToo ", false);
        CheckNameResponse resp = client.execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains("whitespace"));
    }

    public void testEndsWithDot() throws Exception {
        CheckNameRequest req = new CheckNameRequest("willFailToo.", false);
        CheckNameResponse resp = client.execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.WHITESPACE_END.getNumber(), resp.getException().getCode());
        assertTrue(resp.getErrorMessage().contains("whitespace"));
    }

}
