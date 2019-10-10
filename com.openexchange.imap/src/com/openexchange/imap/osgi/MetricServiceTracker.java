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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.imap.osgi;

import java.util.List;
import java.util.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.imap.commandexecutor.AbstractMetricAwareCommandExecutor;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.metrics.MetricService;


/**
 * {@link MetricServiceTracker} - Tracker for metric service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MetricServiceTracker implements ServiceTrackerCustomizer<MetricService, MetricService> {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MetricServiceTracker.class);
    }

    private static ServiceTracker<MetricService, MetricService> instance = null;

    /**
     * Opens the tracker for the metric service.
     */
    public static synchronized void openMetricServiceTracker() {
        Optional<BundleContext> optionalBundleContext = IMAPActivator.getOptionalBundleContext();
        if (optionalBundleContext.isPresent()) {
            MetricServiceTracker trackerCUstomizer = new MetricServiceTracker(optionalBundleContext.get());
            ServiceTracker<MetricService, MetricService> tracker = new ServiceTracker<MetricService, MetricService>(optionalBundleContext.get(), MetricService.class, trackerCUstomizer);
            tracker.open();
            instance = tracker;
        }
    }

    /**
     * Closes the tracker for the metric service.
     */
    public static synchronized void closeMetricServiceTracker() {
        ServiceTracker<MetricService, MetricService> tracker = instance;
        if (tracker != null) {
            instance = null;
            tracker.close();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final BundleContext context;

    /**
     * Initializes a new {@link MetricServiceTracker}.
     *
     * @param context The bundle context
     */
    private MetricServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public MetricService addingService(ServiceReference<MetricService> reference) {
        MetricService service = context.getService(reference);
        List<AbstractMetricAwareCommandExecutor> commandExecutors = IMAPProperties.getInstance().getCommandExecutors();
        if (commandExecutors != null) {
            for (AbstractMetricAwareCommandExecutor commandExecutor : commandExecutors) {
                try {
                    commandExecutor.onMetricServiceAppeared(service);
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed to apply metric service to IMAP command executor: {}", commandExecutor.getDescription(), e);
                }
            }
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<MetricService> reference, MetricService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<MetricService> reference, MetricService service) {
        context.ungetService(reference);
        List<AbstractMetricAwareCommandExecutor> commandExecutors = IMAPProperties.getInstance().getCommandExecutors();
        if (commandExecutors != null) {
            for (AbstractMetricAwareCommandExecutor commandExecutor : commandExecutors) {
                try {
                    commandExecutor.onMetricServiceDisppearing(service);
                } catch (Exception e) {
                    LoggerHolder.LOG.warn("Failed to remove metric service from IMAP command executor: {}", commandExecutor.getDescription(), e);
                }
            }
        }
    }

}
