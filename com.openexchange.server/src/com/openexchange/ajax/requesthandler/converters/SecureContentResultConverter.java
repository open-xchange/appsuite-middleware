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

package com.openexchange.ajax.requesthandler.converters;

import java.util.Collection;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.container.SecureContentResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SecureContentResultConverter} converts a {@link SecureContentWrapper} objects to {@link SecureContentResponse}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class SecureContentResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return SecureContentWrapper.CONTENT_TYPE;
    }

    @Override
    public String getOutputFormat() {
        return "apiResponse";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @SuppressWarnings("unused")
    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        final Response response = new Response(session);
        response.setData(((SecureContentWrapper) result.getResultObject()).getContent());
        response.setTimestamp(result.getTimestamp());
        response.setProperties(result.getResponseProperties());
        OXException exception = result.getException();
        if (null != exception) {
            response.setException(exception);
        }
        final Collection<OXException> warnings = result.getWarnings();
        if (null != warnings && !warnings.isEmpty()) {
            for (final OXException warning : warnings) {
                response.addWarning(warning);
            }
        }

        result.setResultObject(new SecureContentResponse(response));

    }

}
