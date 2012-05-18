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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav.tests;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.w3c.dom.Node;

import com.openexchange.carddav.CardDAVTest;
import com.openexchange.carddav.Config;
import com.openexchange.carddav.PropertyNames;
import com.openexchange.carddav.StatusCodes;

/**
 * {@link CurrentUserPrincipalTest}
 * 
 * Tests discovery of the current user principal, simulating the steps happening during account creation of the addressbook client. 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CurrentUserPrincipalTest extends CardDAVTest {

	public CurrentUserPrincipalTest(String name) {
		super(name);
	}
	
	/**
	 * Checks if the CardDAV server reports the current user principal, it's url and resource type. 
	 * @throws Exception
	 */
	public void testDiscoverCurrentUserPrincipal() throws Exception {
		final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        final PropFindMethod propFind = new PropFindMethod(super.getCardDAVClient().getBaseURI() + "/", 
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        final MultiStatusResponse response = assertSingleResponse(super.getCardDAVClient().doPropFind(propFind));
        final String principal = super.extractHref(PropertyNames.CURRENT_USER_PRINCIPAL, response);
    	assertTrue("username not found in href child of " + PropertyNames.CURRENT_USER_PRINCIPAL, principal.contains(Config.getUsername()));
        final String principalURL = super.extractHref(PropertyNames.PRINCIPAL_URL, response);
    	assertTrue("username not found in href child of " + PropertyNames.PRINCIPAL_URL, principalURL.contains(Config.getUsername()));
    	final Node node = super.extractNodeValue(PropertyNames.RESOURCETYPE, response);
    	assertMatches(PropertyNames.COLLECTION, node);
	}
	
	/**
	 * Checks if the CardDAV server responds with status 404 when requesting the current user principal at an unknown location.
	 * @throws Exception
	 */
	public void testDiscoverCurrentUserPrincipalAtWellKnown() throws Exception {
		final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);	    	
        props.add(PropertyNames.PRINCIPAL_URL);
        props.add(PropertyNames.RESOURCETYPE);
        final PropFindMethod propFind = new PropFindMethod(super.getCardDAVClient().getBaseURI() + "/.well-known/carddav", 
        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        super.getCardDAVClient().doPropFind(propFind, StatusCodes.SC_NOT_FOUND);
	}
}
