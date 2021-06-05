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

package com.openexchange.ajax.requesthandler.converters.cover;

import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CoverResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CoverResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link CoverResultConverter}.
     */
    public CoverResultConverter() {
        super();
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public String getOutputFormat() {
        return "cover";
    }

    @Override
    public String getInputFormat() {
        return "file";
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final IFileHolder fileHolder;
        {
            final Object resultObject = result.getResultObject();
            if (!(resultObject instanceof IFileHolder)) {
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(
                    IFileHolder.class.getSimpleName(),
                    null == resultObject ? "null" : resultObject.getClass().getSimpleName());
            }
            fileHolder = (IFileHolder) resultObject;
        }
        try {
            /*
             * Obtain cover
             */
            final CoverExtractorRegistry extractorRegistry = CoverExtractorRegistry.REGISTRY_REFERENCE.get();
            if (null == extractorRegistry) {
                throw AjaxExceptionCodes.UNSUPPORTED_FORMAT.create(getOutputFormat());
            }
            CoverExtractor extractor = null;
            for (final CoverExtractor cur : extractorRegistry.getExtractors()) {
                if (cur.handlesFile(fileHolder)) {
                    extractor = cur;
                    break;
                }
            }
            if (null == extractor) {
                throw AjaxExceptionCodes.UNSUPPORTED_FORMAT.create(getOutputFormat());
            }
            result.setResultObject(extractor.extractCover(fileHolder), getOutputFormat());
        } finally {
            Streams.close(fileHolder);
        }
    }

}
