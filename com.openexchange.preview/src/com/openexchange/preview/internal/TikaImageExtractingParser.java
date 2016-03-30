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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;

/**
 * {@link TikaImageExtractingParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaImageExtractingParser implements Parser {

    private static final long serialVersionUID = -8054020195071839180L;

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(TikaImageExtractingParser.class);

    private static final Set<MediaType> TYPES_IMAGE;

    private static final Set<MediaType> TYPES_EXCEL;

    private static final Set<MediaType> TYPES;

    static {
        Set<MediaType> types = new HashSet<MediaType>(6);
        types.add(MediaType.image("bmp"));
        types.add(MediaType.image("gif"));
        types.add(MediaType.image("jpg"));
        types.add(MediaType.image("jpeg"));
        types.add(MediaType.image("png"));
        types.add(MediaType.image("tiff"));
        TYPES_IMAGE = Collections.unmodifiableSet(types);
        types = new HashSet<MediaType>(1);
        types.add(MediaType.image("vnd.ms-excel"));
        TYPES_EXCEL = Collections.unmodifiableSet(types);

        types = new HashSet<MediaType>(TYPES_IMAGE);
        types.addAll(TYPES_EXCEL);
        TYPES = Collections.unmodifiableSet(types);
    }

    private final TikaDocumentHandler documentHandler;

    private final TikaConfig config;

    private final ManagedFileManagement fileManagement;

    private final int count = 0;

    public TikaImageExtractingParser(final TikaDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
        config = TikaConfig.getDefaultConfig();
        fileManagement = documentHandler.serviceLookup.getService(ManagedFileManagement.class);
    }

    @Override
    public Set<MediaType> getSupportedTypes(final ParseContext context) {
        return TYPES;
    }

    @Override
    public void parse(final InputStream stream, final ContentHandler handler, final Metadata metadata, final ParseContext context) throws IOException, SAXException, TikaException {
        if (handledImage(stream, metadata)) {
            return;
        }
        if (handledExcel(stream, metadata)) {
            return;
        }
    }

    private boolean handledExcel(final InputStream stream, final Metadata metadata) throws IOException {
        final String fileName = metadata.get(TikaMetadataKeys.RESOURCE_NAME_KEY);
        final String type = metadata.get(HttpHeaders.CONTENT_TYPE);
        if (type != null) {
            for (final MediaType mt : TYPES_EXCEL) {
                if (mt.toString().equals(type)) {
                    //handleImage(stream, fileName, type);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handledImage(final InputStream stream, final Metadata metadata) throws IOException {
        /*
         * Is it a supported image?
         */
        final String fileName = metadata.get(TikaMetadataKeys.RESOURCE_NAME_KEY);
        final String type = metadata.get(HttpHeaders.CONTENT_TYPE);
        if (type != null) {
            for (final MediaType mt : TYPES_IMAGE) {
                if (mt.toString().equals(type)) {
                    handleImage(stream, fileName, type);
                    return true;
                }
            }
        }
        if (fileName != null) {
            for (final MediaType mt : TYPES_IMAGE) {
                final String ext = "." + mt.getSubtype();
                if (fileName.endsWith(ext)) {
                    handleImage(stream, fileName, type);
                    return true;
                }
            }
        }
        return false;
    }

    public void parse(final InputStream stream, final ContentHandler handler, final Metadata metadata) throws IOException, SAXException, TikaException {
        parse(stream, handler, metadata, new ParseContext());
    }

    private void handleImage(final InputStream stream, final String fileName, final String type) throws IOException {
        final InputStream in = stream;
        try {
            final ManagedFile managedFile = documentHandler.extractedFiles.get(fileName);
            managedFile.setContentType(type);
            managedFile.setFileName(fileName);
            final File outputFile = managedFile.getFile();
            final FileOutputStream os = new FileOutputStream(outputFile);
            try {
                IOUtils.copy(in, os);
            } finally {
                IOUtils.closeQuietly(os);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
