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

import static org.junit.Assert.assertTrue;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import org.w3c.dom.Node;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;

/**
 * {@link CurrentUserPrincipalTest}
 *
 * Tests discovery of the current user principal, simulating the steps happening during account creation of the addressbook client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CurrentUserPrincipalTest extends CardDAVTest {

    /**
     * Checks if the CardDAV server reports the current user principal, it's url and resource type.
     * 
     * @throws Exception
     */
    @Test
    public void testDiscoverCurrentUserPrincipal() throws Exception {
        final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        final PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        final MultiStatusResponse response = assertSingleResponse(super.getWebDAVClient().doPropFind(propFind));
        final String principal = super.extractHref(PropertyNames.CURRENT_USER_PRINCIPAL, response);
        assertTrue("username not found in href child of " + PropertyNames.CURRENT_USER_PRINCIPAL, principal.contains("/" + getClient().getValues().getUserId()));
        final Node node = super.extractNodeValue(PropertyNames.RESOURCETYPE, response);
        assertMatches(PropertyNames.COLLECTION, node);
    }

    /**
     * Checks if the CardDAV server responds with status 404 when requesting the current user principal at an unknown location.
     * 
     * @throws Exception
     */
    @Test
    public void testDiscoverCurrentUserPrincipalAtUnknown() throws Exception {
        final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        final PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/gibt/es/nicht", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        super.getWebDAVClient().doPropFind(propFind, StatusCodes.SC_NOT_FOUND);
    }
}
