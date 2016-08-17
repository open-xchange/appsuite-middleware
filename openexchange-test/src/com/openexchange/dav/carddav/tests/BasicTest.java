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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import org.w3c.dom.Node;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.dav.Headers;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link BasicTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class BasicTest extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

	@Test
	public void testPutAndGet() throws Exception {
		/*
		 * prepare vCard
		 */
        String collection = String.valueOf(getDefaultFolderID());
    	String uid = randomUID();
    	String firstName = "John";
    	String lastName = "Doe";
    	String email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + "@example.org";
        String vCard =
    		"BEGIN:VCARD" + "\r\n" +
            "PRODID:-//Example Inc.//Example Client 1.0//EN" + "\r\n" +
			"VERSION:3.0" + "\r\n" +
			"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
			"FN:" + firstName + " " + lastName + "\r\n" +
			"EMAIL;type=INTERNET;type=WORK;type=pref:" + email + "\r\n" +
			"UID:" + uid + "\r\n" +
			"REV:" + formatAsUTC(new Date()) + "\r\n" +
			"END:VCARD" + "\r\n"
		;
        /*
         * create vCard resource on server
         */
        String href = "/carddav/" + collection + "/" + uid + ".vcf";
        PutMethod put = null;
        try {
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            assertEquals("Response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(put));
        } finally {
            release(put);
        }
        /*
         * get created vCard from server
         */
        VCardResource vCardResource;
        GetMethod get = null;
        try {
            get = new GetMethod(getBaseUri() + href);
            String reloadedVCard = getWebDAVClient().doGet(get);
            assertNotNull(reloadedVCard);
            Header eTagHeader = get.getResponseHeader("ETag");
            String eTag = null != eTagHeader ? eTagHeader.getValue() : null;
            vCardResource = new VCardResource(reloadedVCard, href, eTag);
        } finally {
            release(get);
        }
        /*
         * verify created contact
         */
        assertNotNull("No ETag", vCardResource.getETag());
        assertEquals("N wrong", firstName, vCardResource.getGivenName());
        assertEquals("N wrong", lastName, vCardResource.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, vCardResource.getFN());
        /*
         * update contact on client
         */
        String updatedVCard =
            "BEGIN:VCARD" + "\r\n" +
            "PRODID:-//Example Inc.//Example Client 1.0//EN" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
            "FN:" + firstName + " " + lastName + "\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:" + email + "\r\n" +
            "UID:" + uid + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "TEL;type=CELL:352-3534" + "\r\n" +
            "CATEGORIES:Family,Private" + "\r\n" +
            "END:VCARD" + "\r\n"
        ;
        try {
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_MATCH, vCardResource.getETag());
            put.setRequestEntity(new StringRequestEntity(updatedVCard, "text/vcard", "UTF-8"));
            assertEquals("Response code wrong", StatusCodes.SC_CREATED, getWebDAVClient().executeMethod(put));
        } finally {
            release(put);
        }
        /*
         * get updated vCard from server
         */
        try {
            get = new GetMethod(getBaseUri() + href);
            String reloadedVCard = getWebDAVClient().doGet(get);
            assertNotNull(reloadedVCard);
            Header eTagHeader = get.getResponseHeader("ETag");
            String eTag = null != eTagHeader ? eTagHeader.getValue() : null;
            vCardResource = new VCardResource(reloadedVCard, href, eTag);
        } finally {
            release(get);
        }
        /*
         * verify updated contact
         */
        assertNotNull("No ETag", vCardResource.getETag());
        assertEquals("N wrong", firstName, vCardResource.getGivenName());
        assertEquals("N wrong", lastName, vCardResource.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, vCardResource.getFN());
        assertTrue("No TELs found", null != vCardResource.getVCard().getTels() && 1 == vCardResource.getVCard().getTels().size());
        assertEquals("TEL wrong", "352-3534", vCardResource.getVCard().getTels().get(0).getTelephone());
        assertTrue("CATEGORIES wrong", vCardResource.getVCard().getCategories().getCategories().contains("Family"));
        assertTrue("CATEGORIES wrong", vCardResource.getVCard().getCategories().getCategories().contains("Private"));
	}

    @Test
    public void testSyncCollection() throws Exception {
        /*
         * fetch initial sync token
         */
        String collection = String.valueOf(getDefaultFolderID());
        SyncToken syncToken = new SyncToken(fetchSyncToken(collection));
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "John";
        String lastName = "Doe";
        String email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + "@example.org";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setUid(uid);
        contact.setEmail1(email);
        rememberForCleanUp(create(contact));
        /*
         * sync client
         */
        SyncCollectionResponse syncCollectionResponse = syncCollection(syncToken, "/carddav/" + collection + "/");
        Map<String, String> eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(collection, eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        String href = card.getHref();
        /*
         * update contact on server
         */
        contact.setCellularTelephone1("352-3534");
        contact.setCategories("Family,Private");
        contact = update(contact);
        /*
         * sync client
         */
        syncCollectionResponse = syncCollection(syncToken, "/carddav/" + collection + "/");
        eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = addressbookMultiget(collection, eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("No TELs found", null != card.getVCard().getTels() && 1 == card.getVCard().getTels().size());
        assertEquals("TEL wrong", "352-3534", card.getVCard().getTels().get(0).getTelephone());
        assertTrue("CATEGORIES wrong", card.getVCard().getCategories().containsCategory("Family"));
        assertTrue("CATEGORIES wrong", card.getVCard().getCategories().containsCategory("Private"));
        /*
         * delete contact on server
         */
        delete(contact);
        /*
         * sync client
         */
        syncCollectionResponse = syncCollection(syncToken, "/carddav/" + collection + "/");
        List<String> hrefsNotFound = syncCollectionResponse.getHrefsStatusNotFound();
        assertTrue("no resource deletions reported on sync collection", null != hrefsNotFound && 1 == hrefsNotFound.size());
        assertEquals("href not found", href, hrefsNotFound.get(0));
    }


    @Test
    public void testDiscoverAddressbooks() throws Exception {
        /*
         * retrieve expected contact collections from server
         */
        List<String> expectedCollections = new ArrayList<String>();
        VisibleFoldersRequest foldersRequest = new VisibleFoldersRequest(EnumAPI.OX_NEW, "contacts", new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME });
        VisibleFoldersResponse foldersResponse = client.execute(foldersRequest);
        Iterator<FolderObject> folders = foldersResponse.getPrivateFolders();
        while (folders.hasNext()) {
            expectedCollections.add("/carddav/" + folders.next().getObjectID() + '/');
        }
        folders = foldersResponse.getSharedFolders();
        while (folders.hasNext()) {
            expectedCollections.add("/carddav/" + folders.next().getObjectID() + '/');
        }
        folders = foldersResponse.getPublicFolders();
        while (folders.hasNext()) {
            expectedCollections.add("/carddav/" + folders.next().getObjectID() + '/');
        }
        /*
         * discover the current user principal
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(getBaseUri() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse propFindResponse = assertSingleResponse(getWebDAVClient().doPropFind(propFind));
        String principalURL = extractHref(PropertyNames.CURRENT_USER_PRINCIPAL, propFindResponse);
        assertTrue("username not found in href child of " + PropertyNames.CURRENT_USER_PRINCIPAL, principalURL.contains("/" +  getClient().getValues().getUserId()));
        /*
         * discover the principal's addressbook home set
         */
        props = new DavPropertyNameSet();
        props.add(PropertyNames.ADDRESSBOOK_HOME_SET);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.EMAIL_ADDRESS_SET);
        props.add(PropertyNames.PRINCIPAL_COLLECTION_SET);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        propFind = new PropFindMethod(getBaseUri() + principalURL, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        propFindResponse = assertSingleResponse(getWebDAVClient().doPropFind(propFind));
        String addressbookHomeSet = extractHref(PropertyNames.ADDRESSBOOK_HOME_SET, propFindResponse);
        /*
         * do a depth 1 PROPFIND at the adressbook-home-set URL to get available collections
         */
        props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.MAX_IMAGE_SIZE);
        props.add(PropertyNames.MAX_RESOURCE_SIZE);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        List<String> actualCollections = new ArrayList<String>();
        propFind = new PropFindMethod(getBaseUri() + addressbookHomeSet, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        for (MultiStatusResponse response : getWebDAVClient().doPropFind(propFind)) {
            DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.RESOURCETYPE);
            Object value = property.getValue();
            Collection<Node> resourceTypeNodes;
            if (Collection.class.isInstance(value)) {
                resourceTypeNodes = (List<Node>) value;
            } else {
                resourceTypeNodes = Collections.singleton((Node) value);
            }
            for (Node resourceTypeNode : resourceTypeNodes) {
                if ("urn:ietf:params:xml:ns:carddav".equals(resourceTypeNode.getNamespaceURI()) && "addressbook".equals(resourceTypeNode.getNodeName())) {
                    actualCollections.add(response.getHref());
                }
            }
        }
        /*
         * verify that each collection was listed
         */
        for (String collection : actualCollections) {
            assertTrue("Expected collection " + collection + " not found", actualCollections.contains(collection));
        }
        for (String collection : actualCollections) {
            assertTrue("Unexpected collection " + collection + " found", expectedCollections.contains(collection));
        }
    }

}
