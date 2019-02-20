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
 *    trademarks of the OX Software GmbH. group of companies.
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
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fileHolder);
        }
    }

}
