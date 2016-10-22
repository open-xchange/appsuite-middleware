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
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(TaskActionFactory.OAUTH_READ_SCOPE)
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
