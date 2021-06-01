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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MimeTypeException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;

/**
 * {@link FileEmbeddedDocumentExtractor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileEmbeddedDocumentExtractor implements EmbeddedDocumentExtractor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileEmbeddedDocumentExtractor.class);

    private final TikaDocumentHandler documentHandler;

    private final ManagedFileManagement fileManagement;

    private int count;

    private final TikaConfig config;

    /**
     * Initializes a new {@link FileEmbeddedDocumentExtractor}.
     *
     * @param documentHandler
     */
    public FileEmbeddedDocumentExtractor(final TikaDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
        fileManagement = documentHandler.serviceLookup.getService(ManagedFileManagement.class);
        count = 1;
        config = TikaConfig.getDefaultConfig();
    }

    @Override
    public boolean shouldParseEmbedded(final Metadata metadata) {
        return true;
    }

    @Override
    public void parseEmbedded(final InputStream inputStream, final ContentHandler contentHandler, final Metadata metadata, final boolean outputHtml) throws SAXException, IOException {
        InputStream in = inputStream;
        try {
            final String resourceName = metadata.get(TikaMetadataKeys.RESOURCE_NAME_KEY);
            String name = resourceName;
            if (name == null) {
                name = Integer.toString(count++);
            }
            /*
             * MIME type
             */
            String contentType = metadata.get(HttpHeaders.CONTENT_TYPE);
            if (name.indexOf('.') == -1 && contentType != null) {
                try {
                    name += config.getMimeRepository().forName(contentType).getExtension();
                } catch (MimeTypeException e) {
                    LOG.debug("Invalid MIME type encountered: {}", contentType, e);
                }
                if (name.indexOf('.') == -1) {
                    final byte[] bytes = Streams.stream2bytes(in);
                    contentType = documentHandler.getDocumentType(Streams.newByteArrayInputStream(bytes));
                    try {
                        name += config.getMimeRepository().forName(contentType).getExtension();
                    } catch (MimeTypeException e) {
                        LOG.debug("Invalid MIME type encountered: {}", contentType, e);
                    }
                    in = Streams.newByteArrayInputStream(bytes);
                }
            }
            /*
             * Copy to new file
             */
            final int pos = name.indexOf('.');
            String prefix = name.substring(0, pos);
            if (prefix.length() < 2) {
                prefix += "00";
            }
            final File outputFile = fileManagement.newTempFile(prefix + '-', name.substring(pos));
            final FileOutputStream os = new FileOutputStream(outputFile);
            try {
                IOUtils.copy(in, os);
            } finally {
                IOUtils.closeQuietly(os);
            }
            documentHandler.extractedFiles.put(resourceName, fileManagement.createManagedFile(outputFile));
        } catch (OXException e) {
            throw new IOException("An Open-Xchange error occurred.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
