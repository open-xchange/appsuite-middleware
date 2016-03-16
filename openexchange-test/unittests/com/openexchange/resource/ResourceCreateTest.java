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

package com.openexchange.resource;

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.TestCase;

import com.openexchange.databaseold.Database;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.test.AjaxInit;

/**
 * {@link ResourceCreateTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceCreateTest extends TestCase {

	private Context ctx;

	private User user;

	private User admin;

	/**
	 * Initializes a new {@link ResourceCreateTest}
	 */
	public ResourceCreateTest() {
		super();
	}

	/**
	 * Initializes a new {@link ResourceCreateTest}
	 *
	 * @param name
	 *            The test's name
	 */
	public ResourceCreateTest(final String name) {
		super(name);
	}

	private static Context resolveContext(final String ctxStr) throws Exception {
	    int pos = -1;
	    final String c = (pos = ctxStr.indexOf('@')) > -1 ? ctxStr.substring(pos + 1) : ctxStr;
	    return ContextStorage.getStorageContext(ContextStorage.getInstance().getContextId(c));
	}

	private static User resolveUser(final String user, final Context ctx) throws Exception {
		int pos = -1;
		final String u = (pos = user.indexOf('@')) > -1 ? user.substring(0, pos) : user;
		return UserStorage.getInstance().getUser(UserStorage.getInstance().getUserId(u, ctx), ctx);
	}

	@Override
	protected void setUp() throws Exception {
		/*
		 * Init
		 */
		Init.startServer();
		/*
		 * Init test environment
		 */
		final String login = AjaxInit.getAJAXProperty("login");
		ctx = resolveContext(login);
		user = resolveUser(login, ctx);
		admin = UserStorage.getInstance().getUser(ctx.getMailadmin(), ctx);
	}

	@Override
	protected void tearDown() throws Exception {
		Init.stopServer();
	}

	public void testResourceCreation() throws SQLException, OXException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresource@somewhere.com");
		resource.setSimpleName("M-T-R");
		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			assertTrue("Invalid ID detected: " + id + ". ID has not been properly set through creation", id != -1);
			assertTrue("Invalid last-modified detected: " + resource.getLastModified()
					+ ". Last-modified timestamp has not been properly set through creation", resource
					.getLastModified() != null
					&& resource.getLastModified().getTime() < System.currentTimeMillis());

		} finally {
			deleteResource(id, ctx.getContextId());
		}

	}

	public void testResourceCreationFail001() throws SQLException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresource@somewhere.com");
		resource.setSimpleName("M-T-R\u00f6\u00e4\u00fc");
		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			fail("Creation succeeded with invalid string identifier");
		} catch (final OXException e) {
		    // Exception is expected
		} finally {
			deleteResource(id, ctx.getContextId());
		}

	}

	public void testResourceCreationFail002() throws SQLException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresourcesomewhere.com");
		resource.setSimpleName("M-T-R");
		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			fail("Creation succeeded with invalid email address");
		} catch (final OXException e) {
		 //   Exception is expected
		} finally {
			deleteResource(id, ctx.getContextId());
		}

	}

	public void testResourceCreationFail003() throws SQLException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresource@somewhere.com");
		resource.setSimpleName("M-T-R");

		final Resource duplicate = new Resource();
		duplicate.setAvailable(true);
		duplicate.setDescription("My test resource");
		duplicate.setDisplayName("MyTestResource");
		duplicate.setMail("mytestresource2@somewhere.com");
		duplicate.setSimpleName("M-T-R");

		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			assertTrue("Invalid identifier detected: " + id, id != -1);

			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, duplicate);

			fail("Creation succeeded with duplicate identifier");

		} catch (final OXException e) {
		    // Exception is expected
		} finally {
			deleteResource(id, ctx.getContextId());
		}

	}

	public void testResourceCreationFail004() throws SQLException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresource@somewhere.com");
		resource.setSimpleName("M-T-R");

		final Resource duplicate = new Resource();
		duplicate.setAvailable(true);
		duplicate.setDescription("My test resource");
		duplicate.setDisplayName("MyTestResource");
		duplicate.setMail("mytestresource@somewhere.com");
		duplicate.setSimpleName("M-T-R2");

		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			assertTrue("Invalid identifier detected: " + id, id != -1);

			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, duplicate);

			fail("Creation succeeded with duplicate email address");

		} catch (final OXException e) {
		    // Exception is expected
		} finally {
			deleteResource(id, ctx.getContextId());
		}

	}

	public void testResourceFail007() throws SQLException {
		final Resource resource = new Resource();
		resource.setAvailable(true);
		resource.setDescription("My test resource");
		resource.setDisplayName("MyTestResource");
		resource.setMail("mytestresource@somewhere.com");
		resource.setSimpleName(null);
		int id = -1;
		try {
			ServerServiceRegistry.getInstance().getService(ResourceService.class).create(admin, ctx, resource);
			id = resource.getIdentifier();

			fail("Creation succeeded with missing mandatory field");
		} catch (final OXException e) {
		    // Exception is expected
		} finally {
			deleteResource(id, ctx.getContextId());
		}
	}

	private static final String SQL_DELETE = "DELETE FROM resource WHERE cid = ? AND id = ?";

	private static final void deleteResource(final int id, final int cid) throws SQLException {
		if (-1 == id) {
			return;
		}
		final Connection writeCon;
		try {
			writeCon = Database.get(cid, true);
		} catch (final OXException e) {
			e.printStackTrace();
			return;
		}
		PreparedStatement stmt = null;
		try {
			stmt = writeCon.prepareStatement(SQL_DELETE);
			stmt.setInt(1, cid);
			stmt.setInt(2, id);
			stmt.executeUpdate();

		} finally {
			if (null != stmt) {
				try {
					stmt.close();
				} catch (final SQLException e) {
				}
				stmt = null;
			}
			Database.back(cid, true, writeCon);
		}

	}
}
