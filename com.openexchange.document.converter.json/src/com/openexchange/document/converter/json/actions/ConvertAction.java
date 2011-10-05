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

package com.openexchange.document.converter.json.actions;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.document.converter.ByteArrayDocumentContent;
import com.openexchange.document.converter.DocumentContent;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ConvertAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConvertAction extends AbstractDocumentConverterAction {

    /**
     * Initializes a new {@link ConvertAction}.
     * @param serviceLookup
     */
    public ConvertAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        String format = request.getParameter("format");
        String source = request.getParameter("source");
        JSONObject data = (JSONObject) request.getData();
        
        DocumentContent convertedDocument = null;
        if (source.equals("mail")) {
            convertedDocument = convertMail(request, session, data, format);
        } else if (source.equals("infostore")) {
            convertedDocument = convertInfostore(request, session, data, format);
        } else {
            // TODO: throw exception...
        }
        
        java.io.File optFile = convertedDocument.optFile();
        FileHolder holder = new FileHolder(convertedDocument.getInputStream(), optFile.length(), convertedDocument.getContentType(), convertedDocument.getName());
        return new AJAXRequestResult(holder, "file");
    }

    /**
     * @param request
     * @param session
     * @param data
     * @param format
     * @throws OXException 
     */
    private DocumentContent convertInfostore(AJAXRequestData request, ServerSession session, JSONObject data, String format) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = getFileAccessFactory();
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        
        try {
            String id = data.getString("id");
            int version = data.getInt("version");
            File fileMetadata = fileAccess.getFileMetadata(id, version);
            InputStream documentIS = fileAccess.getDocument(id, version);
            byte[] documentBytes = Streams.stream2bytes(documentIS);
            
            DocumentContent document = new ByteArrayDocumentContent(documentBytes, fileMetadata.getFileName(), fileMetadata.getFileMIMEType());
            return convertDocument(document, format);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
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
    private DocumentContent convertMail(AJAXRequestData request, ServerSession session, JSONObject data, String format) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private DocumentContent convertDocument(DocumentContent document, String format) throws OXException {
        DocumentConverterService converterService = getConverterService();
        DocumentContent converted = converterService.convert(document, format);
        
        return converted;
    }

}
