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



package com.openexchange.ajax.request;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * AppointmentRequest
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class AppointmentRequest {
	
	public static final String RECURRENCE_MASTER = "recurrence_master";
	
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
		CalendarObject.RECURRENCE_CALCULATOR,
		CalendarObject.RECURRENCE_ID,
		CalendarObject.PARTICIPANTS,
		CalendarObject.USERS,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL,
		CalendarDataObject.TIMEZONE
	};
	
	private SessionObject sessionObj;
	
	private JSONWriter jsonWriter;
	
	private Date timestamp;
	
	private TimeZone timeZone;
	
	private static final Log LOG = LogFactory.getLog(AppointmentRequest.class);
	
	public AppointmentRequest(SessionObject sessionObj, JSONWriter w) {
		this.sessionObj = sessionObj;
		this.jsonWriter = w;
		
		final String sTimeZone = sessionObj.getUserObject().getTimeZone();
		
		timeZone = TimeZone.getTimeZone(sTimeZone);
		if (LOG.isDebugEnabled()) {
			LOG.debug("use timezone string: " + sTimeZone);
			LOG.debug("use user timezone: " + timeZone);
		}
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, OXConflictException, OXException, JSONException, SearchIteratorException, AjaxException, OXJSONException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_CONFIRM)) {
			actionConfirm(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			actionNew(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			actionDelete(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			actionUpdate(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			actionUpdates(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			actionList(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			actionAll(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			actionGet(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			actionSearch(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_HAS)) {
			actionHas(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_FREEBUSY)) {
			actionFreeBusy(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
			actionCopy(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public void actionNew(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, OXException {
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final CalendarDataObject appointmentObj = new CalendarDataObject();
		appointmentObj.setContext(sessionObj.getContext());
		
		final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
		appointmentParser.parse(appointmentObj, jData);
		
		if (!appointmentObj.containsParentFolderID()) {
			throw new OXMandatoryFieldException("missing folder");
		}
		
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessionObj);
		final CalendarDataObject[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);
		
		jsonWriter.object();
		try {
			if (conflicts != null) {
				jsonWriter.key("conflicts");
				jsonWriter.array();
				try {
					final AppointmentWriter appointmentWriter = new AppointmentWriter(jsonWriter, timeZone);
					for (int a = 0; a < conflicts.length; a++) {
						appointmentWriter.writeAppointment(conflicts[a]);
					}
				} finally {
					jsonWriter.endArray();
				}
			} else {
				jsonWriter.key(AppointmentFields.ID);
				jsonWriter.value(appointmentObj.getObjectID());
			}
		} finally {
			jsonWriter.endObject();
		}
	}
	
	public void actionUpdate(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, OXException {
		final int objectId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final CalendarDataObject appointmentObj = new CalendarDataObject();
		appointmentObj.setContext(sessionObj.getContext());
		
		final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
		appointmentParser.parse(appointmentObj, jData);
		
		appointmentObj.setObjectID(objectId);
		
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		final CalendarDataObject[] conflicts = appointmentsql.updateAppointmentObject(appointmentObj, inFolder, timestamp);
		
		jsonWriter.object();
		
		try {
			if (conflicts != null) {
				jsonWriter.key(AppointmentFields.ID);
				jsonWriter.value(appointmentObj.getObjectID());
				
				jsonWriter.key("conflicts");
				jsonWriter.array();
				final AppointmentWriter appointmentWriter = new AppointmentWriter(jsonWriter, timeZone);
				try {
					for (int a = 0; a < conflicts.length; a++) {
						appointmentWriter.writeAppointment(conflicts[a]);
					}
				} finally {
					jsonWriter.endArray();
				}
			} else {
				jsonWriter.key(AppointmentFields.ID);
				jsonWriter.value(appointmentObj.getObjectID());
			}
		} finally {
			jsonWriter.endObject();
		}
	}
	
	public void actionUpdates(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException {
		Date lastModified = null;
		
		timestamp = new Date(0);
		
		SearchIterator it = null;
		
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		final Date startUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
		final Date endUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);
		final Date start = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
		final Date end = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);
		final String ignore = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_IGNORE);
		
		final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);
		
		final int folderId = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		
		boolean showAppointmentInAllFolders = false;
		
		if (folderId == 0) {
			showAppointmentInAllFolders = true;
		}
		
		boolean bIgnoreDelete = false;
		boolean bIgnoreModified = false;
		
		if (ignore != null && ignore.indexOf("deleted") != -1) {
			bIgnoreDelete = true;
		}
		
		if (ignore != null && ignore.indexOf("changed") != -1) {
			bIgnoreModified = true;
		}
		
		jsonWriter.array();
		
		if (bIgnoreModified && bIgnoreDelete) {
			// nothing requested
			
			jsonWriter.endArray();
			return;
		}
		
		final AppointmentWriter appointmentWriter = new AppointmentWriter(jsonWriter, timeZone);
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		
		try {
			if (!bIgnoreModified) {
				if (showAppointmentInAllFolders) {
					it = appointmentsql.getModifiedAppointmentsBetween(sessionObj.getUserObject().getId(), start, end, _appointmentFields, timestamp, 0, null);
				} else {
					if (start != null && end != null) {
						it = appointmentsql.getModifiedAppointmentsInFolder(folderId, start, end, _appointmentFields, timestamp);
					} else {
						it = appointmentsql.getModifiedAppointmentsInFolder(folderId, _appointmentFields, timestamp);
					}
				}
				
				while (it.hasNext()) {
					final CalendarDataObject appointmentObj = (CalendarDataObject)it.next();
					
					if (appointmentObj.getRecurrenceType() != CalendarObject.NONE && appointmentObj.getRecurrencePosition() == 0) {
						if (bRecurrenceMaster) {
							final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentObj);
							if (recuResults.size() == 1) {
								appointmentObj.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
								appointmentObj.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
								
								appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC);
							} else {
								LOG.warn("cannot load first recurring appointment from appointment object: " + +appointmentObj.getRecurrenceType() + " / "+appointmentObj.getObjectID()+"\n\n\n");
							}
						} else {
							appointmentObj.calculateRecurrence();
							
							RecurringResults recuResults = null;
							if (start != null && end != null) {
								recuResults = CalendarRecurringCollection.calculateRecurring(appointmentObj, start.getTime(), end.getTime(), 0);
							} else {
								recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentObj);
							}
							
							for (int a = 0; a < recuResults.size(); a++) {
								final RecurringResult result = recuResults.getRecurringResult(a);
								appointmentObj.setStartDate(new Date(result.getStart()));
								appointmentObj.setEndDate(new Date(result.getEnd()));
								appointmentObj.setRecurrencePosition(result.getPosition());
								
								if (startUTC != null && endUTC != null) {
									appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC);
								} else {
									appointmentWriter.writeArray(appointmentObj, columns);
								}
							}
						}
					} else {
						if (startUTC != null && endUTC != null) {
							appointmentWriter.writeArray(appointmentObj, columns, startUTC, endUTC);
						} else {
							appointmentWriter.writeArray(appointmentObj, columns);
						}
					}
					
					lastModified = appointmentObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			}
			
			if (!bIgnoreDelete) {
				it = appointmentsql.getDeletedAppointmentsInFolder(folderId, _appointmentFields, timestamp);
				while (it.hasNext()) {
					final AppointmentObject appointmentObj = (AppointmentObject)it.next();
					
					jsonWriter.value(appointmentObj.getObjectID());
					
					lastModified = appointmentObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			}
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
	}
	
	public void actionDelete(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException {
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final CalendarDataObject appointmentObj = new CalendarDataObject();
		appointmentObj.setObjectID(DataParser.checkInt(jData, DataFields.ID));
		final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);
		
		if (jData.has(CalendarFields.RECURRENCE_POSITION)) {
			appointmentObj.setRecurrencePosition(DataParser.checkInt(jData, CalendarFields.RECURRENCE_POSITION));
		}
		
		appointmentObj.setContext(sessionObj.getContext());
		
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		
		try {
			appointmentsql.deleteAppointmentObject(appointmentObj, inFolder, timestamp);
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		}
//		jsonWriter.value("");
	}
	
	public void actionList(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXException {
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		SearchIterator it = null;
		
		final HashMap<Integer, ArrayList<Integer>> recurrencePositionMap = new HashMap<Integer, ArrayList<Integer>>();
				
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		final JSONArray jData = DataParser.checkJSONArray(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);
		
		final HashMap<Integer, Integer> objectIdMap = new HashMap<Integer, Integer>();
		for (int a = 0; a < jData.length(); a++) {
			final JSONObject jObject = jData.getJSONObject(a);
			final int objectId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
			final int folderId = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);
			
			objectIdMap.put(Integer.valueOf(objectId), Integer.valueOf(folderId));
			
			if (jObject.has(CalendarFields.RECURRENCE_POSITION)) {
				final int recurrencePosition = DataParser.checkInt(jObject, CalendarFields.RECURRENCE_POSITION);
				ArrayList<Integer> recurrencePosList = null;
				if (recurrencePositionMap.containsKey(Integer.valueOf(objectId))) {
					recurrencePosList = recurrencePositionMap.get(Integer.valueOf(objectId));
				} else {
					recurrencePosList = new ArrayList<Integer>();
				}
				recurrencePosList.add(Integer.valueOf(recurrencePosition));
				recurrencePositionMap.put(Integer.valueOf(objectId), recurrencePosList);
			}
		}
		
		final int size = objectIdMap.size();
		int[][] objectIdAndFolderId = new int[size][2];
		
		final Iterator<Map.Entry<Integer, Integer>> iterator = objectIdMap.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<Integer, Integer> entry = iterator.next();
			objectIdAndFolderId[i][0] = entry.getKey().intValue();
			objectIdAndFolderId[i][1] = entry.getValue().intValue();
		}
		
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		it = appointmentsql.getObjectsById(objectIdAndFolderId, _appointmentFields);
		
		int counter = 0;
		
		jsonWriter.array();
		
		try {
			while (it.hasNext()) {
				final CalendarDataObject appointmentobject = (CalendarDataObject)it.next();
				final AppointmentWriter appointmentwriter = new AppointmentWriter(jsonWriter, timeZone);
				
				final Date startDate = appointmentobject.getStartDate();
				final Date endDate = appointmentobject.getEndDate();
				
				if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
					if (bRecurrenceMaster) {
						final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentobject);
						if (recuResults.size() == 1) {
							appointmentobject.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
							appointmentobject.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
							
							appointmentwriter.writeArray(appointmentobject, columns);
						} else {
							LOG.warn("cannot load first recurring appointment from appointment object: " + +appointmentobject.getRecurrenceType() + " / "+appointmentobject.getObjectID()+"\n\n\n");
						}
					} else {
						appointmentobject.calculateRecurrence();
						if (recurrencePositionMap.containsKey(Integer.valueOf(appointmentobject.getObjectID()))) {
							final ArrayList<Integer> recurrencePosList = recurrencePositionMap.get(Integer.valueOf(appointmentobject.getObjectID()));
							
							for (int a = 0; a < recurrencePosList.size(); a++) {
								appointmentobject.setStartDate(startDate);
								appointmentobject.setEndDate(endDate);
								final RecurringResults recuResults = CalendarRecurringCollection.calculateRecurring(appointmentobject, 0, 0, recurrencePosList.get(a).intValue());
								if (recuResults.size() > 0) {
									final RecurringResult result = recuResults.getRecurringResult(0);
									appointmentobject.setStartDate(new Date(result.getStart()));
									appointmentobject.setEndDate(new Date(result.getEnd()));
									appointmentobject.setRecurrencePosition(result.getPosition());
								} else {
									throw new OXObjectNotFoundException("no recurrence appointment found at pos: " + counter);
								}
								
								appointmentwriter.writeArray(appointmentobject, columns);
							}
						} else {
							final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentobject);
							if (recuResults.size() > 0) {
								final RecurringResult result = recuResults.getRecurringResult(0);
								appointmentobject.setStartDate(new Date(result.getStart()));
								appointmentobject.setEndDate(new Date(result.getEnd()));
								appointmentobject.setRecurrencePosition(result.getPosition());
							} else {
								throw new OXObjectNotFoundException("no recurrence appointment found at pos: " + counter);
							}
							
							if (appointmentobject.getFullTime() && appointmentobject.getStartDate().getTime() == appointmentobject.getEndDate().getTime()) {
								appointmentobject.setEndDate(new Date(appointmentobject.getStartDate().getTime() + (24*60*60*1000)));
							}
							
							appointmentwriter.writeArray(appointmentobject, columns);
						}
					}
				} else {
					appointmentwriter.writeArray(appointmentobject, columns);
				}
				
				lastModified = appointmentobject.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
				
				counter++;
			}
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
	}
	
	public void actionAll(final JSONObject jsonObj) throws SearchIteratorException, OXMandatoryFieldException, JSONException, OXException {
		timestamp = new Date(0);
		
		SearchIterator it = null;
		
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		final Date startUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_START);
		final Date endUTC = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_END);
		final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
		final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);
		final int folderId = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		
		final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
		final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
		
		final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);
		
		boolean showAppointmentInAllFolders = false;
		
		if (folderId == 0) {
			showAppointmentInAllFolders = true;
		}
		
		/*Date lastModified = new Date(0);*/
		
		
		jsonWriter.array();
		try {
			final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
			if (showAppointmentInAllFolders) {
				it = appointmentsql.getAppointmentsBetween(sessionObj.getUserObject().getId(), start, end, _appointmentFields, orderBy, orderDir);
			} else {
				it = appointmentsql.getAppointmentsBetweenInFolder(folderId, _appointmentFields, start, end, orderBy, orderDir);
			}
			Date lastModified = new Date(0);
			while (it.hasNext()) {
				final CalendarDataObject appointmentobject = (CalendarDataObject)it.next();
				final AppointmentWriter appointmentwriter = new AppointmentWriter(jsonWriter, timeZone);
				
				if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
					if (bRecurrenceMaster) {
						final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentobject);
						if (recuResults.size() == 1) {
							appointmentobject.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
							appointmentobject.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
							
							appointmentwriter.writeArray(appointmentobject, columns, startUTC, endUTC);
						} else {
							LOG.warn("cannot load first recurring appointment from appointment object: " + +appointmentobject.getRecurrenceType() + " / "+appointmentobject.getObjectID()+"\n\n\n");
						}
					} else {
						appointmentobject.calculateRecurrence();
						final RecurringResults recuResults = CalendarRecurringCollection.calculateRecurring(appointmentobject, start.getTime(), end.getTime(), 0);
						for (int a = 0; a < recuResults.size(); a++) {
							final RecurringResult result = recuResults.getRecurringResult(a);
							appointmentobject.setStartDate(new Date(result.getStart()));
							appointmentobject.setEndDate(new Date(result.getEnd()));
							appointmentobject.setRecurrencePosition(result.getPosition());
							
							appointmentwriter.writeArray(appointmentobject, columns, startUTC, endUTC);
						}
					}
				} else {
					appointmentwriter.writeArray(appointmentobject, columns, startUTC, endUTC);
				}
				
				lastModified = appointmentobject.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
	}
	
	public void actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXObjectNotFoundException, OXException {
		timestamp = null;
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final int recurrencePosition = DataParser.parseInt(jsonObj, CalendarFields.RECURRENCE_POSITION);
		
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessionObj);
		try {
			final CalendarDataObject appointmentobject = appointmentSql.getObjectById(id, inFolder);
			
			final AppointmentWriter appointmentwriter = new AppointmentWriter(jsonWriter, timeZone);
			
			if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && recurrencePosition > 0) {
				appointmentobject.calculateRecurrence();
				final RecurringResults recuResults = CalendarRecurringCollection.calculateRecurring(appointmentobject, 0, 0, recurrencePosition);
				final RecurringResult result = recuResults.getRecurringResult(0);
				appointmentobject.setStartDate(new Date(result.getStart()));
				appointmentobject.setEndDate(new Date(result.getEnd()));
				appointmentobject.setRecurrencePosition(result.getPosition());
				
				appointmentwriter.writeAppointment(appointmentobject);
			} else {
				appointmentwriter.writeAppointment(appointmentobject);
			}
			
			timestamp = appointmentobject.getLastModified();
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		}
		
	}
	
	public void actionConfirm(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, OXException {
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		
		final CalendarDataObject appointmentObj = new CalendarDataObject();
		appointmentObj.setContext(sessionObj.getContext());
		
		final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
		appointmentParser.parse(appointmentObj, jData);
		
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessionObj);
		appointmentSql.setUserConfirmation(appointmentObj.getObjectID(), sessionObj.getUserObject().getId(), appointmentObj.getConfirm(), appointmentObj.getConfirmMessage());
//		jsonWriter.value("");
	}
	
	public void actionHas(final JSONObject jsonObj) throws JSONException, OXException {
		final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
		final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);
		
		final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		final boolean[] bHas = appointmentsql.hasAppointmentsBetween(start, end);
		
		jsonWriter.array();
		try {
			for (int a = 0; a < bHas.length; a++) {
				jsonWriter.value(bHas[a]);
			}
		} finally {
			jsonWriter.endArray();
		}
	}
	
	public void actionSearch(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXConflictException, OXException, OXJSONException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		SearchIterator it = null;
		
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final AppointmentSearchObject searchObj = new AppointmentSearchObject();
		if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
			searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
		}
		
		if (jData.has("pattern")) {
			searchObj.setPattern(DataParser.parseString(jData, "pattern"));
		}
		
		final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
		final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
		
		int limit = 0;
		boolean hasLimit = false;
		if (jsonObj.has("limit")) {
			limit = DataParser.checkInt(jsonObj, "limit");
			hasLimit = true;
		}
		
		final Date start = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
		final Date end = DataParser.parseTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);

		final Date startUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
		final Date endUTC = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);
		
		searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
		searchObj.setTitle(DataParser.parseString(jData, AppointmentFields.TITLE));
		searchObj.setSearchInNote(DataParser.parseBoolean(jData, "searchinnote"));
		searchObj.setCatgories(DataParser.parseString(jData, AppointmentFields.CATEGORIES));
		searchObj.setSubfolderSearch(DataParser.parseBoolean(jData, "subfoldersearch"));
		searchObj.setAllFolders(DataParser.parseBoolean(jData, "allfolders"));
		
		if (start != null && end != null) {
			searchObj.setRange(new Date[] { start, end } );
		}
		// searchObj.setRange(DataParser.parseJSONDateArray(jData, "daterange"));
		
		
		if (jData.has(CalendarFields.PARTICIPANTS)) {
			final Participants participants = new Participants();
			searchObj.setParticipants(CalendarParser.parseParticipants(jData, participants));
		}
		
		final boolean bRecurrenceMaster = DataParser.parseBoolean(jsonObj, RECURRENCE_MASTER);
		
		jsonWriter.array();
		
		try {
			/*AppointmentWriter appointmentWriter = */new AppointmentWriter(jsonWriter, timeZone);
			final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
			
			if (searchObj.getFolder() > 0 && searchObj.getPattern() != null) {
				it = appointmentsql.searchAppointments(searchObj.getPattern(), searchObj.getFolder(), orderBy, orderDir, _appointmentFields);
//			} else if (start != null && end != null) {
//				int counter = 0;
//				// it = appointmentsql.getActiveAppointments(sessionObj.getUserObject().getId(), start, end, _appointmentFields);
//				it = appointmentsql.searchAppointments("%", OXFolderTools.getCalendarDefaultFolder(sessionObj.getUserObject().getId(), sessionObj.getContext()), orderBy, orderDir, _appointmentFields);
//
//				while (it.hasNext()) {
//					AppointmentObject appointmentObj = (AppointmentObject)it.next();
//					appointmentWriter.writeArray(appointmentObj, columns);
//
//					lastModified = appointmentObj.getLastModified();
//
//					if (timestamp.getTime() < lastModified.getTime()) {
//						timestamp = lastModified;
//					}
//
//					if (hasLimit) {
//						if (counter >= limit) {
//							break;
//						}
//
//						counter++;
//					}
//				}
			} else {
				it = appointmentsql.getAppointmentsByExtendedSearch(searchObj, orderBy, orderDir, _appointmentFields);
			}
			
			int counter = 0;
			
			List<CalendarDataObject> recurrenceAppointmentList = null;
			List<CalendarDataObject> appointmentSortedList = null;
			
			if (hasLimit) {
				recurrenceAppointmentList = new ArrayList<CalendarDataObject>();
				appointmentSortedList = new ArrayList<CalendarDataObject>();
			}
			
			final AppointmentWriter appointmentwriter = new AppointmentWriter(jsonWriter, timeZone);
			
			while (it.hasNext()) {
				final CalendarDataObject appointmentobject = (CalendarDataObject)it.next();
				
				if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && appointmentobject.getRecurrencePosition() == 0) {
					if (start != null && end != null) {
						if (bRecurrenceMaster) {
							final RecurringResults recuResults = CalendarRecurringCollection.calculateFirstRecurring(appointmentobject);
							if (recuResults.size() == 1) {
								appointmentobject.setStartDate(new Date(recuResults.getRecurringResult(0).getStart()));
								appointmentobject.setEndDate(new Date(recuResults.getRecurringResult(0).getEnd()));
								
								appointmentwriter.writeArray(appointmentobject, columns, startUTC, endUTC);
							} else {
								LOG.warn("cannot load first recurring appointment from appointment object: " + +appointmentobject.getRecurrenceType() + " / "+appointmentobject.getObjectID()+"\n\n\n");
							}
						} else {
							appointmentobject.calculateRecurrence();
							final RecurringResults recuResults = CalendarRecurringCollection.calculateRecurring(appointmentobject, start.getTime(), end.getTime(), 0);
							if (recuResults.size() > 0) {
								final RecurringResult result = recuResults.getRecurringResult(0);
								appointmentobject.setStartDate(new Date(result.getStart()));
								appointmentobject.setEndDate(new Date(result.getEnd()));
								appointmentobject.setRecurrencePosition(result.getPosition());

								if (appointmentobject.getFullTime()) {
									if (CalendarCommonCollection.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
										appointmentwriter.writeArray(appointmentobject, columns);
										counter++;
									}
								} else {
									appointmentwriter.writeArray(appointmentobject, columns);
									counter++;
								}
							}
						}
					} else {
						appointmentwriter.writeArray(appointmentobject, columns);
						counter++;
					}
				} else {
					if (appointmentobject.getFullTime() && (startUTC != null && endUTC != null)) {
						if (CalendarCommonCollection.inBetween(appointmentobject.getStartDate().getTime(), appointmentobject.getEndDate().getTime(), startUTC.getTime(), endUTC.getTime())) {
							appointmentwriter.writeArray(appointmentobject, columns);
							counter++;
						}
					} else {
						appointmentwriter.writeArray(appointmentobject, columns);
						counter++;
					}
				}
				
				lastModified = appointmentobject.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
				
				if (hasLimit && counter >= limit) {
					break;
				}
			}
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
	}
	
	public void actionFreeBusy(final JSONObject jsonObj) throws JSONException, SearchIteratorException, OXMandatoryFieldException, OXException {
		final int userId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int type = DataParser.checkInt(jsonObj, "type");
		final Date start = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_START, timeZone);
		final Date end = DataParser.checkTime(jsonObj, AJAXServlet.PARAMETER_END, timeZone);
		
		timestamp = new Date(0);
		
		SearchIterator it = null;
		
		jsonWriter.array();
		
		try {
			final AppointmentWriter appointmentWriter = new AppointmentWriter(jsonWriter, timeZone);
			
			final AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
			it = appointmentsql.getFreeBusyInformation(userId, type, start, end);
			while (it.hasNext()) {
				final AppointmentObject appointmentObj = (AppointmentObject)it.next();
				appointmentWriter.writeAppointment(appointmentObj);
			}
		} finally {
			if (it != null) {
				it.close();
			}
			jsonWriter.endArray();
		}
	}
	
	public void actionCopy(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, OXObjectNotFoundException, OXException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final boolean ignoreConflicts = DataParser.checkBoolean(jsonObj, AppointmentFields.IGNORE_CONFLICTS);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);
		
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessionObj);
		
		timestamp = new Date(0);
		
		jsonWriter.object();
		
		try {
			final CalendarDataObject appointmentObj = appointmentSql.getObjectById(id, inFolder);
			appointmentObj.removeObjectID();
			appointmentObj.setParentFolderID(folderId);
			appointmentObj.setIgnoreConflicts(ignoreConflicts);
			final CalendarDataObject[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);
			
			if (conflicts != null) {
				jsonWriter.key("conflicts");
				jsonWriter.array();
				try {
					final AppointmentWriter appointmentWriter = new AppointmentWriter(jsonWriter, timeZone);
					for (int a = 0; a < conflicts.length; a++) {
						appointmentWriter.writeAppointment(conflicts[a]);
					}
				} finally {
					jsonWriter.endArray();
				}
			} else {
				jsonWriter.key(AppointmentFields.ID);
				jsonWriter.value(appointmentObj.getObjectID());
			}
		} catch (SQLException e) {
			throw new OXException("SQLException occurred", e);
		} finally {
			jsonWriter.endObject();
		}
	}
}
