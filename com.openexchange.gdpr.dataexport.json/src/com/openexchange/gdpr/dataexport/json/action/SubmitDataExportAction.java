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

package com.openexchange.gdpr.dataexport.json.action;

import static com.openexchange.java.Autoboxing.I;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportArguments;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.HostInfo;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubmitDataExportAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SubmitDataExportAction extends AbstractDataExportAction {

    /**
     * Initializes a new {@link SubmitDataExportAction}.
     *
     * @param services The service look-up
     */
    public SubmitDataExportAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        DataExportService dataExportService = getDataExportService();

        boolean deleteOldDataExport = AJAXRequestDataTools.parseBoolParameter("deleteOldDataExport", requestData);
        if (deleteOldDataExport) {
            dataExportService.deleteDataExportTask(session.getUserId(), session.getContextId());
        }

        JSONValue jBody = requireJSONBody(requestData);
        if (!jBody.isObject()) {
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create(JSONObject.class, jBody.getClass());
        }
        JSONObject jArgs = jBody.toObject();

        DataExportArguments args = parseArguments(jArgs);
        HostData hostData = requestData.getHostData();
        if (hostData != null) {
            args.setHostInfo(new HostInfo(hostData.getHost(), hostData.isSecure()));
        }

        Optional<UUID> optionalTaskId = dataExportService.submitDataExportTaskIfAbsent(args, session);
        if (!optionalTaskId.isPresent()) {
            throw DataExportExceptionCode.DUPLICATE_TASK.create(I(session.getUserId()), I(session.getContextId()));
        }

        JSONObject jId = new JSONObject(2).put("id", UUIDs.getUnformattedString(optionalTaskId.get()));
        return new AJAXRequestResult(jId, "json");
    }

}
