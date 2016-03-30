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

package com.openexchange.conversion.engine.osgi;

import static com.openexchange.conversion.engine.internal.ConversionEngineRegistry.getInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.engine.internal.ConversionEngineRegistry;

/**
 * {@link DataSourceTracker} - The service tracker customizer for {@link DataSource}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataSourceTracker implements ServiceTrackerCustomizer<DataSource, DataSource> {

    private static final String PROP_IDENTIFIER = "identifier";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataSourceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link DataSourceTracker}
     *
     * @param context The bundle context
     */
    public DataSourceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public DataSource addingService(final ServiceReference<DataSource> reference) {
        final DataSource addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("Added service is null!", new Throwable());
            context.ungetService(reference);
            return null;
        }
        final String identifier = (String) reference.getProperty(PROP_IDENTIFIER);
        if (null == identifier) {
            LOG.error("Missing identifier in data source: {}", addedService.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        final ConversionEngineRegistry registry = getInstance();
        synchronized (registry) {
            if (registry.getDataSource(identifier) != null) {
                LOG.error("A data source is already registered for identifier: {}", identifier);
                context.ungetService(reference);
                return null;
            }
            registry.putDataSource(identifier, addedService);
            LOG.info("Data source for identifier '{}' successfully registered", identifier);
        }
        return addedService;

    }

    @Override
    public void modifiedService(final ServiceReference<DataSource> reference, final DataSource service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<DataSource> reference, final DataSource service) {
        if (null == service) {
            return;
        }
        try {
            final String identifier = (String) reference.getProperty(PROP_IDENTIFIER);
            if (null == identifier) {
                LOG.error("Missing identifier in data source: {}", service.getClass().getName());
                return;
            }
            getInstance().removeDataSource(identifier);
            LOG.info("Data source for identifier '{}' successfully unregistered", identifier);

        } finally {
            context.ungetService(reference);
        }

    }

}
