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

import static com.openexchange.ajax.container.Response.DATA;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class TaskRequest {

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
		Task.PERCENT_COMPLETED,
		Task.PRIORITY,
		Task.STATUS,
		Task.TARGET_COSTS,
		Task.TARGET_DURATION,
		Task.TRIP_METER,
		Task.COLOR_LABEL
	};
	
	private Session sessionObj;
	
	private final User userObj;
	
	private Date timestamp;
	
	private TimeZone timeZone;
	
	private static final Log LOG = LogFactory.getLog(TaskRequest.class);
	
	public TaskRequest(Session sessionObj) {
		this.sessionObj = sessionObj;
		userObj = UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext());
		
		final String sTimeZone = userObj.getTimeZone();
		
		timeZone = TimeZone.getTimeZone(sTimeZone);
		if (LOG.isDebugEnabled()) {
			LOG.debug("use timezone string: " + sTimeZone);
			LOG.debug("use user timezone: " + timeZone);
		}
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public Object action(final String action, final JSONObject jsonObject) throws OXMandatoryFieldException, JSONException, OXObjectNotFoundException, OXConflictException, OXPermissionException, OXFolderNotFoundException, SearchIteratorException, AjaxException, OXException, OXJSONException {
		if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				sessionObj.getContext()).hasTask()) {
			throw new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "task");
		}
		
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_CONFIRM)) {
			return actionConfirm(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
			return actionNew(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			return actionDelete(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			return actionUpdate(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			return actionUpdates(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			return actionList(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			return actionAll(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			return actionGet(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			return actionSearch(jsonObject);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
			return actionCopy(jsonObject);
		} else {
			throw new AjaxException(AjaxException.Code.UnknownAction, action);
		}
	}
	
	public JSONObject actionNew(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, AjaxException {
		final Task task = new Task();
		
		final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, DATA);
		
		final TaskParser taskParser = new TaskParser(timeZone);
		taskParser.parse(task, jsonobject);

		final TasksSQLInterface sqlinterface = new TasksSQLInterfaceImpl(sessionObj);
		sqlinterface.insertTaskObject(task);
        timestamp = task.getLastModified();

		final JSONObject jsonResponseObject = new JSONObject();
		jsonResponseObject.put(TaskFields.ID, task.getObjectID());

		return jsonResponseObject;
	}
	
	public JSONObject actionUpdate(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		
		final Task task = new Task();
		
		final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, DATA);
		
		final TaskParser taskParser = new TaskParser(timeZone);
		taskParser.parse(task, jsonobject);
		
		task.setObjectID(id);
		
		final TasksSQLInterface sqlinterface = new TasksSQLInterfaceImpl(sessionObj);
		sqlinterface.updateTaskObject(task, inFolder, timestamp);
        timestamp = task.getLastModified();
		
		return new JSONObject();
	}
	
	public JSONArray actionUpdates(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);
		
		boolean bIgnoreDelete = false;
		
		if (ignore != null && ignore.indexOf("deleted") != -1) {
			bIgnoreDelete = true;
		}
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		SearchIterator it = null;
		try {
			int[] internalColumns = new int[columns.length+1];
			System.arraycopy(columns, 0, internalColumns, 0, columns.length);
			internalColumns[columns.length] = DataObject.LAST_MODIFIED;
			
			final TasksSQLInterface taskssql = new TasksSQLInterfaceImpl(sessionObj);
			final TaskWriter taskWriter = new TaskWriter(timeZone);
			
			it = taskssql.getModifiedTasksInFolder(folderId, internalColumns, timestamp);
			while (it.hasNext()) {
				final Task taskObj = (Task)it.next();
				
				taskWriter.writeArray(taskObj, columns, jsonResponseArray);
				
				lastModified = taskObj.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			if (!bIgnoreDelete) {
				it.close();
				it = taskssql.getDeletedTasksInFolder(folderId, internalColumns, timestamp);
				while (it.hasNext()) {
					final Task taskObj = (Task)it.next();
					
					jsonResponseArray.put(taskObj.getObjectID());
					
					lastModified = taskObj.getLastModified();
					
					if (timestamp.getTime() < lastModified.getTime()) {
						timestamp = lastModified;
					}
				}
			}
			
			return jsonResponseArray;
		} finally {
			if(it!=null) {
				it.close();
			}
		}
	}

	public JSONArray actionDelete(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXPermissionException, OXConflictException, OXObjectNotFoundException, OXFolderNotFoundException, OXException, OXJSONException, AjaxException {
		final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, DATA);
		final int id = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_INFOLDER);
		timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
		
		final TasksSQLInterface sqlinterface = new TasksSQLInterfaceImpl(sessionObj);
		sqlinterface.deleteTaskObject(id, inFolder, timestamp);
		
		return new JSONArray();
	}
	
	public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		final JSONArray jData = DataParser.checkJSONArray(jsonObj, DATA);
		int[][] objectIdAndFolderId = new int[jData.length()][2];
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			final JSONObject jObject = jData.getJSONObject(a);
			objectIdAndFolderId[a][0] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
			objectIdAndFolderId[a][1] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);
		}

		int[] internalColumns = new int[columns.length+1];
		System.arraycopy(columns, 0, internalColumns, 0, columns.length);
		internalColumns[columns.length] = DataObject.LAST_MODIFIED;

		SearchIterator it = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		
		try {
			final TasksSQLInterface taskssql = new TasksSQLInterfaceImpl(sessionObj);
			final TaskWriter taskwriter = new TaskWriter(timeZone);
			it = taskssql.getObjectsById(objectIdAndFolderId, internalColumns);
			
			while (it.hasNext()) {
				final Task taskobject = (Task)it.next();
				taskwriter.writeArray(taskobject, columns, jsonResponseArray);
				
				lastModified = taskobject.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			return jsonResponseArray;
		} finally {
			if(it!=null) {
				it.close();
			}
		}
	}
	
	public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, OXMandatoryFieldException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
		final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
		
		int[] internalColumns = new int[columns.length+1];
		System.arraycopy(columns, 0, internalColumns, 0, columns.length);
		internalColumns[columns.length] = DataObject.LAST_MODIFIED;

		timestamp = new Date(0);
		
		Date lastModified = null;
		
		final JSONArray jsonResponseArray = new JSONArray();
		SearchIterator it = null;
		try {
			
			final TaskWriter taskwriter = new TaskWriter(timeZone);

			final TasksSQLInterface taskssql = new TasksSQLInterfaceImpl(sessionObj);
			it = taskssql.getTaskList(folderId, 0, 500, orderBy, orderDir, internalColumns);
			
			while (it.hasNext()) {
				final Task taskobject = (Task)it.next();
				taskwriter.writeArray(taskobject, columns, jsonResponseArray);
				
				lastModified = taskobject.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			return jsonResponseArray;
		} finally {
			if(it!=null) {
				it.close();
			}
		}
	}
	
	public JSONObject actionGet(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
		
		timestamp = new Date(0);

		final TasksSQLInterface sqlinterface = new TasksSQLInterfaceImpl(sessionObj);
		final Task task = sqlinterface.getTaskById(id, inFolder);
		final TaskWriter taskWriter = new TaskWriter(timeZone);
		
		final JSONObject jsonResponseObject = new JSONObject();
		taskWriter.writeTask(task, jsonResponseObject);
		
		timestamp = task.getLastModified();
		
		return jsonResponseObject;
	}

	public JSONObject actionConfirm(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, AjaxException {
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, DATA);
		final Task taskObj = new Task();
		
		final TaskParser taskParser = new TaskParser(timeZone);
		taskParser.parse(taskObj, jData);
		
		final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(sessionObj);
		taskSql.setUserConfirmation(taskObj.getObjectID(), userObj.getId(), taskObj.getConfirm(), taskObj.getConfirmMessage());

		return new JSONObject();
	}
	
	public JSONArray actionSearch(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXConflictException, SearchIteratorException, OXException, OXJSONException, AjaxException {
		final String[] sColumns = DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS).split(",");
		final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
		
		timestamp = new Date(0);
		
		Date lastModified = null;

		final JSONObject jData = DataParser.checkJSONObject(jsonObj, DATA);
		final TaskSearchObject searchObj = new TaskSearchObject();
		if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
			searchObj.setFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
		}
		
		final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
		final String orderDir = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER);
		
		if (jsonObj.has("limit")) {
			DataParser.checkInt(jsonObj, "limit");
		}
		
		final Date start = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
		final Date end = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);

		if (start != null && end != null) {
			Date[] dateRange = new Date[2];
			dateRange[0] = start;
			dateRange[1] = end;
			// FIXME 
			try {
				searchObj.setRange(dateRange);
			} catch (Exception e) {
				throw new OXException(e);
			}

		}
		
		if (jData.has("pattern")) {
			searchObj.setPattern(DataParser.parseString(jData, "pattern"));
		}
		
		searchObj.setTitle(DataParser.parseString(jData, TaskFields.TITLE));
		searchObj.setPriority(DataParser.parseInt(jData, TaskFields.PRIORITY));
		searchObj.setSearchInNote(DataParser.parseBoolean(jData, "searchinnote"));
		searchObj.setStatus(DataParser.parseInt(jData, TaskFields.STATUS));
		searchObj.setCatgories(DataParser.parseString(jData, TaskFields.CATEGORIES));
		searchObj.setSubfolderSearch(DataParser.parseBoolean(jData, "subfoldersearch"));
		searchObj.setAllFolders(DataParser.parseBoolean(jData, "allfolders"));
		
		if (jData.has(CalendarFields.PARTICIPANTS)) {
			final Participants participants = new Participants();
			searchObj.setParticipants(CalendarParser.parseParticipants(jData, participants));
		}
		
		int[] internalColumns = new int[columns.length+1];
		System.arraycopy(columns, 0, internalColumns, 0, columns.length);
		internalColumns[columns.length] = DataObject.LAST_MODIFIED;

		final JSONArray jsonResponseArray = new JSONArray();
		
		SearchIterator it = null;
		
		try {
			final TaskWriter taskWriter = new TaskWriter(timeZone);

			final TasksSQLInterface taskssql = new TasksSQLInterfaceImpl(sessionObj);
			it = taskssql.getTasksByExtendedSearch(searchObj, orderBy, orderDir, internalColumns);
			
			while (it.hasNext()) {
				final Task taskObj = (Task)it.next();
				taskWriter.writeArray(taskObj, columns, jsonResponseArray);
				
				lastModified = taskObj.getLastModified();
				
				if (timestamp.getTime() < lastModified.getTime()) {
					timestamp = lastModified;
				}
			}
			
			return jsonResponseArray;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public JSONObject actionCopy(final JSONObject jsonObj) throws OXMandatoryFieldException, JSONException, OXException, OXJSONException, AjaxException {
		final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
		final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
		final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
		final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);
		
		final TasksSQLInterface taskInterface = new TasksSQLInterfaceImpl(sessionObj);
		final Task taskObj = taskInterface.getTaskById(id, inFolder);
		taskObj.removeObjectID();
		taskObj.setParentFolderID(folderId);
		taskInterface.insertTaskObject(taskObj);
		
		timestamp = new Date(0);

		final JSONObject jsonResponseObject = new JSONObject();
		jsonResponseObject.put(TaskFields.ID, taskObj.getObjectID());
		
		return jsonResponseObject;
	}
}
