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
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

}
