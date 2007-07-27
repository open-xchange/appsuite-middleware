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



package com.openexchange.api2;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.event.EventClient;
import com.openexchange.event.InvalidStateException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.PrefetchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_API2_DATABASEIMPL_RDBCONTACTSQLINTERFACE,
		component=Component.CONTACT
	)
	
public class RdbContactSQLInterface implements ContactSQLInterface {

	private static final String ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D = "Unable to load objects. Context %1$d User %2$d";
	private final int userId;
	private final int[] memberInGroups;
	private final Context ctx;
	private final SessionObject sessionobject;

	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(RdbContactSQLInterface.class);
	
	private static final Log LOG = LogFactory.getLog(RdbContactSQLInterface.class);
	
	public RdbContactSQLInterface(SessionObject sessionobject) {
		this.userId = sessionobject.getUserObject().getId();
		this.memberInGroups = sessionobject.getUserObject().getGroups();
		this.ctx = sessionobject.getContext();
		this.sessionobject = sessionobject;
	}

	@OXThrows(
			category=Category.CODE_ERROR,
			desc="0",
			exceptionId=0,
			msg= ContactException.EVENT_QUEUE
	)
	public void insertContactObject(final ContactObject co) throws OXException {
		try{
			Contacts.performContactStorageInsert(co,userId,memberInGroups,sessionobject);
			final EventClient ec = new EventClient(sessionobject);
			ec.create(co);
			/*
			ContactObject coo = new ContactObject();
			coo.setSurName("Hoeger");
			ContactSQLInterface csql = new RdbContactSQLInterface(sessionobject);
			csql.insertContactObject(coo);
			*/
		}catch (InvalidStateException ise){
			throw EXCEPTIONS.create(0,ise);
		}catch (OXConflictException ce){
            LOG.debug("Unable to insert contact", ce);
			throw ce;
		}catch (OXException e){
            LOG.debug("Problem while inserting contact.", e);
			throw e;
		}
	}
	
	@OXThrows(
			category=Category.CODE_ERROR,
			desc="1",
			exceptionId=1,
			msg= ContactException.EVENT_QUEUE
	)
	public void updateContactObject(final ContactObject co, final int fid, final java.util.Date d) throws OXException, OXConcurrentModificationException, ContactException {

		try{
			Contacts.performContactStorageUpdate(co,fid,d,userId,memberInGroups,ctx,sessionobject.getUserConfiguration());
			final EventClient ec = new EventClient(sessionobject);
			ec.modify(co);
		}catch (ContactException ise){
			throw ise;
		}catch (InvalidStateException ise){
			throw EXCEPTIONS.create(1,ise);
		}catch (OXConcurrentModificationException cme){
			throw cme;
		}catch (OXConflictException ce){
			throw ce;
		}catch (OXObjectNotFoundException oonfee){
			throw oonfee;
		}catch (OXException e){
			throw e;
		}
	}
	
	@OXThrowsMultiple(
			category={	Category.PERMISSION,
									Category.SOCKET_CONNECTION,
									Category.PERMISSION,
									Category.PERMISSION,
									Category.CODE_ERROR
								},
			desc={"2","3","4","5","6"},
			exceptionId={2,3,4,5,6},
			msg={	ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NO_PERMISSION_MSG,
							ContactException.NO_PERMISSION_MSG,
							"Unable fetch the number of elements in this Folder. Context %1$d Folder %2$d User %3$d"
						}
	)
	public int getNumberOfContacts(final int folderId) throws OXException {
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(sessionobject.getContext());
			
			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
			} else {
				contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(2,Integer.valueOf(folderId),Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readCon);
			}
			throw e;
			//throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(3,e);
		}	
			
		try {
			final ContactSql contactSQL = new ContactMySql(sessionobject);
			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, sessionobject.getUserConfiguration());
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXConflictException(4,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			if (!oclPerm.canReadAllObjects()) {
				if (oclPerm.canReadOwnObjects()) {
					contactSQL.setReadOnlyOwnFolder(userId);
				} else {
					throw EXCEPTIONS.createOXConflictException(5,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			}
			contactSQL.setSelect(contactSQL.iFgetNumberOfContactsString());
			contactSQL.setFolder(folderId);
			int retval = 0;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = readCon.createStatement();
				rs = stmt.executeQuery(contactSQL.getSqlCommand());
				if (rs.next()) {
					retval = rs.getInt(1);
				}
			} catch (SQLException e) {
				throw EXCEPTIONS.create(6,e,Integer.valueOf(ctx.getContextId()),Integer.valueOf(folderId), Integer.valueOf(userId));
				//throw new OXException("Exception during getNumberOfContacts() for User "+ userId + " in folder " + folderId + " cid= " +sessionobject.getContext().getContextId()+' ' + "\n:"+ e.getMessage());
			} finally {
				closeSQLStuff(rs, stmt);
			}
			return retval;
		} catch (OXException e) {
			throw e;
		}  finally {
			if (readCon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readCon);
			}
		}
	}

	@OXThrowsMultiple(
			category={	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.PERMISSION,
									Category.CODE_ERROR,
									Category.CODE_ERROR								
									
								},
			desc={"7","8","9","10","11","12"},
			exceptionId={7,8,9,10,11,12},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_PERMISSION_MSG,
							ContactException.NO_PERMISSION_MSG,
							"An error occurred during the load of folder objects. Context %1$d Folder %2$d User %3$d",
							"An error occurred during the load of folder objects. Context %1$d Folder %2$d User %3$d"
						}
	)
	public SearchIterator getContactsInFolder(final int folderId, final int from, final int to, final int order_field, final String orderMechanism, final int[] cols) throws OXException {
		String orderDir = orderMechanism;
		int orderBy = order_field;
		if (orderBy == 0){
			orderBy = 502;
		}
		if (orderDir == null || orderDir.length() < 1){
			orderDir = " ASC ";
		}
		Connection readCon = null;	
		try{
			readCon = DBPool.pickup(sessionobject.getContext());
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(7,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
		
		try {
			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
			}else{
				contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(8,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder!  (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readCon);
			}
			throw e;
			//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}
		
		SearchIterator si = null;
		try {
			final ContactSql cs = new ContactMySql(sessionobject);
			cs.setFolder(folderId);

			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, sessionobject.getUserConfiguration());
			
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXConflictException(9,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			if (!oclPerm.canReadAllObjects()) {
				if (oclPerm.canReadOwnObjects()) {
					cs.setReadOnlyOwnFolder(userId);
				} else {
					throw EXCEPTIONS.createOXConflictException(10,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			}
			
			final Statement stmt = readCon.createStatement();
			ResultSet rs = null;
			
			if (orderBy > 0){
				final String order = " ORDER BY co." + Contacts.mapping[orderBy].getDBFieldName() + ' ' + orderDir + " LIMIT "	+ from + ',' + to + ' ';
				cs.setOrder(order);
			}

			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());

			si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
            //return new PrefetchIterator(new ContactObjectIterator(rs, stmt, cols, false, readCon));
			
		} catch (SearchIteratorException e){
			throw EXCEPTIONS.create(11,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			throw EXCEPTIONS.create(12,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),	e);
		}
		return new PrefetchIterator(si);
	}
	
	@OXThrowsMultiple(
			category={	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.PERMISSION,
									Category.CODE_ERROR,
									Category.CODE_ERROR,
									Category.SOCKET_CONNECTION
								},
			desc={"13","14","15","16","17","18","19"},
			exceptionId={13,14,15,16,17,18,19},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_PERMISSION_MSG,
							ContactException.NO_PERMISSION_MSG,
							"An error occurred during the load of folder objects by an extended search. Context %1$d Folder %2$d User %3$d",
							"An error occurred during the load of folder objects by an extended search. Context %1$d Folder %2$d User %3$d",
							ContactException.INIT_CONNECTION_FROM_DBPOOL
						}
	)
	public SearchIterator getContactsByExtendedSearch(final ContactSearchObject searchobject,  final int order_field, final String orderMechanism, final int[] cols) throws OXException {
		String orderDir = orderMechanism;
		int orderBy = order_field;
		if (orderBy == 0){
			orderBy = 502;
		}
		if (orderDir == null || orderDir.length() < 1){
			orderDir = " ASC ";
		}
		Connection readcon = null;	
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(13,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
	
		final int folderId = searchobject.getFolder();
		
		if (!searchobject.isAllFolders()){
			try {
				FolderObject contactFolder;
				if (FolderCacheManager.isEnabled()){
					contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readcon);
				}else{
					contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx);
				}
				if (contactFolder.getModule() != FolderObject.CONTACT) {
					throw EXCEPTIONS.createOXConflictException(14,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			} catch (OXException e) {
				if (readcon != null) {
					DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
				}
				throw e;
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		}
		
		SearchIterator si = null;
		try {
			final ContactSql cs = new ContactMySql(sessionobject);
			
			if (!searchobject.isAllFolders()){	
				cs.setFolder(folderId);

				final EffectivePermission oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(folderId, userId, sessionobject.getUserConfiguration());
				if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
					throw EXCEPTIONS.createOXConflictException(15,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
				if (!oclPerm.canReadAllObjects()) {
					if (oclPerm.canReadOwnObjects()) {
						cs.setReadOnlyOwnFolder(userId);
					} else {
						throw EXCEPTIONS.createOXConflictException(16,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
						//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
					}
				}
			}else{
				searchobject.setAllFolderSQLINString(cs.buildAllFolderSearchString(userId,memberInGroups,sessionobject,readcon).toString());
			}
			
			final String order = " ORDER BY co." + Contacts.mapping[orderBy].getDBFieldName() + ' ' + orderDir +  ' ';
			cs.setOrder(order);
			
			cs.setContactSearchObject(searchobject);
			
			final Statement stmt = readcon.createStatement();
			ResultSet rs = null;
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (DBPoolingException e){
			throw EXCEPTIONS.create(19,e);
		} catch (SearchIteratorException e){
			throw EXCEPTIONS.create(17,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			throw EXCEPTIONS.create(18,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId	+ " in folder " + folderId + " cid="+sessionobject.getContext().getContextId()+ "\n:" + e.getMessage(),	e);
		}
		return new PrefetchIterator(si);
	}
	
	@OXThrowsMultiple(
			category={	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.PERMISSION,
									Category.CODE_ERROR,
									Category.CODE_ERROR
								},
			desc={"20","21","22","23","24","25"},
			exceptionId={20,21,22,23,24,25},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_PERMISSION_MSG,
							ContactException.NO_PERMISSION_MSG,
							"An error occurred during the load of folder objects by a simple search. Context %1$d Folder %2$d User %3$d",
							"An error occurred during the load of folder objects by a simple search. Context %1$d Folder %2$d User %3$d"
						}
	)
	public SearchIterator searchContacts(final String searchpattern, final boolean startletter, final int folderId, final int order_field, final String orderMechanism, final int[] cols) throws OXException {
		String orderDir = orderMechanism;
		int orderBy = order_field;
		if (orderBy == 0){
			orderBy = 502;
		}
		if (orderDir == null || orderDir.length() < 1){
			orderDir = " ASC ";
		}
		Connection readcon = null;	
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
		} catch (DBPoolingException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			throw EXCEPTIONS.create(20,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
				
		try {
			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readcon);
			}else{
				contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx, readcon);
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(21,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			throw e;
			//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}	
		
		SearchIterator si = null;
		try {
			final ContactSql cs = new ContactMySql(sessionobject);
			cs.setFolder(folderId);
			cs.setSearchHabit(" OR ");
			final String order = " ORDER BY co." + Contacts.mapping[orderBy].getDBFieldName() + ' ' + orderDir +  ' ';
			cs.setOrder(order);
			
			final EffectivePermission oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(folderId, userId, sessionobject.getUserConfiguration());
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXConflictException(22,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			if (!oclPerm.canReadAllObjects()) {
				if (oclPerm.canReadOwnObjects()) {
					cs.setReadOnlyOwnFolder(userId);
				} else {
					throw EXCEPTIONS.createOXConflictException(23,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			}
			
			final ContactSearchObject cso = new ContactSearchObject();
			if (startletter){
				cs.setStartCharacter(searchpattern);
				cs.setStartCharacterField(ContactConfig.getProperty("contact_first_letter_field"));
			}else{
				cso.setDisplayName(searchpattern);
				cso.setGivenName(searchpattern);
				cso.setSurname(searchpattern);
			}
			
			cs.setContactSearchObject(cso);
			
			final Statement stmt = readcon.createStatement();
			ResultSet rs = null;
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (SearchIteratorException e){
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			throw EXCEPTIONS.create(24,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			throw EXCEPTIONS.create(25,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId	+ " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),	e);
		}
		return new PrefetchIterator(si);		
	}
	
	@OXThrowsMultiple(
			category={	Category.TRY_AGAIN,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.SOCKET_CONNECTION
								},
			desc={"26","27","28","29"},
			exceptionId={26,27,28,29},
			msg={	"The object you requested can not be found. Try again. Context %1$d Folder %2$d User %3$d Object %4$d",
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_READ_PERMISSION_MSG,
							ContactException.INIT_CONNECTION_FROM_DBPOOL
						}
	)
	public ContactObject getObjectById(final int objectId, final int fid) throws OXException {

		Connection readCon = null;	
		ContactObject co = null;
		try{
			readCon = DBPool.pickup(sessionobject.getContext());
			if (objectId > 0){
				co = Contacts.getContactById(objectId, userId, memberInGroups, ctx, sessionobject.getUserConfiguration(), readCon);							
			}else{
				throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(objectId));
				//throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}

			final int folderId = co.getParentFolderID();

			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
			}else{
				contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);	
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}
		
			if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,sessionobject, readCon)){
				throw EXCEPTIONS.createOXConflictException(28,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO SEE OBJECTS");	
			}
		} catch (DBPoolingException e){
			throw EXCEPTIONS.create(29,e);
		} catch (OXException e){
			throw e;
			//throw new OXException("UNABLE TO LOAD CONTACT BY ID - CHECK RIGHTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+objectId+')', e);
		}  finally {
			if (readCon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readCon);
			}			
		}
		return co;
	}

	@OXThrowsMultiple(
			category={	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.CODE_ERROR,
									Category.CODE_ERROR,
									Category.CODE_ERROR
								},
			desc={"30","31","32","33","34","35"},
			exceptionId={30,31,32,33,34,35},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_PERMISSION_MSG,
							ContactException.NO_PERMISSION_MSG,
							"An error occurred during the load of modified objects from a folder. Context %1$d Folder %2$d User %3$d",
							"An error occurred during the load of modified objects from a folder. Context %1$d Folder %2$d User %3$d"
						}
	)
	public SearchIterator getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
		Connection readCon = null;	
		try{
			readCon = DBPool.pickup(sessionobject.getContext());
		} catch (Exception e) {
			throw EXCEPTIONS.create(30,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
		
		try {
			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
			}else{
				contactFolder = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(31,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readCon);
			}
			throw e;
			//throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}
		
		SearchIterator si = null;
		try {
			final ContactSql cs = new ContactMySql(sessionobject);

			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, sessionobject.getUserConfiguration());
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXConflictException(32,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			if (!oclPerm.canReadAllObjects()) {
				if (oclPerm.canReadOwnObjects()) {
					cs.setReadOnlyOwnFolder(userId);
				} else {
					throw EXCEPTIONS.createOXConflictException(33,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			}
						
			if (folderId == FolderObject.SYSTEM_LDAP_FOLDER_ID){
				cs.getInternalUsers();
			}else{
				cs.setFolder(folderId);
			}
			final Statement stmt = readCon.createStatement();
			ResultSet rs = null;
			cs.getAllChangedSince(since.getTime());
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
		} catch (SearchIteratorException e){
			throw EXCEPTIONS.create(34,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e){
			throw EXCEPTIONS.create(35,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			throw e;
			//throw new OXException(	"Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',	e);
		}
		return new PrefetchIterator(si);
	}

	@OXThrowsMultiple(
			category={	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.CODE_ERROR
								},
			desc={"36","37","38"},
			exceptionId={36,37,38},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,
							"An error occurred during the load of deleted objects from a folder. Context %1$d Folder %2$d User %3$d",
							"An error occurred during the load of deleted objects from a folder. Context %1$d Folder %2$d User %3$d"
						}
	)
	public SearchIterator getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
		SearchIterator si = null;
		Connection readcon = null;
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
			
			final ContactSql cs = new ContactMySql(sessionobject);
			cs.setFolder(folderId);

			final Statement stmt = readcon.createStatement();
			ResultSet rs = null;
			cs.getAllChangedSince(since.getTime());
			final String select = cs.iFgetColsStringFromDeleteTable(cols).toString();
			cs.setSelect(select);
			cs.setOrder(" ORDER BY co.field02 ");
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (SearchIteratorException e) {
			throw EXCEPTIONS.create(37,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(36,e);
		} catch (SQLException e) {
			throw EXCEPTIONS.create(38,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
			//throw new OXException("Exception during getDeletedContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',	e);
		}
		return new PrefetchIterator(si);
	}

	@OXThrowsMultiple(
			category={ 	Category.CODE_ERROR,
									Category.TRY_AGAIN,
									Category.CODE_ERROR,
									Category.PERMISSION,
									Category.PERMISSION,
									Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,
									Category.SOCKET_CONNECTION,
									Category.PERMISSION,
									Category.CODE_ERROR
								},
			desc={"39","40","41","42","58","43","44","45","46","56"},
			exceptionId={39,40,41,42,58,43,44,45,46,56},
			msg={	"Unable to delete this contact. Object not found. Context %1$d Folder %2$d User %3$d Object %4$d",
							ContactException.OBJECT_HAS_CHANGED_MSG+" Context %1$d Folder %2$d User %3$d Object %4$d",
							ContactException.NON_CONTACT_FOLDER_MSG,
							ContactException.NO_DELETE_PERMISSION_MSG,
							ContactException.NO_DELETE_PERMISSION_MSG,
							ContactException.INIT_CONNECTION_FROM_DBPOOL,
							"Unable to delete contact object. Context %1$d Folder %2$d User %3$d Object %4$d",
							ContactException.INIT_CONNECTION_FROM_DBPOOL,
							ContactException.NO_DELETE_PERMISSION_MSG,
							ContactException.EVENT_QUEUE
						}
	)
	public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
		Connection writecon = null;	
		Connection readcon = null;
		EffectivePermission oclPerm = null;
		int created_from = 0;
		final ContactObject co = new ContactObject();
		Statement smt = null;
		ResultSet rs = null;
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
			
			int fid = 0;
			boolean pflag = false;
			Date changing_date = null;
			final ContactSql cs = new ContactMySql(sessionobject);
			smt = readcon.createStatement();
			rs = smt.executeQuery(cs.iFdeleteContactObject(oid,sessionobject.getContext().getContextId()));
			if (rs.next()){
				fid = rs.getInt(1);
				created_from = rs.getInt(2);
				
				co.setCreatedBy(created_from);
				co.setParentFolderID(fid);
				co.setObjectID(oid);
				
				final long xx = rs.getLong(3);
				changing_date = new java.util.Date(xx);
				final int pf = rs.getInt(4);
				if (!rs.wasNull() && pf > 0){
					pflag = true;
				}
			}else{
				throw EXCEPTIONS.createOXObjectNotFoundException(39,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
				//throw new OXObjectNotFoundException();			
			}
			
			//try{
			if ( (client_date != null && client_date.getTime() > 0) && (changing_date != null && client_date.before(changing_date))) {
				throw EXCEPTIONS.createOXConcurrentModificationException(40,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
				//throw new OXConflictException("CONTACT HAS CHANGED ON SERVER SIDE SINCE THE LAST VISIT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')');
			}
				/*
			} catch (Exception np3){ 
				LOG.error("UNABLE TO PERFORM CONTACT DELETE LAST-MODIFY-TEST", np3);
			}
			*/
			FolderObject contactFolder;
			if (FolderCacheManager.isEnabled()){
				contactFolder = FolderCacheManager.getInstance().getFolderObject(fid, true, ctx, readcon);
			}else{
				contactFolder = FolderObject.loadFolderObjectFromDB(fid, ctx);
			}
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(41,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("deleteContactObject called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
			}
			
			if ((contactFolder.getType() != FolderObject.PRIVATE) && pflag){
				LOG.debug(new StringBuilder("Here is a contact in a non PRIVATE folder with a set private flag -> (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')'));
			} else if ((contactFolder.getType() == FolderObject.PRIVATE) && pflag && created_from != userId){
				throw EXCEPTIONS.createOXPermissionException(42,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS CONTACT CUZ IT IS PRIVATE (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
			}
			
			oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(fid, userId, sessionobject.getUserConfiguration());
			if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
				throw EXCEPTIONS.createOXPermissionException(58,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
			}
		}catch (DBPoolingException xe){
			throw EXCEPTIONS.create(43,xe);
			//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', xe);	
		}catch (OXObjectNotFoundException xe){
			throw xe;
			//throw new OXObjectNotFoundException("NOT ALLOWED TO DELETE FOLDER OBJECTS CUZ NO OBJECT FOUND (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')',xe);		
		}catch (SQLException e){
			throw EXCEPTIONS.create(44,e,Integer.valueOf(sessionobject.getContext().getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
			//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);	
		}catch (OXException e){
			throw e;
			//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);			
		} finally {
			try{
				if (rs != null){
					rs.close();
				}
				if (smt != null){
					smt.close();
				}
			} catch (SQLException sxe){
				LOG.error("Unable to close Statement or ResultSet",sxe);
			}
			try{
				if (readcon != null) {
					DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
				}
			} catch (Exception ex){
				LOG.error("Unable to return Connection",ex);
			}
		}

		try{
			writecon = DBPool.pickupWriteable(sessionobject.getContext());
			
			if (oclPerm.canDeleteAllObjects()) {
				Contacts.deleteContact(oid, sessionobject.getContext().getContextId(), writecon);	
			} else {
				if (oclPerm.canDeleteOwnObjects() && created_from == userId){
					Contacts.deleteContact(oid, sessionobject.getContext().getContextId(), writecon);	
				}else{
					throw EXCEPTIONS.createOXConflictException(46,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')');					
				}
			}
			final EventClient ec = new EventClient(sessionobject);
			ec.delete(co);
		} catch (InvalidStateException ise){
			throw EXCEPTIONS.create(56,ise);
		} catch (DBPoolingException xe){
			throw EXCEPTIONS.create(45,xe);
		} catch (OXException e){
			throw e;
			//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);			
		} finally {
			if (writecon != null) {
				DBPool.closeWriterSilent(sessionobject.getContext(), writecon);
			}
		}
	}
	
	@OXThrowsMultiple(
			category={ 	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,				
									Category.CODE_ERROR
								},
			desc={"47","48","49"},
			exceptionId={47,48,49},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,	
							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D
						}
	)
	public SearchIterator getObjectsById(final int[][] object_id, final int[] cols) throws OXException {
		Connection readcon = null;
		SearchIterator si = null;
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
	
			final ContactSql contactSQL = new ContactMySql(sessionobject);
			contactSQL.setSelect(contactSQL.iFgetColsString(cols).toString());			
			contactSQL.setObjectArray(object_id);			
			
			final Statement stmt = readcon.createStatement();
			final ResultSet rs = stmt.executeQuery(contactSQL.getSqlCommand());
			si = new ContactObjectIterator(rs,stmt,cols,true,readcon);
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(47,e);
		} catch (SearchIteratorException e) {
			throw EXCEPTIONS.create(48,e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		} catch (SQLException e) {
			throw EXCEPTIONS.create(49,e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		}
		return new PrefetchIterator(si);
	}
	
	public static boolean performSecurityReadCheck(final int fid, final int created_from, final int user, final int[] group, final SessionObject so, final Connection readcon) {
		return Contacts.performContactReadCheck(fid, created_from,user,group,so.getContext(),so.getUserConfiguration(),readcon);		
	}

	@OXThrowsMultiple(
			category={ 	
								Category.CODE_ERROR,				
								Category.CODE_ERROR
								},
			desc={"50","51"},
			exceptionId={50,51},
			msg={	ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D
						}
	)
	protected ContactObject convertResultSet2ContactObject(final ResultSet rs, final int cols[], final boolean check, final Connection readCon) throws OXException {
		final ContactObject co = new ContactObject();
		
		try{
			co.setContextId(rs.getInt(cols.length + 2));
			co.setCreatedBy(rs.getInt(cols.length + 3));
			
			final long xx = rs.getLong((cols.length + 4));
			Date mi = new java.util.Date(xx);		
			co.setCreationDate(mi);
			
			co.setModifiedBy(rs.getInt(cols.length + 5));

			final long xx2 = rs.getLong((cols.length + 6));
			mi = new java.util.Date(xx2);		
			co.setLastModified(mi);

			co.setObjectID(rs.getInt(cols.length + 7));

			int cnt = 1;
			for (int a=0;a<cols.length;a++){
				Contacts.mapping[cols[a]].addToContactObject(rs, cnt, co, readCon, userId,memberInGroups,ctx, sessionobject.getUserConfiguration());
				cnt++;
			}

			if (!co.containsInternalUserId()){		
				co.setParentFolderID(rs.getInt(cols.length+1));
				if (check && !performSecurityReadCheck(co.getParentFolderID(), co.getCreatedBy(),userId,memberInGroups,sessionobject,readCon)){
					throw EXCEPTIONS.createOXConflictException(50,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				}
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(51,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		} catch (OXException e) {
			throw e;
		}
		
		return co;
	}

	private class ContactObjectIterator implements SearchIterator {

        private ContactObject nexto; 
        private ContactObject pre;

        private ResultSet rs; 
        private Statement stmt; 
        private Connection readcon;
        private int[] cols; 
        private boolean first = true;
        private boolean securecheck; 


    	@OXThrowsMultiple(
    			category={ 	
    								Category.CODE_ERROR,				
    								Category.CODE_ERROR
    								},
    			desc={"52","53"},
    			exceptionId={52,53},
    			msg={	ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
    							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D
    						}
    	)
		private ContactObjectIterator(ResultSet rs,Statement stmt, int[] cols, boolean securecheck, Connection readcon) throws SearchIteratorException {
			this.rs = rs;
			this.stmt = stmt;
			this.cols = cols;
			this.readcon = readcon;
			this.securecheck = securecheck;
			
		    try {
				if (rs.next()) {
		    		if (securecheck){
		    			nexto = convertResultSet2ContactObject(rs, cols, true,  readcon);
		    		}else{
		    			nexto = convertResultSet2ContactObject(rs, cols, false,  readcon);
		    		}
				}
		    } catch (SQLException exc) {
		    	throw EXCEPTIONS.createSearchIteratorException(52,exc, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		    } catch (OXException exc) {
		    	throw EXCEPTIONS.createSearchIteratorException(53,exc, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		    }				
		}
		
    	@OXThrowsMultiple(
    			category={ 	
    								Category.CODE_ERROR,				
    								Category.CODE_ERROR
    								},
    			desc={"54","55"},
    			exceptionId={54,55},
    			msg={	"Unable to close Statement Handling. Context %1$d User %2$d",
    							"Unable to close Statement Handling. Context %1$d User %2$d"
    						}
    	)
		public void close() throws SearchIteratorException {
			try{
				rs.close();
			}catch (SQLException e){
				throw EXCEPTIONS.createSearchIteratorException(54,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new SearchIteratorException("UNABLE TO CLOSE SEARCHITERATOR RESULTSET! (cid="+sessionobject.getContext().getContextId()+')',e);
			}
			try{
				stmt.close();
			}catch (SQLException e){
				throw EXCEPTIONS.createSearchIteratorException(55,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new SearchIteratorException("UNABLE TO CLOSE SEARCHITERATOR STATEMENT! (cid="+sessionobject.getContext().getContextId()+')',e);
			}
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
		}
		
		public boolean hasNext() {
			if (!first){
				nexto = pre;	
			}
			return nexto != null; 
		}
		
    	@OXThrowsMultiple(
    			category={ 	
    								Category.CODE_ERROR,				
    								Category.CODE_ERROR
    								},
    			desc={"56","57"},
    			exceptionId={56,57},
    			msg={	"Unable to get next Object. Context %1$d User %2$d",
    							"Unable to get next Object. Context %1$d User %2$d"
    						}
    	)
		public Object next() throws OXException, SearchIteratorException {
		    try {
				if (rs.next()) {
					try{
						if (securecheck){
							pre = convertResultSet2ContactObject(rs, cols, true,  readcon);
						}else{
							pre = convertResultSet2ContactObject(rs, cols, false,  readcon);
						}
					}catch (OXException e){
						throw EXCEPTIONS.create(56,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
						//throw new OXException("ERROR DURING RIGHTS CHECK IN SEARCHITERATOR NEXT (cid="+sessionobject.getContext().getContextId()+')', e);
					}
				} else {
					pre = null;
			    }
		    	if (first) {
		    		first = false;
		    	}

		    	return nexto;
		    } catch (SQLException exc) {
				throw EXCEPTIONS.create(57,exc,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		    } catch (OXException exc) {
		    	throw exc;
		    	//throw new SearchIteratorException("ERROR OCCURRED ON NEXT (cid="+sessionobject.getContext().getContextId()+')',exc);
		    }
		}
		
		public int size() {
			throw new UnsupportedOperationException("Mehtod size() not implemented");
		}
		
		public boolean hasSize() {
			return false;
		}
	}

}