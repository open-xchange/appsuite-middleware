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

import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.preview.Delegating;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewPolicy;
import com.openexchange.preview.PreviewService;
import com.openexchange.session.Session;


/**
 * {@link DelegationPreviewService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DelegationPreviewService implements Delegating, SimpleRegistryListener<InternalPreviewService> {

    private final PreviewService delegate;

    private final ConcurrentMap<String, ConcurrentMap<PreviewOutput, BlockingQueue<InternalPreviewService>>> serviceMap;

    /**
     * Initializes a new {@link DelegationPreviewService}.
     *
     * @param delegate The delegate preview service
     */
    public DelegationPreviewService(final PreviewService delegate) {
        super();
        this.delegate = delegate;
        serviceMap = new ConcurrentHashMap<String, ConcurrentMap<PreviewOutput, BlockingQueue<InternalPreviewService>>>();
    }

    @Override
    public String detectDocumentType(final InputStream inputStream) throws OXException {
        return delegate.detectDocumentType(inputStream);
    }

    @Override
    public PreviewDocument getPreviewFor(final String arg, final PreviewOutput output, final Session session, int pages) throws OXException {
        InputStream is = null;
        try {
            final File file = new File(arg);
            String name = null;
            if (file.isFile()) {
                is = new BufferedInputStream(new FileInputStream(new File(arg)), 65536);
                name = file.getName();
            } else {
                final URL url = new URL(arg);
                final URLConnection connection = url.openConnection();
                connection.setConnectTimeout(2500);
                connection.setReadTimeout(2500);
                is = new BufferedInputStream(connection.getInputStream(), 65536);
            }
            final String mimeType = detectDocumentType(is);
            /*
             * Serve with best-fit or delegate preview service
             */
            final PreviewService previewService = getBestFitOrDelegate(toLowerCase(mimeType), output);
            if (previewService == null) {
                throw PreviewExceptionCodes.NO_PREVIEW_SERVICE2.create(null == mimeType ? "" :  mimeType, null == name ? "<unknown>" : name);
            }
            return previewService.getPreviewFor(arg, output, session, pages);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(is);
        }
    }

    @Override
    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session, int pages) throws OXException {
        final String mimeType = documentData.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
        final PreviewService previewService = getBestFitOrDelegate(toLowerCase(mimeType), output);
        if (previewService == null) {
            String name = documentData.getDataProperties().get(DataProperties.PROPERTY_NAME);
            throw PreviewExceptionCodes.NO_PREVIEW_SERVICE2.create(null == mimeType ? "" :  mimeType, null == name ? "<unknown>" : name);
        }
        return previewService.getPreviewFor(documentData, output, session, pages);
    }

    @Override
    public void added(final ServiceReference<InternalPreviewService> ref, final InternalPreviewService service) {
        for (final PreviewPolicy policy : service.getPreviewPolicies()) {
            final String mimeType = toLowerCase(policy.getMimeType());
            final PreviewOutput output = policy.getOutput();
            ConcurrentMap<PreviewOutput, BlockingQueue<InternalPreviewService>> map = serviceMap.get(mimeType);
            if (map == null) {
                final ConcurrentMap<PreviewOutput, BlockingQueue<InternalPreviewService>> newMap = new ConcurrentHashMap<PreviewOutput, BlockingQueue<InternalPreviewService>>();
                map = serviceMap.putIfAbsent(mimeType, newMap);
                if (null == map) {
                    map = newMap;
                }
            }
            BlockingQueue<InternalPreviewService> queue = map.get(output);
            if (queue == null) {
                final BlockingQueue<InternalPreviewService> newQueue = new PriorityBlockingQueue<InternalPreviewService>(4, new InternalPreviewServiceComparator(mimeType, output));
                queue = map.putIfAbsent(output, newQueue);
                if (queue == null) {
                    queue = newQueue;
                }
            }
            queue.add(service);
        }
    }

    @Override
    public void removed(final ServiceReference<InternalPreviewService> ref, final InternalPreviewService service) {
        final List<PreviewPolicy> previewPolicies = service.getPreviewPolicies();
        for (final PreviewPolicy policy : previewPolicies) {
            final String mimeType = toLowerCase(policy.getMimeType());
            final PreviewOutput output = policy.getOutput();
            final Map<PreviewOutput, BlockingQueue<InternalPreviewService>> map = serviceMap.get(mimeType);
            if (map != null) {
                final BlockingQueue<InternalPreviewService> queue = map.get(output);
                if (queue != null)  {
                    queue.remove(service);
                }
            }

        }
    }

    @Override
    public PreviewService getBestFitOrDelegate(final String mimeType, final PreviewOutput output) {
        final Map<PreviewOutput, BlockingQueue<InternalPreviewService>> map = serviceMap.get(mimeType);
        if (map == null) {
            return null;
        }
        final BlockingQueue<InternalPreviewService> queue = map.get(output);
        if (queue == null) {
            return null;
        }
        return queue.peek();
    }

    private static final class InternalPreviewServiceComparator implements Comparator<InternalPreviewService> {

        private final PreviewOutput output;

        private final String mimeType;

        protected InternalPreviewServiceComparator(final String mimeType, final PreviewOutput output) {
            super();
            this.mimeType = mimeType;
            this.output = output;
        }

        @Override
        public int compare(final InternalPreviewService o1, final InternalPreviewService o2) {
            int o1Quality = 0;
            int o2Quality = 0;
            final List<PreviewPolicy> o1Policies = o1.getPreviewPolicies();
            for (final PreviewPolicy policy : o1Policies) {
                if (policy.getMimeType().equals(mimeType) && policy.getOutput().equals(output)) {
                    o1Quality = policy.getQuality().getValue();
                }
            }
            final List<PreviewPolicy> o2Policies = o2.getPreviewPolicies();
            for (final PreviewPolicy policy : o2Policies) {
                if (policy.getMimeType().equals(mimeType) && policy.getOutput().equals(output)) {
                    o2Quality = policy.getQuality().getValue();
                }
            }
            return o1Quality - o2Quality;
        }

    }

}
