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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.groupware.container.Contact;

/**
 * {@link PrincipalPropertiesTest}
 *
 * Tests discovery of additional properties for WebDAV principal resources,
 * simulating the steps happening during account creation of the addressbook client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrincipalPropertiesTest extends CardDAVTest {

    public PrincipalPropertiesTest() {
        super();
    }

    /**
     * Checks if the CardDAV server reports some information about the current user principal.
     * 
     * @throws Exception
     */
    @Test
    public void testDiscoverPrincipalProperties() throws Exception {
        final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.ADDRESSBOOK_HOME_SET);
        props.add(PropertyNames.DIRECTORY_GATEWAY);
        props.add(PropertyNames.DISPLAYNAME);
        props.add(PropertyNames.EMAIL_ADDRESS_SET);
        props.add(PropertyNames.PRINCIPAL_COLLECTION_SET);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCE_ID);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        final PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/principals/users/" + getClient().getValues().getUserId() + "/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        final MultiStatusResponse response = assertSingleResponse(super.getWebDAVClient().doPropFind(propFind));
        final GetRequest getRequest = new GetRequest(super.getAJAXClient().getValues().getUserId(), super.getAJAXClient().getValues().getTimeZone());
        final GetResponse getResponse = Executor.execute(getClient(), getRequest);
        final Contact contact = getResponse.getContact();
        final String expectedDisplayName = contact.getDisplayName();
        assertEquals(PropertyNames.DISPLAYNAME + " wrong", expectedDisplayName, super.extractTextContent(PropertyNames.DISPLAYNAME, response));
        final String principalURL = super.extractHref(PropertyNames.PRINCIPAL_URL, response);
        assertTrue("username not found in href child of " + PropertyNames.PRINCIPAL_URL, principalURL.contains("/" + getClient().getValues().getUserId()));
        final String addressbookHome = super.extractHref(PropertyNames.ADDRESSBOOK_HOME_SET, response);
        assertEquals(PropertyNames.ADDRESSBOOK_HOME_SET + " wrong", Config.getPathPrefix() + "/carddav/", addressbookHome);

    }
}
