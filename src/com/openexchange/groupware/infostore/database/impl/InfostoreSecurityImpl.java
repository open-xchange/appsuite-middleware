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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.server.EffectivePermission;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.infostore.Classes;


@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_INFOSTORESECURITYIMPL,
		component=Component.INFOSTORE
)
public class InfostoreSecurityImpl extends DBService{

	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(InfostoreSecurityImpl.class);
	
	@OXThrows(
			category = Category.USER_INPUT,
			desc = "The infoitem does not exist, so the permissions cannot be loaded.",
			exceptionId = 0,
			msg = "The requested item does not exist."
	)
	public EffectiveInfostorePermission getInfostorePermission(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		List<DocumentMetadata> documentData = getFolderIdAndCreatorForDocuments(ctx.getContextId(), new int[]{id}, ctx, user, userConfig);			
		if (documentData == null || documentData.size() <= 0 || documentData.get(0) == null) {
			throw EXCEPTIONS.create(0);
		}
		
		
		Connection con = null;
		try {
			con = getReadConnection(ctx);
			EffectivePermission isperm = OXFolderTools.getEffectiveFolderOCL((int)documentData.get(0).getFolderId(), user.getId(), user.getGroups(), ctx, userConfig, con);
			return new EffectiveInfostorePermission(isperm, documentData.get(0),user);
		} finally {
			releaseReadConnection(ctx, con);
		}
		
	}
	
	@OXThrows(
			category = Category.USER_INPUT,
			desc = "To check permissions infoitems must be loaded to find their folderId and creator.",
			exceptionId = 1,
			msg = "Could not load documents to check the permissions"
	)
	private List<DocumentMetadata> getFolderIdAndCreatorForDocuments(int contextId, int[] is, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		InfostoreIterator iter = InfostoreIterator.list(is, new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.CREATED_BY_LITERAL}, getProvider(), ctx);
		
		try {
			return iter.asList();
		} catch (SearchIteratorException e) {
			throw EXCEPTIONS.create(1,e);
		}
	}

	public EffectivePermission getFolderPermission(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		Connection readCon = null;
		try {
			readCon = getReadConnection(ctx);
			return OXFolderTools.getEffectiveFolderOCL((int)folderId, user.getId(), user.getGroups(),ctx, userConfig, readCon);
		} finally {
			releaseReadConnection(ctx, readCon);
		}
	}

	public <L> L injectInfostorePermissions(int[] ids, Context ctx, User user, UserConfiguration userConfig, L list, Injector<L, EffectiveInfostorePermission> injector) throws OXException {
		Map<Integer, EffectivePermission> cache = new HashMap<Integer,EffectivePermission>();
		List<EffectiveInfostorePermission> permissions = new ArrayList<EffectiveInfostorePermission>();
		Connection con = null;
		List<DocumentMetadata> metadata = getFolderIdAndCreatorForDocuments(ctx.getContextId(), ids, ctx, user, userConfig);
		try {
			con = getReadConnection(ctx);
			for(DocumentMetadata m : metadata) {
				EffectivePermission isperm = null;
				if(!cache.containsKey(m.getFolderId())) {
					isperm = OXFolderTools.getEffectiveFolderOCL((int)m.getFolderId(), user.getId(), user.getGroups(), ctx, userConfig, con);
					cache.put((int) m.getFolderId(), isperm);
				} else {
					isperm = cache.get((int) m.getFolderId());
				}
				permissions.add(new EffectiveInfostorePermission(isperm, m,user));
			}
			
		} finally {
			releaseReadConnection(ctx, con);
		}
		
		return OXCollections.inject(list, permissions, injector);
		
	}
	
	@OXThrows(
			category = Category.PROGRAMMING_ERROR,
			desc = "The client tries to put an infoitem into a non infoitem folder.",
			exceptionId = 2,
			msg = "The folder %d is not an Infostore Folder"
	)
	public void checkFolderId(long folderId, Context ctx) throws OXException {
		FolderCacheManager cache = FolderCacheManager.getInstance();
		FolderObject fo = cache.getFolderObject((int)folderId, ctx);
		if(fo == null) {
			Connection readCon = null;
			try {
				readCon = getReadConnection(ctx);
				fo = cache.getFolderObject((int)folderId, false, ctx, readCon);
			} finally {
				releaseReadConnection(ctx, readCon);
			}
		}
		if(fo.getModule() != FolderObject.INFOSTORE) {
			throw EXCEPTIONS.create(1,folderId);
		}
	}

}
