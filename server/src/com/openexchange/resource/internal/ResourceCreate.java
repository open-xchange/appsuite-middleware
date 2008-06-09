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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.resource.internal;

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceException;
import com.openexchange.resource.ResourceStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ResourceCreate} - Performs insertion of a {@link Resource resource}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ResourceCreate {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ResourceCreate.class);

	private final User user;

	private final Context ctx;

	private final Resource resource;

	private final ResourceStorage storage;

	/**
	 * Initializes a new {@link ResourceCreate}
	 * 
	 * @param ctx
	 *            The context
	 * @param resource
	 *            The resource to insert
	 * @throws ResourceException
	 *             If initialization fails
	 */
	ResourceCreate(final User user, final Context ctx, final Resource resource) throws ResourceException {
		super();
		this.user = user;
		this.ctx = ctx;
		this.resource = resource;
		storage = ResourceStorage.getInstance();
	}

	/**
	 * This method glues all operations together. That includes all checks and
	 * the operations to be done.
	 * 
	 * @throws ResourceException
	 */
	void perform() throws ResourceException {
		check();
		insert();
		propagate();
	}

	/**
	 * This method performs all necessary checks before creating a resource.
	 * 
	 * @throws ResourceException
	 *             if a problem was detected during checks.
	 */
	private void check() throws ResourceException {
		if (null == resource) {
			throw new ResourceException(ResourceException.Code.NULL);
		}
		/*
		 * Check mandatory fields: identifier, displayName, and lastModified
		 */
		if (isEmpty(resource.getSimpleName()) || isEmpty(resource.getDisplayName())
				|| null == resource.getLastModified()) {
			throw new ResourceException(ResourceException.Code.MANDATORY_FIELD);
		}
		/*
		 * Check permission: By now caller must be context's admin
		 */
		if (ctx.getMailadmin() != user.getId()) {
			throw new ResourceException(ResourceException.Code.PERMISSION, Integer.valueOf(user.getId()), Integer
					.valueOf(ctx.getContextId()));
		}

	}

	/**
	 * Inserts all data for the resource into the database.
	 * 
	 * @throws ResourceException
	 */
	private void insert() throws ResourceException {
		final Connection con;
		try {
			con = DBPool.pickupWriteable(ctx);
		} catch (final DBPoolingException e) {
			throw new ResourceException(ResourceException.Code.NO_CONNECTION, e);
		}
		try {
			con.setAutoCommit(false);
			insert(con);
			con.commit();
		} catch (final SQLException e) {
			DBUtils.rollback(con);
			throw new ResourceException(ResourceException.Code.SQL_ERROR, e);
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (final SQLException e) {
				LOG.error("Problem setting autocommit to true.", e);
			}
			DBPool.closeWriterSilent(ctx, con);
		}
	}

	/**
	 * Propagates insertion to system: Possible cache invalidation, etc.
	 * 
	 * @throws ResourceException
	 *             If propagating the insertion fails
	 */
	private void propagate() throws ResourceException {
		// TODO: Check if any caches should be invalidated
	}

	/**
	 * This method calls the plain insert methods.
	 * 
	 * @param con
	 *            writable database connection in transaction or not.
	 * @throws ResourceException
	 *             if some problem occurs.
	 */
	public void insert(final Connection con) throws ResourceException {
		try {
			final int id = IDGenerator.getId(ctx.getContextId(), Types.RESOURCE, con);
			resource.setIdentifier(id);
			storage.insertResource(ctx, con, resource);
		} catch (final SQLException e) {
			throw new ResourceException(ResourceException.Code.SQL_ERROR, e);
		}
	}

	private static boolean isEmpty(final String s) {
		if (null == s || s.length() == 0) {
			return true;
		}
		final char[] chars = s.toCharArray();
		for (final char c : chars) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
}
