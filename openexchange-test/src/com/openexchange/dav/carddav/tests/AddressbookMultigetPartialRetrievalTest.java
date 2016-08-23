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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.*;
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

    protected List<VCardResource> addressbookMultiget(String collection, Collection<String> hrefs, final String...propertyNames) throws Exception {
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
