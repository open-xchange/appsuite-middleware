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



package com.openexchange.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.DBPool;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 *
 * vcard
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public final class vcard extends PermissionServlet {
	
	private final static int[] _contactFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		ContactObject.GIVEN_NAME,
		ContactObject.SUR_NAME,
		ContactObject.ANNIVERSARY,
		ContactObject.ASSISTANT_NAME,
		ContactObject.BIRTHDAY,
		ContactObject.BRANCHES,
		ContactObject.BUSINESS_CATEGORY,
		ContactObject.CATEGORIES,
		ContactObject.CELLULAR_TELEPHONE1,
		ContactObject.CELLULAR_TELEPHONE2,
		ContactObject.CITY_BUSINESS,
		ContactObject.CITY_HOME,
		ContactObject.CITY_OTHER,
		ContactObject.COMMERCIAL_REGISTER,
		ContactObject.COMPANY,
		ContactObject.COUNTRY_BUSINESS,
		ContactObject.COUNTRY_HOME,
		ContactObject.COUNTRY_OTHER,
		ContactObject.DEPARTMENT,
		ContactObject.DISPLAY_NAME,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
		ContactObject.FILE_AS,
		ContactObject.FOLDER_ID,
		ContactObject.GIVEN_NAME,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
		ContactObject.MANAGER_NAME,
		ContactObject.MARITAL_STATUS,
		ContactObject.MIDDLE_NAME,
		ContactObject.NICKNAME,
		ContactObject.NOTE,
		ContactObject.NUMBER_OF_CHILDREN,
		ContactObject.NUMBER_OF_EMPLOYEE,
		ContactObject.POSITION,
		ContactObject.POSTAL_CODE_BUSINESS,
		ContactObject.POSTAL_CODE_HOME,
		ContactObject.POSTAL_CODE_OTHER,
		ContactObject.PRIVATE_FLAG,
		ContactObject.PROFESSION,
		ContactObject.ROOM_NUMBER,
		ContactObject.SALES_VOLUME,
		ContactObject.SPOUSE_NAME,
		ContactObject.STATE_BUSINESS,
		ContactObject.STATE_HOME,
		ContactObject.STATE_OTHER,
		ContactObject.STREET_BUSINESS,
		ContactObject.STREET_HOME,
		ContactObject.STREET_OTHER,
		ContactObject.SUFFIX,
		ContactObject.TAX_ID,
		ContactObject.TELEPHONE_ASSISTANT,
		ContactObject.TELEPHONE_BUSINESS1,
		ContactObject.TELEPHONE_BUSINESS2,
		ContactObject.TELEPHONE_CALLBACK,
		ContactObject.TELEPHONE_CAR,
		ContactObject.TELEPHONE_COMPANY,
		ContactObject.TELEPHONE_HOME1,
		ContactObject.TELEPHONE_HOME2,
		ContactObject.TELEPHONE_IP,
		ContactObject.TELEPHONE_ISDN,
		ContactObject.TELEPHONE_OTHER,
		ContactObject.TELEPHONE_PAGER,
		ContactObject.TELEPHONE_PRIMARY,
		ContactObject.TELEPHONE_RADIO,
		ContactObject.TELEPHONE_TELEX,
		ContactObject.TELEPHONE_TTYTDD,
		ContactObject.TITLE,
		ContactObject.URL,
		ContactObject.DEFAULT_ADDRESS
	};
	
	
	private static final String CONTACTFOLDER = "contactfolder";
	private static final String ENABLEDELETE = "enabledelete";
	
	private static String SQL_PRINCIPAL_SELECT = "SELECT object_id, contactfolder FROM vcard_principal WHERE cid = ? AND principal = ?";
	private static String SQL_PRINCIPAL_INSERT = "INSERT INTO vcard_principal (object_id, cid, principal, contactfolder) VALUES (?, ?, ?, ?)";
	private static String SQL_PRINCIPAL_UPDATE = "UPDATE vcard_principal SET contactfolder = ? WHERE object_id = ?";
	
	private static String SQL_ENTRIES_LOAD = "SELECT object_id, client_id, target_object_id FROM vcard_ids WHERE cid = ? AND principal_id = ?";
	private static String SQL_ENTRY_INSERT = "INSERT INTO vcard_ids (object_id, cid, principal_id, client_id, target_object_id) VALUES (?, ?, ?, ? ,?)";
	private static String SQL_ENTRY_DELETE = "DELETE FROM vcard_ids WHERE target_object_id = ? AND principal_id = ?";
	
	private static transient final Log LOG = LogFactory.getLog(vcard.class);
	
	public void oxinit() throws ServletException {
		
	}
	
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("GET");
		}
		
		final OutputStream os = resp.getOutputStream();
		
		String user_agent = null;
		String principal = null;
		
		final SessionObject sessionObj = getSession(req);
		
		final Context context = sessionObj.getContext();
		
		try {
			resp.setStatus(HttpServletResponse.SC_OK);
			
			// get user-agent
			user_agent = getUserAgent(req);
			
			principal = user_agent + '_' + sessionObj.getUserObject().getId();
			
			int contactfolder_id = getContactFolderID(req);
			
			if (contactfolder_id == 0) {
				contactfolder_id = OXFolderTools.getContactDefaultFolder(sessionObj.getUserObject().getId(), context);
			}
			
			int db_contactfolder_id = 0;
			
			int principal_id = 0;
			
			HashMap entries_db = new HashMap();
			final HashSet entries = new HashSet();
			
			Connection readCon = null;
			
			PreparedStatement principalStatement = null;
			
			ResultSet rs = null;
			
			boolean exists = false;
			try {
				readCon = DBPool.pickup(context);
				
				principalStatement = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
				principalStatement.setLong(1, context.getContextId());
				principalStatement.setString(2, principal);
				rs = principalStatement.executeQuery();
				
				exists = rs.next();
				
				if (exists) {
					principal_id = rs.getInt(1);
					db_contactfolder_id = rs.getInt(2);
					
					entries_db = loadDBEntries(context, principal_id);
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				
				if (principalStatement != null) {
					principalStatement.close();
				}
				
				if (readCon != null) {
					DBPool.push(context, readCon);
				}
			}
			
			final VersitDefinition def = Versit.getDefinition("text/vcard");
			final VersitDefinition.Writer w = def.getWriter(os, "UTF-8");
			final OXContainerConverter oxc = new OXContainerConverter(sessionObj);
			
			SearchIterator it = null;
			
			try {
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentType("text/vcard");
				
				final ContactSQLInterface  contactInterface = new RdbContactSQLInterface(sessionObj);
				it = contactInterface.getModifiedContactsInFolder(contactfolder_id, _contactFields, new Date(0));
				
				while (it.hasNext()) {
					final ContactObject contactObject = (ContactObject)it.next();
					
					final VersitObject vo = oxc.convertContact(contactObject, "3.0");
					def.write(w, vo);
					
					entries.add(String.valueOf(contactObject.getObjectID()));
				}
			} finally {
				w.flush();
				w.close();
				oxc.close();
				
				if (it != null) {
					it.close();
				}
			}
			
			Connection writeCon = null;
			
			PreparedStatement ps = null;
			try {
				writeCon = DBPool.pickupWriteable(context);
				if (exists) {
					if (!(db_contactfolder_id == contactfolder_id)) {
						ps = writeCon.prepareStatement(SQL_PRINCIPAL_UPDATE);
						ps.setInt(1, principal_id);
						ps.setInt(2, contactfolder_id);
						
						ps.executeUpdate();
						ps.close();
					}
				} else {
					writeCon.setAutoCommit(false);
					principal_id = IDGenerator.getId(context, Types.ICAL, writeCon);
					writeCon.commit();
					
					ps = writeCon.prepareStatement(SQL_PRINCIPAL_INSERT);
					ps.setInt(1, principal_id);
					ps.setLong(2, context.getContextId());
					ps.setString(3, principal);
					ps.setInt(4, contactfolder_id);
					
					ps.executeUpdate();
					ps.close();
				}
			} finally {
				if (ps != null) {
					ps.close();
				}
				
				if (writeCon != null) {
					writeCon.setAutoCommit(true);
					DBPool.pushWrite(context, writeCon);
				}
			}
			
			final Iterator iterator = entries.iterator();
			while (iterator.hasNext()) {
				final String s_object_id = iterator.next().toString();
				
				if (!entries_db.containsKey(s_object_id)) {
					addEntry(context, principal_id, Integer.parseInt(s_object_id), s_object_id);
				}
			}
			
			final Iterator databaseIterator = entries_db.keySet().iterator();
			while (iterator.hasNext()) {
				final String s_object_id = entries_db.get(databaseIterator.next().toString()).toString();
				
				if (!entries.contains(s_object_id)) {
					deleteEntry(context, principal_id, Integer.parseInt(s_object_id));
				}
			}
		} catch (OXConflictException exc) {
			LOG.debug("doGet", exc);
			doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
		} catch (Exception exc) {
			LOG.error("doGet", exc);
			doError(resp);
		}
	}
	
	public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("PUT");
		}
		
		String content_type = null;
		
		String client_id = null;
		
		final InputStream is = req.getInputStream();
		
		String user_agent = null;
		String principal = null;
		
		final SessionObject sessionObj = getSession(req);
		
		final Context context = sessionObj.getContext();
		
		try {
			resp.setStatus(HttpServletResponse.SC_OK);
			
			user_agent = getUserAgent(req);
			content_type = req.getContentType();
			
			log("read vcard content_type: " + content_type);
			
			if (content_type == null) {
				content_type = "text/vcard";
			}
			
			if (user_agent == null) {
				throw new OXConflictException("missing header field: user-agent");
			}
			
			principal = user_agent + '_' + sessionObj.getUserObject().getId();
			
			int contactfolder_id = getContactFolderID(req);
			
			if (contactfolder_id == 0) {
				contactfolder_id = OXFolderTools.getContactDefaultFolder(sessionObj.getUserObject().getId(), context);
			}
			
			if (contactfolder_id == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
				resp.setContentType("text/html");
				resp.setStatus(HttpServletResponse.SC_CONFLICT);
				
				final PrintWriter pw = resp.getWriter();
				pw.println("folder internal users is only readable!");
				pw.flush();
				
				return ;
			}
			
			HashMap entries_db = new HashMap();
			final HashSet entries = new HashSet();
			
			final boolean enabledelete = getEnableDelete(req);
			
			boolean exists = false;
			
			int principal_id = 0;
			
			Connection readCon = null;
			
			PreparedStatement principalStatement = null;
			
			ResultSet rs = null;
			try {
				readCon = DBPool.pickup(context);
				principalStatement = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
				principalStatement.setLong(1, context.getContextId());
				principalStatement.setString(2, principal);
				rs = principalStatement.executeQuery();
				
				int db_contactfolder_id = 0;
				
				exists = rs.next();
				
				if (exists) {
					principal_id = rs.getInt(1);
					db_contactfolder_id = rs.getInt(2);
					
					if (!(db_contactfolder_id == contactfolder_id)) {
						throw new OXConflictException("no principal found for the given folders: " + principal);
					}
					
					entries_db = loadDBEntries(context, principal_id);
				} else {
					throw new OXConflictException("no principal found: " + principal);
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				
				if (principalStatement != null) {
					principalStatement.close();
				}
				
				if (readCon != null) {
					DBPool.closeReaderSilent(context, readCon);
				}
			}
			
			final VersitDefinition def = Versit.getDefinition(content_type);
			
			final OXContainerConverter oxc = new OXContainerConverter(sessionObj);
			final ContactSQLInterface contactInterface = new RdbContactSQLInterface(sessionObj);
			
			final Date timestamp = new Date();
			try {
				final VersitDefinition.Reader r = def.getReader(is, "UTF-8");
				
				while (true) {
					final VersitObject vo = def.parse(r);
					if (vo == null) {
						break;
					}
					
					final Property property = vo.getProperty("UID");
					
					client_id = null;
					int object_id = 0;
					
					if (property != null) {
						client_id = property.getValue().toString();
					}
					
					final ContactObject contactObj = oxc.convertContact(vo);
					
					if (contactObj.getObjectID() == 0) {
						contactObj.setParentFolderID(contactfolder_id);
					}
					
					try {
						if (client_id != null && entries_db.containsKey(client_id)) {
							try {
								object_id = Integer.parseInt(entries_db.get(client_id).toString());
							} catch (NumberFormatException exc) {
								if (LOG.isDebugEnabled()) {
									LOG.debug("object id is not an integer");
								}
							}
							
							if (object_id > 0) {
								contactObj.setObjectID(object_id);
							}
							
							contactObj.setParentFolderID(contactfolder_id);
							
							if (contactObj.containsObjectID()) {
								contactInterface.updateContactObject(contactObj, contactfolder_id, timestamp);
							} else {
								contactInterface.insertContactObject(contactObj);
							}
							
							entries.add(client_id);
						} else {
							contactObj.setParentFolderID(contactfolder_id);
							
							if (contactObj.containsObjectID()) {
								contactInterface.updateContactObject(contactObj, contactfolder_id, timestamp);
							} else {
								contactInterface.insertContactObject(contactObj);
							}
							
							if (client_id != null) {
								entries.add(client_id);
								addEntry(context, principal_id, contactObj.getObjectID(), client_id);
							}
						}
						if (LOG.isDebugEnabled()) {
							LOG.debug("STATUS: OK");
						}
					} catch (OXObjectNotFoundException exc) {
						LOG.debug("object was already deleted on server: " + object_id, exc);
					}
				}
			} finally {
				oxc.close();
			}
			
			final Iterator it = entries_db.keySet().iterator();
			while (it.hasNext()) {
				final String tmp = it.next().toString();
				if (!entries.contains(tmp)) {
					int object_id = Integer.parseInt(entries_db.get(tmp).toString());
					
					deleteEntry(context, principal_id, object_id);
					
					if (enabledelete) {
						try {
							contactInterface.deleteContactObject(object_id, contactfolder_id, timestamp);
						} catch (OXObjectNotFoundException exc) {
							LOG.debug("object was already deleted on server: " + object_id, exc);
						}
					}
				}
			}
		} catch (OXConflictException exc) {
			LOG.debug("doPut", exc);
			doError( resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
		} catch (OXPermissionException exc) {
			LOG.debug("doPut", exc);
			doError(resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
		} catch (Exception exc) {
			LOG.error("doPut", exc);
			doError(resp);
		}
	}
	
	private void doError(final HttpServletResponse resp) throws ServletException {
		doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
	}
	
	private void doError(final HttpServletResponse resp, final int code, final String msg) throws ServletException {
		log("ERROR: " + code + ": "+ msg);
		resp.setStatus(code);
		resp.setContentType("text/html");
	}
	
	private String getUserAgent(final HttpServletRequest req) throws OXConflictException {
		final Enumeration e = req.getHeaderNames();
		while (e.hasMoreElements()) {
			final String tmp = e.nextElement().toString().toLowerCase();
			if ("user-agent".equals(tmp)) {
				return req.getHeader("user-agent");
			}
		}
		
		throw new OXConflictException("missing header field: user-agent");
	}
	
	private int getContactFolderID(final HttpServletRequest req) throws OXConflictException {
		if ( req.getParameter(CONTACTFOLDER) != null) {
			try {
				return Integer.parseInt(req.getParameter(CONTACTFOLDER));
			} catch (NumberFormatException exc) {
				throw new OXConflictException(CONTACTFOLDER + " is not a number");
			}
		}
		
		return 0;
	}
	
	private HashMap loadDBEntries(final Context context, final int principal_object_id) throws Exception {
		final HashMap entries_db = new HashMap();
		
		Connection readCon = null;
		
		PreparedStatement ps = null;
		
		ResultSet rs = null;
		
		try {
			readCon = DBPool.pickup(context);
			ps = readCon.prepareStatement(SQL_ENTRIES_LOAD);
			ps.setInt(1, principal_object_id);
			ps.setLong(2, context.getContextId());
			rs = ps.executeQuery();
			
			while (rs.next()) {
				entries_db.put(rs.getString(2), String.valueOf(rs.getInt(3)));
			}
			
			rs.close();
			ps.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			
			if (ps != null) {
				ps.close();
			}
			
			if (readCon != null) {
				DBPool.push(context, readCon);
			}
		}
		
		return entries_db;
	}
	
	private boolean getEnableDelete(final HttpServletRequest req) {
		if (( req.getParameter(ENABLEDELETE) != null) && (req.getParameter(ENABLEDELETE).toLowerCase().equals("yes"))) {
			return true;
		}
		
		return false;
	}
	
	private void addEntry(final Context context, final int principal_id, final int object_target_id, final String client_id) throws Exception {
		Connection writeCon = null;
		
		PreparedStatement ps = null;
		
		try {
			writeCon = DBPool.pickupWriteable(context);
			writeCon.setAutoCommit(false);
			final int objectId = IDGenerator.getId(context, Types.ICAL, writeCon);
			writeCon.commit();
			
			ps = writeCon.prepareStatement(SQL_ENTRY_INSERT);
			ps.setInt(1, objectId);
			ps.setLong(2, context.getContextId());
			ps.setInt(3, principal_id);
			ps.setString(4, client_id);
			ps.setInt(5, object_target_id);
			
			ps.executeUpdate();
			ps.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
			
			if (writeCon != null) {
				writeCon.setAutoCommit(true);
				DBPool.pushWrite(context, writeCon);
			}
		}
	}
	
	private void deleteEntry(final Context context, final int principal_id, final int object_target_id) throws Exception {
		Connection writeCon = null;
		
		PreparedStatement ps = null;
		
		try {
			writeCon = DBPool.pickupWriteable(context);
			ps = writeCon.prepareStatement(SQL_ENTRY_DELETE);
			ps.setInt(1, object_target_id);
			ps.setInt(2, principal_id);
			
			ps.executeUpdate();
			ps.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
			
			if (writeCon != null) {
				DBPool.pushWrite(context, writeCon);
			}
		}
	}
	
	protected boolean hasModulePermission(final SessionObject sessionObj) {
		return (sessionObj.getUserConfiguration().hasVCard() && sessionObj.getUserConfiguration().hasContact());
	}
	
}



