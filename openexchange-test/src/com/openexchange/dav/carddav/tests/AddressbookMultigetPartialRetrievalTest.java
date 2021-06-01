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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link AddressbookMultigetPartialRetrievalTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class AddressbookMultigetPartialRetrievalTest extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

    @Test
    public void testFilterRetrieveFNandTEL() throws Exception {
        /*
         * fetch initial sync token
         */
        int folderID = getDefaultFolderID();
        String collection = String.valueOf(folderID);
        String syncToken = fetchSyncToken(collection);
        /*
         * prepare contacts
         */
        String uid1 = randomUID();
        Contact contact1 = new Contact();
        contact1.setUid(uid1);
        contact1.setTelephoneBusiness1(randomUID());
        contact1.setDisplayName(randomUID());
        contact1.setNickname(randomUID());
        contact1.setEmail1(randomUID() + "@example.org");
        contact1 = create(contact1, folderID);
        String uid2 = randomUID();
        Contact contact2 = new Contact();
        contact2.setUid(uid2);
        contact2.setTelephoneBusiness1(randomUID());
        contact2.setDisplayName(randomUID());
        contact2.setNickname(randomUID());
        contact2.setEmail1(randomUID() + "@example.org");
        contact2 = create(contact2, folderID);
        String uid3 = randomUID();
        Contact contact3 = new Contact();
        contact3.setUid(uid3);
        contact3.setTelephoneBusiness1(randomUID());
        contact3.setDisplayName(randomUID());
        contact3.setNickname(randomUID());
        contact3.setEmail1(randomUID() + "@example.org");
        contact3 = create(contact3, folderID);
        /*
         * sync collection & get (partial) vCards via addressbook multiget
         */
        Map<String, String> eTags = syncCollection(collection, syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(collection, eTags.keySet(), "UID", "FN", "TEL");
        VCardResource vCard1 = assertContains(uid1, addressData);
        assertNotNull(vCard1.getVCard().getTels());
        assertNotNull(vCard1.getVCard().getFN());
        assertNull(vCard1.getVCard().getEmails());
        assertNull(vCard1.getVCard().getNicknames());

    }

    protected List<VCardResource> addressbookMultiget(String collection, Collection<String> hrefs, final String... propertyNames) throws Exception {
        DavPropertySet props = new DavPropertySet();
        props.add(new AbstractDavProperty<Object>(PropertyNames.GETETAG, false) {

            @Override
            public Object getValue() {
                return null;
            }
        });
        props.add(new AbstractDavProperty<Object>(PropertyNames.ADDRESS_DATA, false) {

            @Override
            public org.w3c.dom.Element toXml(Document document) {
                Element element = getName().toXml(document);
                for (String propertyName : propertyNames) {
                    Element propElement = DavPropertyName.create("prop", PropertyNames.NS_CARDDAV).toXml(document);
                    propElement.setAttribute("name", propertyName);
                    element.appendChild(propElement);
                }
                return element;
            }

            @Override
            public Object getValue() {
                return null;
            }
        });
        return addressbookMultiget(collection, hrefs, props);
    }

}
