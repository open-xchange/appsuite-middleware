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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.thirdwing.common.ConversionJobfactory;
import net.thirdwing.common.IConversionJob;
import net.thirdwing.exception.XHTMLConversionException;
import net.thirdwing.io.IOUnit;
import net.thirdwing.io.iface.DocumentData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewPolicy;
import com.openexchange.preview.Quality;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link ThirdwingPreviewService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ThirdwingPreviewService implements InternalPreviewService {
    
    private ServiceLookup serviceLookup;
    
    private static final PreviewPolicy[] POLICIES = new PreviewPolicy[10];
    
    static {
        int i = 0;
        POLICIES[i++] = new PreviewPolicy("application/vnd.openxmlformats-officedocument.wordprocessingml.document", PreviewOutput.HTML, Quality.GOOD);
    }  
    
    
    public ThirdwingPreviewService(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public String detectDocumentType(InputStream inputStream) throws OXException {
        return null;
    }

    @Override
    public PreviewDocument getPreviewFor(String arg, PreviewOutput output, Session session) throws OXException {
        File file = new File(arg);
        return generatePreview(file, session);
    }

    @Override
    public PreviewDocument getPreviewFor(InputStream inputStream, String mimeType, String extension, PreviewOutput output, Session session) throws OXException {    
        File file = null;
        try {
            file = streamToFile(inputStream, extension);
            return generatePreview(file, session);
        } catch (IOException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Override
    public PreviewDocument getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session) throws OXException {
        File file = null;
        try {
            file = streamToFile(documentData.getData(), documentData.getDataProperties().get(DataProperties.PROPERTY_NAME));
            return generatePreview(file, session);
        } catch (IOException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } finally {
            if (file != null) {
                file.delete();
            }
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
    
    private PreviewDocument generatePreview(File file, Session session) throws OXException {
        IConversionJob transformer = ConversionJobfactory.getTransformer(file);
        StreamProvider streamProvider = new StreamProvider(serviceLookup);        
        TransformationObservationTask observationTask = new TransformationObservationTask(streamProvider, session);
        
        ThreadPoolService poolService = serviceLookup.getService(ThreadPoolService.class);
        Future<String> future = poolService.submit(observationTask);
        IOUnit unit;
        try {
            unit = new IOUnit(new FileInputStream(file));
            unit.setStreamProvider(streamProvider);
            transformer.addObserver(observationTask);
            transformer.transformDocument(unit);
            
            String content = future.get();
            if (content == null) {
                throw observationTask.getException();
            }
            
            Map<String, String> metaData = new HashMap<String, String>();
            metaData.put("content-type", "text/html");
            metaData.put("resourcename", file.getName());
            ThirdwingPreviewDocument previewDocument = new ThirdwingPreviewDocument(metaData, content);
            return previewDocument;
        } catch (FileNotFoundException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (XHTMLConversionException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (InterruptedException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (ExecutionException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        }
    }
    
    private File streamToFile(InputStream is, String name) throws IOException {
        File file = File.createTempFile("open-xchange", name);
        FileOutputStream fos = new FileOutputStream(file);
        
        byte[] buf = new byte[2048];
        int len;
        while((len=is.read(buf))>0) {
            fos.write(buf, 0, len);
        }
        fos.flush();
        fos.close();
        
        return file;
    }

}
