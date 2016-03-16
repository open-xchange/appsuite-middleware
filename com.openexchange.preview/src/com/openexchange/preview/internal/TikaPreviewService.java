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
        } catch (final MalformedURLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
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
