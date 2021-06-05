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

package com.openexchange.ajax.requesthandler.jobqueue.json.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link CancelAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class CancelAction extends AbstractJobQueueJsonAction {

    /**
     * Initializes a new {@link CancelAction}.
     */
    public CancelAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        JobQueueService jobQueue = services.getOptionalService(JobQueueService.class);
        if (null == jobQueue) {
            throw ServiceExceptionCode.absentService(JobQueueService.class);
        }

        // Get "id"s and parse them to UUIDs
        List<UUID> ids;
        {
            JSONArray jUUIDs = (JSONArray) requestData.requireData();
            ids = new ArrayList<>(jUUIDs.length());
            for (Object jUUID : jUUIDs) {
                try {
                    ids.add(UUIDs.fromUnformattedString(jUUID.toString()));
                } catch (IllegalArgumentException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "id", jUUID);
                }
            }
        }

        for (UUID id : ids) {
            JobInfo jobInfo = jobQueue.get(id, session.getUserId(), session.getContextId());
            if (null != jobInfo) {
                jobInfo.cancel(true);
            }
        }
        return new AJAXRequestResult(new JSONObject(2).put("success", true), "json");
    }

    @Override
    public String getAction() {
        return "cancel";
    }

}
