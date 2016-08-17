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
import static org.junit.Assert.assertTrue;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
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
        final PropFindMethod propFind = new PropFindMethod(
        		super.getWebDAVClient().getBaseURI() + "/principals/users/" + getClient().getValues().getUserId() + "/",
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        final MultiStatusResponse response = assertSingleResponse(super.getWebDAVClient().doPropFind(propFind));
        final GetRequest getRequest = new GetRequest(super.getAJAXClient().getValues().getUserId(),
        		super.getAJAXClient().getValues().getTimeZone());
        final GetResponse getResponse = Executor.execute(client, getRequest);
        final Contact contact = getResponse.getContact();
        final String expectedDisplayName = contact.getDisplayName();
        assertEquals(PropertyNames.DISPLAYNAME + " wrong", expectedDisplayName,
        		super.extractTextContent(PropertyNames.DISPLAYNAME, response));
        final String principalURL = super.extractHref(PropertyNames.PRINCIPAL_URL, response);
    	assertTrue("username not found in href child of " + PropertyNames.PRINCIPAL_URL, principalURL.contains("/" + getClient().getValues().getUserId()));
        final String addressbookHome = super.extractHref(PropertyNames.ADDRESSBOOK_HOME_SET, response);
    	assertEquals(PropertyNames.ADDRESSBOOK_HOME_SET + " wrong", "/carddav/", addressbookHome);

	}
}
