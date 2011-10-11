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

package com.openexchange.preview.thirdwing.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import net.thirdwing.exception.XHTMLConversionException;
import net.thirdwing.io.IStreamProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link StreamProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class StreamProvider implements IStreamProvider {
    
    private static final String DOCUMENT = "document.html";
    
    private final Map<String, ManagedFile> createdFiles = new HashMap<String, ManagedFile>();
    
    private ServiceLookup serviceLookup;
    
    
    public StreamProvider(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public OutputStream createFile(String fileName) throws XHTMLConversionException {
        ManagedFileManagement fileManagement = serviceLookup.getService(ManagedFileManagement.class);
        try {
            File tempFile = fileManagement.newTempFile();
            FileOutputStream fos = new FileOutputStream(tempFile);
            String mimeType = MIMEType2ExtMap.getContentType(fileName);
            ManagedFile managedFile = fileManagement.createManagedFile(tempFile);
            managedFile.setContentType(mimeType);
            createdFiles.put(fileName, managedFile);
            
            return fos;
        } catch (OXException e) {
            throw new XHTMLConversionException("Could not create OutputStream for file " + fileName, e);
        } catch (FileNotFoundException e) {
            throw new XHTMLConversionException("Could not create OutputStream for file " + fileName, e);
        }
    }
    
    public String getLinkForFile(String fileName, Session session) throws OXException {
        ManagedFile managedFile = createdFiles.get(fileName); 
        if (managedFile != null) {
            return managedFile.constructURL(session);
        } else { 
            return null;
        }
    }
    
    public String getDocumentContent() throws OXException {
        ManagedFile managedFile = createdFiles.get(DOCUMENT);
        if (managedFile != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(managedFile.getFile()), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                
                return sb.toString();
            } catch (FileNotFoundException e) {
                // TODO: throw proper exception
                throw PreviewExceptionCodes.ERROR.create();
            } catch (UnsupportedEncodingException e) {
                // TODO: throw proper exception
                throw PreviewExceptionCodes.ERROR.create();
            } catch (IOException e) {
             // TODO: throw proper exception
                throw PreviewExceptionCodes.ERROR.create();
            }            
        } else { 
            // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        }
    }

}
