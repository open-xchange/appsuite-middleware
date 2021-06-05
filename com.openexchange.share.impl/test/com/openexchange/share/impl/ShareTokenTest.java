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
