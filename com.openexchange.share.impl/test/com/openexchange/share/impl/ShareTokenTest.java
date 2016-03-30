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

package com.openexchange.share.impl;

import static org.junit.Assert.assertEquals;
import java.util.Random;
import org.junit.Test;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.share.core.tools.ShareToken;


/**
 * {@link ShareTokenTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTokenTest {

    @Test
    public void testSomeCombinations() throws Exception {
        Random r = new Random();
        for (int i = 0; i < 100000; i++) {
            int userId = r.nextInt(Integer.MAX_VALUE);
            int contextId = r.nextInt(Integer.MAX_VALUE);
            assertToken(new int[] {userId, contextId});
//            System.out.println(token);
        }

        assertToken(new int[] {0, 0});
        assertToken(new int[] {0, Integer.MAX_VALUE});
        assertToken(new int[] {Integer.MAX_VALUE, 0});
        assertToken(new int[] {1, 1});
        assertToken(new int[] {1, Integer.MAX_VALUE});
        assertToken(new int[] {Integer.MAX_VALUE, 1});
    }

    private static String assertToken(int[] cidAndUid) throws Exception {
        UserImpl testGuest = new UserImpl();
        testGuest.setId(cidAndUid[1]);
        ShareToken.assignBaseToken(testGuest);
        ShareToken token = new ShareToken(cidAndUid[0], testGuest);
        assertEquals(cidAndUid[0], token.getContextID());
        assertEquals(cidAndUid[1], token.getUserID());
        return token.getToken();
    }

}
