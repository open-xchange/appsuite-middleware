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

import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.MetricType;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.util.AWSRequestMetrics.Field;
import com.codahale.metrics.Meter;
import com.openexchange.metrics.MetricRegistryService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link S3FileStorageRequestMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class S3FileStorageRequestMetricCollector extends RequestMetricCollector {

    private final ServiceLookup services;

    private Meter headRequestMeter;

    /**
     * Initialises a new {@link S3FileStorageRequestMetricCollector}.
     */
    public S3FileStorageRequestMetricCollector(String filestoreId, ServiceLookup services) {
        super();
        this.services = services;
        registerRequestMetrics();
    }

    /**
     * 
     */
    private void registerRequestMetrics() {
        MetricRegistryService metricRegistryService = services.getService(MetricRegistryService.class);
        headRequestMeter = metricRegistryService.registerMeter(HttpMethodName.HEAD.name());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.amazonaws.metrics.RequestMetricCollector#collectMetrics(com.amazonaws.Request, com.amazonaws.Response)
     */
    @Override
    public void collectMetrics(Request<?> request, Response<?> response) {
        countMethod(request);
        for (MetricType type : AwsSdkMetrics.getPredefinedMetrics()) {
            if (type instanceof Field) {
                Field predefined = (Field) type;
                switch (predefined) {
                    case ClientExecuteTime:
                        System.err.println("client execution time");
                    default:
                        break;
                }
            }
        }
    }

    private void countMethod(Request<?> request) {
        HttpMethodName httpMethod = request.getHttpMethod();
        switch (httpMethod) {
            case HEAD:
                headRequestMeter.mark();
                break;
            case GET:
                break;
            case POST:
                break;
            case PUT:
                break;
            case DELETE:
                break;
            case OPTIONS:
            case PATCH:
            default:
                break;
        }
    }
}
