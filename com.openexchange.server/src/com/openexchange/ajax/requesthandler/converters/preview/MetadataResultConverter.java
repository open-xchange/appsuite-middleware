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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.metadata.MetadataExceptionCodes;
import com.openexchange.metadata.MetadataMap;
import com.openexchange.metadata.MetadataService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MetadataResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link MetadataResultConverter}.
     */
    public MetadataResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "file";
    }

    @Override
    public String getOutputFormat() {
        return "metadata";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        IFileHolder fileHolder = null;
        try {
            MetadataService metadataService = ServerServiceRegistry.getInstance().getService(MetadataService.class, true);
            {
                final Object resultObject = result.getResultObject();
                if (!(resultObject instanceof IFileHolder)) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
                }
                fileHolder = (IFileHolder) resultObject;
            }

            // Check file holder's content
            if (0 == fileHolder.getLength()) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("File holder has no content, hence no preview can be generated.");
            }

            // Grab stream
            InputStream stream = fileHolder.getStream();
            BufferedInputStream bufferedStream = null;
            try {
                bufferedStream = stream instanceof BufferedInputStream ? (BufferedInputStream) stream : new BufferedInputStream(stream, 65536);
                bufferedStream.mark(65536);

                {
                    int read = bufferedStream.read();
                    if (read < 0) {
                        throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("File holder has no content, hence no preview can be generated.");
                    }
                    bufferedStream.reset();
                }

                FileType detectedFileType;
                try {
                    bufferedStream.mark(65536);
                    detectedFileType = FileTypeDetector.detectFileType(bufferedStream);
                } catch (@SuppressWarnings("unused") AssertionError e) {
                    detectedFileType = FileType.Unknown;
                }

                if (FileType.Unknown == detectedFileType) {
                    result.setResultObject(MetadataMap.EMPTY, getOutputFormat());
                    return;
                }

                try {
                    bufferedStream.reset();
                } catch (IOException e) {
                    // Reset failed. Check if it is possible to acquire a new stream.
                    if (!fileHolder.repetitive()) {
                        throw e;
                    }

                    // Close old ones and acquire new
                    Streams.close(bufferedStream, stream);
                    stream = fileHolder.getStream();
                    bufferedStream = stream instanceof BufferedInputStream ? (BufferedInputStream) stream : new BufferedInputStream(stream, 65536);
                }

                try {
                    MetadataMap metadataMap = metadataService.getMetadata(stream, detectedFileType.getName());
                    result.setResultObject(metadataMap, getOutputFormat());
                } catch (OXException e) {
                    if (MetadataExceptionCodes.METADATA_FAILED.equals(e)) {
                        result.setResultObject(MetadataMap.EMPTY, getOutputFormat());
                        return;
                    }
                    throw e;
                }
            } finally {
                Streams.close(bufferedStream, stream);
            }
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fileHolder);
        }
    }

}
