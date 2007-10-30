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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.*;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.*;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.attach.util.SetSwitch;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.TimedResultImpl;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;


@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_ATTACH_IMPL_ATTACHMENTBASEIMPL,
		component = Component.ATTACHMENT
)
public class AttachmentBaseImpl extends DBService implements AttachmentBase {

	public static enum FetchMode {PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY}
	
	private static final FetchMode fetchMode = FetchMode.PREFETCH;
	
	private static final boolean USE_QUOTA = true;
	
	private static final Log LOG = LogFactory.getLog(AttachmentBaseImpl.class);
	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(AttachmentBaseImpl.class, LOG);
	private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(AttachmentBaseImpl.class);

	private static final AttachmentQueryCatalog QUERIES = new AttachmentQueryCatalog();
	
	private final ThreadLocal<Context> contextHolder = new ThreadLocal<Context>();
	private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();
	
	private final Map<Integer,List<AttachmentListener>> moduleListeners = new HashMap<Integer,List<AttachmentListener>>();
	private final Map<Integer,List<AttachmentAuthorization>> moduleAuthorizors = new HashMap<Integer,List<AttachmentAuthorization>>();
	
	
	public AttachmentBaseImpl(){
		
	}
	
	public AttachmentBaseImpl(final DBProvider provider) {
		super(provider);
	}

	@OXThrowsMultiple(
			category = { Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.USER_INPUT },
			desc = { "The file could not be saved in the file store. This probably means that the file store is not reachable.", "An Attachment must contain a file, otherwise it's invalid."},
			exceptionId = { 0,1 },
			msg = { "Could not save file to the file store.", "Attachments must contain a file." }
	)
	public long attachToObject(final AttachmentMetadata attachment, final InputStream data, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		
		checkMayAttach(attachment.getFolderId(),attachment.getAttachedId(),attachment.getModuleId(), ctx, user, userConfig);
		
		checkCharacters(attachment);
		
		contextHolder.set(ctx);
		final boolean newAttachment = attachment.getId() == NEW || attachment.getId() == 0;
			
		initDefaultFields(attachment,ctx,user);
		if(!newAttachment && data != null) {
			final List<String> remove = getFiles(attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), new int[]{attachment.getId()},ctx,user);
			fileIdRemoveList.get().addAll(remove);
		}
		String fileId;
		if(data != null) {
			try {
				fileId = saveFile(data,attachment, ctx);
			} catch (final IOException e) {
				throw EXCEPTIONS.create(0,e);
			} catch (final OXException x) {
				throw x;
			} catch (final AbstractOXException e) {
				throw new AttachmentException(e);
			}
		} else {
			if(!newAttachment) {
				fileId = findFileId(attachment.getId(), ctx);
			} else {
				throw EXCEPTIONS.create(1);
			}
		}
		attachment.setFileId(fileId);
		return save(attachment,newAttachment,ctx,user,userConfig);
		
	}

	public long detachFromObject(final int folderId, final int objectId, final int moduleId, final int[] ids, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		checkMayDetach(folderId, objectId, moduleId, ctx, user, userConfig);
		//System.out.print("\n\n\nREMOVE: ");
		//for(int id : ids) { System.out.println(" "+id+" "); }
		//System.out.println(" | \n\n\n");
		
		if(ids.length == 0) {
			return System.currentTimeMillis();
		}
		
		contextHolder.set(ctx);
		
		final List<String> files = getFiles(folderId, objectId, moduleId, ids,ctx,user);
		
		final long ts = removeAttachments(folderId, objectId, moduleId, ids,ctx,user,userConfig);
		
		
		fileIdRemoveList.get().addAll(files);
		
		return ts;
	}

	public AttachmentMetadata getAttachment(final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId, objectId, moduleId, ctx, user, userConfig);
		
		contextHolder.set(ctx);
		
		return loadAttachment(folderId,id, ctx);
	}
	
	public InputStream getAttachedFile(final int folderId, final int objectId, final int moduleId, final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId,objectId, moduleId, ctx, user, userConfig);
		contextHolder.set(ctx);
		
		return getFile(id, ctx);
	}
	
	//FIXME Allow this to throw Exceptions. Fix in Consistency Tool as well.
	public SortedSet<String> getAttachmentFileStoreLocationsperContext(final Context ctx) {
		final SortedSet<String> retval = new TreeSet<String>();
		Connection readCon = null;
		final String selectfileid = "SELECT file_id FROM prg_attachment WHERE file_id is not null AND cid=?";
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
		} catch(final SQLException x) {
			throw new RuntimeException("SQL ERROR: "+x);
		} catch (final TransactionException e) {
			throw new RuntimeException("SQL ERROR: "+e); // FIXME
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
		
		return retval;
	}
	
	public TimedResult getAttachments(final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		return getAttachments(folderId,attachedId,moduleId,QUERIES.getFields(), null, ASC, ctx, user, userConfig);
	}

	public TimedResult getAttachments(final int folderId, final int attachedId, final int moduleId, final AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		
		checkMayReadAttachments(folderId,attachedId, moduleId, ctx,user, userConfig);
		
		contextHolder.set(ctx);
		
		final StringBuilder select = new StringBuilder("SELECT ");
		QUERIES.appendColumnList(select,columns);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? ");
		if(sort != null) {
			select.append(" ORDER BY ");
			select.append(sort.getName());
			if(order == DESC) {
				select.append(" DESC");
			} else {
				select.append(" ASC");
			}
		}
			
		return new TimedResultImpl(new AttachmentIterator(select.toString(),columns,ctx,folderId,fetchMode,Integer.valueOf(moduleId), Integer.valueOf(attachedId), Integer.valueOf(ctx.getContextId())),System.currentTimeMillis());
	}

	public TimedResult getAttachments(final int folderId, final int attachedId, final int moduleId, final int[] idsToFetch, final AttachmentField[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException{
		checkMayReadAttachments(folderId,attachedId, moduleId, ctx,user, userConfig);
		
		contextHolder.set(ctx);
		
		final StringBuilder select = new StringBuilder("SELECT ");
		QUERIES.appendColumnList(select,columns);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and id in (");
		select.append(join(idsToFetch));
		select.append(')');
		
		return new TimedResultImpl(new AttachmentIterator(select.toString(), columns,ctx, folderId, fetchMode, Integer.valueOf(moduleId), Integer.valueOf(attachedId), Integer.valueOf(ctx.getContextId())),System.currentTimeMillis());
	}

	
	public Delta getDelta(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		return getDelta(folderId,attachedId,moduleId,ts,ignoreDeleted,QUERIES.getFields() ,null,ASC, ctx, user, null);
	}

	public Delta getDelta(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final AttachmentField[] columns, final AttachmentField sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		checkMayReadAttachments(folderId,attachedId,moduleId,ctx,user, userConfig);
		
		contextHolder.set(ctx);
		final StringBuilder select = new StringBuilder("SELECT ");
		for(final AttachmentField field : columns ) {
			select.append(field.getName());
			select.append(',');
		}
		select.setLength(select.length()-1);
		
		select.append(" FROM prg_attachment WHERE module = ? and attached = ? and cid = ? and creation_date > ?");
		
		if(sort != null) {
			select.append(" ORDER BY ");
			select.append(sort.getName());
			if(order == DESC) {
				select.append(" DESC");
			} else {
				select.append(" ASC");
			}
		}
		
		final SearchIterator newIterator = new AttachmentIterator(select.toString(),columns,ctx,folderId,fetchMode,Integer.valueOf(moduleId),Integer.valueOf(attachedId), Integer.valueOf(ctx.getContextId()), Long.valueOf(ts));
		
		SearchIterator deletedIterator = SearchIterator.EMPTY_ITERATOR;
		
		if(!ignoreDeleted) {
			deletedIterator = new AttachmentIterator("SELECT id FROM del_attachment WHERE module = ? and attached = ? and cid = ? and del_date > ?",new AttachmentField[]{AttachmentField.ID_LITERAL},ctx, folderId, fetchMode, Integer.valueOf(moduleId), Integer.valueOf(attachedId), Integer.valueOf(ctx.getContextId()), Long.valueOf(ts));
		}
		
		return new DeltaImpl(newIterator,SearchIterator.EMPTY_ITERATOR,deletedIterator, System.currentTimeMillis());
	}
	
	public void registerAttachmentListener(final AttachmentListener listener, final int moduleId) {
		getListeners(moduleId).add(listener);
	}

	public void removeAttachmentListener(final AttachmentListener listener, final int moduleId) {
		getListeners(moduleId).remove(listener);
	}

	private long fireAttached(final AttachmentMetadata m, final User user, final UserConfiguration userConfig, final Context ctx, final Connection writeCon) throws OXException {
		final FireAttachedEventAction fireAttached = new FireAttachedEventAction();
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
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e) {
			throw new AttachmentException(e);
		}
		
	}
	
	private long fireDetached(final List<AttachmentMetadata> deleted, final int module, final User user, final UserConfiguration userConfig, final Context ctx, final Connection writeCon) throws OXException {
		final FireDetachedEventAction fireDetached = new FireDetachedEventAction();
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
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e) {
			throw new AttachmentException(e);
		}
	}
	
	public void addAuthorization(final AttachmentAuthorization authz, final int moduleId) {
		getAuthorizors(moduleId).add(authz);
	}

	public void removeAuthorization(final AttachmentAuthorization authz, final int moduleId) {
		getAuthorizors(moduleId).remove(authz);	
	}

    @OXThrowsMultiple(
			category = { Category.INTERNAL_ERROR, Category.CODE_ERROR },
			desc = { "Could not delete files from filestore. Context: %d.", "Could not remove attachments from database. Context: %d."},
			exceptionId = { 16,17 },
			msg = { "Could not delete files from filestore. Context: %d.", "Could not remove attachments from database. Context: %d." }
	)
    public void deleteAll(Context context) throws OXException {
        try {
            removeFiles(context);
        } catch (AbstractOXException e) {
            LL.log(e);
            throw EXCEPTIONS.create(16,e,context.getContextId());
        }
        try {
            removeDatabaseEntries(context);
        } catch (SQLException e) {
            LOG.error("SQL Exception: ",e);
            throw EXCEPTIONS.create(17,e,context.getContextId());
        }

    }

    private void removeDatabaseEntries(Context context) throws TransactionException, SQLException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        try {
            writeCon = getWriteConnection(context);
            stmt = writeCon.prepareStatement("DELETE FROM prg_attachment WHERE cid = ?");
            stmt.setInt(1, context.getContextId());
            stmt.executeUpdate();
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Can't close statement", e);
                }
            }
            releaseWriteConnection(context,writeCon);
        }

    }

    private void removeFiles(Context context) throws AbstractOXException {
        FileStorage fs = getFileStorage(context);
        for(String fileId : this.getAttachmentFileStoreLocationsperContext(context)){
            fs.deleteFile(fileId);  
        }
    }

    private List<AttachmentAuthorization> getAuthorizors(final int moduleId){
		List<AttachmentAuthorization> authorizors = moduleAuthorizors.get(Integer.valueOf(moduleId));
		if(authorizors == null) {
			authorizors = new ArrayList<AttachmentAuthorization>();
			moduleAuthorizors.put(Integer.valueOf(moduleId),authorizors);
		}
		return authorizors;
	}


	// Helper Methods
	
	private void checkMayAttach(final int folderId, final int attachedId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		for(final AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayAttach(folderId,attachedId,user,userConfig, ctx); 
		}
	}
	
	private void checkMayReadAttachments(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		for(final AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayReadAttachments(folderId,objectId,user,userConfig, ctx); 
		}
	}


	private void checkMayDetach(final int folderId, final int objectId, final int moduleId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		for(final AttachmentAuthorization authz : getAuthorizors(moduleId)){
			authz.checkMayDetach(folderId,objectId,user,userConfig, ctx); 
		}
	}

	private List<AttachmentListener> getListeners(final int moduleId) {
		List<AttachmentListener> listener = moduleListeners.get(Integer.valueOf(moduleId));
		if(listener == null){
			listener = new ArrayList<AttachmentListener>();
			moduleListeners.put(Integer.valueOf(moduleId),listener);
		}
		return listener;
	}
	
	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An SQL Error occurred while trying to generate an id for the new attachment.",
			exceptionId = 2,
			msg = "Cannot generate ID for new attachment: %s"
	)
	private void initDefaultFields(final AttachmentMetadata attachment, final Context ctx, final User user) throws OXException {
		attachment.setCreationDate(new Date());
		attachment.setCreatedBy(user.getId());
		if(attachment.getId() == NEW) {
			Connection writeCon = null;
			try {
				writeCon = getWriteConnection(ctx);
				attachment.setId(getId(ctx,writeCon));
			} catch (final SQLException e) {
				throw EXCEPTIONS.create(2,e);
			} finally {
				releaseWriteConnection(ctx,writeCon);
			}
		}
		
		if(attachment.getFilename() != null && (attachment.getFileMIMEType() == null || attachment.getFileMIMEType().equals("application/unknown")) ) {
			// Try guessing by filename
			final String mimetypes = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(attachment.getFilename());
			attachment.setFileMIMEType(mimetypes);
		}
	}
	
	private int getId(final Context ctx, final Connection writeCon) throws SQLException {
		if(writeCon.getAutoCommit()) {
			return IDGenerator.getId(ctx, Types.ATTACHMENT);
		}
		return IDGenerator.getId(ctx, Types.ATTACHMENT, writeCon);
	}

	private String saveFile(final InputStream data, final AttachmentMetadata attachment, final Context ctx) throws IOException, AbstractOXException {
		final FileStorage fs = getFileStorage(ctx);
		SaveFileAction action = null;
		if(USE_QUOTA) {
			final SaveFileWithQuotaAction a = new SaveFileWithQuotaAction();
			a.setIn(data);
			a.setSizeHint(attachment.getFilesize());
			a.setStorage((QuotaFileStorage) fs);
			action = a;
		} else {
			final SaveFileAction a = new SaveFileAction();
			a.setIn(data);
			action = a;
		}
		action.perform();
		addUndoable(action);
	
		return action.getId();
	}
	
	@OXThrows(
			category = Category.CODE_ERROR, desc = "An invalid SQL query was sent to the server", exceptionId = 3, msg = "Invalid SQL query: %s"
	)
	private List<String> getFiles(final int folderId, final int objectId, final int moduleId, final int[] ids, final Context ctx, final User user) throws OXException {
		final List<String> files = new ArrayList<String>();
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			final StringBuilder selectFileIds = new StringBuilder("SELECT file_id FROM prg_attachment WHERE id in (");
			selectFileIds.append(join(ids));
			selectFileIds.append(") AND cid = ?");
			
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(selectFileIds.toString());
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				files.add(rs.getString(1));
			}
		} catch(final SQLException x) {
			try {
				rollbackDBTransaction();
			} catch (TransactionException x2) {
				LL.log(x2);
			}
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
	private InputStream retrieveFile(final String fileId,final Context ctx) throws OXException {
		try {
			final FileStorage fs = getFileStorage(ctx);
			return fs.getFile(fileId);
		
		} catch (final AbstractOXException e) {
			throw new AttachmentException(e);
		}
	}
		
	InputStream getFile(final int id, final Context ctx) throws OXException {
		final String fileId = findFileId(id,ctx);
		return retrieveFile(fileId,ctx);
	}

	@OXThrowsMultiple(
			category={Category.USER_INPUT, Category.CODE_ERROR},
			desc={"An attachment with the given ID does not exist, so it cannot be downloaded.","An invalid SQL query was sent to the database."},
			exceptionId={5,6},
			msg={"The attachment you requested no longer exists. Please refresh the view.", "Invalid SQL query: %s"}
	)
	private String findFileId(final int id, final Context ctx) throws OXException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection readCon = null;
		try {
			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(QUERIES.getSelectFileId());
			
			stmt.setInt(1,id);
			stmt.setInt(2,ctx.getContextId());
			
			rs = stmt.executeQuery();
			if(!rs.next()) {
				throw EXCEPTIONS.create(5);
			}
			return rs.getString(1);
		} catch (final SQLException x) {
			throw EXCEPTIONS.create(6,x,DBUtils.getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, readCon);
		}
	}
	
	@OXThrows(
			category=Category.INTERNAL_ERROR, desc="An error occurred while retrieving the attachments that should be deleted.", exceptionId=7, msg="Could not delete attachment.")
	private long removeAttachments(final int folderId,final int objectId,final int moduleId,final int[] ids,final Context ctx,final User user,final UserConfiguration userConfig) throws OXException {
		final TimedResult tr = getAttachments(folderId, objectId, moduleId, ids, QUERIES.getFields(), ctx, user, userConfig);
		boolean found = false;
		
		final SearchIterator iter = tr.results();
		
		
		final List<AttachmentMetadata> recreate = new ArrayList<AttachmentMetadata>();
		try {
			while(iter.hasNext()) {
				found = true;
				AttachmentMetadata att;
				att = (AttachmentMetadata) iter.next();
				att.setFolderId(folderId);
				recreate.add(att);
			}
		} catch (final SearchIteratorException e1) {
			throw EXCEPTIONS.create(7);
		} finally {
			try {
				iter.close();
			} catch (final SearchIteratorException e) {
				LOG.error("",e);
			}
		}
		
		if(!found) {
			return System.currentTimeMillis();
		}
		final DeleteAttachmentAction delAction = new DeleteAttachmentAction();
		delAction.setAttachments(recreate);
		delAction.setContext(ctx);
		delAction.setProvider(this);
		delAction.setQueryCatalog(QUERIES);
		
		try {
			perform(delAction, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e1) {
			throw new AttachmentException(e1);
		}
		
		return this.fireDetached(recreate,moduleId,user,userConfig, ctx,null);	
	
	}
	
	@OXThrowsMultiple(
			category = { Category.INTERNAL_ERROR, Category.CODE_ERROR, Category.CODE_ERROR },
			desc = { "Didn't find an attachment with the given file_id, so the file is propably orphaned or does not belong to the Attachments.","An invalid SQL query was sent to the database.","An invalid SQL query was sent to the database" },
			exceptionId = { 8,9,10 },
			msg = { "Could not find an attachment with the file_id %s. Either the file is orphaned or belongs to another module.", "Invalid SQL query: %s", "Invalid SQL query: %s" }
			
	)
	public int[] removeAttachment(final String file_id, final Context ctx) throws OXException {
		final int[] retval = new int[2];
		final long now = System.currentTimeMillis();
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
				rememberDel.append('(');
				rememberDel.append(rs.getInt(1));
				rememberDel.append(',');
				rememberDel.append(now);
				rememberDel.append(',');
				rememberDel.append(ctx.getContextId());
				rememberDel.append(',');
				rememberDel.append(rs.getInt(2));
				rememberDel.append(',');
				rememberDel.append(rs.getInt(3));
				
				rememberDel.append(')');
			}
			if(!found) {
				throw EXCEPTIONS.create(8,file_id);
			}
		} catch (final SQLException e) {
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
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(10,e,DBUtils.getStatement(stmt));
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
		return retval;
	}

	@OXThrowsMultiple(
			category={Category.CODE_ERROR, Category.CODE_ERROR},
			desc={"An invalid SQL query was sent to the database.", "An invalid SQL query was sent to the database."},
			exceptionId={11,12},
			msg={"Invalid SQL query: %s", "Invalid SQL query: %s"})
	public int modifyAttachment(final String file_id, final String new_file_id,
			final String new_comment, final String new_mime, final Context ctx) throws OXException {
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
		} catch (final SQLException e) {
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
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(12, e, DBUtils.getStatement(stmt));
		} finally {
			close(stmt,null);
			releaseWriteConnection(ctx,readCon);
		}
		return retval;
	}
	
	private long save(final AttachmentMetadata attachment, final boolean newAttachment, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		AbstractAttachmentAction action = null;
		if(newAttachment) {
			final CreateAttachmentAction createAction = new CreateAttachmentAction();
			createAction.setAttachments(Arrays.asList(new AttachmentMetadata[]{attachment}));
			action = createAction;
		} else {
			final AttachmentMetadata oldAttachment = loadAttachment(attachment.getFolderId(), attachment.getId(), ctx);
	
			final UpdateAttachmentAction updateAction = new UpdateAttachmentAction();
			updateAction.setAttachments(Arrays.asList(attachment));
			updateAction.setOldAttachments(Arrays.asList(oldAttachment));
			action = updateAction;
	
		}
		
		action.setProvider(this);
		action.setContext(ctx);
		action.setQueryCatalog(QUERIES);
		
		
		try {
			perform(action, true);
		} catch (final OXException x) {
			throw x;
		} catch (final AbstractOXException e) {
			throw new AttachmentException(e);
		}
		
		if(newAttachment) {
			return fireAttached(attachment, user, userConfig, ctx, null);
		}
		return System.currentTimeMillis();
	}
	
	
	@OXThrows(category=Category.USER_INPUT, desc="", exceptionId=18, msg="Validation failed: %s")
	private void checkCharacters(AttachmentMetadata attachment) throws OXException {
		StringBuilder errors = new StringBuilder();
		boolean invalid = false;
		GetSwitch get = new GetSwitch(attachment);
		for(AttachmentField field : AttachmentField.VALUES_ARRAY) {
			Object value = field.doSwitch(get);
			if(null != value && value instanceof String) {
				String error = Check.containsInvalidChars((String)value);
				if(null != error) {
					invalid = true;
					errors.append(field.getName()).append(" ").append(error).append("\n");
				}
			}
		}
		if(invalid) {
			throw EXCEPTIONS.create(18, errors.toString());
		}
	}
	
	@OXThrowsMultiple(
			category = { Category.USER_INPUT, Category.CODE_ERROR },
			desc = { "The Attachment does not exist (anymore). Reloading the view will propably help.","An invalid SQL query was snet to the database." },
			exceptionId = { 13,14 },
			msg = { "The attachment you requested no longer exists. Please refresh the view.","Invalid SQL query: %s" }
	)
	private AttachmentMetadata loadAttachment(final int folderId, final int id, final Context ctx) throws OXException  {
		
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
			
		} catch (final SQLException x) {
			throw EXCEPTIONS.create(14,x,DBUtils.getStatement(stmt));
		} finally {
			close(stmt, rs);
			releaseReadConnection(ctx,readConnection);
		}
	}
	
	private AttachmentMetadata getFromResultSet(final ResultSet rs, final int folderId) throws SQLException {
		final AttachmentImpl attachment = new AttachmentImpl();
		final SetSwitch set = new SetSwitch(attachment);
		for(final AttachmentField field : QUERIES.getFields()) {
			Object  value = rs.getObject(field.getName());
			value = patchValue(value,field);
			set.setValue(value);
			field.doSwitch(set);
		}
		attachment.setFolderId(folderId);
		return attachment;
	}

	private boolean isDateField(final AttachmentField field) {
		return field.equals(AttachmentField.CREATION_DATE_LITERAL);
	}
	
	private String join(final int[] is) {
		final StringBuilder b = new StringBuilder();
		for(final int i : is) {
			b.append(i);
			b.append(',');
		}
		b.setLength(b.length()-1);
		return b.toString();
	}
	
	private Object patchValue(Object value, final AttachmentField field) {
		if(value instanceof Long) {
			if(isDateField(field)) {
				value = new Date(((Long)value).longValue());
			} else if(!field.equals(AttachmentField.FILE_SIZE_LITERAL)) {
				value = Integer.valueOf(((Long)value).intValue());
			}
		}
		return value;
	}
	
	
	@Override
	@OXThrows(
			category=Category.SUBSYSTEM_OR_SERVICE_DOWN,
			desc="A file could not be removed from the file store. This can lead to inconsistencies if the change could not be undone. Keep your eyes peeled for messages indicating an inconsistency between DB and file store.",
			exceptionId=15,
			msg="Could not delete file from file store. Filestore: %s Context: %s")
	public void commit() throws TransactionException {
		if(fileIdRemoveList.get().size()>0) {
			try {
				final FileStorage fs = getFileStorage(contextHolder.get());
				for(final String fileId : fileIdRemoveList.get()) {
					fs.deleteFile(fileId);
				}
			} catch (final AbstractOXException x) {
				try {
					rollback();	
				} catch (final TransactionException txe) {
					LL.log(x);
				}
				throw new TransactionException(EXCEPTIONS.create(15,x,Integer.valueOf(contextHolder.get().getFilestoreId()), Integer.valueOf(contextHolder.get().getContextId())));
			}
			
		}
	}
	
	@Override
	public void finish() throws TransactionException{
		fileIdRemoveList.set(null);
		contextHolder.set(null);
		super.finish();
	}
	
	@Override
	public void startTransaction() throws TransactionException {
		fileIdRemoveList.set(new ArrayList<String>());
		contextHolder.set(null);
		super.startTransaction();
	}
	
	protected FileStorage getFileStorage(final Context ctx) throws AbstractOXException {
		if(USE_QUOTA) {
			return FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,getProvider());
		}
		return FileStorage.getInstance(FilestoreStorage.createURI(ctx));
	}
	
	public class AttachmentIterator implements SearchIterator {

		private String sql;
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
		
		public AttachmentIterator(final String sql, final AttachmentField[] columns, final Context ctx, final int folderId, final FetchMode mode, final Object...values) {
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
			} catch (final Exception e) {
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
				if(exception instanceof AbstractOXException) {
					throw new SearchIteratorException((AbstractOXException)exception);
				}
				throw new SearchIteratorException(EXCEPTIONS.create(16));
			}
			
			final AttachmentMetadata m = nextFromResult(rs);
			initNext=true;
			return m;
		}
		
		
		@OXThrows(category = Category.SUBSYSTEM_OR_SERVICE_DOWN, desc = "Could not fetch result from database.", exceptionId = 17, msg = "Could not fetch result from database.")
		private AttachmentMetadata nextFromResult(final ResultSet rs) throws SearchIteratorException {
			final AttachmentMetadata m = new AttachmentImpl();
			final SetSwitch set = new SetSwitch(m);
			
			try {
				for (final AttachmentField column : columns) {
					Object value;
					if(column.equals(AttachmentField.FOLDER_ID_LITERAL)) {
						value = Integer.valueOf(folderId);
					} else {
						value = rs.getObject(column.getName());
					}
					value = patchValue(value,column);
					set.setValue(value);
					column.doSwitch(set);
				}
			} catch (final SQLException e) {
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
			if(null != readCon) {
				releaseReadConnection(ctx, readCon);
			}
			
		}
		
		public int size() {
			if(delegate != null) {
				return delegate.size();
			}
			throw new UnsupportedOperationException("Mehtod size() not implemented");
		}
		
		public boolean hasSize() {
			if(delegate != null) {
				return delegate.hasSize();
			}
			return false;
		}
		
		private void query() {
			try {
				readCon = AttachmentBaseImpl.this.getReadConnection(ctx);
				stmt = readCon.prepareStatement(sql);
				int i = 1;
				for (final Object value : values) {
					stmt.setObject(i++,value);
				}
				rs = stmt.executeQuery();
				if(mode.equals(FetchMode.CLOSE_LATER)) {
					return;
				} else if (mode.equals(FetchMode.CLOSE_IMMEDIATELY)) {
					AttachmentBaseImpl.this.close(stmt, null);
					releaseReadConnection(ctx, readCon);
					stmt = null;
					readCon = null;
				} else if (mode.equals(FetchMode.PREFETCH)) {
					final List<Object> values = new ArrayList<Object>();
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
			} catch (final Exception e) {
				LOG.error(e);
				this.exception = e;
			}
		}
	}
}
