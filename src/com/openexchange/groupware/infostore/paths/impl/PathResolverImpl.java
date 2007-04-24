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

package com.openexchange.groupware.infostore.paths.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderCacheNotEnabledException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.PathResolver;
import com.openexchange.groupware.infostore.Resolved;
import com.openexchange.groupware.infostore.webdav.URLCache;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.infostore.Classes;


@OXExceptionSource(
	classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_PATH_IMPL_PATHRESOLVERIMPL,
	component = Component.INFOSTORE
)
public class PathResolverImpl extends AbstractPathResolver implements PathResolver, URLCache {
	private Mode MODE = null;
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(PathResolverImpl.class);
	
	private final ThreadLocal<Map<String,Resolved>> resolveCache = new ThreadLocal<Map<String,Resolved>>();
	private final ThreadLocal<Map<Integer,String>> docPathCache = new ThreadLocal<Map<Integer,String>>();
	private final ThreadLocal<Map<Integer,String>> folderPathCache = new ThreadLocal<Map<Integer,String>>();

	private InfostoreFacade database;
	
	public PathResolverImpl(DBProvider provider, InfostoreFacade database) {
		setProvider(provider);
		this.database =database;
	}
	
	public PathResolverImpl(InfostoreFacade database) {
		this.database = database;
	}
	
	@Override
	public void setProvider(DBProvider provider) {
		super.setProvider(provider);
		MODE = new CACHE_MODE(provider);
	}
	@OXThrows(
			category=Category.CODE_ERROR,
			desc="A WebdavPath for a document without an attached file was requested. In WebDAV only infoitems with files are visible. This points to a problem with the cola supply for the developer and can only be fixed by R&D.",
			exceptionId=0,
			msg="Illegal Argument: Document %d contains no file"
	)
	public String getPathForDocument(int relativeToFolder, int documentId,
			Context ctx, User user, UserConfiguration userConfig) throws OXException {
		Map<Integer, String> cache = docPathCache.get();
		Map<String, Resolved> resCache = resolveCache.get();
		if(cache.containsKey(documentId))
			return relative(relativeToFolder, cache.get(documentId), ctx, user, userConfig);
		
		DocumentMetadata dm = database.getDocumentMetadata(documentId, InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig);
		if(dm.getFileName() == null || dm.getFileName().equals(""))
			throw EXCEPTIONS.create(0, documentId);
		String path = getPathForFolder(FolderObject.SYSTEM_ROOT_FOLDER_ID, (int)dm.getFolderId(),ctx,user,userConfig)+"/"+dm.getFileName();
		
		path = normalize(path);
		cache.put(documentId, path);
		resCache.put(path, new ResolvedImpl(path, documentId, true));
		return relative(relativeToFolder,path, ctx, user, userConfig);
	
	}

	public String getPathForFolder(int relativeToFolder, int folderId,
			Context ctx, User user, UserConfiguration userConfig) throws OXException {
		if(folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID)
			return "/";
		if(folderId == relativeToFolder)
			return "";
		
		Map<String, Resolved> resCache = resolveCache.get();
		Map<Integer,String> cache = folderPathCache.get();
		if(cache.containsKey(folderId))
			return relative(relativeToFolder, cache.get(folderId), ctx, user, userConfig);
		
		List<FolderObject> path = new ArrayList<FolderObject>();
		FolderObject folder = getFolder(folderId, ctx);
		path.add(folder);
		while(folder != null) {
			if(folder.getParentFolderID() == FolderObject.SYSTEM_ROOT_FOLDER_ID) {
				folder = null;
			} else {
				folder = getFolder(folder.getParentFolderID(), ctx);
				path.add(folder);
			}
		}
		
		
		int length = path.size();
		StringBuilder pathBuilder = new StringBuilder('/');
		for(int i = length-1; i > -1; i--) {
			folder = path.get(i);
			pathBuilder.append(folder.getFolderName());
			String p = normalize(pathBuilder.toString());
			cache.put(folder.getObjectID(), p);
			resCache.put(p, new ResolvedImpl(p, folder.getObjectID(), false));
			pathBuilder.append('/');
		}
		
		pathBuilder.setLength(pathBuilder.length()-1);
		String p = normalize(pathBuilder.toString());
		return relative(relativeToFolder, p, ctx, user, userConfig);
	}
	
	@OXThrowsMultiple(
			category = { Category.CODE_ERROR, Category.CODE_ERROR },
			desc = { "A folder contains two folders with the same folder name. This points to an inconsistency in the database, as the second folder by the same name should not have been created. This will certainly cause some headaches in R&D.", "A faulty SQL statement was sent to the DB. R&D must fix this." },
			exceptionId = { 1,2 },
			msg = { "Folder %d has two subfolders named %s. Your database is not consistent.", "Incorrect SQL Query: %s" }
	)
	public Resolved resolve(int relativeToFolder, String path, Context ctx,
			User user, UserConfiguration userConfig) throws OXException,
			OXObjectNotFoundException {
		
		Map<String, Resolved> cache = resolveCache.get();
		
		String absolutePath = absolute(relativeToFolder, path, ctx, user, userConfig);
		
		if(cache.containsKey(absolutePath))
			return cache.get(absolutePath);
		
		String[] components = path.split("/+");
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		
		StringBuilder pathBuilder = new StringBuilder(getPathForFolder(0, relativeToFolder, ctx, user, userConfig));

		Resolved resolved = new ResolvedImpl(pathBuilder.toString(),relativeToFolder, false);
		cache.put(resolved.getPath(), resolved);

		try {
			int parentId = relativeToFolder;
			boolean dbMode = false;
			
			for(int i = 0; i < components.length; i++) {
				String component = components[i];
				if(component == null || component.length() == 0)
					continue;
				
				pathBuilder.append(component);
				pathBuilder.append("/");
				
				String pathString = normalize(pathBuilder.toString());
				
				if(!dbMode) {
					
					resolved = cache.get(pathString);
					if(resolved != null) {
						parentId = resolved.getId();
					} else {
						con = getReadConnection(ctx);
						stmt = con.prepareStatement("SELECT folder.fuid FROM oxfolder_tree AS folder JOIN oxfolder_tree AS parent ON (folder.parent = parent.fuid AND folder.cid = parent.cid) WHERE folder.cid = ? and parent.fuid = ? and folder.fname = ?");
						stmt.setInt(1, ctx.getContextId());
						dbMode = true;
					}
				} 
				
				if(dbMode) {
					stmt.setInt(2, parentId);
					stmt.setString(3, component);
					
					rs = stmt.executeQuery();
					if(!rs.next()) {
						if(i == components.length -1) {
							// Maybe infoitem?
							stmt.close();
							stmt = con.prepareStatement("SELECT info.id FROM infostore AS info JOIN infostore_document AS doc ON (info.cid = doc.cid AND info.id = doc.infostore_id AND doc.version_number = info.version) WHERE info.cid = ? AND info.folder_id = ? AND doc.filename = ?");
							stmt.setInt(1, ctx.getContextId());
							stmt.setInt(2, parentId);
							stmt.setString(3, component);
							rs = stmt.executeQuery();
							if(!rs.next())
								throw new OXObjectNotFoundException();
							resolved = new ResolvedImpl(pathString,rs.getInt(1), true);
							cache.put(resolved.getPath(), resolved);
							return resolved;
						} else {
							throw new OXObjectNotFoundException();
						}
					}
					
					int nextStep = rs.getInt(1);
					
					if(rs.next()) {
						throw EXCEPTIONS.create(1, parentId, component);
					}
					rs.close();
					parentId = nextStep;
					Resolved res = new ResolvedImpl(pathString, parentId, false);
					cache.put(res.getPath(), res);
				}
			}
			return new ResolvedImpl(normalize(pathBuilder.toString()),parentId, false);
		} catch (SQLException x) {
			throw EXCEPTIONS.create(2, x,stmt.toString());
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx,con);
		}
	}
	
	public void invalidate(String url, int id , Type type) {
		url = normalize(url);
		resolveCache.get().remove(url);
		switch(type) {
		case COLLECTION : 
			folderPathCache.get().remove(id);break;
		case RESOURCE : docPathCache.get().remove(id); break;
		default : throw new IllegalArgumentException("Unknown Type "+type);
		}
	}

	
	public void finish() throws TransactionException {
		resolveCache.set(null);
		docPathCache.set(null);
		folderPathCache.set(null);
		super.finish();
	}

	public void startTransaction() throws TransactionException {
		super.startTransaction();
		resolveCache.set(new HashMap<String,Resolved>());
		docPathCache.set(new HashMap<Integer,String>());
		folderPathCache.set(new HashMap<Integer,String>());
	}

	public void commit() throws TransactionException {
		super.commit();
	}

	public void rollback() throws TransactionException {
		super.rollback();
	}
	
	private FolderObject getFolder(int folderid, Context ctx) throws OXException {
		return MODE.getFolder(folderid, ctx);
	}
	
	static interface Mode {
		public FolderObject getFolder(int folderid, Context ctx) throws OXException;
	}
	
	private final class CACHE_MODE implements Mode {

		private DBProvider provider;

		public CACHE_MODE(DBProvider provider) {
			this.provider = provider;
		}
		
		public FolderObject getFolder(int folderid, Context ctx) throws OXException {
			try {
				FolderObject o =  FolderCacheManager.getInstance().getFolderObject(folderid, ctx);
				if(o == null) {
					Connection readCon = null;
					try {
						readCon = provider.getReadConnection(ctx);
						o = FolderCacheManager.getInstance().loadFolderObject(folderid, ctx, readCon);
					} finally {
						provider.releaseReadConnection(ctx, readCon);
					}
				}
				return o;
			} catch (FolderCacheNotEnabledException e) {
				MODE = new NORMAL_MODE();
				return MODE.getFolder(folderid, ctx);
			}
		}
	}
	
	private static final class NORMAL_MODE implements Mode {

		public FolderObject getFolder(int folderid, Context ctx) throws OXException {
			return FolderObject.loadFolderObjectFromDB(folderid, ctx);
		}
		
	}
}
