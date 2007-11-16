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

package com.openexchange.api2.sync;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.sql.SQLException;
import java.util.Date;

import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * RdbFolderSyncInterface
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class RdbFolderSyncInterface implements FolderSyncInterface {

	private static final String STR_EMPTY = "";

	/*
	 * Members
	 */
	private final int userId;

	private final int[] groups;

	private final Context ctx;

	private final Session session;

	private final User user;

	private final OXFolderAccess oxfolderAccess;

	private final UserConfiguration userConfiguration;

	public RdbFolderSyncInterface(final Session sessionObj) {
		this(sessionObj, null);
	}

	public RdbFolderSyncInterface(final Session session, final OXFolderAccess oxfolderAccess) {
		super();
		this.session = session;
		user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
		this.userId = user.getId();
		this.groups = user.getGroups();
		this.ctx = session.getContext();
		this.oxfolderAccess = oxfolderAccess == null ? new OXFolderAccess(ctx) : oxfolderAccess;
		userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
				session.getContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.sync.FolderSyncInterface#deleteFolderContent(int)
	 */
	public int clearFolder(final FolderObject folderobject, final Date clientLastModified) throws OXException {
		try {
			if (folderobject.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess()) {
				throw new OXFolderException(FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS, getUserName(session, user),
						getFolderName(folderobject), Integer.valueOf(ctx.getContextId()));
			}
			if (!folderobject.exists(ctx)) {
				throw new OXFolderNotFoundException(folderobject.getObjectID(), ctx.getContextId());
			}
			if (clientLastModified != null
					&& oxfolderAccess.getFolderLastModified(folderobject.getObjectID()).after(clientLastModified)) {
				throw new OXConcurrentModificationException(Component.FOLDER,
						OXFolderException.DETAIL_NUMBER_CONCURRENT_MODIFICATION, new Object[0]);
			}
			final EffectivePermission effectivePerm = folderobject
					.getEffectiveUserPermission(userId, userConfiguration);
			if (!effectivePerm.hasModuleAccess(folderobject.getModule())) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(session, user),
						folderModule2String(folderobject.getModule()), Integer.valueOf(ctx.getContextId()));
			}
			if (!effectivePerm.isFolderVisible()) {
				if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
					throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, getFolderName(folderobject),
							getUserName(session, user), Integer.valueOf(ctx.getContextId()));
				}
				throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION,
						getFolderName(folderobject), getUserName(session, user), Integer.valueOf(ctx.getContextId()));
			}
			final long lastModified = System.currentTimeMillis();
			new OXFolderManagerImpl(session, oxfolderAccess).clearFolder(folderobject, false, lastModified);
			return folderobject.getObjectID();
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

}
