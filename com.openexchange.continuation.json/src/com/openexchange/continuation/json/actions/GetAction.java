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

package com.openexchange.continuation.json.actions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.continuation.Continuation;
import com.openexchange.continuation.ContinuationExceptionCodes;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.continuation.ContinuationResponse;
import com.openexchange.continuation.json.ContinuationRequest;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class GetAction extends AbstractContinuationAction {

    /**
     * Initializes a new {@link GetAction}.
     *
     * @param services The service look-up
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ContinuationRequest req) throws OXException, JSONException {
        // Acquire service
        final ContinuationRegistryService continuationRegistry = getService(ContinuationRegistryService.class);
        if (null == continuationRegistry) {
            throw ServiceExceptionCode.absentService(ContinuationRegistryService.class);
        }

        // Require UUID
        final String sUUID = req.checkParameter("uuid");
        final UUID uuid = UUIDs.fromUnformattedString(sUUID);

        // Check registry
        final Continuation<Object> continuation = continuationRegistry.getContinuation(uuid, req.getSession());
        if (null == continuation) {
            throw ContinuationExceptionCodes.NO_SUCH_CONTINUATION.create(sUUID);
        }

        try {
            final ContinuationResponse<Object> cr = continuation.getNextResponse(5, TimeUnit.SECONDS);

            if (null == cr.getValue()) {
                // Continuation already consumed
                continuationRegistry.removeContinuation(uuid, req.getSession());
                throw ContinuationExceptionCodes.NO_SUCH_CONTINUATION.create(sUUID);
            }

            final boolean completed = cr.isCompleted();
            if (completed) {
                continuationRegistry.removeContinuation(uuid, req.getSession());
            }

            return new AJAXRequestResult(cr.getValue(), cr.getTimeStamp(), cr.getFormat()).setContinuationUuid(completed ? null : uuid);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

}
