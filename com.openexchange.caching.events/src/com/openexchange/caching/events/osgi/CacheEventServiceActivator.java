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

package com.openexchange.caching.events.osgi;

import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.internal.CacheEventConfigurationImpl;
import com.openexchange.caching.events.internal.CacheEventServiceImpl;
import com.openexchange.caching.events.internal.CacheEventServiceLookup;
import com.openexchange.caching.events.monitoring.ManagementRegisterer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link CacheEventServiceActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheEventServiceActivator extends HousekeepingActivator {

    private volatile CacheEventServiceImpl service;

    /**
     * Initializes a new {@link CacheEventServiceActivator}.
     */
    public CacheEventServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheEventServiceActivator.class);
        logger.info("starting bundle: {}", context.getBundle().getSymbolicName());
        CacheEventServiceLookup.set(this);
        CacheEventServiceImpl service = new CacheEventServiceImpl(new CacheEventConfigurationImpl(getService(ConfigurationService.class)));
        this.service = service;
        registerService(CacheEventService.class, service);
        registerService(Reloadable.class, service);
        try {
            track(ManagementService.class, new ManagementRegisterer(service, context));
            openTrackers();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheEventServiceActivator.class);
        logger.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        CacheEventServiceImpl service = this.service;
        if (null != service) {
            service.shutdown();
            this.service = null;
        }
        CacheEventServiceLookup.set(null);
        super.stopBundle();
    }

}
