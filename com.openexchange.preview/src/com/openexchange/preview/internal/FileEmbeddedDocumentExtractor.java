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

package com.openexchange.preview.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
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

    private final Set<MediaType> imageTypes;

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
        /*
         * Image type
         */
        imageTypes = new HashSet<MediaType>();
        imageTypes.add(MediaType.image("bmp"));
        imageTypes.add(MediaType.image("gif"));
        imageTypes.add(MediaType.image("jpg"));
        imageTypes.add(MediaType.image("jpeg"));
        imageTypes.add(MediaType.image("png"));
        imageTypes.add(MediaType.image("tiff"));
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
                } catch (final MimeTypeException e) {
                    LOG.debug("Invalid MIME type encountered: {}", contentType, e);
                }
                if (name.indexOf('.') == -1) {
                    final byte[] bytes = Streams.stream2bytes(in);
                    contentType = documentHandler.getDocumentType(Streams.newByteArrayInputStream(bytes));
                    try {
                        name += config.getMimeRepository().forName(contentType).getExtension();
                    } catch (final MimeTypeException e) {
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
        } catch (final OXException e) {
            throw new IOException("An Open-Xchange error occurred.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
