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

import java.util.function.Consumer;
import com.openexchange.exception.OXException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricDescriptorCache;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;

/**
 * 
 * {@link MetricHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
final class MetricHandler {

    private static final String SESSIONS = "sessions";

    private static final String COUNT_TOTAL = "Count.Total";
    private static final String COUNT_TOTAL_DESC = "The number of total sessions";

    private static final String COUNT_LONG = "LongTermCount.Total";
    private static final String COUNT_LONG_DESC = "The number of sessions in the long term container";

    private static final String COUNT_SHORT = "ShortTermCount.Total";
    private static final String COUNT_SHORT_DESC = "The number of sessions in the short term container";
    
    private static final String COUNT_ACTIVE = "ActiveCount.Total";
    private static final String COUNT_ACTIVE_DESC = "The number of active sessions";

    private final MetricDescriptorCache metricDescriptorCache;
    private final MetricService metricService;

    /**
     * Initializes a new {@link MetricHandler}.
     * 
     * @throws OXException
     */
    public MetricHandler(MetricService metricService) {
        super();
        this.metricService = metricService;
        this.metricDescriptorCache = new MetricDescriptorCache(metricService, "sessiond");
    }

    /**
     * Increases the number of total sessions
     * 
     * @param numberOfNewSessions
     */
    void increaseSessionCount(int numberOfNewSessions) {
        if (numberOfNewSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_TOTAL, COUNT_TOTAL_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).incrementBy(numberOfNewSessions));
    }

    /**
     * Decreases the number of total sessions
     * 
     * @param numberOfRemovedSessions
     */
    void decreaseSessionCount(int numberOfRemovedSessions) {
        if (numberOfRemovedSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_TOTAL, COUNT_TOTAL_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).decrementBy(numberOfRemovedSessions));
    }

    /**
     * Increases the number of sessions in the long term container
     * 
     * @param numberOfNewSessions
     */
    void increaseLongTermSessionCount(int numberOfNewSessions) {
        if (numberOfNewSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_LONG, COUNT_LONG_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).incrementBy(numberOfNewSessions));
    }

    /**
     * Decreases the number of sessions in the long term container
     * 
     * @param numberOfRemovedSessions
     */
    void decreaseLongTermSessionCount(int numberOfRemovedSessions) {
        if (numberOfRemovedSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_LONG, COUNT_LONG_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).decrementBy(numberOfRemovedSessions));
    }

    /**
     * Increases the number of sessions in the short term container
     * 
     * @param numberOfNewSessions
     */
    void increaseShortTermSessionCount(int numberOfNewSessions) {
        if (numberOfNewSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_SHORT, COUNT_SHORT_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).incrementBy(numberOfNewSessions));
    }

    /**
     * Decreases the number of sessions in the short term container
     * 
     * @param numberOfRemovedSessions
     */
    void decreaseShortTermSessionCount(int numberOfRemovedSessions) {
        if (numberOfRemovedSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_SHORT, COUNT_SHORT_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).decrementBy(numberOfRemovedSessions));
    }
    
    
    /**
     * Increases the number of active sessions
     * 
     * @param numberOfNewSessions
     */
    void increaseActiveSessionCount(int numberOfNewSessions) {
        if (numberOfNewSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_ACTIVE, COUNT_ACTIVE_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).incrementBy(numberOfNewSessions));
    }

    /**
     * Decreases the number of active sessions
     * 
     * @param numberOfRemovedSessions
     */
    void decreaseActiveSessionCount(int numberOfRemovedSessions) {
        if (numberOfRemovedSessions == 0) {
            return;
        }
        MetricDescriptor descriptor = metricDescriptorCache.getMetricDescriptor(MetricType.COUNTER, COUNT_ACTIVE, COUNT_ACTIVE_DESC, SESSIONS);
        updateMetric(t -> t.getCounter(descriptor).decrementBy(numberOfRemovedSessions));
    }

    /**
     * Updates the metric specified in the provided {@link Consumer}
     * 
     * @param consumer The consumer
     */
    private void updateMetric(Consumer<MetricService> consumer) {
        consumer.accept(metricService);
    }
}
