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

package com.openexchange.sessiond.impl;

import com.openexchange.exception.OXException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;

/**
 *
 * {@link SessionMetricHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public final class SessionMetricHandler {

    private static final String SESSIONS = "sessions";
    private static final String GROUP = "sessiond";

    private static final String COUNT_TOTAL = "Count.Total";
    private static final String COUNT_TOTAL_DESC = "The number of total sessions";

    private static final String COUNT_LONG = "LongTermCount.Total";
    private static final String COUNT_LONG_DESC = "The number of sessions in the long term containers";

    private static final String COUNT_SHORT = "ShortTermCount.Total";
    private static final String COUNT_SHORT_DESC = "The number of sessions in the short term containers";

    private static final String COUNT_ACTIVE = "ActiveCount.Total";
    private static final String COUNT_ACTIVE_DESC = "The number of active sessions or in other words the number of sessions within the first two short term containers.";

    private static final MetricDescriptor DESC_TOTAL;
    private static final MetricDescriptor DESC_LONG;
    private static final MetricDescriptor DESC_SHORT;
    private static final MetricDescriptor DESC_ACTIVE;


    static {
        DESC_TOTAL = MetricDescriptor.newBuilder(GROUP, COUNT_TOTAL, MetricType.GAUGE).withUnit(SESSIONS).withDescription(COUNT_TOTAL_DESC).withMetricSupplier(() -> {
            return SessionHandler.getMetricTotalSessions();
        }).build();

        DESC_LONG = MetricDescriptor.newBuilder(GROUP, COUNT_LONG, MetricType.GAUGE).withUnit(SESSIONS).withDescription(COUNT_LONG_DESC).withMetricSupplier(() -> {
            return SessionHandler.getMetricLongSessions();
        }).build();

        DESC_SHORT = MetricDescriptor.newBuilder(GROUP, COUNT_SHORT, MetricType.GAUGE).withUnit(SESSIONS).withDescription(COUNT_SHORT_DESC).withMetricSupplier(() -> {
            return SessionHandler.getMetricShortSessions();
        }).build();

        DESC_ACTIVE = MetricDescriptor.newBuilder(GROUP, COUNT_ACTIVE, MetricType.GAUGE).withUnit(SESSIONS).withDescription(COUNT_ACTIVE_DESC).withMetricSupplier(() -> {
            return SessionHandler.getMetricActiveSessions();
        }).build();
    }

    /**
     * Initializes a new {@link SessionMetricHandler}.
     *
     * @throws OXException
     */
    private SessionMetricHandler() {
        super();
    }

    /**
     * Registers metrics for the SessionHandler
     *
     * @param metricService The {@link MetricService}
     */
    public static void registerMetrics(MetricService metricService){
        metricService.getGauge(DESC_TOTAL);
        metricService.getGauge(DESC_LONG);
        metricService.getGauge(DESC_SHORT);
        metricService.getGauge(DESC_ACTIVE);
    }

    /**
     * Unregisters metrics for the SessionHandler
     *
     * @param metricService The {@link MetricService}
     */
    public static void unregisterMetrics(MetricService metricService) {
        metricService.removeMetric(DESC_TOTAL);
        metricService.removeMetric(DESC_LONG);
        metricService.removeMetric(DESC_SHORT);
        metricService.removeMetric(DESC_ACTIVE);
    }
}
