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

package com.openexchange.ajax.requesthandler;


/**
 * {@link AJAXRequestResultPostProcessor} - A processor for an {@link AJAXRequestResult} instance that performs post-processing tasks.
 * <p>
 * A post-processor is added to an instance of {@code AJAXRequestResult} using the
 * {@link AJAXRequestResult#addPostProcessor(AJAXRequestResultPostProcessor) addPostProcessor} method when currently executing the
 * {@link AJAXActionService#perform(AJAXRequestData, com.openexchange.tools.session.ServerSession) perform} routine of an
 * {@code AJAXActionService} instance.
 * <p>
 * Special {@link HttpErrorCodeException} might be passed in case HTTP flow terminated without an error, but an HHTP error code was returned
 * to the client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface AJAXRequestResultPostProcessor {

    /**
     * Invoked if associated {@link AJAXRequestResult} instance is done and this processor's post-processing tasks are ready being performed.
     *
     * @param requestData The request data associated with the result or <code>null</code>
     * @param requestResult The request result that is done
     * @param e The exception (or <code>HttpErrorCodeException</code>) that caused termination, or <code>null</code> if execution completed normally
     */
    void doPostProcessing(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e);
}
