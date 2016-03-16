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
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;

/**
 * {@link OptionsTest}
 *
 * Tests the "OPTIONS" method for the discovery of support for CardDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OptionsTest extends CardDAVTest {

	private static final String[] EXPECTED_ALLOW_HEADERS = {
		"PROPFIND", "OPTIONS", "REPORT", "PUT", "DELETE", "ACL"
	};

	private static final String[] EXPECTED_DAV_HEADERS = {
		"1", "2", "3", "addressbook", "access-control", "extended-mkcol"
	};

	public OptionsTest() {
		super();
	}

	/**
	 * Tests if the necessary 'Allow' header elements are present in the OPTIONS response.
	 * @throws Exception
	 */
	@Test
	public void testAllowHeaders() throws Exception {
		OptionsMethod options = null;
		try {
			options = new OptionsMethod(getBaseUri());
	    	assertEquals("unexpected http status", StatusCodes.SC_OK, super.getWebDAVClient().executeMethod(options));
	    	assertResponseHeaders(EXPECTED_ALLOW_HEADERS, "Allow", options);
		} finally {
			release(options);
		}
	}

	/**
	 * Tests if the necessary 'DAV' header elements are present in the OPTIONS response.
	 * @throws Exception
	 */
	@Test
	public void testDAVHeaders() throws Exception {
		OptionsMethod options = null;
		try {
			options = new OptionsMethod(getBaseUri());
	    	assertEquals("unexpected http status", StatusCodes.SC_OK, super.getWebDAVClient().executeMethod(options));
	    	assertResponseHeaders(EXPECTED_DAV_HEADERS, "DAV", options);
		} finally {
			release(options);
		}
	}
}
