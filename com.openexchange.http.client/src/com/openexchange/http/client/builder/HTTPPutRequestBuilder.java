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

package com.openexchange.http.client.builder;

import java.io.InputStream;
import com.openexchange.exception.OXException;

public interface HTTPPutRequestBuilder extends HTTPGenericRequestBuilder<HTTPPutRequestBuilder> {

    /**
     * Sets the body
     *
     * @param body The body as a string
     * @return This {@link HTTPRequestBuilder}
     * @throws OXException
     */
	public HTTPPutRequestBuilder body(String body) throws OXException;

    /**
     * Sets the body
     *
     * @param body The body as a {@link InputStream}
     * @return This {@link HTTPRequestBuilder}
     * @throws OXException
     */
	public HTTPPutRequestBuilder body(InputStream body) throws OXException;

    /**
     * Sets the content type
     *
     * @param ctype Ther content type
     * @return This {@link HTTPRequestBuilder}
     */
	public HTTPPutRequestBuilder contentType(String ctype);
}
