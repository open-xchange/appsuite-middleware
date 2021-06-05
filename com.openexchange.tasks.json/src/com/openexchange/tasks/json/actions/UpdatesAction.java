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
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject.Marker;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.READ)
public class UpdatesAction extends TaskAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     *
     * @param services
     */
    public UpdatesAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(TaskRequest req) throws OXException, JSONException {
        int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        int[] columnsToLoad = removeVirtualColumns(columns);
        Date requestedTimestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        Date timestamp = new Date(requestedTimestamp.getTime());
        final int folderId = req.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        String ignore = req.getParameter(AJAXServlet.PARAMETER_IGNORE);

        if (ignore == null) {
            ignore = "deleted";
        }

        boolean bIgnoreDelete = false;

        if (ignore.indexOf("deleted") != -1) {
            bIgnoreDelete = true;
        }

        Date lastModified = null;

        SearchIterator<Task> it = null;
        try {
            int[] internalColumns = new int[columnsToLoad.length + 1];
            System.arraycopy(columnsToLoad, 0, internalColumns, 0, columnsToLoad.length);
            internalColumns[columnsToLoad.length] = DataObject.LAST_MODIFIED;

            final TasksSQLInterface taskssql = new TasksSQLImpl(req.getSession());

            it = taskssql.getModifiedTasksInFolder(folderId, internalColumns, requestedTimestamp);
            List<Task> taskList = new LinkedList<Task>();
            while (it.hasNext()) {
                final Task taskObj = it.next();
                taskList.add(taskObj);

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
                    taskObj.setMarker(Marker.ID_ONLY);
                    taskList.add(taskObj);

                    lastModified = taskObj.getLastModified();

                    if (timestamp.getTime() < lastModified.getTime()) {
                        timestamp = lastModified;
                    }
                }
            }

            return new AJAXRequestResult(taskList, timestamp, "task");
        } finally {
            SearchIterators.close(it);
        }
    }

}
