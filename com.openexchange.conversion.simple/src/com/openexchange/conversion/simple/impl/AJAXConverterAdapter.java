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

package com.openexchange.conversion.simple.impl;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXConverterAdapter} Implementation of the {@link SimpleConverter}
 * conversion service to convert Object "data" from a certain format to a
 * certain format, based on the Converter classes used for converting
 * AJAXRequestResults.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AJAXConverterAdapter implements SimpleConverter{

	private final Converter converter;

	public AJAXConverterAdapter(Converter converter) {
		this.converter = converter;
    }

    @Override
    public Object convert(String from, String to, Object data, ServerSession session) throws OXException {
        AJAXRequestResult result = new AJAXRequestResult(data, from);
        converter.convert(from, to, new AJAXRequestData(), result, session);
        return result.getResultObject();
    }

}
