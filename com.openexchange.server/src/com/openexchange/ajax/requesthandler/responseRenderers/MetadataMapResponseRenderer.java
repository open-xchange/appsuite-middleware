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

import java.io.IOException;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.metadata.MetadataMap;

/**
 * {@link MetadataMapResponseRenderer} - The response renderer for {@link MetadataMap}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataMapResponseRenderer implements ResponseRenderer {

    /**
     * Initializes a new {@link MetadataMapResponseRenderer}.
     */
    public MetadataMapResponseRenderer() {
        super();
    }

    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
        return (result.getResultObject() instanceof MetadataMap);
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest httpReq, final HttpServletResponse httpResp) throws IOException {
        MetadataMap metadataMap = (MetadataMap) result.getResultObject();
        if (metadataMap == null || metadataMap.isEmpty()) {
            httpResp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        JSONObject jMetadata = new JSONObject(metadataMap.asMap());

        Response response = new Response(request.getSession());
        response.setTimestamp(result.getTimestamp());
        response.setData(jMetadata);
        response.setProperties(result.getResponseProperties());

        final Collection<OXException> warnings = result.getWarnings();
        if (!warnings.isEmpty()) {
            for (final OXException warning : warnings) {
                response.addWarning(warning);
            }
        }
        APIResponseRenderer.writeResponse(response, request.getAction(), httpReq, httpResp);
    }

}
