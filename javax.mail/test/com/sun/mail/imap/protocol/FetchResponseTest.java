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

     @Test
     public void testParseSnippetResponse() throws IOException, ProtocolException {
        IMAPResponse response = new IMAPResponse("* 6336 FETCH (UID 92796 INTERNALDATE \"15-Jan-2018 12:42:19 +0100\" RFC822.SIZE 33590 FLAGS (\\Seen) ENVELOPE (\"Mon, 15 Jan 2018 12:41:58 +0100 (CET)\" \"[reports-services] Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk\" ((\"Diego Giron\" NIL \"diego.giron\" \"open-xchange.com\")) ((\"reports-services\" NIL \"reports-services-bounces\" \"open-xchange.com\")) ((\"Diego Giron\" NIL \"diego.giron\" \"open-xchange.com\")) ((NIL NIL \"reports-services\" \"open-xchange.com\")) NIL NIL \"<1757567916.3536.1512385130737@appsuite.open-xchange.com>\" \"<1965994247.484.1516016518443@appsuite-dev.open-xchange.com>\") BODYSTRUCTURE (((\"text\" \"plain\" (\"charset\" \"UTF-8\") NIL NIL \"quoted-printable\" 6019 129 NIL NIL NIL NIL)((\"text\" \"html\" (\"charset\" \"UTF-8\") NIL NIL \"quoted-printable\" 11635 253 NIL NIL NIL NIL)(\"image\" \"png\" NIL \"<ff73932de1344096947f9a9b22997cfa>\" NIL \"base64\" 8300 NIL (\"inline\" NIL) NIL NIL) \"related\" (\"boundary\" \"----=_Part_482_454602123.1516016518201\") NIL NIL NIL) \"alternative\" (\"boundary\" \"----=_Part_481_612880287.1516016518201\") NIL NIL NIL)(\"text\" \"plain\" (\"charset\" \"utf-8\") NIL NIL \"base64\" 252 4 NIL (\"inline\" NIL) NIL NIL) \"mixed\" (\"boundary\" \"===============0103388668==\") NIL NIL NIL) SNIPPET FUZZY {100}\r\n" +
            "Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk Executive management summary * HL Project plan stat BODY[HEADER.FIELDS (IMPORTANCE X-PRIORITY X-OPEN-XCHANGE-SHARE-URL)] {37}\r\n" +
            "X-Priority: 3\r\n" +
            "Importance: Medium\r\n" +
            "\r\n" +
            ")");
        FetchResponse fetchResponse = new FetchResponse(response);

        int itemCount = fetchResponse.getItemCount();
        Assert.assertEquals("Unexpected item count.", 8, itemCount);

        SNIPPET snippet = fetchResponse.getItem(SNIPPET.class);
        Assert.assertNotNull(snippet);
        Assert.assertTrue(snippet.getText().length() > 0);
        Assert.assertEquals("Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk Executive management summary * HL Project plan stat", snippet.getText());


        response = new IMAPResponse("* 6336 FETCH (UID 92796 INTERNALDATE \"15-Jan-2018 12:42:19 +0100\" RFC822.SIZE 33590 FLAGS (\\Seen) ENVELOPE (\"Mon, 15 Jan 2018 12:41:58 +0100 (CET)\" \"[reports-services] Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk\" ((\"Diego Giron\" NIL \"diego.giron\" \"open-xchange.com\")) ((\"reports-services\" NIL \"reports-services-bounces\" \"open-xchange.com\")) ((\"Diego Giron\" NIL \"diego.giron\" \"open-xchange.com\")) ((NIL NIL \"reports-services\" \"open-xchange.com\")) NIL NIL \"<1757567916.3536.1512385130737@appsuite.open-xchange.com>\" \"<1965994247.484.1516016518443@appsuite-dev.open-xchange.com>\") BODYSTRUCTURE (((\"text\" \"plain\" (\"charset\" \"UTF-8\") NIL NIL \"quoted-printable\" 6019 129 NIL NIL NIL NIL)((\"text\" \"html\" (\"charset\" \"UTF-8\") NIL NIL \"quoted-printable\" 11635 253 NIL NIL NIL NIL)(\"image\" \"png\" NIL \"<ff73932de1344096947f9a9b22997cfa>\" NIL \"base64\" 8300 NIL (\"inline\" NIL) NIL NIL) \"related\" (\"boundary\" \"----=_Part_482_454602123.1516016518201\") NIL NIL NIL) \"alternative\" (\"boundary\" \"----=_Part_481_612880287.1516016518201\") NIL NIL NIL)(\"text\" \"plain\" (\"charset\" \"utf-8\") NIL NIL \"base64\" 252 4 NIL (\"inline\" NIL) NIL NIL) \"mixed\" (\"boundary\" \"===============0103388668==\") NIL NIL NIL) SNIPPET (FUZZY {100}\r\n" +
            "Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk Executive management summary * HL Project plan stat) BODY[HEADER.FIELDS (IMPORTANCE X-PRIORITY X-OPEN-XCHANGE-SHARE-URL)] {37}\r\n" +
            "X-Priority: 3\r\n" +
            "Importance: Medium\r\n" +
            "\r\n" +
            ")");
        fetchResponse = new FetchResponse(response);

        itemCount = fetchResponse.getItemCount();
        Assert.assertEquals("Unexpected item count.", 8, itemCount);

        snippet = fetchResponse.getItem(SNIPPET.class);
        Assert.assertNotNull(snippet);
        Assert.assertTrue(snippet.getText().length() > 0);
        Assert.assertEquals("Bi-Weekly report CW51/CW52/CW1/CW2 2018 TalkTalk Executive management summary * HL Project plan stat", snippet.getText());
    }

}
