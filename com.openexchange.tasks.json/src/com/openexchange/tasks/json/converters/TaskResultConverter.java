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

package com.openexchange.tasks.json.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tasks.json.RequestTools;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TaskResultConverter}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskResultConverter extends AbstractTaskJSONResultConverter {

    private static final String INPUT_FORMAT = "task";

    /**
     * Initializes a new {@link TaskResultConverter}.
     */
    public TaskResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    protected void convertTask(String action, Collection<Task> tasks, AJAXRequestData request, AJAXRequestResult result, TimeZone timeZone) throws OXException {
        if (AJAXServlet.ACTION_UPDATES.equalsIgnoreCase(action)) {
            convertTasks4Updates(tasks, request, result, timeZone);
        } else {
            convertTasks(tasks, request, result, timeZone);
        }
    }

    protected void convertTasks(Collection<Task> tasks, AJAXRequestData request, AJAXRequestResult result, TimeZone timeZone) throws OXException {
        int[] columns = RequestTools.checkIntArray(request, AJAXServlet.PARAMETER_COLUMNS);
        JSONArray jArray = new JSONArray(tasks.size());

        TaskWriter taskwriter = new TaskWriter(timeZone).setSession(request.getSession());

        // Optional anonymization
        for (Task task : tasks) {
            try {
                taskwriter.writeArray(task, columns, jArray);
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        result.setResultObject(jArray, OUTPUT_FORMAT);
    }

    protected void convertTasks4Updates(Collection<Task> tasks, AJAXRequestData request, AJAXRequestResult result, TimeZone timeZone) throws OXException {
        int[] columns = RequestTools.checkIntArray(request, AJAXServlet.PARAMETER_COLUMNS);

        // Create list with support for Iterator.remove()
        List<Task> taskList = iterSupportingList(tasks);

        // Any deleted tasks?
        List<Task> deletedTasks = new LinkedList<Task>();
        for (final Iterator<Task> iter = taskList.iterator(); iter.hasNext();) {
            final Task task = iter.next();
            if (hasOnlyId(task, columns)) {
                deletedTasks.add(task);
                iter.remove();
            }
        }

        // Create JSON array
        TaskWriter taskwriter = new TaskWriter(timeZone).setSession(request.getSession());
        JSONArray jArray = new JSONArray(taskList.size());
        for (Task task : taskList) {
            try {
                taskwriter.writeArray(task, columns, jArray);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        if (!deletedTasks.isEmpty()) {
            for (final Task task : deletedTasks) {
                jArray.put(task.getObjectID());
            }
        }

        result.setResultObject(jArray, OUTPUT_FORMAT);
    }

    private List<Task> iterSupportingList(Collection<Task> tasks) {
        if (tasks instanceof LinkedList) {
            return (LinkedList<Task>) tasks;
        }

        if (tasks instanceof ArrayList) {
            return (ArrayList<Task>) tasks;
        }

        return new ArrayList<Task>(tasks);
    }

    private static boolean hasOnlyId(Task task, int[] columns) {
        if (!task.containsObjectID()) {
            return false;
        }
        if (CommonObject.Marker.ID_ONLY.equals(task.getMarker())) {
            return true;
        }
        for (int column : columns) {
            if (DataObject.OBJECT_ID != column && task.contains(column)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void convertTask(AJAXRequestData request, AJAXRequestResult result, ServerSession session, Converter converter, TimeZone timeZone) throws OXException {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Task) {
            convertTask((Task) resultObject, request, result, timeZone);
        } else {
            @SuppressWarnings("unchecked") Collection<Task> tasks = (Collection<Task>) resultObject;
            String action = request.getParameter(AJAXServlet.PARAMETER_ACTION);
            convertTask(action, tasks, request, result, timeZone);
        }
    }

    private void convertTask(Task task, AJAXRequestData request, AJAXRequestResult result, TimeZone timeZone) throws OXException {
        try {
            TaskWriter taskWriter = new TaskWriter(timeZone).setSession(request.getSession());
            JSONObject jTask = new JSONObject(16);
            taskWriter.writeTask(task, jTask);
            result.setResultObject(jTask, OUTPUT_FORMAT);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

}
