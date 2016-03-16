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
import java.util.List;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import org.w3c.dom.Node;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;

/**
 * {@link CollectionsTest}
 *
 * Tests discovery of addressbook collections below the root collection.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CollectionsTest extends CardDAVTest {

	public CollectionsTest() {
		super();
	}

	@Test
	public void testMacOSClients() throws Exception {
		for (String userAgent : UserAgents.MACOS_ALL) {
			super.getWebDAVClient().setUserAgent(userAgent);
			discoverRoot();
			discoverAggregatedCollection(true);
			discoverContactsCollection(false);
			discoverGABCollection(false);
		}
	}

	@Test
	public void testIOSClients() throws Exception {
		for (String userAgent : UserAgents.IOS_ALL) {
			super.getWebDAVClient().setUserAgent(userAgent);
			discoverRoot();
			discoverAggregatedCollection(false);
			discoverContactsCollection(true);
			discoverGABCollection(true);
		}
	}

	@Test
	public void testOtherClients() throws Exception {
		for (String userAgent : UserAgents.OTHER_ALL) {
			super.getWebDAVClient().setUserAgent(userAgent);
			discoverRoot();
			discoverAggregatedCollection(false);
			discoverContactsCollection(true);
			discoverGABCollection(true);
		}
	}

	private void discoverRoot() throws Exception {
		DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/",
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse response = assertSingleResponse(super.getWebDAVClient().doPropFind(propFind));
    	Node node = super.extractNodeValue(PropertyNames.RESOURCETYPE, response);
    	assertMatches(PropertyNames.COLLECTION, node);
	}

	private void discoverAggregatedCollection(boolean shouldExists) throws Exception {
		DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.ADD_MEMBER);
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.MAX_IMAGE_SIZE);
        props.add(PropertyNames.MAX_RESOURCE_SIZE);
        props.add(PropertyNames.ME_CARD);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.PUSH_TRANSPORTS);
        props.add(PropertyNames.PUSHKEY);
        props.add(PropertyNames.QUOTA_AVAILABLE_BYTES);
        props.add(PropertyNames.QUOTA_USED_BYTES);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        props.add(PropertyNames.SYNC_TOKEN);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/carddav/",
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse aggregatedCollectionResponse = null;
        for (MultiStatusResponse response : super.getWebDAVClient().doPropFind(propFind)) {
        	if (response.getHref().equals("/carddav/Contacts/")) {
        		aggregatedCollectionResponse = response;
        		break;
        	}
        }
        if (shouldExists) {
	        assertNotNull("Aggregated collection not found at /carddav/Contacts", aggregatedCollectionResponse);
	        List<Node> nodeList = super.extractNodeListValue(PropertyNames.RESOURCETYPE, aggregatedCollectionResponse);
	        assertContains(PropertyNames.COLLECTION, nodeList);
	        assertContains(PropertyNames.ADDRESSBOOK, nodeList);
        } else {
	        assertNull("Aggregated collection found at /carddav/Contacts", aggregatedCollectionResponse);
        }
	}

	private void discoverContactsCollection(boolean shouldExist) throws Exception {
		String folderName = super.getDefaultFolder().getFolderName();
		DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.ADD_MEMBER);
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.MAX_IMAGE_SIZE);
        props.add(PropertyNames.MAX_RESOURCE_SIZE);
        props.add(PropertyNames.ME_CARD);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.PUSH_TRANSPORTS);
        props.add(PropertyNames.PUSHKEY);
        props.add(PropertyNames.QUOTA_AVAILABLE_BYTES);
        props.add(PropertyNames.QUOTA_USED_BYTES);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        props.add(PropertyNames.SYNC_TOKEN);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/carddav/",
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        boolean found = false;
        for (MultiStatusResponse response : super.getWebDAVClient().doPropFind(propFind)) {
        	String displayName = super.extractTextContent(DavPropertyName.DISPLAYNAME, response);
        	if (null != displayName && 0 < displayName.length() && "\u200A".equals(displayName.substring(0, 1))) {
        	    displayName = displayName.substring(1);
        	}
        	if (folderName.equals(displayName)) {
        		found = true;
        		break;
        	}
        }
        if (shouldExist) {
        	assertTrue("Default contact folder collection not found below /carddav/", found);
        } else {
        	assertFalse("Default contact folder collection found below /carddav/", found);
        }
	}

	private void discoverGABCollection(boolean shouldExist) throws Exception {
		String folderName = super.getGABFolder().getFolderName();
		DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.ADD_MEMBER);
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.MAX_IMAGE_SIZE);
        props.add(PropertyNames.MAX_RESOURCE_SIZE);
        props.add(PropertyNames.ME_CARD);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.PUSH_TRANSPORTS);
        props.add(PropertyNames.PUSHKEY);
        props.add(PropertyNames.QUOTA_AVAILABLE_BYTES);
        props.add(PropertyNames.QUOTA_USED_BYTES);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        props.add(PropertyNames.SYNC_TOKEN);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + "/carddav/",
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        boolean found = false;
        for (MultiStatusResponse response : super.getWebDAVClient().doPropFind(propFind)) {
        	String displayName = super.extractTextContent(DavPropertyName.DISPLAYNAME, response);
        	if (folderName.equals(displayName)) {
        		found = true;
        		break;
        	}
        }
        if (shouldExist) {
        	assertTrue("GAB folder collection not found below /carddav/", found);
        } else {
        	assertFalse("GAB folder collection found below /carddav/", found);
        }
	}

}
