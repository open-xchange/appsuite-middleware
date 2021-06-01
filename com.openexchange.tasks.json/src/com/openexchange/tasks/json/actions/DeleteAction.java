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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskRequest;


/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@RestrictedAction(module = TaskAction.MODULE, type = RestrictedAction.Type.WRITE)
public class DeleteAction extends TaskAction {

    /**
     * Initializes a new {@link DeleteAction}.
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TaskRequest req) throws OXException, JSONException {
        final Date timestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        final Object data = req.getRequest().getData();
        if (data instanceof JSONObject) {
            final JSONObject jsonobject = (JSONObject) data;
            final int id = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_ID);
            final int inFolder = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_INFOLDER);
            final TasksSQLInterface sqlinterface = new TasksSQLImpl(req.getSession());
            sqlinterface.deleteTaskObject(id, inFolder, timestamp);
        } else if (data instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) data;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                final int id = DataParser.checkInt(json, AJAXServlet.PARAMETER_ID);
                final int inFolder = DataParser.checkInt(json, AJAXServlet.PARAMETER_INFOLDER);
                final TasksSQLInterface sqlinterface = new TasksSQLImpl(req.getSession());
                sqlinterface.deleteTaskObject(id, inFolder, timestamp);
            }
        }
        return new AJAXRequestResult(new JSONArray(0), timestamp, "json");
    }

}
