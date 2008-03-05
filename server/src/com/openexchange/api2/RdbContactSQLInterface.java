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
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.PrefetchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_API2_DATABASEIMPL_RDBCONTACTSQLINTERFACE,
		component=Component.CONTACT
	)
	
public class RdbContactSQLInterface implements ContactSQLInterface, ContactInterface {

	private static final String ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D = "Unable to load objects. Context %1$d User %2$d";
	private final int userId;
	private final int[] memberInGroups;
	private final Context ctx;
	private final Session session;
	private final UserConfiguration userConfiguration;

	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(RdbContactSQLInterface.class);
	
	private static final Log LOG = LogFactory.getLog(RdbContactSQLInterface.class);
	
	public RdbContactSQLInterface(final Session session) throws ContextException {
		Context ctx = ContextStorage.getStorageContext(session);
		this.ctx = ctx;
		this.userId = session.getUserId();
		this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
		this.session = session;
		userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
				ctx);
	}
	
	public RdbContactSQLInterface(final Session session, final Context ctx) {
		this.userId = session.getUserId();
		this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
		this.ctx = ctx;
		this.session = session;
		userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
				ctx);
	}
	

	@OXThrows(
			category=Category.CODE_ERROR,
			desc="0",
			exceptionId=0,
			msg= ContactException.EVENT_QUEUE
	)
	public void insertContactObject(final ContactObject co) throws OXException {
		try{
			Contacts.performContactStorageInsert(co,userId,memberInGroups,session);
			final EventClient ec = new EventClient(session);
			ec.create(co);
			/*
			ContactObject coo = new ContactObject();
			coo.setSurName("Hoeger");
			ContactSQLInterface csql = new RdbContactSQLInterface(sessionobject);
			csql.insertContactObject(coo);
			*/
		}catch (EventException ise){
			throw EXCEPTIONS.create(0,ise);
		}catch (ContextException ise){
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
			Contacts.performContactStorageUpdate(co,fid,d,userId,memberInGroups,ctx,userConfiguration);
			final EventClient ec = new EventClient(session);
			ec.modify(co);
		}catch (ContactException ise){
			throw ise;
		}catch (EventException ise){
			throw EXCEPTIONS.create(1,ise);
		}catch (ContextException ise){
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
			readCon = DBPool.pickup(ctx);
			
			final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(2,Integer.valueOf(folderId),Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
			
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(ctx, readCon);
			}
			throw e;
			//throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(3,e);
		}	
			
		try {
			final ContactSql contactSQL = new ContactMySql(session, ctx);
			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
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
				DBPool.closeReaderSilent(ctx, readCon);
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
	public SearchIterator<?> getContactsInFolder(final int folderId, final int from, final int to, final int order_field, final String orderMechanism, final int[] cols) throws OXException {
		boolean error = false;
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
			readCon = DBPool.pickup(ctx);
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(7,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
		
		try {
			final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(8,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder!  (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(ctx, readCon);
			}
			throw e;
			//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}
		
		SearchIterator<?> si = null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			final ContactSql cs = new ContactMySql(session, ctx);
			cs.setFolder(folderId);

			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
			
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
			
			stmt = readCon.createStatement();

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
			error = true;
			throw EXCEPTIONS.create(11,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			error = true;
			throw EXCEPTIONS.create(12,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			error = true;
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),	e);
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
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
	public SearchIterator<?> getContactsByExtendedSearch(final ContactSearchObject searchobject,  final int order_field, final String orderMechanism, final int[] cols) throws OXException {
		boolean error = false;
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
			readcon = DBPool.pickup(ctx);
		} catch (DBPoolingException e) {
			throw EXCEPTIONS.create(13,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
	
		final int folderId = searchobject.getFolder();
		
		if (!(searchobject.isAllFolders() || searchobject.getEmailAutoComplete())){
			try {
				final FolderObject contactFolder = new OXFolderAccess(readcon, ctx).getFolderObject(folderId);
				if (contactFolder.getModule() != FolderObject.CONTACT) {
					throw EXCEPTIONS.createOXConflictException(14,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
				}
			} catch (OXException e) {
				if (readcon != null) {
					DBPool.closeReaderSilent(ctx, readcon);
				}
				throw e;
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		}
		
		SearchIterator si = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			final ContactSql cs = new ContactMySql(session, ctx);
			
			if (!(searchobject.isAllFolders() || searchobject.getEmailAutoComplete())){	
				cs.setFolder(folderId);

				final EffectivePermission oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(folderId, userId, userConfiguration);
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
			}else if (searchobject.isAllFolders()){
				searchobject.setAllFolderSQLINString(cs.buildAllFolderSearchString(userId,memberInGroups,session,readcon).toString());
			} else if (searchobject.getEmailAutoComplete()){
				final OXFolderAccess oxfs = new OXFolderAccess(readcon, ctx);
				FolderObject user_private_folder = oxfs.getDefaultFolder(userId, FolderObject.CONTACT);
				searchobject.setEmailAutoCompleteFolder(user_private_folder.getObjectID());
			}
			
			final String order = " ORDER BY co." + Contacts.mapping[orderBy].getDBFieldName() + ' ' + orderDir +  ' ';
			cs.setOrder(order);
			
			cs.setContactSearchObject(searchobject);
			
			stmt = readcon.createStatement();
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (DBPoolingException e){
			error = true;
			throw EXCEPTIONS.create(19,e);
		} catch (SearchIteratorException e){
			error = true;
			throw EXCEPTIONS.create(17,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			error = true;
			throw EXCEPTIONS.create(18,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			error = true;
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId	+ " in folder " + folderId + " cid="+sessionobject.getContext().getContextId()+ "\n:" + e.getMessage(),	e);
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readcon != null) {
						DBPool.closeReaderSilent(ctx, readcon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
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
		boolean error = false;
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
			readcon = DBPool.pickup(ctx);
		} catch (DBPoolingException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(ctx, readcon);
			}
			throw EXCEPTIONS.create(20,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
				
		try {
			final FolderObject contactFolder = new OXFolderAccess(readcon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(21,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readcon != null) {
				DBPool.closeReaderSilent(ctx, readcon);
			}
			throw e;
			//throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}	

		SearchIterator si = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			final ContactSql cs = new ContactMySql(session, ctx);
			cs.setFolder(folderId);
			cs.setSearchHabit(" OR ");
			final String order = " ORDER BY co." + Contacts.mapping[orderBy].getDBFieldName() + ' ' + orderDir +  ' ';
			cs.setOrder(order);
			
			final EffectivePermission oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(folderId, userId, userConfiguration);
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
				cso.setEmail1(searchpattern);
				cso.setEmail2(searchpattern);
				cso.setEmail3(searchpattern);
			}
			
			cs.setContactSearchObject(cso);
			
			stmt = readcon.createStatement();
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (SearchIteratorException e){
			error = true;
			throw EXCEPTIONS.create(24,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e) {
			error = true;
			throw EXCEPTIONS.create(25,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			error = true;
			throw e;
			//throw new OXException("Exception during getContactsInFolder() for User " + userId	+ " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),	e);
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readcon != null) {
						DBPool.closeReaderSilent(ctx, readcon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
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
			readCon = DBPool.pickup(ctx);
			if (objectId > 0){
				co = Contacts.getContactById(objectId, userId, memberInGroups, ctx, userConfiguration, readCon);							
			}else{
				throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(objectId));
				//throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}

			final int folderId = co.getParentFolderID();

			final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}
		
			if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,session, readCon, ctx)){
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
				DBPool.closeReaderSilent(ctx, readCon);
			}			
		}
		return co;
	}
	
	public ContactObject getUserById(final int userid) throws OXException {

		Connection readCon = null;	
		ContactObject co = null;
		int fid = FolderObject.SYSTEM_LDAP_FOLDER_ID;
		try{			
			readCon = DBPool.pickup(ctx);
			if (userid > 0){
				co = Contacts.getUserById(userid, userId, memberInGroups, ctx, userConfiguration, readCon);							
			}else{
				throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(userid));
				//throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}

			final int folderId = co.getParentFolderID();

			final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
			}
		
			if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,session, readCon, ctx)){
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
				DBPool.closeReaderSilent(ctx, readCon);
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
		boolean error = false;
		Connection readCon = null;	
		try{
			readCon = DBPool.pickup(ctx);
		} catch (Exception e) {
			throw EXCEPTIONS.create(30,e);
			//throw new OXException("UNABLE TO GET READ CONNECTION", e);
		}
		
		try {
			final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(31,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
			}
		} catch (OXException e) {
			if (readCon != null) {
				DBPool.closeReaderSilent(ctx, readCon);
			}
			throw e;
			//throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
		}
		
		SearchIterator si = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			final ContactSql cs = new ContactMySql(session, ctx);

			final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
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
			
			stmt = readCon.createStatement();

			cs.getAllChangedSince(since.getTime());
			final String select = cs.iFgetColsString(cols).toString();
			cs.setSelect(select);
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
		} catch (SearchIteratorException e){
			error = true;
			throw EXCEPTIONS.create(34,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (SQLException e){
			error = true;
			throw EXCEPTIONS.create(35,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (OXException e) {
			error = true;
			throw e;
			//throw new OXException(	"Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',	e);
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
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
		boolean error = false;
		SearchIterator si = null;
		Connection readcon = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			readcon = DBPool.pickup(ctx);
			
			final ContactSql cs = new ContactMySql(session, ctx);
			cs.setFolder(folderId);

			stmt = readcon.createStatement();

			cs.getAllChangedSince(since.getTime());
			final String select = cs.iFgetColsStringFromDeleteTable(cols).toString();
			cs.setSelect(select);
			cs.setOrder(" ORDER BY co.field02 ");
			rs = stmt.executeQuery(cs.getSqlCommand());
			si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
		} catch (SearchIteratorException e) {
			error = true;
			throw EXCEPTIONS.create(37,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
		} catch (DBPoolingException e) {
			error = true;
			throw EXCEPTIONS.create(36,e);
		} catch (SQLException e) {
			error = true;
			throw EXCEPTIONS.create(38,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
			//throw new OXException("Exception during getDeletedContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',	e);
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readcon != null) {
						DBPool.closeReaderSilent(ctx, readcon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
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
			readcon = DBPool.pickup(ctx);
			
			int fid = 0;
			boolean pflag = false;
			Date changing_date = null;
			final ContactSql cs = new ContactMySql(session, ctx);
			smt = readcon.createStatement();
			rs = smt.executeQuery(cs.iFdeleteContactObject(oid,ctx.getContextId()));
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
			final FolderObject contactFolder = new OXFolderAccess(readcon, ctx).getFolderObject(fid);
			if (contactFolder.getModule() != FolderObject.CONTACT) {
				throw EXCEPTIONS.createOXConflictException(41,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXException("deleteContactObject called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
			}
			
			if ((contactFolder.getType() != FolderObject.PRIVATE) && pflag){
				LOG.debug(new StringBuilder("Here is a contact in a non PRIVATE folder with a set private flag -> (cid="+ctx.getContextId()+" fid="+fid+" oid="+oid+')'));
			} else if ((contactFolder.getType() == FolderObject.PRIVATE) && pflag && created_from != userId){
				throw EXCEPTIONS.createOXPermissionException(42,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
				//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS CONTACT CUZ IT IS PRIVATE (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
			}
			
			oclPerm = new OXFolderAccess(readcon, ctx).getFolderPermission(fid, userId, userConfiguration);
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
			throw EXCEPTIONS.create(44,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
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
					DBPool.closeReaderSilent(ctx, readcon);
				}
			} catch (Exception ex){
				LOG.error("Unable to return Connection",ex);
			}
		}

		try{
			writecon = DBPool.pickupWriteable(ctx);
			
			if (oclPerm.canDeleteAllObjects()) {
				Contacts.deleteContact(oid, ctx.getContextId(), writecon);	
			} else {
				if (oclPerm.canDeleteOwnObjects() && created_from == userId){
					Contacts.deleteContact(oid, ctx.getContextId(), writecon);	
				}else{
					throw EXCEPTIONS.createOXConflictException(46,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
					//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')');					
				}
			}
			final EventClient ec = new EventClient(session);
			ec.delete(co);
		} catch (EventException ise){
			throw EXCEPTIONS.create(56,ise);
		} catch (ContextException ise){
			throw EXCEPTIONS.create(56,ise);
		} catch (DBPoolingException xe){
			throw EXCEPTIONS.create(45,xe);
		} catch (OXException e){
			throw e;
			//throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);			
		} finally {
			if (writecon != null) {
				DBPool.closeWriterSilent(ctx, writecon);
			}
		}
	}
	
	@OXThrowsMultiple(
			category={ 	Category.SOCKET_CONNECTION,
									Category.CODE_ERROR,				
									Category.CODE_ERROR,
									Category.TRY_AGAIN
								},
			desc={"47","48","49","59"},
			exceptionId={47,48,49,59},
			msg={	ContactException.INIT_CONNECTION_FROM_DBPOOL,	
							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
							ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
							"The contact you requested is not valid."
						}
	)
	public SearchIterator getObjectsById(final int[][] object_id, final int[] cols) throws OXException {
		boolean error = false;
		Connection readcon = null;
		SearchIterator si = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			readcon = DBPool.pickup(ctx);
	
			final ContactSql contactSQL = new ContactMySql(session, ctx);
			contactSQL.setSelect(contactSQL.iFgetColsString(cols).toString());			
			contactSQL.setObjectArray(object_id);			
			
			stmt = readcon.createStatement();
			rs = stmt.executeQuery(contactSQL.getSqlCommand());
			
			if (object_id.length == 1 && !rs.first()){
				throw EXCEPTIONS.createOXObjectNotFoundException(59);
			} else {
				rs.beforeFirst();
			}
			
			si = new ContactObjectIterator(rs,stmt,cols,true,readcon);
		} catch (DBPoolingException e) {
			error = true;
			throw EXCEPTIONS.create(47,e);
		} catch (SearchIteratorException e) {
			error = true;
			throw EXCEPTIONS.create(48,e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		} catch (SQLException e) {
			error = true;
			throw EXCEPTIONS.create(49,e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
		} finally {
			if (error){
				try{
					if (rs != null){
						rs.close();
					}
					if (stmt != null){
						stmt.close();
					}
				} catch (SQLException sxe){
					LOG.error("Unable to close Statement or ResultSet",sxe);
				}
				try{
					if (readcon != null) {
						DBPool.closeReaderSilent(ctx, readcon);
					}
				} catch (Exception ex){
					LOG.error("Unable to return Connection",ex);
				}
			}
		}	
		return new PrefetchIterator(si);
	}
	
	public static boolean performSecurityReadCheck(final int fid, final int created_from, final int user, final int[] group, final Session so, final Connection readcon, final Context ctx) {
		return Contacts.performContactReadCheck(fid, created_from, user, group, ctx,
				UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx),
				readcon);		
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
			for (int a = 0; a < cols.length; a++) {
				Contacts.mapping[cols[a]].addToContactObject(rs, cnt, co, readCon, userId, memberInGroups, ctx,
						userConfiguration);
				cnt++;
			}

			if (!co.containsInternalUserId()){		
				co.setParentFolderID(rs.getInt(cols.length+1));
				if (check && !performSecurityReadCheck(co.getParentFolderID(), co.getCreatedBy(),userId,memberInGroups,session,readCon, ctx)){
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
				DBPool.closeReaderSilent(ctx, readcon);
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
	
	public int getFolderId() {
		return 0;
	}

}