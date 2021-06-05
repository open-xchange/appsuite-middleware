/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tasks.json.actions;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.READ)
public class SearchAction extends TaskAction {

    /**
     * Initializes a new {@link SearchAction}.
     * @param services
     */
    public SearchAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(TaskRequest req) throws OXException, JSONException {
        int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        int[] columnsToLoad = removeVirtualColumns(columns);
        Date timestamp = new Date(0);
        Date lastModified = null;

        final JSONObject jData = (JSONObject) req.getRequest().requireData();
        final TaskSearchObject searchObj = new TaskSearchObject();
        if (jData.has(AJAXServlet.PARAMETER_INFOLDER)) {
            searchObj.addFolder(DataParser.parseInt(jData, AJAXServlet.PARAMETER_INFOLDER));
        }

        int orderBy = getOrderBy(req);
        Order order = OrderFields.parse(req.getParameter(AJAXServlet.PARAMETER_ORDER));

        //if (jsonObj.has("limit")) {
        //    DataParser.checkInt(jsonObj, "limit");
        //}

        Date start = req.getDate(AJAXServlet.PARAMETER_START);
        Date end = req.getDate(AJAXServlet.PARAMETER_END);

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

        int[] internalColumns = new int[columnsToLoad.length+1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        SearchIterator<Task> it = null;
        try {
            final TasksSQLInterface taskssql = new TasksSQLImpl(req.getSession());
            it = taskssql.getTasksByExtendedSearch(searchObj, orderBy, order, internalColumns);

            List<Task> taskList = new LinkedList<Task>();
            while (it.hasNext()) {
                final Task taskObj = it.next();
                taskList.add(taskObj);

                lastModified = taskObj.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }
            }

            int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
            int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);

            if (leftHandLimit >= 0 || rightHandLimit > 0) {
                final int size = taskList.size();
                final int fromIndex = leftHandLimit > 0 ? leftHandLimit : 0;
                final int toIndex = rightHandLimit > 0 ? (rightHandLimit > size ? size : rightHandLimit) : size;
                if ((fromIndex) > size) {
                    taskList = Collections.<Task> emptyList();
                } else if (fromIndex >= toIndex) {
                    taskList = Collections.<Task> emptyList();
                } else {
                    /*
                     * Check if end index is out of range
                     */
                    if (toIndex < size) {
                        taskList = taskList.subList(fromIndex, toIndex);
                    } else if (fromIndex > 0) {
                        taskList = taskList.subList(fromIndex, size);
                    }
                }
            }

            return new AJAXRequestResult(taskList, timestamp, "task");
        } finally {
            SearchIterators.close(it);
        }
    }

}
