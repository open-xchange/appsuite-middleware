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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.io.IOException;
import java.util.Collection;
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

            final JSONArray jsonArray = new JSONArray();
            for (final String previewPage : previewDocument.getContent()) {
                jsonArray.put(previewPage);
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
        } catch (final JSONException e) {
            LOG.error("JSON Error", e);
        }
    }

}
