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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.ContactEventInterface;
import com.openexchange.event.impl.InfostoreEventInterface;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;


/**
 Links
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 
 */
	
	
public class LinksEventHandler  implements AppointmentEventInterface, TaskEventInterface, ContactEventInterface, InfostoreEventInterface{
	
	private static final Log LOG = LogFactory.getLog(LinksEventHandler.class);
	
	public LinksEventHandler (){	}

	public void appointmentCreated(final AppointmentObject appointmentObj, final Session sessionObj) {
		//  nix
	}

	public void appointmentModified(final AppointmentObject appointmentObj, final Session sessionObj) {
		updateLink(appointmentObj.getObjectID(),Types.APPOINTMENT,appointmentObj.getParentFolderID(),sessionObj);
	}

	public void appointmentDeleted(final AppointmentObject appointmentObj, final Session sessionObj) {
		deleteLink(appointmentObj.getObjectID(),Types.APPOINTMENT,appointmentObj.getParentFolderID(),sessionObj);
	}

	public void taskCreated(final Task taskObj, final Session sessionObj) {
		// nix
	}

	public void taskModified(final Task taskObj, final Session sessionObj) {
		updateLink(taskObj.getObjectID(),Types.TASK,taskObj.getParentFolderID(),sessionObj);
	}

	public void taskDeleted(final Task taskObj, final Session sessionObj) {
		deleteLink(taskObj.getObjectID(),Types.TASK,taskObj.getParentFolderID(),sessionObj);
	}

	public void contactCreated(final ContactObject contactObj, final Session sessionObj) {
		// nix
	}

	public void contactModified(final ContactObject contactObj, final Session sessionObj) {
		updateLink(contactObj.getObjectID(),Types.CONTACT,contactObj.getParentFolderID(),sessionObj);
	}

	public void contactDeleted(final ContactObject contactObj, final Session sessionObj) {
		deleteLink(contactObj.getObjectID(),Types.CONTACT,contactObj.getParentFolderID(),sessionObj);
	}

	public void infoitemCreated(final DocumentMetadata metadata, final Session sessionObject) {
		// nix
	}

	public void infoitemModified(final DocumentMetadata metadata, final Session sessionObject) {
		// BOESE TODO
		final int x = Integer.parseInt(metadata.getFolderId()+"");
		updateLink(metadata.getId(),Types.INFOSTORE,x,sessionObject);
	}

	public void infoitemDeleted(final DocumentMetadata metadata, final Session sessionObject) {
		// BOESE TODO
		final int x = Integer.parseInt(metadata.getFolderId()+"");
		deleteLink(metadata.getId(),Types.INFOSTORE,x,sessionObject);
	}
	
	public void deleteLink(final int id, final int type, final int fid, final Session so){
		Connection writecon = null;
		Statement del = null;
		
		Context ct = null;
		try {
			ct = ContextStorage.getStorageContext(so.getContextId());
		} catch (ContextException e) {
			//
		}
		
		try{
			writecon = DBPool.pickupWriteable(ct);
			writecon.setAutoCommit(false);	
			
			del = writecon.createStatement();
			del.execute("DELETE from prg_links WHERE (firstid = "+id+" AND firstmodule = "+type+" AND firstfolder = "+fid+") OR (secondid = "+id+" AND secondmodule = "+type+" AND secondfolder = "+fid+") AND cid = "+so.getContextId());
			
			writecon.commit();
		} catch (Exception se) {
			try {
				writecon.rollback();
			} catch (SQLException see){
				LOG.error("Uable to rollback Link Delete", see);
			}
			LOG.error("ERROR: Unable to Delete Links from Object! cid="+so.getContextId()+" oid="+id+" fid="+fid,se);
		} finally {
			try{
				del.close();
			}catch (SQLException sq){
				LOG.error("UNABLE TO CLOSE STATEMENT ",sq);
			}
			try {
				writecon.setAutoCommit(true);
			} catch (SQLException see){
				LOG.error("Uable to close Writeconnection", see);
			}
			if (writecon != null) {
				DBPool.closeWriterSilent(ct, writecon);
			}
		}		
			
	}
	
	public void updateLink(final int id, final int type, final int fid, final Session so){
		Connection readcon = null;
		Statement smt = null;
		ResultSet rs = null;
		boolean updater = false;
		
		Context ct = null;
		try {
			ct = ContextStorage.getStorageContext(so.getContextId());
		} catch (ContextException e) {
			//
		}
		
		try{
			readcon = DBPool.pickup(ct);
			smt = readcon.createStatement();
			rs = smt.executeQuery("SELECT firstid, firstfolder, secondid, secondfolder FROM prg_links WHERE ((firstid = "+id+" AND firstmodule = "+type+") OR (secondid = "+id+" AND secondmodule = "+type+")) AND cid = "+so.getContextId()+" LIMIT 1");

			if (rs.next()){
				int tp = rs.getInt(1);
				int fp = rs.getInt(2);
				if (tp != id){
					tp = rs.getInt(3);
					fp = rs.getInt(4);
				}
				if (fid !=  fp) {
					updater = true;
				}
			}		
		} catch (Exception e){
			LOG.error("UNABLE TO LOAD LINK OBJECT FOR UPDATE (cid="+so.getContextId()+" uid="+id+" type="+type+" fid="+fid+')', e);
		}  finally {
			try{
				if (rs != null) {
					rs.close();
				}
				if (smt != null) {
					smt.close();
				}
			} catch (SQLException see){
				LOG.warn("Unable to close Statement", see);
			}
			if (readcon != null) {
				DBPool.closeReaderSilent(ct, readcon);
			}
		}
		
		Connection writecon = null;
		Statement upd = null;
		
		if (updater){
			try{
				writecon = DBPool.pickupWriteable(ct);
				writecon.setAutoCommit(false);	
				
				upd = writecon.createStatement();
				upd.execute("UPDATE prg_links SET firstfolder = "+fid+" WHERE firstid = "+id+" AND firstmodule = "+type+" AND cid = "+so.getContextId());
				upd.execute("UPDATE prg_links SET secondfolder = "+fid+" WHERE secondid = "+id+" AND secondmodule = "+type+" AND cid = "+so.getContextId());
				
				writecon.commit();
			} catch (Exception se) {
				try {
					writecon.rollback();
				} catch (SQLException see){
					LOG.error("Uable to rollback Link Update", see);
				}
				LOG.error("ERROR: Unable to Update Links for Object! cid="+so.getContextId()+" oid="+id+" fid="+fid,se);
			} finally {
				try{
					upd.close();
				}catch (SQLException sq){
					LOG.error("UNABLE TO CLOSE STATEMENT ",sq);
				}
				try {
					writecon.setAutoCommit(true);
				} catch (SQLException see){
					LOG.error("Uable to close Writeconnection", see);
				}
				if (writecon != null) {
					DBPool.closeWriterSilent(ct, writecon);
				}
			}
		}
	}
	
}
