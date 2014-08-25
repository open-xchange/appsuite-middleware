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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.database.migration.ox.osgi;

import java.util.ArrayList;
import java.util.List;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.BundleContext;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.ox.DBMigrationOXExcecutorService;
import com.openexchange.database.migration.ox.internal.DBMigrationOXExcecutorServiceImpl;
import com.openexchange.database.migration.ox.internal.Services;
import com.openexchange.database.migration.ox.internal.accessor.ClassLoaderResourceAccessor;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link OXMigrationActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class OXMigrationActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXMigrationActivator.class);

    private static final Class<?>[] NEEDED_SERVICES = { DBMigrationExecutorService.class };

    private static final String OX_CHANGELOG_NAME = "ox.changelog.xml";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: " + this.context.getBundle().getSymbolicName());
        Services.setServiceLookup(this);

        DBMigrationExecutorService dbMigrationExecutorService = Services.getService(DBMigrationExecutorService.class);

        if (dbMigrationExecutorService == null) {
            LOG.error("Required service null");
            return;
        }

        List<ResourceAccessor> accessors = new ArrayList<ResourceAccessor>();
        accessors.add(new ClassLoaderResourceAccessor());

        dbMigrationExecutorService.execute(OX_CHANGELOG_NAME, accessors);

        registerService(DBMigrationOXExcecutorService.class, new DBMigrationOXExcecutorServiceImpl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        super.stopBundle();

        LOG.info("Stopping bundle: " + this.context.getBundle().getSymbolicName());
        Services.setServiceLookup(null);
    }
}
