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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.READ)
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
