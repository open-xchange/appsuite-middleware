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

import static com.openexchange.java.Autoboxing.I;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONException;
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
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class GetAction extends AbstractJobQueueJsonAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetAction.class);

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        JobQueueService jobQueue = services.getOptionalService(JobQueueService.class);
        if (null == jobQueue) {
            throw ServiceExceptionCode.absentService(JobQueueService.class);
        }

        // Get "id" and parse it to UUID
        String identifier = requestData.requireParameter("id");
        UUID id;
        try {
            id = UUIDs.fromUnformattedString(identifier);
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "id", identifier);
        }

        JobInfo jobInfo = jobQueue.require(id, session.getUserId(), session.getContextId());
        try {
            long maxRequestAgeMillis = jobQueue.getMaxRequestAgeMillis();
            return jobInfo.get(maxRequestAgeMillis, TimeUnit.MILLISECONDS, true);
        } catch (InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "Interrupted");
        } catch (TimeoutException e) {
            // Result not computed in time
            LOG.debug("Action \"{}\" of module \"{}\" could not be executed in time for user {} in context {}.", requestData.getAction(), requestData.getModule(), I(session.getUserId()), I(session.getContextId()), e);
            return new AJAXRequestResult(jobInfo, "enqueued");
        }
    }

    @Override
    public String getAction() {
        return "get";
    }

}
