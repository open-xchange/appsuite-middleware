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

package com.openexchange.groupware.attach.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentException;
import com.openexchange.groupware.attach.AttachmentExceptionFactory;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.util.SetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.TimedResultImpl;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.groupware.attach.Classes;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;


@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_ATTACH_IMPL_ATTACHMENTBASEIMPL,
		component = Component.ATTACHMENT
)
public class AttachmentBaseImpl extends DBService implements AttachmentBase {

	public static enum FetchMode {PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY};
	
	private static final FetchMode fetchMode = FetchMode.PREFETCH;
	
	private static final boolean USE_QUOTA = true;
	
	private static final Log LOG = LogFactory.getLog(AttachmentBaseImpl.class);
	private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(AttachmentBaseImpl.class);

	private static final AttachmentQueryCatalog QUERIES = new AttachmentQueryCatalog();
	
	private static String SELECT_BY_ID = null;
	
	
	private ThreadLocal<Context> contextHolder = new ThreadLocal<Context>();
	private ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();
	
	private Map<Integer,List<AttachmentListener>> moduleListeners = new HashMap<Integer,List<AttachmentListener>>();
	private Map<Integer,List<AttachmentAuthorization>> moduleAuthorizors = new HashMap<Integer,List<AttachmentAuthorization>>();
	
	
	public AttachmentBaseImpl(){
		
	}
	
	public AttachmentBaseImpl(DBProvider provider) {
		super(provider);
	}

	@OXThrowsMultiple(
			category = { Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.USER_INPUT },
			desc = { "The file could not be saved in the file store. This probably means that the file store is not reachable.", "An Attachment must contain a file, otherwise it's invalid."},
			exceptionId = { 0,1 },
			msg = { "Could not save file to the file store.", "Attachments must contain a file." }
	)
	public long attachToObject(AttachmentMetadata attachment, InputStream data, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		
		checkMayAttach(attachment.getFolderId(),attachment.getAttachedId(),attachment.getModuleId(), ctx, user, userConfig);
		
		contextHolder.set(ctx);
		boolean newAttachment = attachment.getId() == NEW || attachment.getId() == 0;
			
		initDefaultFields(attachment,ctx,user);
		if(!newAttachment && data != null) {
			List<String> remove = getFiles(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), new int[]{attachment.getId()},ctx,user);
			fileIdRemoveList.get().addAll(remove);
		}
		String fileId;
		if(data != null) {
			try {
				fileId = saveFile(data,attachment, ctx);
			} catch (IOException e) {
				throw EXCEPTIONS.create(0,e);
			} catch (OXException x) {
				throw x;
			} catch (AbstractOXException e) {
				throw new AttachmentException(e);
			}
		} else {
			if(!newAttachment)
				fileId = findFileId(attachment.getId(), ctx);
			else 
				throw EXCEPTIONS.create(1);
		}
		attachment.setFileId(fileId);
		return save(attachment,newAttachment,ctx,user,userConfig);
		
	}

	public long detachFromObject(int folderId, int objectId, int moduleId, int[] ids, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		checkMayDetach(folderId, objectId, moduleId, ctx, user, userConfig);
		//System.out.print("\n\n\nREMOVE: ");
		//for(int id : ids) { System.out.println(" "+id+" "); }
		//System.out.println(" | \n\n\n");
		
		if(ids.length == 0)
			return System.currentTimeMillis();
		
		contextHolder.set(ctx);
		
		List<String> files = getFiles(folderId, objectId, moduleId, ids,ctx,user);
		
		long ts = removeAttachments(folderId, objectId, moduleId, ids,ctx,user,userConfig);
		
		
		fileIdRemoveList.get().addAll(files);
		
		return ts;
	}

	public AttachmentMetadata getAttachment(int folderId, int objectId, int moduleId, int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);
		
		contextHolder.set(ctx);
		
		return loadAttachment(folderId,id, ctx);
	}
	
	public InputStream getAttachedFile(int folderId, int objectId, int moduleId, int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId,objectId, moduleId, ctx, user, userConfig);
		contextHolder.set(ctx);
		
		return getFile(id, ctx);
	}
	
	//FIXME Allow this to throw Exceptions. Fix in Consistency Tool as well.
	public SortedSet<String> getAttachmentFileStoreLocationsperContext(Context ctx) {
		SortedSet<String> retval = new TreeSet<String>();
		Connection readCon = null;
		String selectfileid = "SELECT file_id FROM prg_attachment WHERE file_id is not null AND cid=?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(selectfileid);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				retval.add(rs.getString(1));
			}
		} catch(SQLException x) {
			throw new RuntimeException("SQL ERROR: "+x);
		} catch (TransactionException e) {
			throw new RuntimeException("SQL ERROR: "+e); // FIXME
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
		
		return retval;
	}
	
	public TimedResult getAttachments(int folderId, int attachedId, int moduleId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return getAttachments(folderId,attachedId,moduleId,QUERIES.getFields(), null, ASC, ctx, user, userConfig);
	}

	public TimedResult getAttachments(int folderId, int attachedId, int moduleId, AttachmentField[] columns, AttachmentField sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		
		checkMayReadAttachments(folderId,attachedId, moduleId, ctx,user, userConfig);
		
		contextHolder.set(ctx);
		
		StringBuilder select = new StringBuilder("SELECT ");
		QUERIES.appendColumnList(select,columns);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? ");
		if(sort != null) {
			select.append(" ORDER BY ");
			select.append(sort.getName());
			if(order == DESC)
				select.append(" DESC");
			else
				select.append(" ASC");
		}
			
		return new TimedResultImpl(new AttachmentIterator(select.toString(),columns,ctx,folderId,fetchMode,moduleId, attachedId, ctx.getContextId()),System.currentTimeMillis());
	}

	public TimedResult getAttachments(int folderId, int attachedId, int moduleId, int[] idsToFetch, AttachmentField[] columns, Context ctx, User user, UserConfiguration userConfig) throws OXException{
		checkMayReadAttachments(folderId,attachedId, moduleId, ctx,user, userConfig);
		
		contextHolder.set(ctx);
		
		StringBuilder select = new StringBuilder("SELECT ");
		QUERIES.appendColumnList(select,columns);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and id in (");
		select.append(join(idsToFetch));
		select.append(")");
		
		return new TimedResultImpl(new AttachmentIterator(select.toString(), columns,ctx, folderId, fetchMode, moduleId, attachedId, ctx.getContextId()),System.currentTimeMillis());
	}

	
	public Delta getDelta(int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		return getDelta(folderId,attachedId,moduleId,ts,ignoreDeleted,QUERIES.getFields() ,null,ASC, ctx, user, null);
	}

	public Delta getDelta(int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, AttachmentField[] columns, AttachmentField sort, int order, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId,attachedId,moduleId,ctx,user, userConfig);
		
		contextHolder.set(ctx);
		StringBuilder select = new StringBuilder("SELECT ");
		for(AttachmentField field : columns ) {
			select.append(field.getName());
			select.append(",");
		}
		select.setLength(select.length()-1);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and creation_date > ?");
		
		if(sort != null) {
			select.append(" ORDER BY ");
			select.append(sort.getName());
			if(order == DESC)
				select.append(" DESC");
			else
				select.append(" ASC");
		}
		
		SearchIterator newIterator = new AttachmentIterator(select.toString(),columns,ctx,folderId,fetchMode,moduleId,attachedId, ctx.getContextId(), ts);
		
		SearchIterator deletedIterator = SearchIterator.EMPTY_ITERATOR;
		
		if(!ignoreDeleted) {
			deletedIterator = new AttachmentIterator("SELECT id FROM del_attachment WHERE module = ? and attached = ? and cid = ? and del_date > ?",new AttachmentField[]{AttachmentField.ID_LITERAL},ctx, folderId, fetchMode, moduleId, attachedId, ctx.getContextId(), ts);
		}
		
		return new DeltaImpl(newIterator,SearchIterator.EMPTY_ITERATOR,deletedIterator, System.currentTimeMillis());
	}
	
	public void registerAttachmentListener(AttachmentListener listener, int moduleId) {
		getListeners(moduleId).add(listener);
	}

	public void removeAttachmentListener(AttachmentListener listener, int moduleId) {
		getListeners(moduleId).remove(listener);
	}

	private long fireAttached(AttachmentMetadata m, User user, UserConfiguration userConfig, Context ctx, Connection writeCon) throws OXException {
		FireAttachedEventAction fireAttached = new FireAttachedEventAction();
		fireAttached.setAttachments(Arrays.asList(m));
		fireAttached.setContext(ctx);
		fireAttached.setSource(this);
		fireAttached.setUser(user);
		fireAttached.setUserConfiguration(userConfig);
		fireAttached.setProvider(this);
		fireAttached.setAttachmentListeners(getListeners(m.getModuleId()));
		try {
			perform(fireAttached, false);
			return fireAttached.getTimestamp();
		} catch (OXException x) {
			throw x;
		} catch (AbstractOXException e) {
			throw new AttachmentException(e);
		}
		
	}
	
	private long fireDetached(List<AttachmentMetadata> deleted, int module, User user, UserConfiguration userConfig, Context ctx, Connection writeCon) throws OXException {
		FireDetachedEventAction fireDetached = new FireDetachedEventAction();
		fireDetached.setAttachments(deleted);
		fireDetached.setContext(ctx);
		fireDetached.setSource(this);
		fireDetached.setUser(user);
		fireDetached.setUserConfiguration(userConfig);
		fireDetached.setProvider(this);
		fireDetached.setAttachmentListeners(getListeners(module));
		try {
			perform(fireDetached, false);
			return fireDetached.getTimestamp();
		} catch (OXException x) {
			throw x;
		} catch (AbstractOXException e) {
			throw new AttachmentException(e);
		}
	}
	
	public void addAuthorization(AttachmentAuthorization authz, int moduleId) {
		getAuthorizors(moduleId).add(authz);
	}

	public void removeAuthorization(AttachmentAuthorization authz, int moduleId) {
		getAuthorizors(moduleId).remove(authz);	
	}
	
	private List<AttachmentAuthorization> getAuthorizors(int moduleId){
		List<AttachmentAuthorization> authorizors = moduleAuthorizors.get(moduleId);
		if(authorizors == null) {
			authorizors = new ArrayList<AttachmentAuthorization>();
			moduleAuthorizors.put(moduleId,authorizors);
		}
		return authorizors;
	}


	// Helper Methods
	
	private void checkMayAttach(int folderId, int attachedId, int moduleId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		for(AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayAttach(folderId,attachedId,user,userConfig, ctx); 
		}
	}
	
	private void checkMayReadAttachments(int folderId, int objectId, int moduleId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		for(AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayReadAttachments(folderId,objectId,user,userConfig, ctx); 
		}
	}


	private void checkMayDetach(int folderId, int objectId, int moduleId, Context ctx, User user, UserConfiguration userConfig) throws OXException {
		for(AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayDetach(folderId,objectId,user,userConfig, ctx); 
		}
	}

	private List<AttachmentListener> getListeners(int moduleId) {
		List<AttachmentListener> listener = moduleListeners.get(moduleId);
		if(listener == null){
			listener = new ArrayList<AttachmentListener>();
			moduleListeners.put(moduleId,listener);
		}
		return listener;
	}
	
	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An SQL Error occurred while trying to generate an id for the new attachment.",
			exceptionId = 2,
			msg = "Cannot generate ID for new attachment: %s"
	)
	private void initDefaultFields(AttachmentMetadata attachment, Context ctx, User user) throws OXException {
		attachment.setCreationDate(new Date());
		attachment.setCreatedBy(user.getId());
		if(attachment.getId() == NEW) {
			Connection writeCon = null;
			try {
				writeCon = getWriteConnection(ctx);
				attachment.setId(getId(ctx,writeCon));
			} catch (SQLException e) {
				throw EXCEPTIONS.create(2,e);
			} finally {
				releaseWriteConnection(ctx,writeCon);
			}
		}
		
		if(attachment.getFilename() != null && (attachment.getFileMIMEType() == null || attachment.getFileMIMEType().equals("application/unknown")) ) {
			// Try guessing by filename
			String mimetypes = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(attachment.getFilename());
			attachment.setFileMIMEType(mimetypes);
		}
	}
	
	private int getId(Context ctx, Connection writeCon) throws SQLException {
		if(writeCon.getAutoCommit())
			return IDGenerator.getId(ctx, Types.ATTACHMENT);
		return IDGenerator.getId(ctx, Types.ATTACHMENT, writeCon);
	}

	private String saveFile(InputStream data, AttachmentMetadata attachment, Context ctx) throws IOException, AbstractOXException {
		FileStorage fs = getFileStorage(ctx);
		SaveFileAction action = null;
		if(USE_QUOTA) {
			SaveFileWithQuotaAction a = new SaveFileWithQuotaAction();
			a.setIn(data);
			a.setSizeHint(attachment.getFilesize());
			a.setStorage((QuotaFileStorage) fs);
			action = a;
		} else {
			SaveFileAction a = new SaveFileAction();
			a.setIn(data);
			action = a;
		}
		action.perform();
		addUndoable(action);
	
		return action.getId();
	}
	
	@OXThrows(
			category = Category.CODE_ERROR, desc = "An invalid SQL Query was sent to the server", exceptionId = 3, msg = "Invalid SQL Query: %s"
	)
	private List<String> getFiles(int folderId, int objectId, int moduleId, int[] ids, Context ctx, User user) throws OXException {
		List<String> files = new ArrayList<String>();
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder selectFileIds = new StringBuilder("SELECT file_id FROM prg_attachment WHERE id in (");
			selectFileIds.append(join(ids));
			selectFileIds.append(") AND cid = ?");
			
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(selectFileIds.toString());
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				files.add(rs.getString(1));
			}
		} catch(SQLException x) {
			rollbackDBTransaction();
			throw EXCEPTIONS.create(3,x,DBUtils.getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
		return files;
	}
	
	@OXThrows(
			category=Category.SUBSYSTEM_OR_SERVICE_DOWN,
			desc="A file could not be loaded from the file store. This means either that the file does not exist (and your database is inconsistent), or that the file store is not reachable.",
			exceptionId=4,
			msg="Could not retrieve file: %s")
	private InputStream retrieveFile(String fileId,Context ctx) throws OXException {
		try {
			FileStorage fs = getFileStorage(ctx);
			return fs.getFile(fileId);
		
		} catch (FileStorageException e) {
			throw new AttachmentException(e);
		}
	}
		
	InputStream getFile(int id, Context ctx) throws OXException {
		String fileId = findFileId(id,ctx);
		return retrieveFile(fileId,ctx);
	}

	@OXThrowsMultiple(
			category={Category.USER_INPUT, Category.CODE_ERROR},
			desc={"An attachment with the given ID does not exist, so it cannot be downloaded.","An invalid SQL Query was sent to the database."},
			exceptionId={5,6},
			msg={"The attachment you requested no longer exists. Please refresh the view.", "Invalid SQL Query: %s"}
	)
	private String findFileId(int id, Context ctx) throws OXException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection readCon = null;
		try {
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(QUERIES.getSelectFileId());
			
			stmt.setInt(1,id);
			stmt.setInt(2,ctx.getContextId());
			
			rs = stmt.executeQuery();
			if(!rs.next())
				throw EXCEPTIONS.create(5);
			return rs.getString(1);
		} catch (SQLException x) {
			throw EXCEPTIONS.create(6,x,DBUtils.getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}
	
	@OXThrows(
			category=Category.INTERNAL_ERROR, desc="An error occurred while retrieving the attachments that should be deleted.", exceptionId=7, msg="Could not delete attachment.")
	private long removeAttachments(final int folderId,final int objectId,final int moduleId,final int[] ids,final Context ctx,final User user,final UserConfiguration userConfig) throws OXException {
		TimedResult tr = getAttachments(folderId, objectId, moduleId, ids, QUERIES.getFields(), ctx, user, userConfig);
		boolean found = false;
		
		SearchIterator iter = tr.results();
		
		
		final List<AttachmentMetadata> recreate = new ArrayList<AttachmentMetadata>();
		try {
			while(iter.hasNext()) {
				found = true;
				AttachmentMetadata att;
				att = (AttachmentMetadata) iter.next();
				att.setFolderId(folderId);
				recreate.add(att);
			}
		} catch (SearchIteratorException e1) {
			throw EXCEPTIONS.create(7);
		} finally {
			try {
				iter.close();
			} catch (SearchIteratorException e) {
				LOG.error("",e);
			}
		}
		
		if(!found)
			return System.currentTimeMillis();
		DeleteAttachmentAction delAction = new DeleteAttachmentAction();
		delAction.setAttachments(recreate);
		delAction.setContext(ctx);
		delAction.setProvider(this);
		delAction.setQueryCatalog(QUERIES);
		
		try {
			perform(delAction, true);
		} catch (OXException x) {
			throw x;
		} catch (AbstractOXException e1) {
			throw new AttachmentException(e1);
		}
		
		return this.fireDetached(recreate,moduleId,user,userConfig, ctx,null);	
	
	}
	
	@OXThrowsMultiple(
			category = { Category.INTERNAL_ERROR, Category.CODE_ERROR, Category.CODE_ERROR },
			desc = { "Didn't find an attachment with the given file_id, so the file is propably orphaned or does not belong to the Attachments.","An invalid SQL Query was sent to the database.","An invalid SQL Query was sent to the database" },
			exceptionId = { 8,9,10 },
			msg = { "Could not find an attachment with the file_id %s. Either the file is orphaned or belongs to another module.", "Invalid SQL Query: %s", "Invalid SQL Query: %s" }
			
	)
	public int[] removeAttachment(String file_id, Context ctx) throws OXException {
		int[] retval = new int[2];
		long now = System.currentTimeMillis();
		Connection readCon = null;
		Connection writeCon = null;
		PreparedStatement stmt = null;
		StringBuilder rememberDel = null;
		ResultSet rs = null;
		try {
			readCon = getReadConnection(ctx);
			
			stmt = readCon.prepareStatement("SELECT id, attached, module FROM prg_attachment WHERE cid=? AND file_id=?");
			stmt.setInt(1,ctx.getContextId());
			stmt.setString(2, file_id);
						
			rs = stmt.executeQuery();
			
			rememberDel = new StringBuilder("INSERT INTO del_attachment (id, del_date, cid, attached, module) VALUES ");
			boolean found = false;
			if (rs.next()) {
				found = true;
				rememberDel.append("(");
				rememberDel.append(rs.getInt(1));
				rememberDel.append(",");
				rememberDel.append(now);
				rememberDel.append(",");
				rememberDel.append(ctx.getContextId());
				rememberDel.append(",");
				rememberDel.append(rs.getInt(2));
				rememberDel.append(",");
				rememberDel.append(rs.getInt(3));
				
				rememberDel.append(")");
			}
			if(!found) {
				throw EXCEPTIONS.create(8,file_id);
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(9,e,DBUtils.getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx,readCon);
			readCon = null;
		}

		try {

			writeCon = getWriteConnection(ctx);
			
			stmt = writeCon.prepareStatement(rememberDel.toString());
			retval[0] = stmt.executeUpdate();
			stmt.close();

			stmt = writeCon.prepareStatement("DELETE FROM prg_attachment WHERE cid=? AND file_id=?");
			stmt.setInt(1, ctx.getContextId());
			stmt.setString(2, file_id);
			retval[1] = stmt.executeUpdate();
		} catch (SQLException e) {
			throw EXCEPTIONS.create(10,e,DBUtils.getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
		return retval;
	}

	@OXThrowsMultiple(
			category={Category.CODE_ERROR, Category.CODE_ERROR},
			desc={"An invalid SQL Query was sent to the database.", "An invalid SQL Query was sent to the database."},
			exceptionId={11,12},
			msg={"Invalid SQL Query: %s", "Invalid SQL Query: %s"})
	public int modifyAttachment(String file_id, String new_file_id,
			String new_comment, String new_mime, Context ctx) throws OXException {
		int retval = -1;
		Connection writeCon = null;
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String comment = null;
		try {
			readCon = getReadConnection(ctx);
			
			stmt = readCon.prepareStatement("SELECT comment FROM prg_attachment WHERE cid=? AND file_id=?");
			stmt.setInt(1, ctx.getContextId());
			stmt.setString(2, file_id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				comment = rs.getString(1);
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(11, e, DBUtils.getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx,readCon);
			readCon = null;
		}

		if (comment == null) {
			comment = new_comment;
		} else {
			comment = comment.concat(new_comment);
		}
		
		try {
			writeCon =  getWriteConnection(ctx);
			stmt = writeCon.prepareStatement("UPDATE prg_attachment SET file_id=?, file_mimetype=?, comment=? WHERE cid=? AND file_id=?");
			stmt.setString(1, new_file_id);
			stmt.setString(2, new_mime);
			stmt.setString(3, comment);
			stmt.setInt(4, ctx.getContextId());
			stmt.setString(5, file_id);
			retval = stmt.executeUpdate();
		} catch (SQLException e) {
			throw EXCEPTIONS.create(12, e, DBUtils.getStatement(stmt));
		} finally {
			close(stmt,null);
			releaseWriteConnection(ctx,readCon);
		}
		return retval;
	}
	
	private long save(final AttachmentMetadata attachment, boolean newAttachment, final Context ctx, User user, UserConfiguration userConfig) throws OXException {
		AbstractAttachmentAction action = null;
		if(newAttachment) {
			CreateAttachmentAction createAction = new CreateAttachmentAction();
			createAction.setAttachments(Arrays.asList(new AttachmentMetadata[]{attachment}));
			action = createAction;
		} else {
			final AttachmentMetadata oldAttachment = loadAttachment(attachment.getFolderId(), attachment.getId(), ctx);
	
			UpdateAttachmentAction updateAction = new UpdateAttachmentAction();
			updateAction.setAttachments(Arrays.asList(attachment));
			updateAction.setOldAttachments(Arrays.asList(oldAttachment));
			action = updateAction;
	
		}
		
		action.setProvider(this);
		action.setContext(ctx);
		action.setQueryCatalog(QUERIES);
		
		
		try {
			perform(action, true);
		} catch (OXException x) {
			throw x;
		} catch (AbstractOXException e) {
			throw new AttachmentException(e);
		}
		
		if(newAttachment) {
			return fireAttached(attachment, user, userConfig, ctx, null);
		}
		return System.currentTimeMillis();
	}
	
	@OXThrowsMultiple(
			category = { Category.USER_INPUT, Category.CODE_ERROR },
			desc = { "The Attachment does not exist (anymore). Reloading the view will propably help.","An invalid SQL Query was snet to the database." },
			exceptionId = { 13,14 },
			msg = { "The attachment you requested no longer exists. Please refresh the view.","Invalid SQL Query: %s" }
	)
	private AttachmentMetadata loadAttachment(int folderId, int id, Context ctx) throws OXException  {
		
		Connection readConnection = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try {
			readConnection = getReadConnection(ctx);
			stmt = readConnection.prepareStatement(QUERIES.getSelectById());
			stmt.setInt(1,id);
			stmt.setInt(2,ctx.getContextId());
			
			rs = stmt.executeQuery();
			if(!rs.next()) {
				throw EXCEPTIONS.create(13);
			}
			return getFromResultSet(rs, folderId);
			
		} catch (SQLException x) {
			throw EXCEPTIONS.create(14,x,DBUtils.getStatement(stmt));
		} finally {
			close(stmt, rs);
			releaseReadConnection(ctx,readConnection);
		}
	}
	
	private AttachmentMetadata getFromResultSet(ResultSet rs, int folderId) throws SQLException {
		AttachmentImpl attachment = new AttachmentImpl();
		SetSwitch set = new SetSwitch(attachment);
		for(AttachmentField field : QUERIES.getFields()) {
			Object  value = rs.getObject(field.getName());
			value = patchValue(value,field);
			set.setValue(value);
			field.doSwitch(set);
		}
		attachment.setFolderId(folderId);
		return attachment;
	}

	private boolean isDateField(AttachmentField field) {
		return field.equals(AttachmentField.CREATION_DATE_LITERAL);
	}
	
	private String join(int[] is) {
		StringBuilder b = new StringBuilder();
		for(int i : is) {
			b.append(i);
			b.append(",");
		}
		b.setLength(b.length()-1);
		return b.toString();
	}
	
	private Object patchValue(Object value, AttachmentField field) {
		if(value instanceof Long) {
			if(isDateField(field))
				value = new Date((Long)value);
			else if(!field.equals(AttachmentField.FILE_SIZE_LITERAL))
				value = ((Long)value).intValue();
		}
		return value;
	}
	
	
	@OXThrows(
			category=Category.SUBSYSTEM_OR_SERVICE_DOWN,
			desc="A file could not be removed from the file store. This can lead to inconsistencies if the change could not be undone. Keep your eyes peeled for messages indicating an inconsistency between DB and file store.",
			exceptionId=15,
			msg="Could not delete file from file store. Filestore: %s Context: %s")
	public void commit() throws TransactionException {
		if(fileIdRemoveList.get().size()>0) {
			try {
				FileStorage fs = getFileStorage(contextHolder.get());
				for(String fileId : fileIdRemoveList.get()) {
					fs.deleteFile(fileId);
				}
			} catch (FileStorageException x) {
				try {
					rollback();	
				} catch (TransactionException txe) {
					LOG.fatal("Could not execute undo. The system propably contains inconsistent data. Run the recovery tool.", x);//FIXME
				}
				throw new TransactionException(EXCEPTIONS.create(15,x,contextHolder.get().getFilestoreId(), contextHolder.get().getContextId()));
			}
			
		}
	}
	
	public void finish() throws TransactionException{
		fileIdRemoveList.set(null);
		contextHolder.set(null);
		super.finish();
	}
	
	public void startTransaction() throws TransactionException {
		fileIdRemoveList.set(new ArrayList<String>());
		contextHolder.set(null);
		super.startTransaction();
	}
	
	protected FileStorage getFileStorage(Context ctx) throws FileStorageException {
		try {
			if(USE_QUOTA)
				return FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,getProvider());
			else
				return FileStorage.getInstance(FilestoreStorage.createURI(ctx));
		} catch (FilestoreException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e);
			}
        }
		return null;
	}
	
	public class AttachmentIterator implements SearchIterator {

		private String sql = null;
		private AttachmentField[] columns;
		private boolean queried;
		private Context ctx;
		private Connection readCon;
		private PreparedStatement stmt;
		private ResultSet rs;
		private Exception exception;
		private boolean initNext;
		private boolean hasNext;
		private Object[] values;
		private int folderId;
		private FetchMode mode;
		private SearchIteratorAdapter delegate;
		
		public AttachmentIterator(String sql, AttachmentField[] columns, Context ctx, int folderId, FetchMode mode, Object...values) {
			this.sql = sql;
			this.columns = columns;
			this.ctx = ctx;
			this.values = values;
			this.folderId = folderId;
			this.mode = mode;
		}
		
		public boolean hasNext() {
			if(delegate != null) {
				return delegate.hasNext();
			}
			try {
				if (!queried) {
					queried=true;
					query();
					if(delegate != null) {
						return delegate.hasNext();
					}
					initNext=true;
				}
				if (initNext) {
					hasNext = rs.next();
				}
				initNext = false;
				return hasNext;
			} catch (Exception e) {
				this.exception = e;
				return true;
			}
		}
		
		@OXThrows(category = Category.CODE_ERROR, desc = "An error occurred executing the search in the database", exceptionId = 16, msg = "An error occurred executing the search in the database.")
		public Object next() throws SearchIteratorException, OXException {
			if(delegate != null) {
				return delegate.next();
			}
			hasNext();
			if(exception != null){
				if(exception instanceof AbstractOXException)
					throw new SearchIteratorException((AbstractOXException)exception);
				else {
					throw new SearchIteratorException(EXCEPTIONS.create(16));
				}
			}
			
			AttachmentMetadata m = nextFromResult(rs);
			initNext=true;
			return m;
		}
		
		
		@OXThrows(category = Category.SUBSYSTEM_OR_SERVICE_DOWN, desc = "Could not fetch result from database.", exceptionId = 17, msg = "Could not fetch result from database.")
		private AttachmentMetadata nextFromResult(ResultSet rs) throws SearchIteratorException {
			AttachmentMetadata m = new AttachmentImpl();
			SetSwitch set = new SetSwitch(m);
			
			try {
				for (AttachmentField column : columns) {
					Object value;
					if(column.equals(AttachmentField.FOLDER_ID_LITERAL)) {
						value = folderId;
					} else {
						value = rs.getObject(column.getName());
					}
					value = patchValue(value,column);
					set.setValue(value);
					column.doSwitch(set);
				}
			} catch (SQLException e) {
				throw new SearchIteratorException(EXCEPTIONS.create(17,e));
			}
			return m;
		}
		
		public void close() throws SearchIteratorException {
			if(delegate != null) {
				delegate.close();
				return;
			}
			AttachmentBaseImpl.this.close(stmt, rs);
			if(null != readCon)
				releaseReadConnection(ctx, readCon);
			
		}
		
		public int size() {
			if(delegate != null)
				return delegate.size();
			throw new UnsupportedOperationException("Mehtod size() not implemented");
		}
		
		public boolean hasSize() {
			if(delegate != null)
				return delegate.hasSize();
			return false;
		}
		
		private void query() {
			try {
				readCon = AttachmentBaseImpl.this.getReadConnection(ctx);
				stmt = readCon.prepareStatement(sql);
				int i = 1;
				for(Object value : values)
					stmt.setObject(i++,value);
				rs = stmt.executeQuery();
				if(mode.equals(FetchMode.CLOSE_LATER)) {
					return;
				} else if (mode.equals(FetchMode.CLOSE_IMMEDIATELY)) {
					AttachmentBaseImpl.this.close(stmt, null);
					releaseReadConnection(ctx, readCon);
					stmt = null;
					readCon = null;
				} else if (mode.equals(FetchMode.PREFETCH)) {
					List<Object> values = new ArrayList<Object>();
					while(rs.next()) {
						values.add(nextFromResult(rs));
					}
					AttachmentBaseImpl.this.close(stmt, rs);
					releaseReadConnection(ctx, readCon);
					stmt = null;
					readCon = null;
					rs = null;
					delegate = new SearchIteratorAdapter(values.iterator());
				}
			} catch (Exception e) {
				LOG.error(e);
				this.exception = e;
			}
		}
	}
}
