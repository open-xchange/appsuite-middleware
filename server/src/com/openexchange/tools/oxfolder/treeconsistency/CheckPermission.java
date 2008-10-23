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

package com.openexchange.tools.oxfolder.treeconsistency;

import java.sql.Connection;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * {@link CheckPermission}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
abstract class CheckPermission {

	protected final Session session;

	protected final int sessionUser;

	protected final Context ctx;

	protected final Connection writeCon;

	/**
	 * Initializes a new {@link CheckPermission}
	 * 
	 * @param session
	 *            The session
	 * @param writeCon
	 *            A connection with write capability
	 * @param ctx
	 *            The context
	 */
	protected CheckPermission(final Session session, final Connection writeCon, final Context ctx) {
		super();
		this.ctx = ctx;
		this.writeCon = writeCon;
		this.session = session;
		this.sessionUser = session.getUserId();
	}

	protected FolderObject getFolderFromMaster(final int folderId) throws OXException {
		return getFolderFromMaster(folderId, false);
	}

	protected FolderObject getFolderFromMaster(final int folderId, final boolean withSubfolders) throws OXException {
		try {
			/*
			 * Use writable connection to ensure to fetch from master database
			 */
			Connection wc = writeCon;
			if (wc == null) {
				try {
					wc = DBPool.pickupWriteable(ctx);
					return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
				} finally {
					if (wc != null) {
						DBPool.closeWriterSilent(ctx, wc);
					}
				}
			}
			return FolderObject.loadFolderObjectFromDB(folderId, ctx, wc, true, withSubfolders);
		} catch (final OXFolderNotFoundException e) {
			throw e;
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}
}
