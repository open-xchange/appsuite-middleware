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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

    public void testCheckNameAction() throws Exception {
        CheckNameRequest req1 = new CheckNameRequest("thisShouldNotFail", false);
        CheckNameResponse resp1 = client.execute(req1);
        assertFalse(resp1.hasError());

        CheckNameRequest req2 = new CheckNameRequest("withInvalidCharacters?", false);
        CheckNameResponse resp2 = client.execute(req2);
        assertTrue(resp2.hasError());
        assertEquals(FileStorageExceptionCodes.ILLEGAL_CHARACTERS.getNumber(), resp2.getException().getCode());
        assertTrue(resp2.getErrorMessage().contains("?"));

        CheckNameRequest req3 = new CheckNameRequest("COM1", false); // withReservedName
        CheckNameResponse resp3 = client.execute(req3);
        assertTrue(resp3.hasError());
        assertEquals(FileStorageExceptionCodes.ILLEGAL_CHARACTERS.getNumber(), resp3.getException().getCode());
        assertTrue(resp3.getErrorMessage().contains("COM1"));
    }

}
