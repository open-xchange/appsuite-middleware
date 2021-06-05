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

package com.openexchange.oauth.yahoo;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.yahoo.internal.YahooOAuthServiceMetaData;
import com.openexchange.oauth.yahoo.internal.YahooServiceImpl;
import com.openexchange.oauth.yahoo.osgi.YahooOAuthActivator;

/**
 * {@link YahooConnectionTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class YahooConnectionTest {

    // Obviously these 2 values need to fit together. token and tokenSecret can be obtained via YahooSimpleConnectionTest
    private final String token = "A=tuky.uX3oSgvhynUoHD4Bqi1Jbi4_BXYlaPVRG1MyUNdYlHKdX6eLcGA7sd5u_vELlWhgMf1VKJONOF2SUG85T5hNOEue2kZiF3YEZY7YisF4cIamR_zZWwukmmLhDP414UVvR_jhuAXTuDiTMv9vYBPoFhhSd78MRkPUOOTGvkV6snd3rOYfob8FkFa7GDYaSzgBeE8875ekRzTFP6MVDUUdMI0poLRyCgzLRWrploJsHy8lcPgo7fLG2fq1hT03ZRk6OOt.SFSskiyxcCg6kb2cRSkd5_xeivQItjjIzCY3yJy6L.9vFRAAoS4sohnAgUhj0qKnuEvoOYjNVycAJS5fSyKdcbD5N3U3WDgMvPf0uoJNiswFFjwSoyvFAN1E6BqqqTmOa2KCmZY1QckzOts4stdO00jxaMD_Si75ZTce1.hF7WLJrPGHCnEP4jIVWvR0Ky9kan5RivMJJkPOOjYJbGGxkVkDrIjWWMt9VIVYyc57BgITrSPYAo6tAWjzBQQW54D.ZB0Mhy.Y.5NmTub42quLG4pDi.yRGlbnUnp8TKPHOllb5r1kvdNoYvDmCNjPaiN_Oogx04Gj99a8ZTS4qbVndvUMkazvOxK.8Nn1N8V9fDCFF0aPF1qlKXe6MFm2A_UwK2LFdCzKle0ISxyGRE.4Qva_qN5IdzLPMqD8AuPYr7DMAGdngPOIb8NafaZInp4FefJG23K0Xi51O_nqB0R1EIh_edH908VYZ3gah8AOW3ZiHHOBS.rvV4SrA--";
    private final String tokenSecret = "7a28439ac5d9a3bec5a27f09cc72a402e061bf62";

         @Test
     public void testUsingExistingAccessToken() throws OXException {
        final YahooOAuthActivator activator = new YahooOAuthActivator();
        activator.setOAuthMetaData(new YahooOAuthServiceMetaData(null));
        final MockOAuthService oAuthService = new MockOAuthService();
        oAuthService.setToken(token);
        oAuthService.setSecret(tokenSecret);
        activator.setOauthService(oAuthService);

        final YahooService service = new YahooServiceImpl(activator);
        final List<Contact> contacts = service.getContacts(null, 1, 1, 1);
        assertTrue("there should be contacts in here", contacts.size() > 0);
        for (final Contact contact : contacts) {
            System.out.println(contact.getGivenName() + " " + contact.getSurName());
        }
    }

}
