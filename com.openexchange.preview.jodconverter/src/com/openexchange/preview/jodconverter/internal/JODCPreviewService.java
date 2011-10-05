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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.HashMap;
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
import com.openexchange.server.ServiceExceptionCodes;
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
    
    
    public JODCPreviewService(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public String detectDocumentType(InputStream inputStream) throws OXException {
        return null;
    }

    @Override
    public PreviewDocument getPreviewFor(String arg, PreviewOutput output, Session session) throws OXException {
        if (!isConvertableToOutput(output)) {
            return null;
        }
        
        File file = new File(arg);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            DocumentContent document = createDocumentContent(is, file.getName(), new MimetypesFileTypeMap().getContentType(file));            
            
            return convertDocument(document, output);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            Streams.close(is);
        }
    }

    @Override
    public PreviewDocument getPreviewFor(InputStream is, String mimeType, String extension, PreviewOutput output, Session session) throws OXException {
        if (!isConvertableToOutput(output)) {
            return null;
        }
        
        DocumentContent document = createDocumentContent(is, "inputDoc." + extension, mimeType);       
        return convertDocument(document, output);
    }

    @Override
    public PreviewDocument getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session) throws OXException {
        if (!isConvertableToOutput(output)) {
            return null;
        }
        
        InputStream is = documentData.getData();
        String name = documentData.getDataProperties().get(DataProperties.PROPERTY_NAME);
        String mimeType = documentData.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
        DocumentContent document = createDocumentContent(is, name, mimeType);       
        return convertDocument(document, output);
    }
    
    private DocumentContent createDocumentContent(InputStream is, String fileName, String mimeType) {
        try {
            byte[] documentBytes = Streams.stream2bytes(is);
            DocumentContent document = new ByteArrayDocumentContent(documentBytes, fileName, mimeType);
            
            return document;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }         
    }
    
    private boolean isConvertableToOutput(PreviewOutput output) {
        return getOutputExtension(output) != null;
    }
    
    private String getOutputExtension(PreviewOutput output) {
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

    
    private PreviewDocument convertDocument(DocumentContent document, PreviewOutput output) throws OXException {
        DocumentConverterService converterService = serviceLookup.getService(DocumentConverterService.class);
        if (converterService == null) {
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(DocumentConverterService.class.getName());
        }
        
        DocumentContent documentContent = converterService.convert(document, getOutputExtension(output));
        JODCPreviewDocument previewDocument = new JODCPreviewDocument();
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("content-type", documentContent.getContentType());
        metaData.put("resourcename", documentContent.getName());
        previewDocument.setMetaData(metaData);
        
        InputStream is = documentContent.getInputStream();
        byte[] documentBytes;
        try {
            documentBytes = Streams.stream2bytes(is);
            String str = new String(documentBytes, "UTF-8");            
            previewDocument.setContent(str);
            
            return previewDocument;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            Streams.close(is);
        }
        
    }

    @Override
    public PreviewPolicy[] getPreviewPolicies() {
        return POLICIES;
    }

    @Override
    public boolean canDetectContentType() {
        return false;
    }

}
