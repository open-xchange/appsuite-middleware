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


	
package com.openexchange.groupware.links;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;

/**
 Links
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 
 */
	
@OXExceptionSource(
		classId=1,
		component=Component.LINKING
)
public class Links {

	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(Links.class);
	
	private static final Log LOG = LogFactory.getLog(Links.class);
	
	public static modules[] module;	
	
	public static interface modules {
		boolean isReadable(int oid, int folder, int user, int[] group, SessionObject so);
		boolean hasModuleRights(SessionObject so);
	}
	
	/*
	 *  Some Modules are Deprecated but you never know what comes
	 */
	
	static {
		module = new modules[138];
		
		module[Types.APPOINTMENT] = new modules() {
			public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final SessionObject so) {
				if (!so.getUserConfiguration().hasCalendar()){
					return false;
				}
				try{
					return CalendarCommonCollection.getReadPermission(oid,fid,so);
				}catch (OXException ox){
					return false;
				}
			}
			public boolean hasModuleRights(final SessionObject so){
				if (!so.getUserConfiguration().hasCalendar()){
					return false;
				}
				return true;
			}
		};
		module[Types.TASK] = new modules() {
			public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final SessionObject so) {
				if (!so.getUserConfiguration().hasTask()){
					return false;
				}
				return com.openexchange.groupware.tasks.Task2Links.checkMayReadTask(so, oid, fid);
			}
			public boolean hasModuleRights(final SessionObject so){
				if (!so.getUserConfiguration().hasTask()){
					return false;
				}
				return true;
			}
		};
		module[Types.CONTACT] = new modules() {
			public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final SessionObject so) {
				if (!so.getUserConfiguration().hasContact()){
					return false;
				}
				try{
					return Contacts.performContactReadCheckByID(oid, user, group, so.getContext(), so.getUserConfiguration());
				} catch (Exception e) {
					//System.out.println("UNABLE TO CHECK CONTACT READRIGHT FOR LINK");
					LOG.error("UNABLE TO CHECK CONTACT READRIGHT FOR LINK",e);
					return false;
				}
			}
			public boolean hasModuleRights(final SessionObject so){
				if (!so.getUserConfiguration().hasContact()){
					return false;
				}
				return true;
			}
		};
		module[Types.INFOSTORE] = new modules() {
			public boolean isReadable(final int oid, final int fid, final int user, final int[] group, final SessionObject so) {				
				final InfostoreFacade DATABASE = new InfostoreFacadeImpl(new DBPoolProvider());
				try {
					return  DATABASE.exists(oid,InfostoreFacade.CURRENT_VERSION, so.getContext(), so.getUserObject(), so.getUserConfiguration());
				} catch (OXException e) {
					return false;
				}
			}
			public boolean hasModuleRights(final SessionObject so){
				if (!so.getUserConfiguration().hasInfostore()){
					return false;
				}
				return true;
			}
		};
		/*
		module[Types.PROJECT] = new modules() {
			public boolean isReadable(int oid, int fid, int user, int[] group, SessionObject so) {
				return false;
			}
			public boolean hasModuleRights(SessionObject so){
				if (!so.getUserConfiguration().hasProject()){
					return false;
				}else{
					return true;
				}
			}
		};
		module[Types.FORUM] = new modules() {
			public boolean isReadable(int oid, int fid, int user, int[] group, SessionObject so) {
				if (!so.getUserConfiguration().hasForum()){
					return false;
				}else{
					return true;
				}
			}
			public boolean hasModuleRights(SessionObject so){
				if (!so.getUserConfiguration().hasForum()){
					return false;
				}else{
					return true;
				}
			}
		};
		module[Types.PINBOARD] = new modules() {
			public boolean isReadable(int oid, int fid, int user, int[] group, SessionObject so) {
				if (!so.getUserConfiguration().hasPinboardWriteAccess()){
					return false;
				}else{
					return true;
				}
			}
			public boolean hasModuleRights(SessionObject so){
				if (!so.getUserConfiguration().hasPinboardWriteAccess()){
					return false;
				}else{
					return true;
				}
			}
		};
		module[Types.EMAIL] = new modules() {
			public boolean isReadable(int oid, int fid, int user, int[] group, SessionObject so) {
				if (!so.getUserConfiguration().hasWebMail()){
					return false;
				}else{
					return true;
				}
			}
			public boolean hasModuleRights(SessionObject so){
				if (!so.getUserConfiguration().hasWebMail()){
					return false;
				}else{
					return true;
				}
			}
		};
		*/
	}
	
	public Links (){	}
	
	@OXThrowsMultiple(
			category={
					Category.PERMISSION,
					Category.USER_INPUT,
					Category.CODE_ERROR,
					Category.CODE_ERROR,
					Category.CODE_ERROR
				},
			desc={"", "","","",""},
			exceptionId={0,1,2,3,4},
			msg={	
					"Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
					"Unable to create a link between these two objects. This link already exists. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
					ContactException.INIT_CONNECTION_FROM_DBPOOL,
					"An error occurred. Unable to save this linking between those two objects. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
					"An error occurred. Unable to save this linking between those two objects. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d"
					}
	)
	public static void performLinkStorage(final LinkObject l, final int user, final int[] group, final SessionObject so, final Connection writecon) throws OXException{
		
		if (!module[l.getFirstType()].isReadable(l.getFirstId(),l.getFirstFolder(),user,group,so) || !module[l.getSecondType()].isReadable(l.getSecondId(),l.getSecondFolder(), user, group, so)){
			throw EXCEPTIONS.create(0,l.getFirstId(),l.getFirstFolder(),l.getSecondId(),l.getSecondFolder(),so.getContext().getContextId());
			//throw new OXException("THIS LINK IS NOT VISIBLE TO THE USER. MISSING READRIGHTS FOR ONE OR BOTH OBJECTS");
		}
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection readCon = null;
		final LinksSql lms = new LinksMySql();
		try {
			readCon = DBPool.pickup(so.getContext());
			stmt = readCon.createStatement();	
			rs = stmt.executeQuery(lms.iFperformLinkStorage(l,so.getContext().getContextId()));

			if (rs.next()){
				throw EXCEPTIONS.create(1,l.getFirstId(),l.getFirstFolder(),l.getSecondId(),l.getSecondFolder(),so.getContext().getContextId());
				//throw new OXException("This Link allready exists");
			}
		} catch (DBPoolingException se) {
			throw EXCEPTIONS.create(2,se);
		} catch (SQLException se) {
			throw EXCEPTIONS.create(3,l.getFirstId(),l.getFirstFolder(),l.getSecondId(),l.getSecondFolder(),so.getContext().getContextId());
		} catch (OXException se) {
			throw se;
			//throw new OXException("UNABLE TO SAVE LINK",se);
		} finally {
			try{
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement or ResultSet",sqle);
			}
			if (readCon != null) {
				DBPool.closeReaderSilent(so.getContext(), readCon);
			}
		}
		
		PreparedStatement ps = null;
		try{
			ps = writecon.prepareStatement(lms.iFperformLinkStorageInsertString());
			ps.setInt(1,l.getFirstId());
			ps.setInt(2,l.getFirstType());
			ps.setInt(3,l.getFirstFolder());
			ps.setInt(4,l.getSecondId());
			ps.setInt(5,l.getSecondType());
			ps.setInt(6,l.getSecondFolder());
			ps.setInt(7,l.getContectId());
			ps.execute();
		} catch (SQLException se){
			throw EXCEPTIONS.create(4,se,l.getFirstId(),l.getFirstFolder(),l.getSecondId(),l.getSecondFolder(),so.getContext().getContextId());
			//throw new OXException("UNABLE TO SAVE LINK",se);
		} finally {
			try{
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException se) {
				LOG.error("Unable to close Statement",se);
			}
		}
	}
	
	@OXThrowsMultiple(
			category={
					Category.PERMISSION,
					Category.PERMISSION,
					Category.CODE_ERROR
				},
			desc={"", "",""},
			exceptionId={5,6,7},
			msg={	
					"Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d 2. Object %2$d Context %3$d",
					"Unable to create a link between these two objects. Insufficient rights. 1. Object %1$d Folder %2$d 2. Object %3$d Folder %4$d Context %5$d",
					"An error occurred. Unable to load some links for this objects. 1. Object %1$d 2. Object %2$d Context %3$d"
					}
	)
	public static LinkObject getLinkFromObject(final int first_id, final int first_type, final int second_id, final int second_type, final int user, final int[] group, final SessionObject so, final Connection readcon) throws OXException {

		if (!module[first_type].hasModuleRights(so) || !module[second_type].hasModuleRights(so)){
			throw EXCEPTIONS.create(5,first_id,second_id,so.getContext().getContextId());
			//throw new OXException("ONE OF THE REQUESTED MODULES IS NOT VISIBLE TO THE USER, MAYBE BOTH.");
		}
		
		LinkObject lo = null;
		Statement stmt = null;
		ResultSet rs = null;
		final LinksSql lms = new LinksMySql();
		try{
			stmt = readcon.createStatement();
			rs = stmt.executeQuery(lms.iFgetLinkFromObject(first_id,first_type,second_id,second_type,so.getContext().getContextId()));

			if (rs.next()){
				lo = new LinkObject(rs.getInt(1),
						rs.getInt(2),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getInt(7));
				if (!module[lo.getFirstType()].isReadable(lo.getFirstId(),lo.getFirstFolder(),user,group,so) || !module[lo.getSecondType()].isReadable(lo.getSecondId(), lo.getSecondFolder(), user, group, so)){
					throw EXCEPTIONS.create(6,lo.getFirstId(),lo.getFirstFolder(),lo.getSecondId(),lo.getSecondFolder(),so.getContext().getContextId());
					//throw new OXException("THIS LINK IS NOT VISIBLE TO THE USER. MISSING READRIGHTS FOR ONE OR BOTH OBJECTS");
				}
			}
		} catch (SQLException sql){
			throw EXCEPTIONS.create(7,sql,first_id,second_id,so.getContext().getContextId());
			//throw new OXException("UNABLE TO LOAD LINKOBJECT ",sql);
		} finally {
			try{
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement or ResultSet",sqle);
			}
		}
		return lo;
	}
	
	@OXThrows(
			category=Category.CODE_ERROR,
			desc="",
			exceptionId=9,
			msg="Unable to load all links from this objects. Object %1$d Folder %2$d User %3$d Context %4$d"
	)
	public static LinkObject[] getAllLinksFromObject(final int id, final int type, final int folder, final int user, final int[] group, final SessionObject so, final Connection readcon) throws OXException {
		LinkObject[] los = null;
		Statement stmt = null;
		ResultSet rs = null;
		final LinksSql lms = new LinksMySql();
		try{
			stmt = readcon.createStatement();
			rs = stmt.executeQuery(lms.iFgetAllLinksFromObject(id,type,folder,so.getContext().getContextId()));
			
			if (rs.next()){				
				rs.last();
				final int size = rs.getRow();
			    rs.beforeFirst();

			    int cnt = 0;
			    los = new LinkObject[size];
			    
			    while(rs.next()){
			    	LinkObject	lo = new LinkObject(rs.getInt(1),
							rs.getInt(2),
							rs.getInt(3),
							rs.getInt(4),
							rs.getInt(5),
							rs.getInt(6),
							rs.getInt(7));

					if (module[lo.getFirstType()].isReadable(lo.getFirstId(),lo.getFirstFolder(),user,group,so) && module[lo.getSecondType()].isReadable(lo.getSecondId(),lo.getSecondFolder(), user, group, so)){
						los[cnt] = lo;
				    	cnt++;
					} else {
						lo = null;
					}
			    }
			} 
		} catch (SQLException sql){
			throw EXCEPTIONS.create(9,sql,id,folder,user,so.getContext().getContextId());
			//throw new OXException("UNABLE TO LOAD LINKOBJECT ",sql);
		} finally {
			try{
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement or ResultSet",sqle);
			}
		}			
		return los;
	}
	
	@OXThrowsMultiple(
			category={
					Category.PERMISSION,
					Category.CODE_ERROR
				},
			desc={"", ""},
			exceptionId={10,11},
			msg={	
					"Unable to create a link between these two objects. Insufficient rights. Object %1$d Folder %2$d Context %3$d",
					"An error occurred. Unable to delete some links from this objects. Object %1$d Folder %2$d Context %3$d"
					}
	)
	public static int[][] deleteLinkFromObject(final int id, final int type, final int folder, final int[][] data, final int user, final int[] group, final SessionObject so, final Connection readcon, final Connection writecon) throws OXException {
		Statement smt = null;
		Statement del = null;
		ResultSet rs = null;
		final LinksSql lms = new LinksMySql();
		final List<int[]> resp = new ArrayList<int[]>();
		
        if (LOG.isDebugEnabled()) {
        	LOG.debug(new StringBuilder("Fetching rights for Module: "+type+" id:"+id+" folder:"+folder+" user:"+user+" group:"+group));
        }
		
		if (!module[type].isReadable(id,folder,user,group,so)){		
			//System.out.println("Unable to delete Link");
            for (int[] tmp : data) {
                resp.add(tmp);
            }
		}
		
		try{
			del = writecon.createStatement();
			smt = readcon.createStatement();
			rs = smt.executeQuery(lms.iFgetAllLinksFromObject(id,type,folder,so.getContext().getContextId()));
            int cnt = 0;
			while (rs.next()){
				int loadid = rs.getInt(1);
				int loadtype = rs.getInt(2);
				int loadfolder= rs.getInt(3);
				boolean second = false;
				
				if (loadid == id){
					loadid = rs.getInt(4);
					loadtype = rs.getInt(5);
					loadfolder= rs.getInt(6);
					second = true;
				}
				
				for (int i = 0; i< data.length;i++) {
					if ( (data[i][0] == loadid) && (data[i][1] == loadtype) && (data[i][2] == loadfolder) ){
						try{
							if (!module[loadtype].isReadable(loadid,loadfolder,user,group,so)){	
								throw EXCEPTIONS.create(10,loadid,loadfolder,so.getContext().getContextId());
								//throw new OXException("NO RIGHT");
							}
							lms.iFDeleteLinkFromObject(del,second,id,type,folder,loadid,loadfolder,loadtype,so.getContext().getContextId());
                            cnt++;
						} catch (OXException ox){
							LOG.error("Unable to delete Link!",ox);
                            resp.add(new int[] {loadid, loadtype, loadfolder});
						}
					}
				}
			}
            if (cnt == 0) {
                for (int[] tmp : data) {
                    resp.add(tmp);
                }
            }
		} catch (SQLException se){
			throw EXCEPTIONS.create(11,se,id,folder,so.getContext().getContextId());
			//throw new OXException("UNABLE TO DELETE LINKS",se);
		} finally {
			try{
				if (rs != null) {
					rs.close();
				}
				if (smt != null) {
					smt.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement or ResultSet",sqle);
			}
			try{
				if (del != null) {
					del.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement",sqle);
			}
		}	
		
		return resp.toArray(new int[resp.size()][]);
	}
	
	@OXThrows(
			category=Category.CODE_ERROR,
			desc="",
			exceptionId=12,
			msg="Unable to delete all links from this objects. Object %1$d Context %2$d"
	)
	public static void deleteAllObjectLinks(final int id, final int type, final int cid, final Connection writecon) throws OXException {
		//TODO RIGHTS CHECK on onject id and fid!
		/*
		 *  this right check is realy not requiered because this method only comes up when we delete an object and all its links. 
		 *  and at this point, all rights are already checked
		 */
		final LinksSql lms = new LinksMySql();
		PreparedStatement ps = null;
		try {
			ps = writecon.prepareStatement(lms.iFdeleteAllObjectLinks());
			ps.setInt(1, id);
			ps.setInt(2, type);
			ps.setInt(3, id);
			ps.setInt(4, type);
			ps.setInt(5, cid);
			ps.execute();
		} catch (SQLException se){
			throw EXCEPTIONS.create(12,se,id,cid);
			//throw new OXException("UNABLE TO DELETE LINKS FROM OBJECT "+id, se);
		} finally {
			try{
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle){
				LOG.error("Unable to close Statement",sqle);
			}
		}
	}

	
}
