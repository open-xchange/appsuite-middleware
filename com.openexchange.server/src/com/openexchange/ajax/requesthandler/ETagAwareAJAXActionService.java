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

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ETagAwareAJAXActionService} - Introduces <i>ETag</i> awareness to an {@link AJAXActionService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ETagAwareAJAXActionService extends AJAXActionService {

    /**
     * Checks for equality of passed client ETag compared to currently valid ETag for requested resource.
     * <p>
     * A new expiry may be specified by setting "X-ETag-Expiry" header in passed {@link AJAXRequestData} instance.
     *
     * @param clientETag The client's ETag
     * @param request The request data
     * @param session The session
     * @return <code>true</code> if ETags are equal; otherwise <code>false</code>
     * @throws OXException If checking ETag fails
     */
    boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException;

    /**
     * Sets specified <i>ETag</i> header with given optional expires value to specified request result.
     *
     * @param eTag The <i>ETag</i> header value
     * @param expires The optional expires; set to <code>-1</code> for no expiry
     * @param result The request result
     * @throws OXException If setting <i>ETag</i> fails
     */
    void setETag(String eTag, long expires, AJAXRequestResult result) throws OXException;
}
