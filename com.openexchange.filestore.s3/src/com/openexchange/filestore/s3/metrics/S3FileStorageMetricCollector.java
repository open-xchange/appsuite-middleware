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

package com.openexchange.filestore.s3.metrics;

import java.util.concurrent.atomic.AtomicReference;
import com.amazonaws.metrics.MetricCollector;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.metrics.ServiceMetricCollector;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.s3.internal.config.S3Property;

/**
 * {@link S3FileStorageMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageMetricCollector extends MetricCollector {

    private boolean started;
    private final LeanConfigurationService config;
    private final AtomicReference<RequestMetricCollector> s3FileStorageRequestMetricCollector;
    private final AtomicReference<ServiceMetricCollector> s3FileStorageServiceMetricCollector;

    /**
     * Initialises a new {@link S3FileStorageMetricCollector}.
     *
     * @throws OXException
     */
    public S3FileStorageMetricCollector(LeanConfigurationService config) {
        super();
        s3FileStorageRequestMetricCollector = new AtomicReference<RequestMetricCollector>(RequestMetricCollector.NONE);
        s3FileStorageServiceMetricCollector = new AtomicReference<ServiceMetricCollector>(ServiceMetricCollector.NONE);
        this.config = config;
        start();
    }

    @Override
    public synchronized boolean start() {
        if (started) {
            return false;
        }

        // Not started? Initialize the request and service metric collectors
        s3FileStorageServiceMetricCollector.set(new S3FileStorageServiceMetricCollector());
        started = true;
        return true;
    }

    @Override
    public synchronized boolean stop() {
        if (false == started) {
            return false;
        }

        // Was started? Replace the request and service metric collectors
        {
            RequestMetricCollector tmpReqCollector = s3FileStorageRequestMetricCollector.get();
            if (tmpReqCollector != RequestMetricCollector.NONE) {
                s3FileStorageRequestMetricCollector.set(RequestMetricCollector.NONE);
            }
        }

        {
            ServiceMetricCollector tmpServCollector = s3FileStorageServiceMetricCollector.get();
            if (tmpServCollector != ServiceMetricCollector.NONE) {
                s3FileStorageServiceMetricCollector.set(ServiceMetricCollector.NONE);
            }
        }

        started = false;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return config.getBooleanProperty(S3Property.METRIC_COLLECTION);
    }

    @Override
    public RequestMetricCollector getRequestMetricCollector() {
        return s3FileStorageRequestMetricCollector.get();
    }

    @Override
    public ServiceMetricCollector getServiceMetricCollector() {
        return s3FileStorageServiceMetricCollector.get();
    }

}
