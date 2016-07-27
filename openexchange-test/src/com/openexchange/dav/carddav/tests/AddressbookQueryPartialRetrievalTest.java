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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.carddav.reports.AddressbookQueryReportInfo;
import com.openexchange.dav.carddav.reports.PropFilter;
import com.openexchange.groupware.container.Contact;

/**
 * {@link AddressbookQueryPartialRetrievalTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class AddressbookQueryPartialRetrievalTest extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

    @Test
    public void testFilterByTELRetrieveFN() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String telephone1 = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setTelephoneBusiness1(telephone1);
        contact.setDisplayName(randomUID());
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        VCardResource vCardResource = assertQueryMatch(folderID, new PropFilter("TEL", null, telephone1.substring(4, 11)), uid, "FN", "UID");
        assertNull(vCardResource.getVCard().getTels());
    }

    @Test
    public void testFilterByEqualTELRetrieveFN() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String telephone1 = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setTelephoneBusiness1(telephone1);
        contact.setDisplayName(randomUID());
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        VCardResource vCardResource = assertQueryMatch(folderID, new PropFilter("TEL", "equals", telephone1), uid, "FN", "UID");
        assertNull(vCardResource.getVCard().getTels());
    }

    @Test
    public void testFilterByTELWithOriginalVCardRetrieveFN() throws Exception {
        int folderID = getDefaultFolderID();
        String collection = String.valueOf(folderID);
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken(collection));
        /*
         * create contact
         */
        String telephone1 = randomUID();
        String uid = randomUID();
        String firstName = "test";
        String lastName = "horst";
        String vCard =
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
            "FN:" + firstName + " " + lastName + "\r\n" +
            "ORG:test3;" + "\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:test@example.com" + "\r\n" +
            "TEL;type=WORK;type=pref:" + telephone1 + "\r\n" +
            "TEL;type=CELL:352-3534" + "\r\n" +
            "TEL;type=HOME:346346" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "X-OTTO-HABICHNICHT:Hallo" + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
            "END:VCARD" + "\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard, collection));
        /*
         * verify contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = syncCollection(collection, syncToken.getToken());
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(collection, eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * perform query & check results
         */
        VCardResource vCardResource = assertQueryMatch(folderID, new PropFilter("TEL", "equals", telephone1), uid, "FN", "UID");
        assertTrue(vCardResource.getExtendedTypes("X-OTTO-HABICHNICHT").isEmpty());
        assertNull(vCardResource.getVCard().getTels());
        assertNull(vCardResource.getVCard().getOrg());
    }

    private VCardResource assertQueryMatch(int folderID, PropFilter filter, String expectedUID, String...propertyNames) throws Exception {
        return assertQueryMatch(folderID, Collections.singletonList(filter), null, expectedUID, propertyNames);
    }

	private VCardResource assertQueryMatch(int folderID, List<PropFilter> filters, String filterTest, String expectedUID, final String...propertyNames) throws Exception {
        /*
         * construct query
         */
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
        ReportInfo reportInfo = new AddressbookQueryReportInfo(filters, props, filterTest);
        MultiStatusResponse[] responses = getWebDAVClient().doReport(reportInfo, getBaseUri() + "/carddav/" + folderID + '/');
        List<VCardResource> addressData = new ArrayList<VCardResource>();
        for (MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                String href = response.getHref();
                assertNotNull("got no href from response", href);
                String data = this.extractTextContent(PropertyNames.ADDRESS_DATA, response);
                assertNotNull("got no address data from response", data);
                String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
                assertNotNull("got no etag data from response", eTag);
                addressData.add(new VCardResource(data, href, eTag));
            }
        }
        /*
         * check results
         */
        VCardResource matchingResource = null;
        for (VCardResource vCardResource : addressData) {
            if (expectedUID.equals(vCardResource.getUID())) {
                matchingResource = vCardResource;
                break;
            }
        }
        assertNotNull("no matching vcard resource found", matchingResource);
        return matchingResource;
	}

}
