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

package com.openexchange.imap.commandexecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricService;
import com.sun.mail.imap.CommandExecutor;

/**
 * {@link AbstractMetricAwareCommandExecutor} - Basic abstract class for metric aware command executors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class AbstractMetricAwareCommandExecutor implements CommandExecutor {

    /** The registered metric descriptors */
    protected final AtomicReference<List<MetricDescriptor>> metricDescriptors;

    /** The reference for the metric service */
    protected final AtomicReference<MetricService> metricServiceReference;

    /**
     * Initializes a new {@link AbstractMetricAwareCommandExecutor}.
     */
    protected AbstractMetricAwareCommandExecutor() {
        super();
        metricServiceReference = new AtomicReference<>(null);
        metricDescriptors = new AtomicReference<>(null);
    }

    /**
     * Gets a short description for this circuit breaker.
     *
     * @return The description
     */
    public abstract String getDescription();

    /**
     * Adds metric descriptors to given list.
     *
     * @param descriptors The list to add to
     * @param metricService The mtric service
     */
    protected abstract void addMetricDescriptors(List<MetricDescriptor> descriptors, MetricService metricService);

    /**
     * Invoked when given metric service appeared.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    public void onMetricServiceAppeared(MetricService metricService) throws Exception {
        metricServiceReference.set(metricService);
        List<MetricDescriptor> descriptors = new CopyOnWriteArrayList<MetricDescriptor>();
        addMetricDescriptors(descriptors, metricService);
        this.metricDescriptors.set(descriptors);
    }

    /**
     * Invoked when given metric service is about to disappear.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    public void onMetricServiceDisppearing(MetricService metricService) throws Exception {
        metricServiceReference.set(null);

        List<MetricDescriptor> descriptors = metricDescriptors.getAndSet(null);
        if (descriptors != null) {
            for (MetricDescriptor metricDescriptor : descriptors) {
                metricService.removeMetric(metricDescriptor);
            }
        }
    }

}
