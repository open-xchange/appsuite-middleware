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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.preview.jodconverter.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.document.converter.ByteArrayDocumentContent;
import com.openexchange.document.converter.DocumentContent;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewPolicy;
import com.openexchange.preview.Quality;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link JODCPreviewService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class JODCPreviewService implements InternalPreviewService {

    private final ServiceLookup serviceLookup;

    private static final PreviewPolicy[] POLICIES = new PreviewPolicy[10];

    static {
        int i = 0;
        POLICIES[i++] = new PreviewPolicy("application/vnd.oasis.opendocument.text", PreviewOutput.HTML, Quality.GOOD);
        POLICIES[i++] = new PreviewPolicy("application/vnd.oasis.opendocument.spreadsheet", PreviewOutput.HTML, Quality.GOOD);
    }


    public JODCPreviewService(final ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public String detectDocumentType(final InputStream inputStream) throws OXException {
        return null;
    }

    public PreviewDocument getPreviewFor(final String arg, final PreviewOutput output, final Session session) throws OXException {
        if (!isConvertableToOutput(output)) {
            return null;
        }

        final File file = new File(arg);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            final DocumentContent document = createDocumentContent(is, file.getName(), new MimetypesFileTypeMap().getContentType(file));

            return convertDocument(document, output);
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            Streams.close(is);
        }
    }

    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session) throws OXException {
        if (!isConvertableToOutput(output)) {
            return null;
        }

        final InputStream is = documentData.getData();
        final String name = documentData.getDataProperties().get(DataProperties.PROPERTY_NAME);
        final String mimeType = documentData.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
        final DocumentContent document = createDocumentContent(is, name, mimeType);
        return convertDocument(document, output);
    }

    private DocumentContent createDocumentContent(final InputStream is, final String fileName, final String mimeType) {
        try {
            final byte[] documentBytes = Streams.stream2bytes(is);
            final DocumentContent document = new ByteArrayDocumentContent(documentBytes, fileName, mimeType);

            return document;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private boolean isConvertableToOutput(final PreviewOutput output) {
        return getOutputExtension(output) != null;
    }

    private String getOutputExtension(final PreviewOutput output) {
        final String retval;
        switch (output) {

            case XHTML:
                retval = "xhtml";
                break;

            case HTML:
                retval = "html";
                break;

            case TEXT:
                retval = "txt";
                break;

            default:
                retval = null;
                break;

        }

        return retval;
    }


    private PreviewDocument convertDocument(final DocumentContent document, final PreviewOutput output) throws OXException {
        final DocumentConverterService converterService = serviceLookup.getService(DocumentConverterService.class);
        if (converterService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DocumentConverterService.class.getName());
        }

        final DocumentContent documentContent = converterService.convert(document, getOutputExtension(output));
        final JODCPreviewDocument previewDocument = new JODCPreviewDocument();
        final Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("content-type", documentContent.getContentType());
        metaData.put("resourcename", documentContent.getName());
        previewDocument.setMetaData(metaData);

        final InputStream is = documentContent.getInputStream();
        byte[] documentBytes;
        try {
            documentBytes = Streams.stream2bytes(is);
            final String str = new String(documentBytes, com.openexchange.java.Charsets.UTF_8);
            previewDocument.setContent(str);

            return previewDocument;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            Streams.close(is);
        }

    }

    @Override
    public List<PreviewPolicy> getPreviewPolicies() {
        return Arrays.asList(POLICIES);
    }

    @Override
    public boolean canDetectContentType() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.preview.PreviewService#getPreviewFor(java.lang.String, com.openexchange.preview.PreviewOutput, com.openexchange.session.Session, int)
     */
    @Override
    public PreviewDocument getPreviewFor(final String arg, final PreviewOutput output, final Session session, final int pages) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.preview.PreviewService#getPreviewFor(com.openexchange.conversion.Data, com.openexchange.preview.PreviewOutput, com.openexchange.session.Session, int)
     */
    @Override
    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session, final int pages) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
