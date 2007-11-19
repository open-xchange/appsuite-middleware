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
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.DBPool;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * ical
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public final class ical extends PermissionServlet {
	
	private static final String _doPut = "doPut";
	
	private final static int[] _appointmentFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL
	};
	
	protected final static int[] _taskFields = {
		DataObject.OBJECT_ID,
		DataObject.CREATED_BY,
		DataObject.CREATION_DATE,
		DataObject.LAST_MODIFIED,
		DataObject.MODIFIED_BY,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		CalendarObject.PARTICIPANTS,
		Task.ACTUAL_COSTS,
		Task.ACTUAL_DURATION,
		Task.ALARM,
		Task.BILLING_INFORMATION,
		Task.CATEGORIES,
		Task.COMPANIES,
		Task.CURRENCY,
		Task.DATE_COMPLETED,
		Task.IN_PROGRESS,
		Task.PERCENT_COMPLETED,
		Task.PRIORITY,
		Task.STATUS,
		Task.TARGET_COSTS,
		Task.TARGET_DURATION,
		Task.TRIP_METER,
		Task.COLOR_LABEL
	};
	
	private static final String CALENDARFOLDER = "calendarfolder";
	private static final String TASKFOLDER = "taskfolder";
	private static final String ENABLEDELETE = "enabledelete";
	
	private static String SQL_PRINCIPAL_SELECT = "SELECT object_id, calendarfolder, taskfolder FROM ical_principal WHERE cid = ? AND principal = ?";
	private static String SQL_PRINCIPAL_INSERT = "INSERT INTO ical_principal (object_id, cid, principal, calendarfolder, taskfolder) VALUES (?, ?, ?, ?, ?)";
	private static String SQL_PRINCIPAL_UPDATE = "UPDATE ical_principal SET calendarfolder = ?, taskfolder = ? WHERE object_id = ?";
	
	private static String SQL_ENTRIES_LOAD = "SELECT object_id, client_id, target_object_id, module FROM ical_ids WHERE cid = ? AND principal_id = ?";
	private static String SQL_ENTRY_INSERT = "INSERT INTO ical_ids (object_id, cid, principal_id, client_id, target_object_id, module) VALUES (?, ?, ?, ? ,?, ?)";
	private static String SQL_ENTRY_DELETE = "DELETE FROM ical_ids WHERE target_object_id = ? AND principal_id = ?";
	
	private static transient final Log LOG = LogFactory.getLog(ical.class);
	
	public void oxinit() throws ServletException {
		
	}
	
	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("GET");
		}
		
		OutputStream os = null;
		
		VersitDefinition.Writer w = null;
		
		String user_agent = null;
		String principal = null;
		
		int calendarfolder_id = 0;
		int taskfolder_id = 0;
		
		final Session sessionObj = getSession(req);
		
		final Context context = sessionObj.getContext();
		
		try {
			user_agent = getUserAgent(req);
			
			principal = user_agent + '_' + sessionObj.getUserId();
			
			calendarfolder_id = getCalendarFolderID(req);
			taskfolder_id = getTaskFolderID(req);
			
			if (calendarfolder_id == 0 && taskfolder_id == 0) {
				final OXFolderAccess oAccess = new OXFolderAccess(context);
				calendarfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.CALENDAR).getObjectID();
				taskfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.TASK).getObjectID();
				/*calendarfolder_id = OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), context);
				taskfolder_id = OXFolderTools.getTaskDefaultFolder(sessionObj.getUserObject().getId(), context);*/
			}
			
			HashMap entries_db = new HashMap();
			HashMap entries_db_reverse = new HashMap();
			HashMap entries_module = new HashMap();
			final HashMap<String, String> entries = new HashMap<String, String>();
			
			Connection readCon = null;
			
			boolean exists = false;
			
			int principal_id = 0;
			int db_calendarfolder_id = 0;
			int db_taskfolder_id = 0;
			
			PreparedStatement principalStatement = null;
			ResultSet rs = null;
			
			try {
				readCon = DBPool.pickup(context);
				
				principalStatement = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
				principalStatement.setLong(1, context.getContextId());
				principalStatement.setString(2, principal);
				rs = principalStatement.executeQuery();
				
				exists = rs.next();
				
				if (exists) {
					principal_id = rs.getInt(1);
					db_calendarfolder_id = rs.getInt(2);
					db_taskfolder_id = rs.getInt(3);
					
					final HashMap h[] = loadDBEntries(context, principal_id);
					entries_db = h[0];
					entries_module = h[1];
					entries_db_reverse = h[2];
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
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/calendar");
			
			resp.setHeader("Accept-Ranges", "bytes");
			resp.setHeader("Keep-Alive", "timeout=15 max=100");
			
			os = resp.getOutputStream();
			
			final VersitDefinition def = Versit.getDefinition("text/calendar");
			w = def.getWriter(os, "UTF-8");
			final VersitObject ical = OXContainerConverter.newCalendar("2.0");
			def.writeProperties(w, ical);
			final VersitDefinition eventDef = def.getChildDef("VEVENT");
			final VersitDefinition taskDef = def.getChildDef("VTODO");
			final OXContainerConverter oxc = new OXContainerConverter(sessionObj);
			
			SearchIterator it = null;
			
			try {
				VersitObject vo = null;
				
				final AppointmentSQLInterface appointmentSql = new CalendarSql(sessionObj);
				
				if (calendarfolder_id != 0) {
					try {
						it = appointmentSql.getModifiedAppointmentsInFolder(calendarfolder_id, _appointmentFields, new Date(0));
						while (it.hasNext()) {
							final AppointmentObject appointmentObj = (AppointmentObject)it.next();
							vo = oxc.convertAppointment(appointmentObj);
							
							final int object_id = appointmentObj.getObjectID();
							
							final Property uid = vo.getProperty("UID");
							if (entries_db_reverse.containsKey(String.valueOf(object_id))) {
								uid.setValue(entries_db_reverse.get(String.valueOf(object_id)));
							}
							
							eventDef.write(w, vo);
							
							entries.put(uid.getValue().toString(), String.valueOf(object_id));
							if (!entries_module.containsKey(String.valueOf(object_id))) {
								entries_module.put(String.valueOf(object_id), String.valueOf(Types.APPOINTMENT));
							}
							
						}
					} catch (ConverterException exc) {
						LOG.error("ical.createVEVENT", exc);
					}
				}

				final TasksSQLInterface taskInterface = new TasksSQLInterfaceImpl(sessionObj);
				
				if (taskfolder_id != 0) {
					try {
						it = taskInterface.getModifiedTasksInFolder(taskfolder_id, _taskFields, new Date(0));
						while (it.hasNext()) {
							final Task taskObj = (Task)it.next();
							vo = oxc.convertTask(taskObj);
							
							final int object_id = taskObj.getObjectID();
							
							final Property uid = vo.getProperty("UID");
							if (entries_db_reverse.containsKey(String.valueOf(object_id))) {
								uid.setValue(entries_db_reverse.get(String.valueOf(object_id)).toString());
							}
							
							taskDef.write(w, vo);
							
							entries.put(uid.getValue().toString(), String.valueOf(object_id));
							if (!entries_module.containsKey(String.valueOf(object_id))) {
								entries_module.put(String.valueOf(object_id), String.valueOf(Types.TASK));
							}
						}
					} catch (ConverterException exc) {
						LOG.error("ical.createVTODO", exc);
					}
				}

				def.writeEnd(w, ical);
			} finally {
				os.flush();
				w.flush();
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
					if (!(db_calendarfolder_id == calendarfolder_id && db_taskfolder_id == taskfolder_id)) {
						ps = writeCon.prepareStatement(SQL_PRINCIPAL_UPDATE);
						ps.setInt(1, calendarfolder_id);
						ps.setInt(2, taskfolder_id);
						ps.setInt(3, principal_id);
						
						ps.executeUpdate();
						ps.close();
					}
				} else {
					writeCon.setAutoCommit(false);
					principal_id = IDGenerator.getId(context, Types.ICAL, writeCon);
					
					ps = writeCon.prepareStatement(SQL_PRINCIPAL_INSERT);
					ps.setInt(1, principal_id);
					ps.setLong(2, sessionObj.getContext().getContextId());
					ps.setString(3, principal);
					ps.setInt(4, calendarfolder_id);
					ps.setInt(5, taskfolder_id);
					
					ps.executeUpdate();
					ps.close();
					
					writeCon.commit();
				}
			} catch (SQLException exc) {
				writeCon.rollback();
				throw exc;
			} finally {
				if (ps != null) {
					ps.close();
				}
				
				if (writeCon != null) {
					if (!writeCon.getAutoCommit()) {
						writeCon.setAutoCommit(true);
					}
					
					DBPool.closeWriterSilent(context, writeCon);
				} 
			}
			
			final Iterator iterator = entries.keySet().iterator();
			while (iterator.hasNext()) {
				final String client_id = iterator.next().toString();
				final String s_object_id = entries.get(client_id);
				
				if (!entries_db.containsKey(client_id)) {
					if (entries_module.containsKey(s_object_id)) {
						final int i_module = Integer.parseInt(entries_module.get(s_object_id).toString());
						addEntry(context, principal_id, Integer.parseInt(s_object_id), client_id, i_module);
					} else {
						throw new OXConflictException("no module found!");
					}
				}
			}
			
			final Iterator databaseIterator = entries_db.keySet().iterator();
			while (databaseIterator.hasNext()) {
				final String client_id = databaseIterator.next().toString();
				final String s_object_id = entries_db.get(client_id).toString();
				
				if (!entries.containsKey(client_id)) {
					deleteEntry(context, principal_id, Integer.parseInt(s_object_id));
				}
			}
		} catch (OXConflictException exc) {
			LOG.debug("ical.doGet", exc);
			doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
		} catch (Exception exc) {
			LOG.error("ical.doGet", exc);
			doError(resp);
		}
	}
	
	@Override
	public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("PUT");
		}
		
		int calendarfolder_id = 0;
		int taskfolder_id = 0;
		
		String user_agent = null;
		String principal = null;
		
		String content_type = null;
		
		final Session sessionObj = getSession(req);
		
		final Context context = sessionObj.getContext();
		
		try {			
			user_agent = getUserAgent(req);
			content_type = req.getContentType();
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("read ical content_type: " + content_type);
			}
			
			calendarfolder_id = getCalendarFolderID(req);
			taskfolder_id = getTaskFolderID(req);
			
			if (calendarfolder_id == 0 && taskfolder_id == 0) {
				final OXFolderAccess oAccess = new OXFolderAccess(context);
				calendarfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.CALENDAR).getObjectID();
				taskfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.TASK).getObjectID();
				/*calendarfolder_id = OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), context);
				taskfolder_id = OXFolderTools.getTaskDefaultFolder(sessionObj.getUserObject().getId(), context);*/
			}
			
			if (content_type == null) {
				content_type = "text/calendar";
			}
			
			if (user_agent == null) {
				throw new OXConflictException("missing header field: user-agent");
			}
			
			principal = user_agent + '_' + sessionObj.getUserId();
			
			calendarfolder_id = getCalendarFolderID(req);
			taskfolder_id = getTaskFolderID(req);
			
			if (calendarfolder_id == 0 && taskfolder_id == 0) {
				final OXFolderAccess oAccess = new OXFolderAccess(context);
				calendarfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.CALENDAR).getObjectID();
				taskfolder_id = oAccess.getDefaultFolder(sessionObj.getUserId(), FolderObject.TASK).getObjectID();
				/*calendarfolder_id = OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), context);
				taskfolder_id = OXFolderTools.getTaskDefaultFolder(sessionObj.getUserObject().getId(), context);*/
			}
			
			final boolean enabledelete = getEnableDelete(req);
			
			HashMap entries_db = new HashMap();
			HashMap<String, String> entries_module = new HashMap<String, String>();
			final HashSet<String> entries = new HashSet<String>();
			
			Connection readCon = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			int principal_id = 0;
			
			try {
				readCon = DBPool.pickup(context);
				ps = readCon.prepareStatement(SQL_PRINCIPAL_SELECT);
				ps.setInt(1, context.getContextId());
				ps.setString(2, principal);
				rs = ps.executeQuery();
				
				int db_calendarfolder_id = 0;
				int db_taskfolder_id = 0;
				
				final boolean exists = rs.next();
				
				if (exists) {
					principal_id = rs.getInt(1);
					db_calendarfolder_id = rs.getInt(2);
					db_taskfolder_id = rs.getInt(3);
					
					if (!(db_calendarfolder_id == calendarfolder_id && db_taskfolder_id == taskfolder_id)) {
						throw new OXConflictException("no principal found for the given folders: " + principal);
					}
					
					final HashMap<String, String>[] h = loadDBEntries(context, principal_id);
					entries_db = h[0];
					entries_module = h[1];
				} else {
					throw new OXConflictException("no principal found: " + principal);
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				
				if (ps != null) {
					ps.close();
				}
				
				if (readCon != null) {
					DBPool.closeReaderSilent(context, readCon);
				}
			}
			
			final VersitDefinition def = Versit.getDefinition(content_type);
			final VersitDefinition.Reader r = def.getReader(req.getInputStream(), "UTF-8");
			final OXContainerConverter oxc = new OXContainerConverter(sessionObj);
			
			final AppointmentSQLInterface appointmentInterface = new CalendarSql(sessionObj);
			final TasksSQLInterface taskInterface = new TasksSQLInterfaceImpl(sessionObj);

			final Date timestamp = new Date();
			
			try {
				final VersitObject root_vo = def.parseBegin(r);
				VersitObject vo = def.parseChild(r, root_vo);
				while (vo != null) {
					int object_id = 0;
					
					try {
						final Property property = vo.getProperty("UID");
						
						String client_id = null;
						
						
						if (property != null) {
							client_id = property.getValue().toString();
						}
						
						if ("VEVENT".equals(vo.name)) {
							final CalendarDataObject appointmentObj = oxc.convertAppointment(vo);
							appointmentObj.setContext(context);
							
							if (client_id != null && entries_db.containsKey(client_id)) {
								try {
									object_id = Integer.parseInt(entries_db.get(client_id).toString());
								} catch (NumberFormatException exc) {
									if (LOG.isDebugEnabled()) {
										LOG.debug("object id is not an int");
									}
								}
								
								if (object_id > 0) {
									appointmentObj.setObjectID(object_id);
								}
								
								appointmentObj.setParentFolderID(calendarfolder_id);
								
								if (appointmentObj.containsObjectID()) {
									appointmentInterface.updateAppointmentObject(appointmentObj, calendarfolder_id, timestamp);
								} else {
									appointmentInterface.insertAppointmentObject(appointmentObj);
								}
								
								entries.add(client_id);
							} else {
								appointmentObj.setParentFolderID(calendarfolder_id);
								
								if (appointmentObj.containsObjectID()) {
									appointmentInterface.updateAppointmentObject(appointmentObj, calendarfolder_id, timestamp);
								} else {
									appointmentInterface.insertAppointmentObject(appointmentObj);
								}
								
								if (client_id != null) {
									entries.add(client_id);
									object_id = appointmentObj.getObjectID();
									addEntry(context, principal_id, object_id, client_id, Types.APPOINTMENT);
									
									if (!entries_module.containsKey(String.valueOf(object_id))) {
										entries_module.put(String.valueOf(object_id), String.valueOf(Types.APPOINTMENT));
									}
								}
							}
							
							LOG.debug("STATUS: OK");
						} else if ("VTODO".equals(vo.name)) {
							final Task taskObj = oxc.convertTask(vo);
							
							if (client_id != null && entries_db.containsKey(client_id)) {
								try {
									object_id = Integer.parseInt(entries_db.get(client_id).toString());
								} catch (NumberFormatException exc) {
									LOG.debug("object id is not an int");
								}
								
								if (object_id > 0) {
									taskObj.setObjectID(object_id);
								}
								
								taskObj.setParentFolderID(taskfolder_id);
								
								if (taskObj.containsObjectID()) {
									taskInterface.updateTaskObject(taskObj, taskfolder_id, timestamp);
								} else {
									taskInterface.insertTaskObject(taskObj);
								}
								
								entries.add(client_id);
							} else {
								taskObj.setParentFolderID(taskfolder_id);
								
								if (taskObj.containsObjectID()) {
									taskInterface.updateTaskObject(taskObj, taskfolder_id, timestamp);
								} else {
									taskInterface.insertTaskObject(taskObj);
								}
								
								if (client_id != null) {
									entries.add(client_id);
									object_id = taskObj.getObjectID();
									addEntry(context, principal_id, object_id, client_id, Types.TASK);
									
									if (!entries_module.containsKey(String.valueOf(object_id))) {
										entries_module.put(String.valueOf(object_id), String.valueOf(Types.TASK));
									}
								}
							}
							
							LOG.debug("STATUS: OK");
						} else {
							LOG.warn("invalid versit object: " + vo.name);
						}
					} catch (OXObjectNotFoundException exc) {
						LOG.debug("object was already delete", exc);
					}
					
					vo = def.parseChild(r, root_vo);
				}
			} finally {
				oxc.close();
			}
			
			final CalendarDataObject appointmentObject = new CalendarDataObject();
			
			final Iterator it = entries_db.keySet().iterator();
			while (it.hasNext()) {
				final String tmp = it.next().toString();
				if (!entries.contains(tmp)) {
					final int object_id = Integer.parseInt(entries_db.get(tmp).toString());
					final int i_module = Integer.parseInt(entries_module.get(String.valueOf(object_id)));
					
					deleteEntry(context, principal_id, object_id);
					
					if (enabledelete) {
						try {
							if (i_module == Types.APPOINTMENT) {
								appointmentObject.reset();
								appointmentObject.setObjectID(object_id);
								appointmentObject.setParentFolderID(calendarfolder_id);
								appointmentInterface.deleteAppointmentObject(appointmentObject, calendarfolder_id, timestamp);
							} else if (i_module == Types.TASK) {
								taskInterface.deleteTaskObject(object_id, taskfolder_id, timestamp);
							} else {
								throw new OXConflictException("unknown module: " + i_module);
							}
						} catch (OXObjectNotFoundException exc) {
							LOG.debug("object was already delete", exc);
						}
					}
				}
			}
			
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (OXMandatoryFieldException exc) {
			LOG.debug(_doPut, exc);
			doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
		} catch (OXPermissionException exc) {
			LOG.debug(_doPut, exc);
			doError(resp, HttpServletResponse.SC_FORBIDDEN, exc.getMessage());
		} catch (OXConflictException exc) {
			LOG.debug(_doPut, exc);
			doError(resp, HttpServletResponse.SC_CONFLICT, exc.getMessage());
		} catch (Exception exc) {
			LOG.error(_doPut, exc);
			doError(resp);
		}
	}
	
	private void doError(final HttpServletResponse resp) throws ServletException {
		doError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
	}
	
	private void doError(final HttpServletResponse resp, final int code, final String msg) throws ServletException {
		try {
			log(code + " msg --> " + msg);
			
			resp.setStatus(code);
			resp.setContentType("text/html");
			final OutputStream os = resp.getOutputStream();
			os.write(("<html><body>" + msg + "</body></html>").getBytes());
		} catch (Exception exc) {
			log("Error while doError --> " + exc);
			exc.printStackTrace();
		}
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
	
	private int getCalendarFolderID(final HttpServletRequest req) throws OXConflictException {
		if ( req.getParameter(CALENDARFOLDER) != null) {
			try {
				return Integer.parseInt(req.getParameter(CALENDARFOLDER));
			} catch (NumberFormatException exc) {
				throw new OXConflictException(CALENDARFOLDER + " is not a number");
			}
		}
		
		return 0;
	}
	
	private int getTaskFolderID(final HttpServletRequest req) throws OXConflictException {
		if ( req.getParameter(TASKFOLDER) != null) {
			try {
				return Integer.parseInt(req.getParameter(TASKFOLDER));
			} catch (NumberFormatException exc) {
				throw new OXConflictException(TASKFOLDER + " is not a number");
			}
		}
		
		return 0;
	}
	
	private boolean getEnableDelete(final HttpServletRequest req) {
		if (( req.getParameter(ENABLEDELETE) != null) && (req.getParameter(ENABLEDELETE).toLowerCase().equals("yes"))) {
			return true;
		}
		
		return false;
	}
	
	private void addEntry(final Context context, final int principal_id, final int object_target_id, final String client_id, final int module) throws Exception {
		Connection writeCon = null;
		PreparedStatement ps = null;
		
		try {
			writeCon = DBPool.pickupWriteable(context);
			writeCon.setAutoCommit(false);
			final int objectId = IDGenerator.getId(context, Types.ICAL, writeCon);
			ps = writeCon.prepareStatement(SQL_ENTRY_INSERT);
			ps.setInt(1, objectId);
			ps.setLong(2, context.getContextId());
			ps.setInt(3, principal_id);
			ps.setString(4, client_id);
			ps.setInt(5, object_target_id);
			ps.setInt(6, module);
			
			ps.executeUpdate();
			writeCon.commit();
		} catch (SQLException exc) {
			if (writeCon != null) {
				writeCon.rollback();
			}
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
			
			if (writeCon != null) {
				if (!writeCon.getAutoCommit()) {
					writeCon.setAutoCommit(true);
				}
				DBPool.closeWriterSilent(context, writeCon);
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
		} finally {
			if (ps != null) {
				ps.close();
			}
			
			if (writeCon != null) {
				DBPool.closeWriterSilent(context, writeCon);
			}
		}
	}
	
	private HashMap<String, String>[] loadDBEntries(final Context context, final int principal_object_id) throws Exception {
		final HashMap<String, String>[] h = new HashMap[3];
		final HashMap<String, String> entries_db = new HashMap<String, String>();
		final HashMap<String, String> entries_db_reverse = new HashMap<String, String>();
		final HashMap<String, String> entries_module = new HashMap<String, String>();
		
		Connection readCon = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			readCon = DBPool.pickup(context);
			
			ps = readCon.prepareStatement(SQL_ENTRIES_LOAD);
			ps.setInt(1, principal_object_id);
			ps.setLong(2, context.getContextId());
			rs = ps.executeQuery();
			
			String client_id = null;
			int target_id = 0;
			int module = 0;
			
			while (rs.next()) {
				client_id = rs.getString(2);
				target_id = rs.getInt(3);
				module = rs.getInt(4);
				
				entries_db.put(client_id, String.valueOf(target_id));
				entries_db_reverse.put(String.valueOf(target_id), client_id);
				entries_module.put(String.valueOf(target_id), String.valueOf(module));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			
			if (ps != null) {
				ps.close();
			}
			
			if (readCon != null) {
				DBPool.closeReaderSilent(context, readCon);
			}
		}
		
		h[0] = entries_db;
		h[1] = entries_module;
		h[2] = entries_db_reverse;

		return h;
	}
	
	@Override
	protected boolean hasModulePermission(final Session sessionObj) {
		final UserConfiguration uc =  UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext());
		return (uc.hasICal() && uc.hasCalendar() && uc.hasTask());
	}
}
