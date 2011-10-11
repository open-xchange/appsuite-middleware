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

package com.openexchange.filemanagement.json.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.AJAXFile;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Action;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link HTMLAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@Action(defaultFormat = "preview")
public class HTMLAction implements AJAXActionService {    

    public HTMLAction(ServiceLookup serviceLookup) {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        final String id = request.getParameter(AJAXFile.PARAMETER_ID);
        if (id == null || id.length() == 0) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXFile.PARAMETER_ID).setAction(AJAXFile.ACTION_GET);
        }
        
        final ManagedFileManagement fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        final ManagedFile file = fileManagement.getByID(id);     
        
        /*
         * Get content as String
         */
        final String content;
        BufferedReader reader = null;
        StringWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            writer  = new StringWriter();
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
            
            writer.flush();
            content = writer.toString();
        } catch (UnsupportedEncodingException e) {
            throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
            Streams.close(writer);
        }   
        
        final PreviewDocument previewDocument = new PreviewDocument() {
            
            @Override
            public Map<String, String> getMetaData() {
                final Map<String, String> metaData = new HashMap<String, String>(2);
                metaData.put("content-type", file.getContentType());
                metaData.put("resourcename", file.getFileName());
                
                return metaData;
            }
            
            @Override
            public String getContent() {
                return content;
            }
        };
        
        return new AJAXRequestResult(previewDocument, "preview");
    }

}
