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

package com.openexchange.rest.client.v2.parser;

import java.io.InputStream;
import java.util.Set;
import org.apache.http.HttpResponse;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link RESTResponseBodyParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface RESTResponseBodyParser {

    /**
     * Parses the {@link InputStream} from the specified {@link HttpResponse}
     * and sets the response body to the specified {@link RESTResponse}
     * 
     * @param httpResponse The {@link HttpResponse}
     * @param restResponse The {@link RESTResponse}
     * @throws OXException if a parsing error occurs
     */
    void parse(HttpResponse httpResponse, RESTResponse restResponse) throws OXException;

    /**
     * Returns a {@link Set} with all the content types that this parser supports
     * 
     * @return a {@link Set} with all the content types that this parser supports
     */
    Set<String> getContentTypes();
}
