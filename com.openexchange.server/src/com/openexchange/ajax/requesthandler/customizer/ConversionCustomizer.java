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

package com.openexchange.ajax.requesthandler.customizer;

import com.openexchange.ajax.requesthandler.AJAXActionCustomizer;
import com.openexchange.ajax.requesthandler.AJAXActionCustomizerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ConversionCustomizer} - Converts from request data's format to the format indicated by request result.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Some JavaDoc
 */
public class ConversionCustomizer implements AJAXActionCustomizer, AJAXActionCustomizerFactory {

    private final Converter converter;

    public ConversionCustomizer(final Converter converter) {
        this.converter = converter;
    }

    @Override
    public AJAXRequestData incoming(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        return requestData;
    }

    @Override
    public AJAXRequestResult outgoing(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
        final String requestedFormat = requestData.getFormat();
        final String currentFormat = result.getFormat();
        if (null == currentFormat || currentFormat.equals(requestedFormat)) {
            return result;
        }
        if (result.getType() == ResultType.HTTP_ERROR) {
            return result;
        }
        converter.convert(currentFormat, requestedFormat, requestData, result, session);
        return result;
    }

    @Override
    public AJAXActionCustomizer createCustomizer(final AJAXRequestData request, final ServerSession session) {
        return this;
    }

}
