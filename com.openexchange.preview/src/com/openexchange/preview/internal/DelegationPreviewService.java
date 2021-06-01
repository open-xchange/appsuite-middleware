/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
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

    private final ConcurrentMap<String, ConcurrentMap<PreviewOutput, SortableConcurrentList<ComparableInternalPreviewService>>> serviceMap;

    /**
     * Initializes a new {@link DelegationPreviewService}.
     *
     * @param delegate The delegate preview service
     */
    public DelegationPreviewService(final PreviewService delegate) {
        super();
        this.delegate = delegate;
        serviceMap = new ConcurrentHashMap<>();
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
            final PreviewService previewService = getBestFitOrDelegate(toLowerCase(mimeType), output, session);
            if (previewService == null) {
                throw PreviewExceptionCodes.NO_PREVIEW_SERVICE2.create(null == mimeType ? "" :  mimeType, null == name ? "<unknown>" : name);
            }
            return previewService.getPreviewFor(arg, output, session, pages);
        } catch (IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(is);
        }
    }

    @Override
    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session, int pages) throws OXException {
        final String mimeType = documentData.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
        final PreviewService previewService = getBestFitOrDelegate(toLowerCase(mimeType), output, session);
        if (previewService == null) {
            String name = documentData.getDataProperties().get(DataProperties.PROPERTY_NAME);
            throw PreviewExceptionCodes.NO_PREVIEW_SERVICE2.create(null == mimeType ? "" :  mimeType, null == name ? "<unknown>" : name);
        }
        return previewService.getPreviewFor(documentData, output, session, pages);
    }

    @Override
    public synchronized void added(final ServiceReference<InternalPreviewService> ref, final InternalPreviewService service) {
        for (final PreviewPolicy policy : service.getPreviewPolicies()) {
            final String mimeType = toLowerCase(policy.getMimeType());
            final PreviewOutput output = policy.getOutput();
            ConcurrentMap<PreviewOutput, SortableConcurrentList<ComparableInternalPreviewService>> map = serviceMap.get(mimeType);
            if (map == null) {
                final ConcurrentMap<PreviewOutput, SortableConcurrentList<ComparableInternalPreviewService>> newMap = new ConcurrentHashMap<>();
                map = serviceMap.putIfAbsent(mimeType, newMap);
                if (null == map) {
                    map = newMap;
                }
            }
            SortableConcurrentList<ComparableInternalPreviewService> list = map.get(output);
            if (list == null) {
                SortableConcurrentList<ComparableInternalPreviewService> newList = new SortableConcurrentList<>();
                list = map.putIfAbsent(output, newList);
                if (list == null) {
                    list = newList;
                }
            }
            list.addAndSort(new ComparableInternalPreviewService(service, mimeType, output));
        }
    }

    @Override
    public synchronized void removed(final ServiceReference<InternalPreviewService> ref, final InternalPreviewService service) {
        final List<PreviewPolicy> previewPolicies = service.getPreviewPolicies();
        for (final PreviewPolicy policy : previewPolicies) {
            final String mimeType = toLowerCase(policy.getMimeType());
            final PreviewOutput output = policy.getOutput();
            final Map<PreviewOutput, SortableConcurrentList<ComparableInternalPreviewService>> map = serviceMap.get(mimeType);
            if (map != null) {
                SortableConcurrentList<ComparableInternalPreviewService> list = map.get(output);
                if (list != null)  {
                    list.remove(new ComparableInternalPreviewService(service, mimeType, output));
                }
            }
        }
    }

    @Override
    public PreviewService getBestFitOrDelegate(String mimeType, PreviewOutput output, Session session) throws OXException {
        final Map<PreviewOutput, SortableConcurrentList<ComparableInternalPreviewService>> map = serviceMap.get(mimeType);
        if (map == null) {
            return null;
        }
        SortableConcurrentList<ComparableInternalPreviewService> list = map.get(output);
        if (list == null) {
            return null;
        }

        for (ComparableInternalPreviewService comparableInternalPreviewService : list) {
            InternalPreviewService internalPreviewService = comparableInternalPreviewService.internalPreviewService;
            if (internalPreviewService.isSupportedFor(session)) {
                return internalPreviewService;
            }
        }

        return null;
    }

    private static final class ComparableInternalPreviewService implements Comparable<ComparableInternalPreviewService> {

        final InternalPreviewService internalPreviewService;
        private final PreviewOutput output;
        private final String mimeType;
        private final int hash;

        ComparableInternalPreviewService(InternalPreviewService internalPreviewService, String mimeType, PreviewOutput output) {
            super();
            this.internalPreviewService = internalPreviewService;
            this.mimeType = mimeType;
            this.output = output;

            int prime = 31;
            int result = 1;
            result = prime * result + ((internalPreviewService == null) ? 0 : internalPreviewService.hashCode());
            result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
            result = prime * result + ((output == null) ? 0 : output.hashCode());
            this.hash = result;
        }

        @Override
        public int compareTo(ComparableInternalPreviewService other) {
            InternalPreviewService o1 = this.internalPreviewService;
            InternalPreviewService o2 = other.internalPreviewService;

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

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ComparableInternalPreviewService other = (ComparableInternalPreviewService) obj;
            if (internalPreviewService == null) {
                if (other.internalPreviewService != null) {
                    return false;
                }
            } else if (!internalPreviewService.equals(other.internalPreviewService)) {
                return false;
            }
            if (mimeType == null) {
                if (other.mimeType != null) {
                    return false;
                }
            } else if (!mimeType.equals(other.mimeType)) {
                return false;
            }
            if (output != other.output) {
                return false;
            }
            return true;
        }

    }

}
