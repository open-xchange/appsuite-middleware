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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ConfirmAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.WRITE)
public class ConfirmAction extends TaskAction {

    /**
     * Initializes a new {@link ConfirmAction}.
     * @param services
     */
    public ConfirmAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TaskRequest req) throws OXException {
        final JSONObject data = (JSONObject) req.getRequest().requireData();
        final Task task = new Task();
        final ServerSession session = req.getSession();
        new TaskParser(req.getTimeZone()).parse(task, data, session.getUser().getLocale());
        final TasksSQLInterface taskSql = new TasksSQLImpl(session);
        final int taskIdFromParameter = req.optInt(AJAXServlet.PARAMETER_ID);
        final int taskId;
        if (TaskRequest.NOT_FOUND == taskIdFromParameter) {
            if (!task.containsObjectID()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( AJAXServlet.PARAMETER_ID);
            }
            taskId = task.getObjectID();
        } else {
            taskId = taskIdFromParameter;
        }
        final Date timestamp = taskSql.setUserConfirmation(taskId, session.getUserId(), task.getConfirm(), task.getConfirmMessage());

        return new AJAXRequestResult(new JSONObject(0), timestamp, "json");
    }

}
