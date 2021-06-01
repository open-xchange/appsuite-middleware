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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug40471Test}
 *
 * single contact will be sent as ISO-8859-1,but content type UTF-8
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40471Test extends CardDAVTest {

    public Bug40471Test() {
        super();
    }

    @Test
    public void testContentTypeInGet() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        String syncToken = super.fetchSyncToken();
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "S\u00f6ren";
        String lastName = "S\u00fc\u00df";
        String country = "\u4e2d\u534e\u4eba\u6c11\u5171\u548c\u56fd";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setCountryBusiness(country);
        contact.setUid(uid);
        rememberForCleanUp(create(contact));
        /*
         * verify contact on client (via addressbook multiget)
         */
        Map<String, String> eTags = syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertEquals("Country wrong", country, card.getVCard().getAdrs().get(0).getCountryName());
        /*
         * verify contact on client (via plain get)
         */
        String href = card.getHref();
        GetMethod get = new GetMethod(getWebDAVClient().getBaseURI() + href);
        String vCard = getWebDAVClient().doGet(get);
        assertNotNull(vCard);
        card = new VCardResource(vCard, href, null);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertEquals("Country wrong", country, card.getVCard().getAdrs().get(0).getCountryName());
    }
}
