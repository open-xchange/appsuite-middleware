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

package com.openexchange.ajax.resource;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceAllRequest;
import com.openexchange.ajax.resource.actions.ResourceAllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceAllAJAXTest} - Tests the ALL request on resource servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceAllAJAXTest extends AbstractResourceTest {

	/**
	 * Initializes a new {@link ResourceAllAJAXTest}
	 *
	 * @param name
	 *            The test name
	 */
	public ResourceAllAJAXTest(final String name) {
		super(name);
	}

	/**
	 * Tests the <code>action=all</code>
	 */
	public void testAll() throws OXException, JSONException, IOException, SAXException {
		int id = -1;
		try {
			/*
			 * Create a resource
			 */
			final Resource resource = new Resource();
			resource.setAvailable(true);
			resource.setMail("my.resource@dom"+System.currentTimeMillis()+"ain.tdl");
			resource.setSimpleName(ResourceAllAJAXTest.class.getName());
			resource.setDisplayName(ResourceAllAJAXTest.class.getName());
			resource.setDescription(ResourceAllAJAXTest.class.getName());
			id = createResource(resource);

			/*
			 * Perform all request
			 */
			final ResourceAllResponse allResponse = Executor.execute(getSession(),
					new ResourceAllRequest(true));
			final int[] ids = allResponse.getIDs();
			assertTrue("All request failed", ids != null && ids.length >= 1);

		} finally {
			try {
				deleteResource(id);
			} catch (final Exception e) {
				System.err.println("Temporary created resource with ID " + id + " could not be deleted: "
						+ e.getMessage());
			}
		}
	}
}
