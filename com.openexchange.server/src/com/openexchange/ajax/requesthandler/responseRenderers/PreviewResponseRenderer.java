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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.preview.PreviewDocument;

/**
 * {@link PreviewResponseRenderer} - The response renderer for {@link PreviewDocument}s.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PreviewResponseRenderer implements ResponseRenderer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PreviewResponseRenderer.class);

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return (result.getResultObject() instanceof PreviewDocument);
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest httpReq, final HttpServletResponse httpResp) throws IOException {
        //httpResp.setContentType(AJAXServlet.CONTENTTYPE_HTML);
        try {
            final PreviewDocument previewDocument = (PreviewDocument) result.getResultObject();
            if (previewDocument==null){
                LOG.error("The AJAXRequestResult doesn't contain a result object!");
                return;
            }

            JSONArray jsonArray;
            List<String> content = previewDocument.getContent();
            if (null != content) {
                jsonArray = new JSONArray(content.size());
                for (final String previewPage : content) {
                    jsonArray.put(previewPage);
                }
            } else {
                jsonArray = new JSONArray(0);
            }

            final JSONObject jsonObject = new JSONObject();
            if (previewDocument.isMoreAvailable() != null) {
                jsonObject.put("moreAvailable", previewDocument.isMoreAvailable());
            }
            jsonObject.put("document", jsonArray);

            final Response response = new Response(request.getSession());
            response.setTimestamp(result.getTimestamp());
            response.setData(jsonObject);
            response.setProperties(result.getResponseProperties());

            final Collection<OXException> warnings = result.getWarnings();
            if (warnings != null && !warnings.isEmpty()) {
                for (final OXException warning : warnings) {
                    response.addWarning(warning);
                }
            }
            APIResponseRenderer.writeResponse(response, request.getAction(), httpReq, httpResp);
        } catch (JSONException e) {
            LOG.error("JSON Error", e);
        }
    }

}
