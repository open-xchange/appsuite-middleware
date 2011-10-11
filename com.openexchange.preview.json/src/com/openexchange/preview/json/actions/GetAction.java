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

package com.openexchange.preview.json.actions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GetAction extends AbstractPreviewAction {

    /**
     * Initializes a new {@link GetAction}.
     * @param serviceLookup
     */
    public GetAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        final String source = request.getParameter("source");
        JSONObject data = (JSONObject) request.getData();
        
        /*
         * Fill data for testing purposes
         */
        data = new JSONObject();
        try {
            data.put("version", 1);
            data.put("id", 4973);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        PreviewDocument previewDocument = null;
        if (source.equals("mail")) {
            previewDocument = convertMail(request, session, data);
        } else if (source.equals("infostore")) {            
            previewDocument = convertInfostore(request, session, data);
        } else {
            // TODO: throw exception...
        }
        
        final ManagedFile managedFile;
        try {
            final ManagedFileManagement fileManagement = getFileManagementService();
            final java.io.File tempFile = fileManagement.newTempFile();
            final FileOutputStream fos = new FileOutputStream(tempFile);
            try {
                fos.write(previewDocument.getContent().getBytes("UTF-8"));
                fos.flush();
            } finally {
                Streams.close(fos);
            }
            managedFile = fileManagement.createManagedFile(tempFile);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        /*
         * Set meta data
         */
        final Map<String, String> metaData = previewDocument.getMetaData();
        managedFile.setContentType(metaData.get("content-type"));
        managedFile.setFileName(metaData.get("resourcename"));
        
        return new AJAXRequestResult(previewDocument, "preview");
    }

    /**
     * @param request
     * @param session
     * @param data
     * @param format
     * @throws OXException 
     */
    private PreviewDocument convertInfostore(final AJAXRequestData request, final ServerSession session, final JSONObject data) throws OXException {
        final IDBasedFileAccessFactory fileAccessFactory = getFileAccessFactory();
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        
        try {
            final String id = data.getString("id");
            final int version = data.getInt("version");
            final File fileMetadata = fileAccess.getFileMetadata(id, version);
            
            final DataProperties dataProperties = new DataProperties(4);
            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, fileMetadata.getFileMIMEType());
            dataProperties.put(DataProperties.PROPERTY_NAME, fileMetadata.getFileName());
            dataProperties.put(DataProperties.PROPERTY_SIZE, String.valueOf(fileMetadata.getFileSize()));
            final Data<InputStream> documentData = new Data<InputStream>() {
                
                @Override
                public DataProperties getDataProperties() {
                    return dataProperties;
                }
                
                @Override
                public InputStream getData() {
                    try {
                        return fileAccess.getDocument(id, version);
                    } catch (final OXException e) {
                        return Streams.newByteArrayInputStream(new byte[0]);
                    }
                }
            };
            
            final PreviewService previewService = getPreviewService();
            final PreviewDocument preview = previewService.getPreviewFor(documentData, PreviewOutput.HTML, session);
            
            return preview;
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }  
    }

    /**
     * @param request
     * @param session
     * @param data
     * @param format
     */
    private PreviewDocument convertMail(final AJAXRequestData request, final ServerSession session, final JSONObject data) {
        // TODO Auto-generated method stub
        return null;
    }
}
