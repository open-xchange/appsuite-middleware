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

package com.openexchange.oauth.yahoo;

import java.util.List;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.yahoo.internal.OAuthServiceMetaDataYahooImpl;
import com.openexchange.oauth.yahoo.internal.YahooServiceImpl;
import com.openexchange.oauth.yahoo.osgi.YahooOAuthActivator;


/**
 * {@link YahooConnectionTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class YahooConnectionTest extends TestCase {

    // Obviously these 5 values need to fit together. token and tokenSecret can be obtained vio YahooSimpleConnectionTest
    private final String apiKey = "dj0yJmk9eDY3MW9VNXhqYTRWJmQ9WVdrOVJYWTFiRGhKTXpBbWNHbzlNelF6TURnMU5qWXkmcz1jb25zdW1lcnNlY3JldCZ4PTkx";
    private final String apiSecret = "b94fbe3f52d364b4ae5a28228ac7b558fcfbe58c";
    private final String token = "A=tuky.uX3oSgvhynUoHD4Bqi1Jbi4_BXYlaPVRG1MyUNdYlHKdX6eLcGA7sd5u_vELlWhgMf1VKJONOF2SUG85T5hNOEue2kZiF3YEZY7YisF4cIamR_zZWwukmmLhDP414UVvR_jhuAXTuDiTMv9vYBPoFhhSd78MRkPUOOTGvkV6snd3rOYfob8FkFa7GDYaSzgBeE8875ekRzTFP6MVDUUdMI0poLRyCgzLRWrploJsHy8lcPgo7fLG2fq1hT03ZRk6OOt.SFSskiyxcCg6kb2cRSkd5_xeivQItjjIzCY3yJy6L.9vFRAAoS4sohnAgUhj0qKnuEvoOYjNVycAJS5fSyKdcbD5N3U3WDgMvPf0uoJNiswFFjwSoyvFAN1E6BqqqTmOa2KCmZY1QckzOts4stdO00jxaMD_Si75ZTce1.hF7WLJrPGHCnEP4jIVWvR0Ky9kan5RivMJJkPOOjYJbGGxkVkDrIjWWMt9VIVYyc57BgITrSPYAo6tAWjzBQQW54D.ZB0Mhy.Y.5NmTub42quLG4pDi.yRGlbnUnp8TKPHOllb5r1kvdNoYvDmCNjPaiN_Oogx04Gj99a8ZTS4qbVndvUMkazvOxK.8Nn1N8V9fDCFF0aPF1qlKXe6MFm2A_UwK2LFdCzKle0ISxyGRE.4Qva_qN5IdzLPMqD8AuPYr7DMAGdngPOIb8NafaZInp4FefJG23K0Xi51O_nqB0R1EIh_edH908VYZ3gah8AOW3ZiHHOBS.rvV4SrA--";
    private final String tokenSecret = "7a28439ac5d9a3bec5a27f09cc72a402e061bf62";
    private final String callbackURL = "http://www.open-xchange.com";

    public void testUsingExistingAccessToken() throws OXException{

        final YahooOAuthActivator activator = new YahooOAuthActivator();
        activator.setOAuthMetaData(new OAuthServiceMetaDataYahooImpl(null));
        final MockOAuthService oAuthService = new MockOAuthService();
        oAuthService.setToken(token);
        oAuthService.setSecret(tokenSecret);
        activator.setOauthService(oAuthService);

        final YahooService service = new YahooServiceImpl(activator);
        final List<Contact> contacts = service.getContacts(null, 1, 1, 1);
        assertTrue("there should be contacts in here", contacts.size()>0);
        for (final Contact contact : contacts){
            System.out.println(contact.getGivenName() + " " + contact.getSurName());
        }
    }

}
