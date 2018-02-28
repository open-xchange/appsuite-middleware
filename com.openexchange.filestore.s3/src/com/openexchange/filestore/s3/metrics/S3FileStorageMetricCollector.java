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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.filestore.s3.metrics;

import com.amazonaws.metrics.MetricCollector;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.metrics.ServiceMetricCollector;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.s3.internal.S3FileStoreProperty;
import com.openexchange.metrics.MetricService;

/**
 * {@link S3FileStorageMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageMetricCollector extends MetricCollector {

    private boolean started;
    private final MetricService metrics;
    private final LeanConfigurationService config;
    private RequestMetricCollector s3FileStorageRequestMetricCollector;
    private ServiceMetricCollector s3FileStorageServiceMetricCollector;

    /**
     * Initialises a new {@link S3FileStorageMetricCollector}.
     *
     * @throws OXException
     */
    public S3FileStorageMetricCollector(MetricService metrics, LeanConfigurationService config) {
        super();
        this.metrics = metrics;
        this.config = config;
        start();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.MetricCollector#start()
     */
    @Override
    public synchronized boolean start() {
        if (started) {
            return false;
        }

        // Not started? Initialise the request and service metric collectors
        s3FileStorageRequestMetricCollector = new S3FileStorageRequestMetricCollector(metrics);
        s3FileStorageServiceMetricCollector = new S3FileStorageServiceMetricCollector(metrics);
        started = true;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.MetricCollector#stop()
     */
    @Override
    public synchronized boolean stop() {
        if (false == started) {
            return false;
        }

        // Was started? Replace the request and service metric collectors
        S3FileStorageRequestMetricCollector tmpReqCollector = (S3FileStorageRequestMetricCollector) s3FileStorageRequestMetricCollector;
        S3FileStorageServiceMetricCollector tmpServCollector = (S3FileStorageServiceMetricCollector) s3FileStorageServiceMetricCollector;

        s3FileStorageRequestMetricCollector = RequestMetricCollector.NONE;
        s3FileStorageServiceMetricCollector = ServiceMetricCollector.NONE;

        tmpReqCollector.stop();
        tmpServCollector.stop();

        started = false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.MetricCollector#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return config.getBooleanProperty(S3FileStoreProperty.metricCollection);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.MetricCollector#getRequestMetricCollector()
     */
    @Override
    public RequestMetricCollector getRequestMetricCollector() {
        return s3FileStorageRequestMetricCollector;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.amazonaws.metrics.MetricCollector#getServiceMetricCollector()
     */
    @Override
    public ServiceMetricCollector getServiceMetricCollector() {
        return s3FileStorageServiceMetricCollector;
    }
}
