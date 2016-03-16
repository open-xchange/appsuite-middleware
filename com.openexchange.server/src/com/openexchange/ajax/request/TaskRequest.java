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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.java.Strings;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class TaskRequest extends CalendarRequest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskRequest.class);

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
        CommonObject.UID,
        Task.ACTUAL_COSTS,
        Task.ACTUAL_DURATION,
        CalendarObject.ALARM,
        Task.BILLING_INFORMATION,
        CommonObject.CATEGORIES,
        Task.COMPANIES,
        Task.CURRENCY,
        Task.DATE_COMPLETED,
        Task.PERCENT_COMPLETED,
        Task.PRIORITY,
        Task.STATUS,
        Task.TARGET_COSTS,
        Task.TARGET_DURATION,
        Task.TRIP_METER,
        CommonObject.COLOR_LABEL
    };

    public TaskRequest(final ServerSession session) {
        super();
        this.session = session;
        this.timeZone = TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public JSONValue action(final String action, final JSONObject json) throws JSONException, OXException {
        if (!session.getUserPermissionBits().hasTask()) {
            throw OXException.noPermissionForModule("task");
        }
        final String sTimeZone = DataParser.parseString(json, AJAXServlet.PARAMETER_TIMEZONE);
        if (null != sTimeZone) {
            timeZone = getTimeZone(sTimeZone);
        }
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_CONFIRM)) {
            return actionConfirm(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
            return actionNew(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
            return actionDelete(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
            return actionUpdate(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            return actionUpdates(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
            return actionList(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
            return actionAll(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            return actionGet(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
            return actionSearch(json);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
            return actionCopy(json);
        } else {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
    }

    public JSONObject actionNew(final JSONObject jsonObj) throws JSONException, OXException {
        final Task task = new Task();

        final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, ResponseFields.DATA);

        final TaskParser taskParser = new TaskParser(timeZone);
        taskParser.parse(task, jsonobject, session.getUser().getLocale());

        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);

        convertExternalToInternalUsersIfPossible(task, session.getContext(), LOG);
        sqlinterface.insertTaskObject(task);
        timestamp = task.getLastModified();

        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, task.getObjectID());

        return jsonResponseObject;
    }

    public JSONObject actionUpdate(final JSONObject jsonObj) throws OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);

        final Task task = new Task();

        final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, ResponseFields.DATA);

        final TaskParser taskParser = new TaskParser(timeZone);
        taskParser.parse(task, jsonobject, session.getUser().getLocale());

        task.setObjectID(id);

        convertExternalToInternalUsersIfPossible(task, session.getContext(), LOG);

        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);
        sqlinterface.updateTaskObject(task, inFolder, timestamp);
        timestamp = task.getLastModified();

        return new JSONObject();
    }

    public JSONArray actionUpdates(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtualColumns(columns);
        final Date requestedTimestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);
        timestamp = new Date(requestedTimestamp.getTime());
        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        String ignore = DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_IGNORE);

        if (ignore == null) {
            ignore = "deleted";
        }

        boolean bIgnoreDelete = false;

        if (ignore.indexOf("deleted") != -1) {
            bIgnoreDelete = true;
        }

        Date lastModified = null;

        final JSONArray jsonResponseArray = new JSONArray();
        SearchIterator<Task> it = null;
        try {
            final int[] internalColumns = new int[columnsToLoad.length+1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            final TasksSQLInterface taskssql = new TasksSQLImpl(session);
            final TaskWriter taskWriter = new TaskWriter(timeZone).setSession(session);

            it = taskssql.getModifiedTasksInFolder(folderId, internalColumns, requestedTimestamp);
            while (it.hasNext()) {
                final Task taskObj = it.next();

                taskWriter.writeArray(taskObj, columns, jsonResponseArray);

                lastModified = taskObj.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }
            }

            if (!bIgnoreDelete) {
                it.close();
                it = taskssql.getDeletedTasksInFolder(folderId, internalColumns, requestedTimestamp);
                while (it.hasNext()) {
                    final Task taskObj = it.next();

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

    public JSONArray actionDelete(final JSONObject jsonObj) throws OXException {
        final JSONObject jsonobject = DataParser.checkJSONObject(jsonObj, ResponseFields.DATA);
        final int id = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = DataParser.checkDate(jsonObj, AJAXServlet.PARAMETER_TIMESTAMP);

        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);
        sqlinterface.deleteTaskObject(id, inFolder, timestamp);

        return new JSONArray();
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        Date lastModified = null;

        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtualColumns(columns);
        final JSONArray jData = DataParser.checkJSONArray(jsonObj, ResponseFields.DATA);
        final int[][] objectIdAndFolderId = new int[jData.length()][2];
        for (int a = 0; a < objectIdAndFolderId.length; a++) {
            final JSONObject jObject = jData.getJSONObject(a);
            objectIdAndFolderId[a][0] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_ID);
            objectIdAndFolderId[a][1] = DataParser.checkInt(jObject, AJAXServlet.PARAMETER_FOLDERID);
        }
        final int[] internalColumns = new int[columnsToLoad.length+1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        SearchIterator<Task> it = null;

        final JSONArray jsonResponseArray = new JSONArray();

        try {
            final TasksSQLInterface taskssql = new TasksSQLImpl(session);
            final TaskWriter taskwriter = new TaskWriter(timeZone).setSession(session);
            it = taskssql.getObjectsById(objectIdAndFolderId, internalColumns);

            while (it.hasNext()) {
                final Task taskobject = it.next();
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

    public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtualColumns(columns);
        final int folderId = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final Order order = OrderFields.parse(DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER));
        final int leftHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.LEFT_HAND_LIMIT);
        final int rightHandLimit = DataParser.parseInt(jsonObj, AJAXServlet.RIGHT_HAND_LIMIT);

        final int[] internalColumns = new int[columnsToLoad.length+1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        timestamp = new Date(0);

        Date lastModified = null;

        final JSONArray jsonResponseArray = new JSONArray();
        SearchIterator<Task> it = null;
        try {

            final TaskWriter taskwriter = new TaskWriter(timeZone).setSession(session);

            final TasksSQLInterface taskssql = new TasksSQLImpl(session);
            if (leftHandLimit == 0) {
                it = taskssql.getTaskList(folderId, leftHandLimit, -1, orderBy, order, internalColumns);
            } else {
                it = taskssql.getTaskList(folderId, leftHandLimit, rightHandLimit, orderBy, order, internalColumns);
            }

            while (it.hasNext()) {
                final Task taskobject = it.next();
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

    public JSONObject actionGet(final JSONObject jsonObj) throws OXException, JSONException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_INFOLDER);
        timestamp = new Date(0);

        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);
        final Task task = sqlinterface.getTaskById(id, inFolder);
        final TaskWriter taskWriter = new TaskWriter(timeZone).setSession(session);

        final JSONObject jsonResponseObject = new JSONObject();
        taskWriter.writeTask(task, jsonResponseObject);

        timestamp = task.getLastModified();

        return jsonResponseObject;
    }

    public JSONObject actionConfirm(final JSONObject json) throws OXException, JSONException {
        final JSONObject data = DataParser.checkJSONObject(json, ResponseFields.DATA);
        final Task task = new Task();
        new TaskParser(timeZone).parse(task, data, session.getUser().getLocale());
        final TasksSQLInterface taskSql = new TasksSQLImpl(session);
        final int taskIdFromParameter = DataParser.parseInt(json, AJAXServlet.PARAMETER_ID);
        final int taskId;
        if (DataParser.NO_INT == taskIdFromParameter) {
            if (!task.containsObjectID()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( AJAXServlet.PARAMETER_ID);
            }
            taskId = task.getObjectID();
        } else {
            taskId = taskIdFromParameter;
        }
        timestamp = taskSql.setUserConfirmation(taskId, session.getUserId(), task.getConfirm(), task.getConfirmMessage());
        return new JSONObject();
    }

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, OXException {
        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        final int[] columnsToLoad = removeVirtualColumns(columns);
        timestamp = new Date(0);
        Date lastModified = null;

        final JSONObject jData = DataParser.checkJSONObject(jsonObj, ResponseFields.DATA);
        final TaskSearchObject searchObj = new TaskSearchObject();
        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            searchObj.addFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
        }

        final int orderBy = DataParser.parseInt(jsonObj, AJAXServlet.PARAMETER_SORT);
        final Order order = OrderFields.parse(DataParser.parseString(jsonObj, AJAXServlet.PARAMETER_ORDER));

        if (jsonObj.has("limit")) {
            DataParser.checkInt(jsonObj, "limit");
        }

        final Date start = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_START);
        final Date end = DataParser.parseDate(jsonObj, AJAXServlet.PARAMETER_END);

        if (start != null) {
            final Date[] dateRange;
            if (end == null) {
                dateRange = new Date[1];
            } else {
                dateRange = new Date[2];
                dateRange[1] = end;
            }
            dateRange[0] = start;
            searchObj.setRange(dateRange);
        }

        if (jData.has(SearchFields.PATTERN)) {
            searchObj.setPattern(DataParser.parseString(jData, SearchFields.PATTERN));
        }

        searchObj.setTitle(DataParser.parseString(jData, CalendarFields.TITLE));
        searchObj.setSearchInNote(DataParser.parseBoolean(jData, "searchinnote"));
        searchObj.setStatus(DataParser.parseInt(jData, TaskFields.STATUS));
        searchObj.setCatgories(DataParser.parseString(jData, CommonFields.CATEGORIES));
        searchObj.setSubfolderSearch(DataParser.parseBoolean(jData, "subfoldersearch"));

        if (jData.has(CalendarFields.PARTICIPANTS)) {
            final Participants participants = new Participants();
            searchObj.setParticipants(CalendarParser.parseParticipants(jData, participants));
        }

        final int[] internalColumns = new int[columnsToLoad.length+1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        final JSONArray jsonResponseArray = new JSONArray();

        SearchIterator<Task> it = null;

        try {
            final TaskWriter taskWriter = new TaskWriter(timeZone).setSession(session);

            final TasksSQLInterface taskssql = new TasksSQLImpl(session);
            it = taskssql.getTasksByExtendedSearch(searchObj, orderBy, order, internalColumns);

            while (it.hasNext()) {
                final Task taskObj = it.next();
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

    public JSONObject actionCopy(final JSONObject jsonObj) throws JSONException, OXException {
        final int id = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_ID);
        final int inFolder = DataParser.checkInt(jsonObj, AJAXServlet.PARAMETER_FOLDERID);
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, AJAXServlet.PARAMETER_DATA);
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);

        final TasksSQLInterface taskInterface = new TasksSQLImpl(session);
        final Task taskObj = taskInterface.getTaskById(id, inFolder);
        taskObj.removeObjectID();
        taskObj.setParentFolderID(folderId);
        taskInterface.insertTaskObject(taskObj);

        timestamp = new Date(0);

        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, taskObj.getObjectID());

        return jsonResponseObject;
    }

    private static int[] removeVirtualColumns(final int[] columns) {
        final TIntList tmp = new TIntArrayList(columns.length);
        for (final int col : columns) {
            if (col != DataObject.LAST_MODIFIED_UTC) {
                tmp.add(col);
            }
        }
        return tmp.toArray();
    }
}
