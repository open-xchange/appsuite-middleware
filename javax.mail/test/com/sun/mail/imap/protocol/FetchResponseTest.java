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

package com.sun.mail.imap.protocol;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import com.sun.mail.iap.ProtocolException;


/**
 * {@link FetchResponseTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FetchResponseTest {

    /**
     * Initializes a new {@link FetchResponseTest}.
     */
    public FetchResponseTest() {
        super();
    }

    @Test
    public void testParseResponse() throws IOException, ProtocolException {
        IMAPResponse response = new IMAPResponse("* 1 FETCH (ENVELOPE (\"Fri, 4 Jul 2014 15:44:36 +0200\" \"[undeliverable] Fwd: Meble Fwd: FAKTURA\" ((NIL NIL \"Mail Delivery System\" \"\")) ((NIL NIL \"Mail Delivery System\" \"\")) ((NIL NIL \"Mail Delivery System\" \"\")) ((NIL NIL \"i.dzik\" \"cmkardiomed.pl\")) NIL NIL \"<1128626943.74607.1404481476736.open-xchange@poczta-ng.home.pl>\" \"<c9908b086a41c446@serwer1331422.home.pl>\") INTERNALDATE \"04-Jul-2014 15:44:36 +0200\" RFC822.SIZE 2803 FLAGS () BODYSTRUCTURE ((\"text\" \"plain\" (\"charset\" \"utf-8\") NIL NIL \"7bit\" 576 14 NIL NIL NIL NIL)(\"message\" \"delivery-status\" (\"name\" \"Delivery status\") NIL NIL \"7bit\" 351 NIL NIL NIL NIL)(\"message\" \"rfc822\" (\"name\" \"Message headers\") NIL NIL \"7bit\" 899 (\"Fri, 4 Jul 2014 15:44:36 +0200 (CEST)\" \"Fwd: Meble Fwd: FAKTURA\" ((\"Ireneusz Dzik\" NIL \"i.dzik\" \"cmkardiomed.pl\")) ((\"Ireneusz Dzik\" NIL \"i.dzik\" \"cmkardiomed.pl\")) ((\"Ireneusz Dzik\" NIL \"i.dzik\" \"cmkardiomed.pl\")) ((\"i.dzik\" NIL \"i.dzik\" \"cmkardioimed.pl\")) NIL NIL \"<365300416.4775.1404063954568.open-xchange@webmail.home.pl>\" \"<1128626943.74607.1404481476736.open-xchange@poczta-ng.home.pl>\") (NIL \"mixed\" (\"boundary\" \"----=_Part_74606_1220838199.1404481476525\") NIL NIL NIL) 19 NIL NIL NIL NIL) \"report\" (\"report-type\" \"delivery-status\" \"boundary\" \"_0293396865ec58f4e83d0f02fd22325c_idea\") NIL NIL NIL) UID 2 BODY[HEADER.FIELDS (IMPORTANCE X-PRIORITY)] {2}\r\n\r\n)");
        FetchResponse fetchResponse = new FetchResponse(response);

        int itemCount = fetchResponse.getItemCount();
        Assert.assertEquals("Unexpected item count.", 7, itemCount);

        BODYSTRUCTURE bodystructure = fetchResponse.getItem(BODYSTRUCTURE.class);
        Assert.assertNotNull(bodystructure);
        Assert.assertTrue(bodystructure.isMulti());

        BODYSTRUCTURE[] bodies = bodystructure.bodies;
        Assert.assertNotNull(bodies);
        Assert.assertEquals("Unexpected mulipart count.", 3, bodies.length);

        BODYSTRUCTURE nested = bodies[2];
        Assert.assertTrue(nested.isNested());

        BODYSTRUCTURE[] nestedBodies = nested.bodies;
        Assert.assertEquals("Unexpected nested part count.", 1, nestedBodies.length);

        BODYSTRUCTURE nestedMulti = nestedBodies[0];
        Assert.assertTrue(nestedMulti.isMulti());
        Assert.assertEquals("Unexpected nested mulipart count.", 0, nestedMulti.bodies.length);
    }

}
