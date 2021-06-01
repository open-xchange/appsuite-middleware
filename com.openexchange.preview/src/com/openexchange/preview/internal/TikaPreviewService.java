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

package com.openexchange.preview.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link TikaPreviewService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaPreviewService implements PreviewService {

    private static final String UTF_8 = "UTF-8";

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link TikaPreviewService}.
     */
    public TikaPreviewService(final ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    private TikaDocumentHandler newDefaultHandler(final Session session) throws OXException {
        return new TikaDocumentHandler(UTF_8, serviceLookup, session);
    }

    private TikaDocumentHandler newHandler(final String mimeType, final Session session) throws OXException {
        return new TikaDocumentHandler(mimeType, UTF_8, serviceLookup, session);
    }

    @Override
    public String detectDocumentType(final InputStream inputStream) throws OXException {
        return newDefaultHandler(null).getDocumentType(inputStream);
    }

    @Override
    public PreviewDocument getPreviewFor(final String arg, final PreviewOutput output, final Session session, int pages) throws OXException {
        try {
            final URL url;
            {
                final File file = new File(arg);
                if (file.isFile()) {
                    url = file.toURI().toURL();
                } else {
                    url = new URL(arg);
                }
            }
            final TikaDocumentHandler documentHandler = newDefaultHandler(session);
            final InputStream input = TikaInputStream.get(url, documentHandler.getMetadata());
            return getPreviewFor(documentHandler, input, output);
        } catch (MalformedURLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session, int pages) throws OXException {
        final DataProperties dataProperties = documentData.getDataProperties();
        /*
         * Get content according to output format
         */
        return getPreviewFor(newHandler(dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE), session), documentData.getData(), output);
    }

    private PreviewDocument getPreviewFor(final TikaDocumentHandler documentHandler, final InputStream inputStream, final PreviewOutput output) throws OXException {
        /*
         * Get content according to output format
         */
        final String content = documentHandler.getDocumentContent(inputStream, output);
        /*
         * Convert meta data to a map
         */
        final Metadata metadata = documentHandler.getMetadata();
        final String[] names = metadata.names();
        final Map<String, String> map = new HashMap<String, String>(names.length);
        for (final String name : names) {
            map.put(com.openexchange.java.Strings.toLowerCase(name), metadata.get(name));
        }
        /*
         * Return preview document
         */
        return new TikaPreviewDocument(Collections.singletonList(content), map);
    }
}
