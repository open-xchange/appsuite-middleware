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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.GET, name = "all", description = "Get all tasks.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for tasks are defined in Common object data, Detailed task and appointment data and Detailed task data. The alias \"all\" uses the predefined columnset [20, 1, 2, 5, 4]."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.")
}, responseDescription = "An array with task data. Each array element describes one task and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(TaskActionFactory.OAUTH_READ_SCOPE)
public class AllAction extends TaskAction {

    /**
     * Initializes a new {@link AllAction}.
     * @param serviceLookup
     */
    public AllAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(TaskRequest req) throws OXException {
        int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        int[] columnsToLoad = removeVirtualColumns(columns);
        int folderId = req.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        int orderBy = getOrderBy(req);
        Order order = OrderFields.parse(req.getParameter(AJAXServlet.PARAMETER_ORDER));
        int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
        int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);

        int[] internalColumns = new int[columnsToLoad.length+1];
        System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
        internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

        Date timestamp = new Date(0);
        Date lastModified = null;

        SearchIterator<Task> it = null;
        try {
            final TasksSQLInterface taskssql = new TasksSQLImpl(req.getSession());
            if (leftHandLimit < 0) {
                it = taskssql.getTaskList(folderId, leftHandLimit, -1, orderBy, order, internalColumns);
            } else {
                it = taskssql.getTaskList(folderId, leftHandLimit, rightHandLimit, orderBy, order, internalColumns);
            }

            List<Task> taskList = new LinkedList<Task>();
            while (it.hasNext()) {
                Task taskobject = it.next();
                taskList.add(taskobject);

                lastModified = taskobject.getLastModified();

                if (timestamp.getTime() < lastModified.getTime()) {
                    timestamp = lastModified;
                }
            }

            return new AJAXRequestResult(taskList, timestamp, "task");
        } finally {
            SearchIterators.close(it);
        }
    }

}
