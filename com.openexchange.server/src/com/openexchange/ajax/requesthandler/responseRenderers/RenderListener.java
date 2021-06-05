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

package com.openexchange.ajax.requesthandler.responseRenderers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.HttpErrorCodeException;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;

/**
 * {@link RenderListener} - A listener which receives various call-backs before/after a {@link ResponseRenderer#write(AJAXRequestData, AJAXRequestResult, HttpServletRequest, HttpServletResponse)} processing.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public interface RenderListener {

    /**
     * Checks whether this render listener wants to receive call-backs for given request data.
     *
     * @param request The associated request data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean handles(AJAXRequestData request);

    /**
     * Called before the write operation of the {@link ResponseRenderer} is invoked.
     *
     * @param request The associated request data
     * @param result The result
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws OXException If this listener signals to abort further processing
     * @see ResponseRenderer#write(AJAXRequestData, AJAXRequestResult, HttpServletRequest, HttpServletResponse)
     */
    void onBeforeWrite(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) throws OXException;

    /**
     * Called after the write operation of the {@link ResponseRenderer} is invoked.
     *
     * @param request The associated request data
     * @param result The request result that has been created
     * @param writeException The optional exception instance in case actual write yielded an error;
     *                       in case no <code>"200 - OK"</code> status was set an instance of {@link HttpErrorCodeException} is passed
     * @throws OXException If this listener signals to abort further processing
     */
    void onAfterWrite(AJAXRequestData request, AJAXRequestResult result, Exception writeException) throws OXException;
}
