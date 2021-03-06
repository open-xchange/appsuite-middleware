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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.carddav.reports.AddressbookQueryReportInfo;
import com.openexchange.dav.carddav.reports.PropFilter;
import com.openexchange.groupware.container.Contact;

/**
 * {@link AddressbookQueryTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AddressbookQueryTest extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

    @Test
    public void testFilterByMAIL() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String email = randomUID() + "@example.org";
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setEmail1(email);
        contact.setGivenName("Otto");
        contact.setSurName("Test");
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("EMAIL", "contains", email.substring(3, 13)), uid);
    }

    @Test
    public void testFilterByFN() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String displayName = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setDisplayName(displayName);
        contact.setSurName(displayName);
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("FN", "contains", displayName.substring(2, 8)), uid);
    }

    @Test
    public void testFilterByN() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String givenName = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setDisplayName(givenName);
        contact.setGivenName(givenName);
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("N", "starts-with", givenName.substring(0, 3)), uid);
    }

    @Test
    public void testFilterByTEL() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String telephone1 = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setTelephoneBusiness1(telephone1);
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("TEL", null, telephone1.substring(4, 11)), uid);
    }

    @Test
    public void testFilterByUID() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setDisplayName(randomUID());
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("UID", "equals", uid), uid);
    }

    @Test
    public void testFilterByNICKNAME() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String nickName = randomUID();
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setDisplayName(nickName);
        contact.setNickname(nickName);
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        assertQueryMatch(folderID, new PropFilter("NICKNAME", "ends-with", nickName), uid);
    }

    @Test
    public void testAnyOfFilter() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String email = randomUID() + "@example.org";
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setEmail1(email);
        contact.setGivenName("Otto");
        contact.setSurName("Test");
        contact = create(contact, folderID);
        String email2 = randomUID() + "@example.org";
        String uid2 = randomUID();
        Contact contact2 = new Contact();
        contact2.setUid(uid2);
        contact2.setEmail1(email2);
        contact2.setGivenName("Horst");
        contact2.setSurName("Test");
        contact2 = create(contact2, folderID);
        /*
         * perform query & check results
         */
        List<PropFilter> filters = new ArrayList<PropFilter>();
        filters.add(new PropFilter("EMAIL", "contains", email.substring(3, 13)));
        filters.add(new PropFilter("EMAIL", "contains", email2.substring(3, 13)));
        assertQueryMatch(folderID, filters, "anyof", uid);
        assertQueryMatch(folderID, filters, "anyof", uid2);
    }

    @Test
    public void testAllOfFilter() throws Exception {
        /*
         * prepare contact to search for
         */
        int folderID = getDefaultFolderID();
        String email = randomUID() + "@example.org";
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setEmail1(email);
        contact.setGivenName("Otto");
        contact.setSurName("Test");
        contact = create(contact, folderID);
        /*
         * perform query & check results
         */
        List<PropFilter> filters = new ArrayList<PropFilter>();
        filters.add(new PropFilter("EMAIL", "contains", email.substring(3, 13)));
        filters.add(new PropFilter("UID", "starts-with", uid.substring(0, 5)));
        assertQueryMatch(folderID, filters, "allof", uid);
    }

    private VCardResource assertQueryMatch(int folderID, PropFilter filter, String expectedUID) throws Exception {
        return assertQueryMatch(folderID, Collections.singletonList(filter), null, expectedUID);
    }

    private VCardResource assertQueryMatch(int folderID, List<PropFilter> filters, String filterTest, String expectedUID) throws Exception {
        /*
         * construct query
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.ADDRESS_DATA);
        ReportInfo reportInfo = new AddressbookQueryReportInfo(filters, props, filterTest);
        MultiStatusResponse[] responses = getWebDAVClient().doReport(reportInfo, getBaseUri() + Config.getPathPrefix() + "/carddav/" + folderID + '/');
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
