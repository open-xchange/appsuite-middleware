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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import org.w3c.dom.Node;
import com.openexchange.dav.Config;
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
        String aggregatedCollectionName = getDefaultCollectionName();
        for (String userAgent : UserAgents.MACOS_ALL) {
            super.getWebDAVClient().setUserAgent(userAgent);
            discoverRoot();
            discoverAggregatedCollection(aggregatedCollectionName, true);
            discoverContactsCollection(false);
            discoverGABCollection(false);
        }
    }

    @Test
    public void testIOSClients() throws Exception {
        String aggregatedCollectionName = getDefaultCollectionName();
        for (String userAgent : UserAgents.IOS_ALL) {
            super.getWebDAVClient().setUserAgent(userAgent);
            discoverRoot();
            discoverAggregatedCollection(aggregatedCollectionName, false);
            discoverContactsCollection(true);
            discoverGABCollection(true);
        }
    }

    @Test
    public void testOtherClients() throws Exception {
        String aggregatedCollectionName = getDefaultCollectionName();
        for (String userAgent : UserAgents.OTHER_ALL) {
            super.getWebDAVClient().setUserAgent(userAgent);
            discoverRoot();
            discoverAggregatedCollection(aggregatedCollectionName, false);
            discoverContactsCollection(true);
            discoverGABCollection(true);
        }
    }

    private void discoverRoot() throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse response = assertSingleResponse(super.getWebDAVClient().doPropFind(propFind));
        Node node = super.extractNodeValue(PropertyNames.RESOURCETYPE, response);
        assertMatches(PropertyNames.COLLECTION, node);
    }

    private void discoverAggregatedCollection(String aggregatedCollectionName, boolean shouldExists) throws Exception {
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
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/carddav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse aggregatedCollectionResponse = null;
        for (MultiStatusResponse response : super.getWebDAVClient().doPropFind(propFind)) {
            if (response.getHref().equals(buildCollectionHref(aggregatedCollectionName))) {
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
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/carddav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
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
        PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/carddav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
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
