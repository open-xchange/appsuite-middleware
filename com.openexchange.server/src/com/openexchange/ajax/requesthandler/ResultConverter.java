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
 * {@link ResultConverter} is used to convert {@link AJAXRequestData} and
 * {@link AJAXRequestResult} data from the given input to the specified output
 * format.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public interface ResultConverter {

    /**
     * A converter's quality.
     */
    public static enum Quality {
        GOOD, BAD;
    }

    /**
     * Gets the input format.
     *
     * @return The input format
     */
    String getInputFormat();

    /**
     * Gets the output format.
     *
     * @return The output format
     */
    String getOutputFormat();

    /**
     * Gets the quality.
     *
     * @return The quality
     */
    Quality getQuality();

    /**
     * Converts specified request data and result pair using given converter.
     *
     * @param requestData The request data
     * @param result The result
     * @param session The associated session
     * @param converter The converter
     * @throws OXException If conversion fails
     */
    void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException;

}
