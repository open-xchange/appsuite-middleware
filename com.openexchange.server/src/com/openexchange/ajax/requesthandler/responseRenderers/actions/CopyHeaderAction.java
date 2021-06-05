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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;

/**
 * {@link CopyHeaderAction} - Copies headers into the response
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>response
 * </ul>
 * 
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class CopyHeaderAction implements IFileResponseRendererAction {

    @Override
    public void call(IDataWrapper data) throws Exception {
        HttpServletResponse response = data.getResponse();
        AJAXRequestResult result = data.getResult();

        /*
         * The result is send to the client, so copy headers
         * set to the response into the result.
         */
        for (Entry<String, String> entry : result.getHeaders().entrySet()) {
            if (null == response.getHeader(entry.getKey())) {
                response.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

}
