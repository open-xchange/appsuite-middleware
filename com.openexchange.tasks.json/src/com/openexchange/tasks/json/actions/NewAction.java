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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.WRITE)
public class NewAction extends TaskAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services
     */
    public NewAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TaskRequest req) throws OXException, JSONException {
        final Task task = new Task();
        final JSONObject jsonobject = (JSONObject) req.getRequest().requireData();
        ServerSession session = req.getSession();
        final TaskParser taskParser = new TaskParser(req.getTimeZone());
        taskParser.parse(task, jsonobject, session.getUser().getLocale());
        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);
        convertExternalToInternalUsersIfPossible(task, req.getSession().getContext());
        sqlinterface.insertTaskObject(task);
        countObjectUse(session, task);
        final Date timestamp = task.getLastModified();
        final JSONObject jsonResponseObject = new JSONObject();
        jsonResponseObject.put(DataFields.ID, task.getObjectID());
        return new AJAXRequestResult(jsonResponseObject, timestamp, "json");
    }

}
