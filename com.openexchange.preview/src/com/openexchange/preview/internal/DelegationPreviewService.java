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

package com.openexchange.preview.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewPolicy;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.osgiservice.SimpleRegistryListener;
import com.openexchange.session.Session;


/**
 * {@link DelegationPreviewService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DelegationPreviewService implements PreviewService, SimpleRegistryListener<InternalPreviewService> {
    
    private PreviewService delegate;
    
    private Map<String, Map<PreviewOutput, List<InternalPreviewService>>> serviceMap = new HashMap<String, Map<PreviewOutput, List<InternalPreviewService>>>();
        

    public DelegationPreviewService(PreviewService delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String detectDocumentType(InputStream inputStream) throws OXException {                
        return delegate.detectDocumentType(inputStream);
    }

    @Override
    public PreviewDocument getPreviewFor(String arg, PreviewOutput output, Session session) throws OXException {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(arg));
            String mimeType = detectDocumentType(fis);
            fis.close();
            
            PreviewService previewService = getBestFitOrDelegate(mimeType, output);
            PreviewDocument preview = previewService.getPreviewFor(arg, output, session);
                
            return preview;
        } catch (FileNotFoundException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (IOException e) {
         // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        }        
    }

    @Override
    public PreviewDocument getPreviewFor(InputStream inputStream, String mimeType, String name, PreviewOutput output, Session session) throws OXException {        
        PreviewService previewService = getBestFitOrDelegate(mimeType, output);
        PreviewDocument preview = previewService.getPreviewFor(inputStream, mimeType, name, output, session);

        return preview;
    }

    @Override
    public PreviewDocument getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session) throws OXException {
        String mimeType = documentData.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
        PreviewService previewService = getBestFitOrDelegate(mimeType, output);
        PreviewDocument preview = previewService.getPreviewFor(documentData, output, session);
        
        return preview;
    }
    
    @Override
    public void added(ServiceReference<InternalPreviewService> ref, InternalPreviewService service) {
        PreviewPolicy[] previewPolicies = service.getPreviewPolicies();
        for (PreviewPolicy policy : previewPolicies) {
            String mimeType = policy.getMimeType();
            PreviewOutput output = policy.getOutput();
            Map<PreviewOutput, List<InternalPreviewService>> map = serviceMap.get(mimeType);
            if (map == null) {
                map = new HashMap<PreviewOutput, List<InternalPreviewService>>();
                List<InternalPreviewService> list = new ArrayList<InternalPreviewService>();
                list.add(service);
                map.put(output, list);
                serviceMap.put(mimeType, map);
                continue;
            }
            
            List<InternalPreviewService> list = map.get(output);
            if (list == null) {
                list = new ArrayList<InternalPreviewService>();
                list.add(service);
                map.put(output, list);
                continue;
            }
            
            list.add(service);
        }
        
        calc();
    }

    @Override
    public void removed(ServiceReference<InternalPreviewService> ref, InternalPreviewService service) {
        PreviewPolicy[] previewPolicies = service.getPreviewPolicies();
        for (PreviewPolicy policy : previewPolicies) {
            String mimeType = policy.getMimeType();
            PreviewOutput output = policy.getOutput();
            Map<PreviewOutput, List<InternalPreviewService>> map = serviceMap.get(mimeType);
            if (map != null) {
                List<InternalPreviewService> list = map.get(output);
                if (list != null)  {
                    list.remove(service);
                }                
            }
            
        }

        calc();
    }
    
    private void calc() {
        for (String mimeType : serviceMap.keySet()) {
            Map<PreviewOutput, List<InternalPreviewService>> map = serviceMap.get(mimeType);
            for (PreviewOutput output : map.keySet()) {
                List<InternalPreviewService> list = map.get(output);
                Collections.sort(list, new InternalPreviewServiceComparator(mimeType, output));
            }
        }
    }
    
    private PreviewService getBestFitOrDelegate(String mimeType, PreviewOutput output) {
        Map<PreviewOutput, List<InternalPreviewService>> map = serviceMap.get(mimeType);
        if (map == null) {
            return delegate;
        }
        
        List<InternalPreviewService> list = map.get(output);
        if (list == null || list.isEmpty()) {
            return delegate;
        }
        
        return list.get(0);
    }
    
    private static final class InternalPreviewServiceComparator implements Comparator<InternalPreviewService> {
        
        private PreviewOutput output;
        
        private String mimeType;

        public InternalPreviewServiceComparator(String mimeType, PreviewOutput output) {
            super();
            this.mimeType = mimeType;
            this.output = output;
        }

        @Override
        public int compare(InternalPreviewService o1, InternalPreviewService o2) {
            int o1Quality = 0;
            int o2Quality = 0;
            PreviewPolicy[] o1Policies = o1.getPreviewPolicies();
            for (PreviewPolicy policy : o1Policies) {
                if (policy.getMimeType().equals(mimeType) && policy.getOutput().equals(output)) {
                    o1Quality = policy.getQuality().getValue();
                }
            }
            
            PreviewPolicy[] o2Policies = o2.getPreviewPolicies();
            for (PreviewPolicy policy : o2Policies) {
                if (policy.getMimeType().equals(mimeType) && policy.getOutput().equals(output)) {
                    o2Quality = policy.getQuality().getValue();
                }
            }

            return o1Quality - o2Quality;
        }
        
    }

}
